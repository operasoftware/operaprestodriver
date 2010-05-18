package com.opera.core.systems.scope.stp;

import java.util.logging.Logger;

import com.opera.core.systems.util.SocketMonitor;
import com.opera.core.systems.scope.handlers.IConnectionHandler;


/**
 *
 * @author Jan Vidar Krey
 */
public class StpThread extends Thread {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private StpConnectionListener listener;
    private StpConnection stpConnection;
    private boolean running = true;
    
    public StpThread(int port, IConnectionHandler handler) throws java.io.IOException
    {
        super("StpThread");
        SocketMonitor.instance();
        listener = new StpConnectionListener(port, handler);
        start();
    }
    
    public void shutdown() {
        running = false;
        listener.stop();
        SocketMonitor.instance().stop();
    }
    
    
    @Override
    public void run() {
        logger.info("Started StpThread.");
        while (running)
        {
            SocketMonitor.poll(50);
        }
        logger.info("Stopping StpThread.");
    }

}
