/*
Copyright 2008-2011 Opera Software ASA

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

import java.io.IOException;
import java.util.logging.Logger;

import com.opera.core.systems.scope.handlers.AbstractEventHandler;
import com.opera.core.systems.scope.handlers.IConnectionHandler;
import com.opera.core.systems.util.SocketMonitor;

/**
 * This thread starts the StpConnectionListener which listens for incoming
 * connections and accepts those connections.
 * 
 * This thread also owns the SocketMonitor instance - so all network traffic and
 * communication is happening on this thread.
 * 
 * @author Jan Vidar Krey <janv@opera.com>
 */
public class StpThread extends Thread {

  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private StpConnectionListener listener;
  private volatile boolean cancelled;

  public StpThread(int port, IConnectionHandler handler,
      AbstractEventHandler eventHandler, boolean manualConnect)
      throws IOException {
    SocketMonitor.instance();
    listener = new StpConnectionListener(port, handler, eventHandler,
        manualConnect);
    setName("stp-thread");
  }

  public void shutdown() {
    cancelled = true;
    listener.stop();
    SocketMonitor.instance().stop();
  }

  @Override
  public void run() {
    logger.fine("Started StpThread.");
    while (!cancelled) {
      SocketMonitor.poll(60000);
    }
    logger.fine("Stopping StpThread.");
  }

}
