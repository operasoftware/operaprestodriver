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
package com.opera.core.systems.scope.internal;

/**
 *
 * Enumerator for delay values
 * in miliseconds
 *
 */
public enum OperaIntervals {
	RESPONSE_TIMEOUT(5000),
	PAGE_LOAD_TIMEOUT(60000),
	WINDOW_EVENT_TIMEOUT(5000),
	OPERA_IDLE_TIMEOUT(5000),
	POLL_INVERVAL(10),
	SCRIPT_RETRY(5),
	SCRIPT_RETRY_INTERVAL(50),
	EXEC_SLEEP(100),
	HANDSHAKE_TIMEOUT(30000),
	SERVER_PORT(7001),
	ENABLE_DEBUGGER(1),
	KILL_GRACE_TIMEOUT(1000),
	BACKWARDS_COMPATIBLE(1),
	DEFAULT_RESPONSE_TIMEOUT(10000),
	WAIT_FOR_ELEMENT(0),
	QUIT_POLL_INTERVAL(100),
	QUIT_RESPONSE_TIMEOUT(10000),
	SCRIPT_TIMEOUT(10000);

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
