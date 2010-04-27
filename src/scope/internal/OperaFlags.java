package com.opera.core.systems.scope.internal;

public class OperaFlags {
	//compile flags
	
	//enable checks, disabled in internal use to allow clicking on disabled elements, etc..
	public static final boolean ENABLE_CHECKS = false;
	
	//enable active window to be of type dialog, not allowed in interactive test (not cross platform)
	//true for spartan, false for watir/webdriver
	public static final boolean ENABLE_DIALOGS = false;
}
