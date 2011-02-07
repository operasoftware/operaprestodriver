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
package com.opera.core.systems.runner;

import com.opera.core.systems.model.ScreenShotReply;

public interface OperaRunner {

	/**
	 * Start opera, does nothing if opera is already started
	 */
	public void startOpera();

	/**
	 * Stops opera, does nothing if opera isnot running
	 */
	public void stopOpera();

	/**
	 * Is opera running?
	 * @return true if running, false if not running or crashed.
	 */
	public boolean isOperaRunning();
	public boolean isOperaRunning(int processId);

	/**
	 * Did opera crash? This is reset on next call, so If you dont chech, you wont know
	 * @return true if opera has crashed.
	 */
	public boolean hasOperaCrashed();

	/**
	 * If opera crashed ther should be a
	 * @return
	 */
	public String getOperaCrashlog();

	// In case the runner has sockets and stuff, it needs to be shut down
	public void shutdown();

	/**
	 * Take a screenshot
	 * @param timeout
	 * @param hashes
	 * @return a screenshot with md5 and bytes.
	 */
	public ScreenShotReply saveScreenshot(long timeout, String... hashes);
}
