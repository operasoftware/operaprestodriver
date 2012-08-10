/*
Copyright 2008-2012 Opera Software ASA

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.opera.core.systems.internal;

import com.opera.core.systems.common.io.Closeables;

import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * This will monitor any selectable channel, such as a {@link java.nio.channels.SocketChannel} or
 * {@link java.nio.channels.ServerSocketChannel} and fire {@link SocketListener#canWrite(java.nio.channels.SelectableChannel)}
 * or {@link SocketListener#canRead(java.nio.channels.SelectableChannel)} events when such events
 * are detected by {@link #poll()}.
 */
public class SocketMonitor {

  private final Logger logger = Logger.getLogger(getClass().getName());
  private final ReentrantLock lock = new ReentrantLock();
  private final List<SelectorChangeRequest> changes = new LinkedList<SelectorChangeRequest>();
  private Selector selector;

  private enum Operation {
    ADD, MODIFY, REMOVE
  }

  public SocketMonitor() {
    try {
      selector = SelectorProvider.provider().openSelector();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean add(SelectableChannel channel, SocketListener listener, int selectMask) {
    logger.finest(String.format("Add channel: %s, mask=%s", channel, debugMask(selectMask)));

    synchronized (changes) {
      changes.add(new SelectorChangeRequest(channel, Operation.ADD, selectMask, listener));
      selector.wakeup();
    }

    return true;
  }

  public boolean modify(SelectableChannel channel, SocketListener listener, int selectMask) {
    logger.finest(String.format("Modify channel: %s, mask=%s", channel, debugMask(selectMask)));

    if (channel == null) {
      return false;
    }

    synchronized (changes) {
      changes.add(new SelectorChangeRequest(channel, Operation.MODIFY, selectMask, listener));
      selector.wakeup();
    }

    return true;
  }

  public void remove(SelectableChannel channel) {
    logger.finest(String.format("Remove channel: %s", channel));

    if (channel == null) {
      return;
    }

    synchronized (changes) {
      changes.add(new SelectorChangeRequest(channel, Operation.REMOVE));
      selector.wakeup();
    }
  }

  public void stop() {
    Closeables.closeQuietly(selector);
  }

  public boolean poll() {
    return poll(Long.MAX_VALUE);
  }

  public boolean poll(long ms) {
    boolean ok = pollSockets(ms);
    applyChanges();
    return ok;
  }

  private boolean pollSockets(long ms) {
    if (selector.keys().isEmpty()) {
      return true;
    }

    lock.lock();
    try {
      synchronized (selector) {
        selector.select(ms);
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    } finally {
      lock.unlock();
    }

    lock.lock();
    try {
      for (SelectionKey key : selector.selectedKeys()) {
        try {
          processSelectionKey(key);
        } catch (AsynchronousCloseException e) {
          // ignore
        } catch (CancelledKeyException e) {
          e.printStackTrace();
        } catch (IOException e) {
          // what are you doing with the channel variable here?
          //SelectableChannel channel = key.channel();
          e.printStackTrace();
          key.cancel();
        }
      }
    } finally {
      lock.unlock();
    }

    return true;
  }

  private void applyChanges() {
    synchronized (changes) {
      if (changes.isEmpty()) {
        return;
      }

      for (SelectorChangeRequest request : changes) {
        try {
          request.dump();
          switch (request.operation) {
            case ADD:
              try {
                request.channel.register(selector, request.mask, request.listener);
              } catch (ClosedChannelException e) {
                e.printStackTrace();
              }
              break;
            case MODIFY:
              SelectionKey key = request.channel.keyFor(selector);
              if (key != null) {
                key.interestOps(request.mask);
              }
              break;
            case REMOVE:
              SelectionKey selKey = request.channel.keyFor(selector);
              if (selKey != null) {
                selKey.cancel();
              }
              break;
          }
        } catch (CancelledKeyException e) {
          logger.finest("Key was cancelled - ignoring...");
        }
      }
      changes.clear();
    }
  }

  protected void processSelectionKey(SelectionKey key) throws IOException {
    SelectableChannel channel = key.channel();

    if (!key.isValid()) {
      return;
    }

    SocketListener listener = (SocketListener) key.attachment();

    int currentMask = key.interestOps();
    int triggerMask = key.readyOps();
    int wantedMask = 0;

    if (key.isValid() && key.isAcceptable() && listener.canRead(channel)) {
      wantedMask |= SelectionKey.OP_ACCEPT;
    }

    if (key.isValid() && key.isConnectable() && listener.canRead(channel)) {
      wantedMask |= SelectionKey.OP_CONNECT;
    }

    if (key.isValid() && key.isReadable() && listener.canRead(channel)) {
      wantedMask |= SelectionKey.OP_READ;
    }

    if (key.isValid() && key.isWritable() && listener.canWrite(channel)) {
      wantedMask |= SelectionKey.OP_WRITE;
    }

    // In case we did not trigger something we want to poll for
    int notTriggered = (currentMask & ~triggerMask);
    wantedMask |= notTriggered;

    // Update the selection mask, if it now differs
    if (wantedMask != currentMask) {
      if (wantedMask != 0) {
        if (key.isValid()) {
          key.interestOps(wantedMask);
        }
      } else {
        key.cancel();
      }
    }
  }

  private String debugMask(int mask) {
    StringBuilder builder = new StringBuilder();
    builder.append('{');

    if ((mask & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
      builder.append(" READ");
    }

    if ((mask & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
      builder.append(" WRITE");
    }

    if ((mask & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
      builder.append(" ACCEPT");
    }

    if ((mask & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT) {
      builder.append(" CONNECT");
    }

    builder.append('}');
    return builder.toString();
  }

  private class SelectorChangeRequest {

    protected final SelectableChannel channel;
    protected final Operation operation;
    protected final int mask;
    protected final SocketListener listener;

    public SelectorChangeRequest(SelectableChannel channel, Operation operation) {
      this(channel, operation, 0, null);
    }

    public SelectorChangeRequest(SelectableChannel channel, Operation operation,
                                 int mask, SocketListener listener) {
      this.channel = channel;
      this.operation = operation;
      this.mask = mask;
      this.listener = listener;
    }

    public void dump() {
      logger.finest(String.format("SelectorChangeRequest: %s socket=%s, mask=%d",
                                  operation, channel, mask));
    }

  }

}
