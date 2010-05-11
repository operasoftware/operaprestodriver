package com.opera.core.systems.scope.stp;

import java.net.*;
import java.io.IOException;
import com.opera.core.systems.scope.handlers.IConnectionHandler;
import java.util.logging.Logger;

/**
 * This class handles accepting STP connections.
 *
 * @author Jan Vidar Krey
 */
public class StpConnectionListener extends Thread {

    private int port;
    private ServerSocket server = null;
    private IConnectionHandler handler;
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private boolean running = true;
        
    public StpConnectionListener(int port, IConnectionHandler handler) {
            this.port = port;
            this.handler = handler;
            start();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (server != null) {
            try {
                server.close();
            } catch (IOException ignored) {
                /* do nothing */
            }
        }
    }

    public void shutdown()
    {
        logger.fine("Shutting down...");
        running = false;
        this.interrupt();
    }
    
    @Override
    public void start()
    {
        try {
            server = new ServerSocket(port, 5);
            server.setReuseAddress(true);
            logger.info("webdriver-opera v0.2.100506, ready to accept connections on port " + port);
            running = true;
        } catch (IOException e) {
            handler.onException(e);
            running = false;
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Socket socket = server.accept();
                StpConnection con = new StpConnection(socket, handler);
            } catch (IOException ioe) {
                /* Unable to accept socket */
                logger.warning("Error while accepting socket: " + ioe.getMessage());
                continue;
            }
        }
        logger.fine("Stopped.");

        try {
            server.close();
        } catch (IOException ignored) {
            /* do nothing */
        } finally {
            server = null;
        }
        
    }
    
}
