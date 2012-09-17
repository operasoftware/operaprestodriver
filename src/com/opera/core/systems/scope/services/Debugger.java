package com.opera.core.systems.scope.services;

import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.scope.Service;
import com.opera.core.systems.scope.protos.EcmascriptProtos;
import com.opera.core.systems.scope.protos.EsdbgProtos;

import java.util.List;

public interface Debugger extends Service {

  /**
   * Set the driver this service belongs to. Needed so that we can create OperaWebElements in this
   * service.
   */
  void setDriver(OperaDriver driver);

  /**
   * Get the current runtime ID.
   *
   * @return runtime ID
   */
  int getRuntimeId();

  /**
   * Sets the runtime to be used in injection required for event handler (STP/1 only)
   */
  void setRuntime(EsdbgProtos.RuntimeInfo runtime);

  /**
   * Removes a runtime from the list, doesnt get called often due to runtime-stopped being sent late
   * (runtimes get suspended)
   */
  void removeRuntime(int runtimeId);

  /**
   * Adds a runtime to the list of runtimes maintained in the service (STP/1 only)
   */
  void addRuntime(EsdbgProtos.RuntimeInfo info);

  /**
   * Executes the given javascript via eval call
   *
   * @param script the script to be executed on host
   * @param params array of params, can be string, long or web element(s)
   */
  Object scriptExecutor(String script, Object... params);

  /**
   * Execute a script and wait for response
   *
   * @param using script to execute
   */
  String executeJavascript(String using);

  /**
   * Execute a script, this method allows fine tuning of responses, we are not interested in return
   * value in some injections
   *
   * @param using            script to execute
   * @param responseExpected if true parse the response
   * @return string representation of the response
   */
  String executeJavascript(String using, boolean responseExpected);

  /**
   * Executes a script and returns the response (based on responseExpected)
   *
   * @param using            the script to be injected
   * @param responseExpected the flag to enable/disable parsing response
   * @return the raw result object if response is expected, <code>null</code> otherwise
   */
  Object executeScript(String using, boolean responseExpected);

  /**
   * Gets the object id using a script
   *
   * @param using the script to find the object
   * @return object id with the result
   */
  Integer getObject(String using);

  /**
   * The script to be executed on/using an object
   *
   * @param using    script with reference to the object "locator"
   * @param objectId the object to be used for injection
   * @return the string value returned by the executed Javascript
   */
  String callFunctionOnObject(String using, int objectId);

  Object callFunctionOnObject(String using, int objectId, boolean responseExpected);

  Integer executeScriptOnObject(String using, int objectId);

  /**
   * Changes the frame by index
   */
  void changeRuntime(int index);

  /**
   * Changes the frame by frame name address nested frames by dot (first.second)
   */
  void changeRuntime(String frameName);

  /**
   * Cleanup possible dead/suspended runtimes on runtime-started or window-closed
   */
  void cleanUpRuntimes(int windowId);

  /**
   * Cleanup runtimes with the active window id
   */
  void cleanUpRuntimes();

  /**
   * Request additional information about an object, such as its prototype or its properties.
   *
   * @param id object ID previously returned from an eval call to the debugger
   * @return list of ID's of object's which are considered web elements
   */
  // TODO(andreastt): This method does not do what it's meant to do, see Scope doc
  List<Integer> examineObjects(Integer id);

  /**
   * Lists the frame paths of available runtimes
   *
   * @return a {@link List} of {@link String} framepaths
   */
  List<String> listFramePaths();

  /**
   * Makes objects available to gc, this doesn't free the objects immediately
   */
  void releaseObjects();

  /**
   * Finds a valid runtime and updates the runtime reference (active runtime)
   *
   * @return true if a runtime is found and updated
   */
  boolean updateRuntime();

  /**
   * Resets the list of runtimes and fetches a fresh list from Opera, this can be used to recover in
   * cases where runtimes go out of sync
   */
  void resetRuntimesList();

  /**
   * Triggered on the ecmascript service when the state of a runtime changes
   *
   * @param change {@link com.opera.core.systems.scope.protos.EcmascriptProtos.ReadyStateChange}
   *               object with runtime info
   */
  void readyStateChanged(EcmascriptProtos.ReadyStateChange change);

  /**
   * Releases a specific object
   *
   * @param objectId Id of the object to be released
   */
  void releaseObject(int objectId);

  void resetFramePath();

  String executeJavascript(String using, Integer windowId);

  Object examineScriptResult(Integer id);

  /**
   * Set value of a form element.
   *
   * @param objectId the ID of the object to set value
   * @param value    the value to set
   */
  void setFormElementValue(int objectId, String value);

  /**
   * Queries the current runtime to see if it's script injectable.
   *
   * @return true if runtime is script injectable, false otherwise
   */
  boolean isScriptInjectable();

}