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
package com.opera.core.systems.util;

import java.nio.channels.SelectableChannel;
import java.io.IOException;

/**
 * Interface for use with SocketMonitor for being notified about read/write
 * events.
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
