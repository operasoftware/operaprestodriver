package com.opera.core.systems.scope;

import com.opera.core.systems.model.ICommand;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This enum maps the commands for the <a href=
 * "http://dragonfly.opera.com/app/scope-interface/services/Scope/Scope_1_0.html"
 * >Scope 1.0</a>.
 * 
 */
public enum ScopeCommand implements ICommand {
    CONNECT(3),
    DISCONNECT(4),
    ENABLE(5),
    DISABLE(6),
    INFO(7),
    QUIT(8),
    HOST_INFO(10),
    MESSAGE_INFO(11),
    DEFAULT(-1);

    private int code;
    private static final Map<Integer, ScopeCommand> lookup = new HashMap<Integer, ScopeCommand>();

    static {
        for(ScopeCommand command : EnumSet.allOf(ScopeCommand.class))
            lookup.put(command.getCommandID(), command);
    }

    private ScopeCommand(int code) {
        this.code = code;
    }

    public int getCommandID() {
        return code;
    }

    public String getServiceName() {
        return "scope";
    }

    public static ScopeCommand get(int code) {
    	ScopeCommand command = lookup.get(code);
        return (command != null) ? command : DEFAULT;
    }
}

