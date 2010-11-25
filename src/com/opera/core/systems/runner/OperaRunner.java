package com.opera.core.systems.runner;

public interface OperaRunner {
	
	public void startOpera();
	
	public void stopOpera();
	
	public boolean isOperaRunning();
	
	public boolean hasOperaCrashed();
	
	public String getOperaCrashlog();
	
	// In case the runner has sockets and stuff, it needs to be shut down
	public void shutdown();
}
