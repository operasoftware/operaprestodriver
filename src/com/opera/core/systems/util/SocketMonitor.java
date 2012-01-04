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

package com.opera.core.systems.util;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This will monitor any selectable channel, such as a SocketChannel or
 * ServerSocketChannel and fire canWrite() or canRead() events when such events
 * are detected by poll().
 *
 * @author Jan Vidar Krey <janv@opera.com>
 */
public class SocketMonitor {

  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private Selector selector;
  boolean locked = false;

  private enum Operation {
    ADD, MODIFY, REMOVE
  }

  class SelectorChangeRequest {
    protected SelectableChannel channel;
    protected Operation op;
    protected int mask;
    protected SocketListener listener;

    public SelectorChangeRequest(SelectableChannel channel, Operation op,
        int mask, SocketListener listener) {
      this.channel = channel;
      this.op = op;
      this.mask = mask;
      this.listener = listener;
    }

    public SelectorChangeRequest(SelectableChannel channel, Operation op) {
      this.channel = channel;
      this.op = op;
      this.mask = 0;
      this.listener = null;
    }

    public void dump() {
      switch (op) {
      case ADD:
        logger.finest("SelectorChangeRequest: Add socket=" + channel.toString()
            + ", mask=" + mask);
        break;

      case MODIFY:
        logger.finest("SelectorChangeRequest: Modify socket="
            + channel.toString() + ", mask=" + mask);
        break;

      case REMOVE:
        logger.finest("SelectorChangeRequest: Remove socket="
            + channel.toString());
        break;
      }
    }

  }

  private LinkedList<SelectorChangeRequest> changes = new LinkedList<SelectorChangeRequest>();

  public SocketMonitor() {
    try {
      selector = SelectorProvider.provider().openSelector();
      logger.setLevel(Level.OFF);
      logger.severe("Starting up...");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public boolean add(SelectableChannel channel, SocketListener listener,
      int selectMask) {
    logger.info("Add channel: " + channel.toString() + ", mask="
        + debugMask(selectMask));

    synchronized (changes) {
      changes.add(new SocketMonitor.SelectorChangeRequest(channel,
          Operation.ADD, selectMask, listener));
      selector.wakeup();
    }
    return true;
  }

  public boolean modify(SelectableChannel channel, SocketListener listener,
      int selectMask) {
    logger.info("Modify channel: "
        + (channel != null ? channel.toString() : "null") + ", mask="
        + debugMask(selectMask));
    if (channel == null) return false;

    synchronized (changes) {
      changes.add(new SocketMonitor.SelectorChangeRequest(channel,
          Operation.MODIFY, selectMask, listener));
      selector.wakeup();
    }
    return true;
  }

  public void remove(SelectableChannel channel) {
    if (channel == null) return;
    logger.info("Remove channel: " + channel.toString());
    synchronized (changes) {
      changes.add(new SocketMonitor.SelectorChangeRequest(channel,
          Operation.REMOVE));
      selector.wakeup();
    }
  }

  public void stop() {
    selector.wakeup();
  }

  public boolean poll() {
    return poll(java.lang.Long.MAX_VALUE);
  }

  public boolean poll(long ms) {
    boolean ok = pollSockets(ms);
    applyChanges();
    return ok;
  }

  protected boolean pollSockets(long ms) {
    if (selector.keys().isEmpty()) {
      return true;
    }

    try {
      locked = true;
      synchronized (selector) {
        selector.select(ms);
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    } finally {
      locked = false;
    }

    locked = true;
    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
    while (iterator.hasNext()) {
      SelectionKey key = iterator.next();
      iterator.remove();
      try {
        processSelectionKey(key);
      } catch (CancelledKeyException cke) {
        cke.printStackTrace();
      } catch (IOException e) {
        // what are you doing with the channel variable here?
        // SelectableChannel channel = key.channel();
        e.printStackTrace();
        key.cancel();
      }
    }
    locked = false;
    return true;
  }

  protected void applyChanges() {
    synchronized (changes) {
      if (changes.isEmpty()) return;

      Iterator<SocketMonitor.SelectorChangeRequest> it = changes.iterator();
      while (it.hasNext()) {
        try {
          SocketMonitor.SelectorChangeRequest req = it.next();
          req.dump();
          switch (req.op) {
          case ADD:
            try {
              req.channel.register(selector, req.mask, req.listener);
            } catch (ClosedChannelException e) {
              e.printStackTrace();
            }
            break;
          case MODIFY:
            SelectionKey key = req.channel.keyFor(selector);
            if (key != null) key.interestOps(req.mask);
            break;
          case REMOVE:
            SelectionKey selKey = req.channel.keyFor(selector);
            if (selKey != null) selKey.cancel();
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

    if (!key.isValid()) return;

    SocketListener listener = (SocketListener) key.attachment();

    int currentMask = key.interestOps();
    int triggerMask = key.readyOps();
    int wantedMask = 0;

    if (key.isValid() && key.isAcceptable()) {
      if (listener.canRead(channel)) wantedMask |= SelectionKey.OP_ACCEPT;
    }

    if (key.isValid() && key.isConnectable()) {
      if (listener.canRead(channel)) wantedMask |= SelectionKey.OP_CONNECT;
    }

    if (key.isValid() && key.isReadable()) {
      if (listener.canRead(channel)) wantedMask |= SelectionKey.OP_READ;
    }

    if (key.isValid() && key.isWritable()) {
      if (listener.canWrite(channel)) wantedMask |= SelectionKey.OP_WRITE;
    }

    // In case we did not trigger something we want to poll for
    int notTriggered = (currentMask & ~triggerMask);
    wantedMask |= notTriggered;

    // Update the selection mask, if it now differs
    if (wantedMask != currentMask) {
      if (wantedMask != 0) {
        if (key.isValid()) key.interestOps(wantedMask);
      } else {
        key.cancel();
      }
    }
  }

  private String debugMask(int mask) {
    StringBuilder builder = new StringBuilder();
    builder.append("{");

    if ((mask & SelectionKey.OP_READ) == SelectionKey.OP_READ) builder.append(" READ");

    if ((mask & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) builder.append(" WRITE");

    if ((mask & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) builder.append(" ACCEPT");

    if ((mask & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT) builder.append(" CONNECT");

    builder.append("}");
    return builder.toString();
  }

}