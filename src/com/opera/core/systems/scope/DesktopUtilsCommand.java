package com.opera.core.systems.scope;

import com.opera.core.systems.model.ICommand;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This enum maps the commands 
 * 
 */
public enum DesktopUtilsCommand implements ICommand {
	GET_STRING(1),
	GET_OPERA_PATH(2),
	GET_LARGE_PREFERENCES_PATH(3),
	GET_SMALL_PREFERENCES_PATH(4),
	GET_CACHE_PREFERENCES_PATH(5),
	GET_OPERA_PID(6),
	DEFAULT(-1);    // event
	
	private int code;
	private static final Map<Integer, DesktopUtilsCommand> lookup = new HashMap<Integer, DesktopUtilsCommand>();
	
	static {
	    for (DesktopUtilsCommand command : EnumSet.allOf(DesktopUtilsCommand.class))
	        lookup.put(command.getCommandID(), command);
	}
	
	private DesktopUtilsCommand(int code) {
	    this.code = code;
	}
	
	public int getCommandID() {
	    return code;
	}
	
	public String getServiceName() {
	    return "desktop-utils";
	}
	
	public static DesktopUtilsCommand get(int code) {
		DesktopUtilsCommand command = lookup.get(code);
	    return (command != null) ? command : DEFAULT;
	}
}
