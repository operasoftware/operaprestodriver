package com.opera.core.systems.scope;

import com.opera.core.systems.model.ICommand;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum SystemInputCommand implements ICommand {
	    CLICK(1),
	    KEYPRESS(2),
	    MOUSEUP(3),
	    MOUSEDOWN(4),
	    MOUSEMOVE(5),
	    KEYUP(6),
	    DEFAULT(-1);
	    
	    private int code;
	    private static final Map<Integer, SystemInputCommand> lookup = new HashMap<Integer, SystemInputCommand>();

	    static {
	        for (SystemInputCommand command : EnumSet.allOf(SystemInputCommand.class))
	            lookup.put(command.getCommandID(), command);
	    }

	    private SystemInputCommand(int code) {
	        this.code = code;
	    }

	    public int getCommandID() {
	        return code;
	    }

	    public String getServiceName() {
	        return "system-input";
	    }

	    public static SystemInputCommand get(int code) {
	        SystemInputCommand command = lookup.get(code);
	        return (command != null) ? command : DEFAULT;
	    }

}
