package com.opera.core.systems.util;

import java.util.Iterator;
import java.io.IOException;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.logging.Logger;

/**
 * This will monitor any selectable channel, such as a SocketChannel or 
 * ServerSocketChannel and fire canWrite() or canRead() events when such events
 * are detected by poll().
 *
 * @author Jan Vidar Krey
 */
public class SocketMonitor {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private Selector selector;
    private static SocketMonitor monitor = null;
    
    public static SocketMonitor instance() { 
        if (monitor == null)
            monitor = new SocketMonitor();
        return monitor;
    }
    
    private SocketMonitor() {

        try {
            selector = SelectorProvider.provider().openSelector();
            logger.fine("Starting up...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean add(SelectableChannel channel, SocketListener listener, int selectMask) {
        try {
            logger.fine("Add channel: " + channel.toString());
            channel.register(selector, selectMask, listener);

            if (channel.isBlocking())
                channel.configureBlocking(false);

            return true;
        } catch (ClosedChannelException e) {
            e.printStackTrace(); // FIXME
        } catch (IOException e) {
            e.printStackTrace(); // FIXME
        }
        return false;
    }
    
    public boolean modify(SelectableChannel channel, SocketListener listener, int selectMask) {
        logger.fine("Modify channel: " + channel.toString());
        try {
            channel.register(selector, selectMask);
            return true;
        } catch (ClosedChannelException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void remove(SelectableChannel channel) {
        logger.fine("Remove channel: " + channel.toString());
        selector.keys().remove(channel);
    }
    
    public static boolean poll()
    {
        return instance().pollSockets(java.lang.Long.MAX_VALUE);
    }
    
    public static boolean poll(long ms)
    {
        return instance().pollSockets(ms);
    }
    
    
    public boolean pollSockets(long ms) {
        logger.finest("Poll " + selector.keys().size() + " sockets");
        if (selector.keys().isEmpty())
            return false;

        try {
            selector.select(ms);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();

            try {
                processSelectionKey(key);
            } catch (IOException e) {
                SelectableChannel channel = key.channel();
                key.cancel();
            }
        }
        
        return true;
    }

    protected void processSelectionKey(SelectionKey key) throws IOException
    {
        SelectableChannel channel = key.channel();
        SocketListener listener = (SocketListener) key.attachment();
        
        int currentMask = key.interestOps();
        int triggerMask = key.readyOps();
        int wantedMask = 0;
        
        if (key.isValid() && key.isAcceptable()) {
            if (listener.canRead(channel))
                wantedMask |= SelectionKey.OP_ACCEPT;
        }

        if (key.isValid() && key.isConnectable()) {
            if (listener.canRead(channel))
                wantedMask |= SelectionKey.OP_CONNECT;
        }
        
        if (key.isValid() && key.isReadable()) {
            if (listener.canRead(channel))
                wantedMask |= SelectionKey.OP_READ;
        }

        if (key.isValid() && key.isWritable()) {
            if (listener.canWrite(channel))
                wantedMask |= SelectionKey.OP_WRITE;
        }

        // In case we did not trigger something we want to poll for
        int not_triggered = (currentMask & ~triggerMask);
        wantedMask |= not_triggered;

        // Update the selection mask, if it now differs
        if (wantedMask != currentMask)
        {
            if (wantedMask != 0)
            {
                key.interestOps(wantedMask);
            }
            else
            {
                key.cancel();
            }
        }
    }

/*
    private String debugMask(int mask)
    {
        String str  = "{";
        
        if ((mask & SelectionKey.OP_READ) == SelectionKey.OP_READ)
            str += " READ";
        
        if ((mask & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE)
            str += " WRITE";

        if ((mask & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT)
            str += " ACCEPT";
        
        if ((mask & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT)
            str += " CONNECT";

        str += " }";
        return str;
    }
*/
}
