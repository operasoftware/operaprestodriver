package com.opera.core.systems.scope.stp;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum Status {
	OK(0),
	CONFLICT(1),
	UNSUPPORTED_TYPE(2),
	BAD_REQUEST(3),
	INTERNAL_ERROR(4),
	COMMAND_NOT_FOUND(5),
	SERVICE_NOT_FOUND(6),
	OOM(7),
	SERVICE_NOT_ENABLED(8),
	SERVICE_ALREADY_ENABLED(9);
	
	private int code;
	private static final Map<Integer,Status> lookup = new HashMap<Integer,Status>();

	static {
    for(Status status : EnumSet.allOf(Status.class))
         lookup.put(status.getCode(), status);
	}

	private Status(int code) {
		this.code = code;
	}
	 public int getCode() { return code; }

     public static Status get(int code) { 
          return lookup.get(code); 
     }

}