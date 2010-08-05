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
 * @author Jan Vidar Krey
 */
public class StpThread extends Thread {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private StpConnectionListener listener;
    private volatile boolean cancelled;
    
    public StpThread(int port, IConnectionHandler handler, AbstractEventHandler eventHandler) throws IOException
    {
        SocketMonitor.instance();
        listener = new StpConnectionListener(port, handler, eventHandler);
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
        while (!cancelled)
        {
            SocketMonitor.poll(60000);
        }
        logger.fine("Stopping StpThread.");
    }

}
