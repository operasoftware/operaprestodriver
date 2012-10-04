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

package com.opera.core.systems.scope.stp;

import com.opera.core.systems.scope.handlers.ConnectionHandler;
import com.opera.core.systems.scope.handlers.EventHandler;
import com.opera.core.systems.internal.SocketListener;
import com.opera.core.systems.internal.SocketMonitor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

/**
 * This class handles accepting STP connections. STP connections are accepted in the {@link
 * SocketListener#canRead(java.nio.channels.SelectableChannel)} method, which then spawns an {@link
 * StpConnection}.
 */
public class StpConnectionListener implements SocketListener {

  private final Logger logger = Logger.getLogger(getClass().getName());
  private final int port;
  private final ConnectionHandler handler;
  private final EventHandler eventHandler;
  private final boolean manualConnect;

  private ServerSocketChannel server = null;
  private SocketMonitor monitor;

  public StpConnectionListener(int port, ConnectionHandler handler, EventHandler eventHandler,
                               boolean manualConnect, SocketMonitor monitor) throws IOException {
    this.port = port;
    this.handler = handler;
    this.eventHandler = eventHandler;
    this.manualConnect = manualConnect;
    this.monitor = monitor;

    start();
  }

  @Override
  protected void finalize() throws Throwable {
    stop();
    super.finalize();
  }

  /**
   * Shuts down and cleans up the STP connection to Opera.
   */
  public void stop() {
    if (server == null) {
      return;
    }

    logger.fine("Shutting down STP connection listener");
    monitor.remove(server);
    try {
      server.close();
    } catch (IOException e) {
      // ignored
    } finally {
      server = null;
    }
  }

  /**
   * Starts the STP connection listener.
   *
   * @throws IOException if unable to bind to address
   */
  public void start() throws IOException {
    server = ServerSocketChannel.open();
    server.configureBlocking(false);
    server.socket().setReuseAddress(true);
    server.socket().bind(new InetSocketAddress(port));

    monitor.add(server, this, SelectionKey.OP_ACCEPT);

    // Print a message when we are waiting to connect manually
    if (manualConnect) {
      System.out.println("Ready to accept connections on port " + port);
    }
  }

  public boolean canRead(SelectableChannel channel) throws IOException {
    if (!server.isOpen()) {
      return false;
    }

    SocketChannel socket = server.accept();
    if (socket != null) {
      logger.fine("Accepted connection from " + socket.socket().getLocalAddress());
      socket.socket().setTcpNoDelay(true);
      new StpConnection(socket, handler, eventHandler, monitor);
    }

    return true;
  }

  public boolean canWrite(SelectableChannel ch) throws IOException {
    return false;
  }

}