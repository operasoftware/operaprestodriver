package com.opera.core.systems.scope;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.opera.core.systems.model.ICommand;

public enum CoreCommand implements ICommand {
	ONACTIVE(1),
	ONIDLE(2),
    DEFAULT(-1);
	
	private int code;
    private static final Map<Integer, CoreCommand> lookup = new HashMap<Integer, CoreCommand>();

    static {
        for (CoreCommand command : EnumSet.allOf(CoreCommand.class))
            lookup.put(command.getCommandID(), command);
    }

    private CoreCommand(int code) {
        this.code = code;
    }

    public int getCommandID() {
        return code;
    }

    public String getServiceName() {
        return "ecmascript-debugger";
    }

    public static CoreCommand get(int code) {
    	CoreCommand command = lookup.get(code);
        return (command != null) ? command : DEFAULT;
    }
}
