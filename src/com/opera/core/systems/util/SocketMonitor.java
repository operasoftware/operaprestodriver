package com.opera.core.systems.util;

import java.util.Iterator;
import java.io.IOException;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.logging.Logger;
import java.util.LinkedList;

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
    boolean locked = false;

    private enum Operation {
        ADD, MODIFY, REMOVE
    }
    
    class SelectorChangeRequest
    {
        protected SelectableChannel channel;
        protected Operation op;
        protected int mask;
        protected SocketListener listener;

        public SelectorChangeRequest(SelectableChannel channel, Operation op, int mask, SocketListener listener)
        {
            this.channel = channel;
            this.op = op;
            this.mask = mask;
            this.listener = listener;
        }

        public SelectorChangeRequest(SelectableChannel channel, Operation op)
        {
            this.channel = channel;
            this.op = op;
            this.mask = 0;
            this.listener = null;
        }
        
        public void dump()
        {
            switch (op)
            {
                case ADD:
                        System.out.println("SelectorChangeRequest: Add socket=" + channel.toString() + ", mask=" + mask);
                        break;

                case MODIFY:
                        System.out.println("SelectorChangeRequest: Modify socket=" + channel.toString() + ", mask=" + mask);
                        break;

                case REMOVE:
                        System.out.println("SelectorChangeRequest: Remove socket=" + channel.toString());
                        break;
            }
        }

    }

    private LinkedList<SelectorChangeRequest> changes = new LinkedList<SelectorChangeRequest>();

    
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
        logger.fine("Add channel: " + channel.toString());

        synchronized (changes) {
            changes.add(new SocketMonitor.SelectorChangeRequest(channel, Operation.ADD, selectMask, listener));
            selector.wakeup();
        }
        return true;
    }
    
    public boolean modify(SelectableChannel channel, SocketListener listener, int selectMask) {
        logger.fine("Modify channel: " + channel.toString() + ", locked=" + locked);
         synchronized (changes) {
            changes.add(new SocketMonitor.SelectorChangeRequest(channel, Operation.MODIFY, selectMask, listener));
            selector.wakeup();
        }
        return true;
    }
    
    public void remove(SelectableChannel channel) {
        logger.fine("Remove channel: " + channel.toString());
        synchronized (changes) {
            changes.add(new SocketMonitor.SelectorChangeRequest(channel, Operation.REMOVE));
            selector.wakeup();
        }
    }
    
    public static boolean poll()
    {
        return instance().pollSockets(java.lang.Long.MAX_VALUE);
    }
    
    public static boolean poll(long ms)
    {
        boolean ok = instance().pollSockets(ms);
        instance().applyChanges();
        return ok;
    }
    
    
    protected boolean pollSockets(long ms) {
        // logger.finest("Poll " + selector.keys().size() + " sockets");
        
        if (selector.keys().isEmpty())
            return false;

        
        try {
            locked = true;
            selector.select(ms);
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
            } catch (IOException e) {
                SelectableChannel channel = key.channel();
                key.cancel();
            }
        }
        locked = false;
        return true;
    }
    
    protected void applyChanges()
    {
 
        synchronized (changes)
        {
        
            if (changes.size() == 0)
                return;

            logger.finest("Applying " + changes.size() + " changes to selector.");

            Iterator<SocketMonitor.SelectorChangeRequest> it = changes.iterator();
            while (it.hasNext())
            {
                SocketMonitor.SelectorChangeRequest req = it.next();
                req.dump();
                switch (req.op)
                {
                    case ADD:
                        try {
                            req.channel.register(selector, req.mask, req.listener);
                        } catch (ClosedChannelException e) {
                            e.printStackTrace();
                        }
                        break;
                    case MODIFY:
                        SelectionKey key = req.channel.keyFor(selector);
                        if (key != null)
                        {
                            key.interestOps(req.mask);
                        } else {
                            try {
                                req.channel.register(selector, req.mask, req.listener);
                            } catch (ClosedChannelException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case REMOVE:
                        selector.keys().remove(req.channel);
                        break;
                }
            }
            changes.clear();
        }
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
