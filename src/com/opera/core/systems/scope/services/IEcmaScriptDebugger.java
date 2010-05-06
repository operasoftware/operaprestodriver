package com.opera.core.systems.scope.services;

import java.util.List;

import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo;
import com.opera.core.systems.scope.beans.Runtime;

public interface IEcmaScriptDebugger {

	 int getRuntimeId();

	 void setRuntime(Runtime runtime);
	 
	 void setRuntime(RuntimeInfo runtime);

	/**
	 * Removes a runtime from the list, doesnt get called often
	 * due to runtime-stopped being sent late (runtimes get suspended)
	 * @param runtimeId
	 */
	 void removeRuntime(int runtimeId);
	 
	 void addRuntime(Runtime started);
	 
	 void addRuntime(RuntimeInfo info);

	/**
	 * Initialize the debugger, set the configuration to NOT
	 * stop on error, exception, script or abort
	 * Create all runtimes and find a suitable runtime for injection
	 * (Currently has a workaround or 'return 1;' for GOGI dialog at opera:debug)
	 */
	 void init();

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

	 Object executeScript(String using, boolean responseExpected);

	 Integer getObject(String using);

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

	 List<String> listFramePaths();
	 
	 void releaseObjects();
	 
	 boolean updateRuntime();

	 void resetRuntimesList();
}