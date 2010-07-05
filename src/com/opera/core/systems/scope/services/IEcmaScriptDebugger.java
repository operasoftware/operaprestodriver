package com.opera.core.systems.scope.services;

import java.util.List;

import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo;

public interface IEcmaScriptDebugger {

	/**
	 * Gets the active runtime's id
	 * @return ID of active runtime
	 */
	 int getRuntimeId();
	 
	 void setRuntime(RuntimeInfo runtime);

	/**
	 * Removes a runtime from the list, doesnt get called often
	 * due to runtime-stopped being sent late (runtimes get suspended)
	 * @param runtimeId
	 */
	 void removeRuntime(int runtimeId);
	 
	 /**
	  * Adds a runtime to the list of runtimes
	  * maintained in the service (STP/1 only)
	  * @param started
	  */
	 void addRuntime(RuntimeInfo info);

	/**
	 * Initialize the debugger, set the configuration to NOT
	 * stop on error, exception, script or abort
	 * Create all runtimes and find a suitable runtime for injection
	 * (Currently has a workaround or 'return 1;' for GOGI dialog at opera:debug)
	 */
	 void init();

	 /**
	  * Executes the given javascript via eval call
	  * @param script The script to be executed on host
	  * @param params Array of params, can be string, long or web element(s)
	  * @return
	  */
	 Object scriptExecutor(String script, Object... params);

	/**
	 * Execute a script and wait for response
	 * @param using script to execute
	 * @return
	 */
	 String executeJavascript(String using);

	/**
	 * Execute a script, this method allows fine tuning of
	 * responses, we are not interested in return value in some
	 * injections
	 * @param using script to execute
	 * @param responseExpected if true parse the response
	 * @return String representation of the response
	 */
	 String executeJavascript(String using, boolean responseExpected);

	 /**
	  * Executes a script and returns the response
	  * (based on responseExpected)
	  * @param using The script to be injected
	  * @param responseExpected The flag to enable/disable parsing response
	  * @return The raw result object if response is expected, <code>null</code>
	  * otherwise
	  */
	 Object executeScript(String using, boolean responseExpected);

	 /**
	  * Gets the object id using a script
	  * @param using the script to find the object
	  * @return object id with the result
	  */
	 Integer getObject(String using);
	 
	 /**
	  * The script to be executed on/using an object
	  * @param using Script with reference to the object "locator"
	  * @param objectId The object to be used for injection
	  * @return
	  */
	 String callFunctionOnObject(String using, int objectId);

	 Object callFunctionOnObject(String using, int objectId, boolean responseExpected);

	 Integer executeScriptOnObject(String using, int objectId);

	/**
	 * Change the runtime so we can inject to other frames
	 * @param framePath
	 */
	 void changeRuntime(String framePath);

	/**
	 * Change frame by index, needed for frames with nonames or duplicate names
	 * @param index
	 */
	 void changeRuntime(int index);

	/**
	 * Cleanup possible dead/suspended runtimes on
	 * runtime-started or window-closed
	 * @param windowId
	 */
	 void cleanUpRuntimes(int windowId);

	/**
	 * Cleanup runtimes with the active window id
	 */
	 void cleanUpRuntimes();

	//TODO needs retry approach?
	 List<Integer> examineObjects(Integer id);

	 /**
	  * Lists the frame paths of available runtimes
	  * @return a {@link List} of {@link String} framepaths
	  */
	 List<String> listFramePaths();
	 
	 /**
	  * Makes objects available to gc, this doesn't free
	  * the objects immediately
	  */
	 void releaseObjects();
	 
	 /**
	  * Finds a valid runtime and updates the runtime
	  * reference (active runtime)
	  * @return true if a runtime is found and updated
	  */
	 boolean updateRuntime();

	 /**
	  * Resets the list of runtimes and fetches a fresh
	  * list from Opera, this can be used to recover
	  * in cases where runtimes go out of sync
	  */
	 void resetRuntimesList();
}
