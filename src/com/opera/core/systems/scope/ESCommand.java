package com.opera.core.systems.scope;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.opera.core.systems.model.ICommand;

public enum ESCommand implements ICommand {
	LIST_RUNTIMES(1), 
	EVAL(2),
	EXAMINE_OBJECTS(3), 
	RELEASE_OBJECTS(4),
	ON_READY_STATE_CHANGED(5),
	DEFAULT(-1);

	private int code;
	private static final Map<Integer, ESCommand> lookup = new HashMap<Integer, ESCommand>();

	static {
		for (ESCommand command : EnumSet.allOf(ESCommand.class))
			lookup.put(command.getCommandID(), command);
	}

	private ESCommand(int code) {
		this.code = code;
	}

	public int getCommandID() {
		return code;
	}

	public static ESCommand get(int code) {
		ESCommand command = lookup.get(code);
		if(command == null)
			return DEFAULT;
		return command;
	}

	public String getServiceName() {
		return "ecmascript";
	}

}