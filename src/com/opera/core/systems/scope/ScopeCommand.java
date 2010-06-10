package com.opera.core.systems.scope;

import com.opera.core.systems.model.ICommand;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum ScopeCommand implements ICommand {
    CONNECT(3),
    DISCONNECT(4),
    ENABLE(5),
    DISABLE(6),
    HOST_INFO(10),
    MESSAGE_INFO(11);

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

    public static ScopeCommand get(int code) {
        return lookup.get(code);
    }
}
