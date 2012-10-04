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

import com.opera.core.systems.internal.SocketMonitor;
import com.opera.core.systems.scope.handlers.EventHandler;
import com.opera.core.systems.scope.handlers.ConnectionHandler;
import com.opera.core.systems.scope.internal.OperaIntervals;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * This thread starts the {@link StpConnectionListener} which listens for incoming connections and
 * accepts those connections.
 *
 * This thread also owns the {@link SocketMonitor} instances so all network traffic and
 * communication is happening on this thread.
 */
public class StpThread extends Thread {

  private final Logger logger = Logger.getLogger(getClass().getName());
  private final StpConnectionListener listener;
  private final SocketMonitor monitor;
  private volatile boolean cancelled;

  public StpThread(int port, ConnectionHandler handler, EventHandler eventHandler,
                   boolean manualConnect) throws IOException {
    monitor = new SocketMonitor();
    listener = new StpConnectionListener(port, handler, eventHandler, manualConnect, monitor);
    setName("stp-thread");
  }

  public void shutdown() {
    cancelled = true;
    listener.stop();
    monitor.stop();
  }

  @Override
  public void run() {
    logger.finer("Started StpThread");
    while (!cancelled) {
      monitor.poll(OperaIntervals.RESPONSE_TIMEOUT.getMs());
    }
    logger.finer("Stopping StpThread");
  }

}