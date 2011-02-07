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
	 * If opera crashed there should be a crashlog. 
	 * @return Returns the crashlog, or null if it does not exist.
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
