/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.scope.internal;

/**
 * 
 * @author Deniz Turkoglu
 * Enumerator for delay values
 */
public enum OperaIntervals {
	RESPONSE_TIMEOUT(5000),
	PAGE_LOAD_TIMEOUT(5000),
	POLL_INVERVAL(10),
	SCRIPT_RETRY(5),
	SCRIPT_RETRY_INTERVAL(50),
	EXEC_SLEEP(10), 
	HANDSHAKE_TIMEOUT(5000),
	SERVER_PORT(7001),
	ENABLE_DEBUGGER(1),
	BACKWARDS_COMPATIBLE(1);
	
	private long value;
	
	private OperaIntervals(long value) {
		this.value = value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public long getValue() {
		return value;
	}
}
