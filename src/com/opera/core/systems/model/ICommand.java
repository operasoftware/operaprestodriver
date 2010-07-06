package com.opera.core.systems.model;

public interface ICommand {
	
	/**
	 * Get command id to execute on host, service definitions are
	 * available on <a href="http://dragonfly.opera.com/app/scope-interface/index.html">Scope Interface</a>
	 * 
	 * @return The id associated with the command
	 */
	int getCommandID();

	/**
	 * Associates a command id with a service name
	 * 
	 * @return Name of the service the command belongs to
	 */
	String getServiceName();
}
