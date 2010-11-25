package com.opera.core.systems.runner;

/**
 * User whenever runner has problems. We are NOT supposed to fix
 * launcher problems. Just pass them upwards
 * 
 * @author anderso
 */
public class OperaRunnerException extends RuntimeException {
	private static final long serialVersionUID = -2379934570244878775L;

	public OperaRunnerException(){
		super();
	}
	public OperaRunnerException(String message){
		super(message);
	}
	public OperaRunnerException(String message, Throwable cause){
		super(message, cause);
	}
}
