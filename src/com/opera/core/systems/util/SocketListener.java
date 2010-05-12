package com.opera.core.systems.util;

import java.nio.channels.SelectableChannel;
import java.io.IOException;

/**
 *
 * @author Jan Vidar Krey
 */
public interface SocketListener {

    /**
     * @return true if more reading is expected.
     */
    public abstract boolean canRead(SelectableChannel ch) throws IOException;
    
    /**
     * @return true if more writing is needed.
     */
    public abstract boolean canWrite(SelectableChannel ch) throws IOException;
    
}
