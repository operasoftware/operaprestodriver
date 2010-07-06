package com.opera.core.systems.scope;

import com.opera.core.systems.model.ICommand;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This enum maps the commands for the <a href=
 * "http://dragonfly.opera.com/app/scope-interface/services/WindowManager/WindowManager_2_0.html"
 * >Window Manager 2.0</a>.
 * 
 */
public enum WindowManagerCommand implements ICommand {
    GET_ACTIVE_WINDOW(1),
    LIST_WINDOWS(2),
    MODIFY_FILTER(3),
    WINDOW_UPDATED(4),   // event
    WINDOW_CLOSED(5),    // event
    WINDOW_ACTIVATED(6), // event
    WINDOW_LOADED(7),
    DEFAULT(-1);    // event

    private int code;
    private static final Map<Integer, WindowManagerCommand> lookup = new HashMap<Integer, WindowManagerCommand>();

    static {
        for (WindowManagerCommand command : EnumSet.allOf(WindowManagerCommand.class))
            lookup.put(command.getCommandID(), command);
    }

    private WindowManagerCommand(int code) {
        this.code = code;
    }

    public int getCommandID() {
        return code;
    }

    public String getServiceName() {
        return "window-manager";
    }

    public static WindowManagerCommand get(int code) {
        WindowManagerCommand command = lookup.get(code);
        return (command != null) ? command : DEFAULT;
    }

}
