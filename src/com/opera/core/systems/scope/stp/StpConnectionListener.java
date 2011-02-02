package com.opera.core.systems.scope.stp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

import com.opera.core.systems.scope.handlers.AbstractEventHandler;
import com.opera.core.systems.scope.handlers.IConnectionHandler;
import com.opera.core.systems.util.SocketListener;
import com.opera.core.systems.util.SocketMonitor;

/**
 * This class handles accepting STP connections.
 * STP connections are accepted in the canRead() metod, which then
 * spawns an StpConnection.
 *
 * @author Jan Vidar Krey
 */
public class StpConnectionListener implements SocketListener {

    private int port;
    private ServerSocketChannel server = null;
    private IConnectionHandler handler;
    private AbstractEventHandler eventHandler;
    private final Logger logger = Logger.getLogger(this.getClass().getName());
        
    public StpConnectionListener(int port, IConnectionHandler handler, AbstractEventHandler eventHandler) throws IOException {
        this.port = port;
        this.handler = handler;
        this.eventHandler = eventHandler;
        start();
    }

    @Override
    protected void finalize() throws Throwable {
        stop();
        super.finalize();
    }

    public void stop()
    {
        if (server == null)
            return;

        logger.fine("Shutting down STP connection listener...");
        SocketMonitor.instance().remove(server);
        try {
            server.close();
        } catch (Exception ignored) { // IOException or NullPointerException
            // ignored
        } finally {
            server = null;
        }
    }
    
    public void start() throws IOException
    {
        server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.socket().setReuseAddress(true);
        server.socket().bind(new InetSocketAddress(port));

        SocketMonitor.instance().add(server, this, SelectionKey.OP_ACCEPT);

        logger.info("webdriver-opera " + "0.2.9" /*VERSION*/ + " is ready to accept connections on port " + port);
    }

    public boolean canRead(SelectableChannel channel) throws IOException {
    	
		if (!server.isOpen()) {
			return false;
		}
		
        SocketChannel socket = server.accept();
        if (socket != null) {
            logger.fine("Accepted STP connection from " + socket.socket().getLocalAddress());
            socket.socket().setTcpNoDelay(true);
            new StpConnection(socket, handler, eventHandler);
        }
        return true;
    }
    
    //FIXME ?
    public boolean canWrite(SelectableChannel ch) throws IOException {
        return false;
    }
}
