package com.opera.core.systems.scope.stp;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SelectableChannel;

import com.opera.core.systems.util.SocketMonitor;
import com.opera.core.systems.util.SocketListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.opera.core.systems.scope.handlers.IConnectionHandler;
import java.util.logging.Logger;

/**
 * This class handles accepting STP connections.
 *
 * @author Jan Vidar Krey
 */
public class StpConnectionListener implements SocketListener {

    private int port;
    private ServerSocketChannel server = null;
    private IConnectionHandler handler;
    private final Logger logger = Logger.getLogger(this.getClass().getName());
        
    public StpConnectionListener(int port, IConnectionHandler handler) throws IOException {
        this.port = port;
        this.handler = handler;
        start();
    }

    @Override
    protected void finalize() throws Throwable {
        if (server != null) {
            stop();
        }
        super.finalize();
    }

    public void stop()
    {
        logger.fine("Shutting down...");
        SocketMonitor.instance().remove(server);
        try {
            server.close();
        } catch (IOException ignored) { 
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

        logger.info("webdriver-opera v0.2.100506, ready to accept connections on port " + port);
    }

    public boolean canRead(SelectableChannel channel) throws IOException {
        SocketChannel socket = server.accept();
        if (socket != null) {
            logger.fine("Accepted STP connection from " + socket.socket().getLocalAddress());
            StpConnection con = new StpConnection(socket, handler);
        }
        return true;
    }
    
    public boolean canWrite(SelectableChannel ch) throws IOException {
        return false;
    }
}
