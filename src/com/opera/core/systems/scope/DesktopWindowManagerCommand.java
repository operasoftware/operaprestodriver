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
package com.opera.core.systems.scope;

import com.opera.core.systems.model.ICommand;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This enum maps the commands for the <a href=
 * "http://dragonfly.opera.com/app/scope-interface/services/DesktopWindowManager/DesktopWindowManager_2_0.html"
 * >Window Manager 2.0</a>.
 *
 */
public enum DesktopWindowManagerCommand implements ICommand {
	    GET_ACTIVE_WINDOW(1),
	    LIST_WINDOWS(2),
	    LIST_QUICK_WIDGETS(3),
	    GET_QUICK_WIDGET(4),
	    WINDOW_SHOWN(5),     // event
	    WINDOW_UPDATED(6),   // event
	    WINDOW_CLOSED(7),    // event
	    WINDOW_ACTIVATED(8), // event
	    WINDOW_LOADED(9), // event
	    DEFAULT(-1);

	    private int code;
	    private static final Map<Integer, DesktopWindowManagerCommand> lookup = new HashMap<Integer, DesktopWindowManagerCommand>();

	    static {
	        for (DesktopWindowManagerCommand command : EnumSet.allOf(DesktopWindowManagerCommand.class))
	            lookup.put(command.getCommandID(), command);
	    }

	    private DesktopWindowManagerCommand(int code) {
	        this.code = code;
	    }

	    public int getCommandID() {
	        return code;
	    }

	    public String getServiceName() {
	        return "desktop-window-manager";
	    }

	    public static DesktopWindowManagerCommand get(int code) {
	        DesktopWindowManagerCommand command = lookup.get(code);
	        return (command != null) ? command : DEFAULT;
	    }

}
