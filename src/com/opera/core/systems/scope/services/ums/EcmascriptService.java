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

package com.opera.core.systems.scope.services.ums;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicStampedReference;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.OperaWebElement;
import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.model.ScriptResult;
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.ESCommand;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.protos.EcmascriptProtos.EvalArg;
import com.opera.core.systems.scope.protos.EcmascriptProtos.EvalArg.Variable;
import com.opera.core.systems.scope.protos.EcmascriptProtos.EvalResult;
import com.opera.core.systems.scope.protos.EcmascriptProtos.EvalResult.Status;
import com.opera.core.systems.scope.protos.EcmascriptProtos.ExamineObjectsArg;
import com.opera.core.systems.scope.protos.EcmascriptProtos.ListRuntimesArg;
import com.opera.core.systems.scope.protos.EcmascriptProtos.Object.Property;
import com.opera.core.systems.scope.protos.EcmascriptProtos.ObjectList;
import com.opera.core.systems.scope.protos.EcmascriptProtos.ReadyStateChange;
import com.opera.core.systems.scope.protos.EcmascriptProtos.ReleaseObjectsArg;
import com.opera.core.systems.scope.protos.EcmascriptProtos.Runtime;
import com.opera.core.systems.scope.protos.EcmascriptProtos.RuntimeList;
import com.opera.core.systems.scope.protos.EcmascriptProtos.Value;
import com.opera.core.systems.scope.protos.EcmascriptProtos.Value.Type;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IWindowManager;

/**
 * Ecmascript service is a lightweight service to enable javascript injection (incomplete)
 *
 * @author Deniz Turkoglu <denizt@opera.com>
 */
// TODO extend ecmascript debugger? the signatures do vary a lot, might
// be a better idea to keep this interface seperate, maybe create an abstract
// ecmascript service?
public class EcmascriptService extends AbstractService implements
                                                       IEcmaScriptDebugger {

  /*
     * List all runtimes (in all Windows).
     * For instance to fetch all runtimes of all windows one can do::
     *
     *   ListRuntimes(runtimeList=[], create=true)
     *
     * If you already have a runtime ID you can pass it in runtimeList
     * to get get back information, e.g.::
     *
     *   ListRuntimes(runtimeList=[1])
     */
  // command ListRuntimes(ListRuntimesArg) returns (RuntimeList) = 1;

  /**
   * Evaluate a piece of ECMAScript in the global scope. For instance to fetch
   * information on the window object, one can do::
   *
   * Eval(runtimeID=1, scriptData="window")
   *
   * EvalResult.value.object will be set, note however that this will not
   * include properties on the return object to conserve the amount of data that
   * needs to be sent. To get properties use ExamineObjects().
   */
  // command Eval(EvalArg) returns (EvalResult) = 2;

  /**
   * Request additional information about an object, such as its prototype or
   * its properties.
   *
   * For instance to get more information on the window object, one can do::
   *
   * ExamineObjects(runtimeID=1, objectList=[2], examinePrototypes=true)
   *
   * Here the object ID was the ID previously returned from an Eval() call.
   */
  // command ExamineObjects(ExamineObjectsArg) returns (ObjectList) = 3;

  /**
   * Release protected ECMAScript objects. This will just make them garbage collectible. Released
   * objects are not necessarily freed immediately.
   *
   * Calling ReleaseObjects with an empty list causes all objects to be released. Otherwise, only
   * the specified objects will be released. Attempting to release a non-existent object has no
   * effect, and will fail silently.
   *
   * Releasing objects invalidates associated object IDs immediately.
   */
  // command ReleaseObjects(ReleaseObjectsArg) returns (Default) = 4;

  private AtomicStampedReference<Runtime> runtime = new AtomicStampedReference<Runtime>(
      null, 0);
  private IWindowManager windowManager;
  private int retries;
  private long sleepDuration;
  protected int activeWindowId;
  private Queue<Integer> runtimesQueue = new LinkedList<Integer>();
  private Queue<Integer> garbageQueue = new LinkedList<Integer>();

  private ConcurrentMap<Integer, Runtime> runtimesList = new ConcurrentHashMap<Integer, Runtime>();
  protected OperaDriver driver;

  public EcmascriptService(ScopeServices services, String version) {
    super(services, version);
    services.setDebugger(this);
    this.windowManager = services.getWindowManager();
    this.services = services;
    resetCounters();
  }

  public void setDriver(OperaDriver driver) {
    this.driver = driver;
  }

  private void resetCounters() {
    retries = 0;
    sleepDuration = OperaIntervals.SCRIPT_RETRY_INTERVAL.getValue();
  }

  public void addRuntime(
      com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo info) {
    throw new UnsupportedOperationException(
        "Not suppported without ecmascript-debugger");

  }

  private Response eval(String using, Variable... variables) {

    if (windowManager.getActiveWindowId() != activeWindowId) {
      recover();
    }

    processQueues();

    EvalArg.Builder builder = buildEval(using);
    builder.addAllVariableList(Arrays.asList(variables));

    Response response = executeCommand(ESCommand.EVAL, builder);

    if (response == null && retries < OperaIntervals.SCRIPT_RETRY.getValue()) {
      retries++;
      sleepDuration += sleepDuration;
      sleep(sleepDuration);
      recover();
      return eval(using, variables);
    } else if (retries >= OperaIntervals.SCRIPT_RETRY.getValue()) {
      resetCounters();
      throw new WebDriverException("Internal error");
    }

    resetCounters();

    return response;
  }

  private void recover() {
    windowManager.findDriverWindow();
    activeWindowId = windowManager.getActiveWindowId();
    updateRuntime();
  }

  public String callFunctionOnObject(String using, int objectId) {
    Object result = callFunctionOnObject(using, objectId, true);
    return (result == null) ? null : String.valueOf(result);
  }

  public Object callFunctionOnObject(String using, int objectId,
                                     boolean responseExpected) {
    Variable variable = buildVariable("locator", objectId);

    Response response = eval(using, variable);
    return responseExpected ? parseEvalReply(parseEvalData(response)) : null;
  }

  private Object parseEvalReply(EvalResult result) {
    Status status = result.getStatus();
    switch (status) {
      case CANCELLED:
        return null;
      case EXCEPTION:
        throw new WebDriverException("Ecmascript exception");
      case NO_MEMORY:
        // releaseObjects();
        throw new WebDriverException("Out of memory");
      case FAILURE:
        throw new WebDriverException("Could not execute script");
      default:
        break;
    }

    Value value = result.getValue();
    Type type = value.getType();
    switch (type) {
      case STRING:
        return value.getStr();
      case FALSE:
        return false;
      case TRUE:
        return true;
      case OBJECT:
        return value.getObject();
      case NUMBER:
        return parseNumber(String.valueOf(value.getNumber()));
      case NAN:
        return Float.NaN;
      case MINUS_INFINITY:
        return Float.NEGATIVE_INFINITY;
      case PLUS_INFINITY:
        return Float.POSITIVE_INFINITY;
      case UNDEFINED:
      case NULL:
      default:
        return null;
    }
  }

  protected Object parseNumber(String value) {
    Number number;
    try {
      number = NumberFormat.getInstance().parse(value);
      if (number instanceof Long) {
        return number.longValue();
      } else {
        return number.doubleValue();
      }
    } catch (ParseException e) {
      throw new WebDriverException(
          "The result from the script can not be parsed");
    }
  }

  protected final Variable buildVariable(String name, int objectId) {
    Variable.Builder variable = Variable.newBuilder();
    variable.setName(name);
    variable.setObjectID(objectId);
    return variable.build();
  }

  private EvalResult parseEvalData(Response response) {
    EvalResult.Builder builder = EvalResult.newBuilder();
    buildPayload(response, builder);
    return builder.build();
  }

  public void cleanUpRuntimes(int windowId) {
    for (Runtime runtime : runtimesList.values()) {
      if (runtime.getWindowID() == windowId) {
        runtimesList.remove(runtime.getRuntimeID());
      }
    }
  }

  public void cleanUpRuntimes() {
    int windowId = windowManager.getActiveWindowId();
    cleanUpRuntimes(windowId);
  }

  public List<Integer> examineObjects(Integer objectId) {
    ExamineObjectsArg.Builder builder = ExamineObjectsArg.newBuilder();
    builder.setExaminePrototypes(false);
    builder.setRuntimeID(getRuntimeId());
    builder.addObjectList(objectId);
    Response response = executeCommand(ESCommand.EXAMINE_OBJECTS, builder);

    ObjectList.Builder objListBuilder = ObjectList.newBuilder();
    buildPayload(response, objListBuilder);
    ObjectList list = objListBuilder.build();

    List<Integer> ids = new ArrayList<Integer>();
    List<Property> objects = list.getPrototypeListList().get(0).getObjectListList().get(
        0).getPropertyListList();

    for (Property obj : objects) {
      if (obj.getValue().getType().equals(Value.Type.OBJECT)) {
        ids.add(obj.getValue().getObject().getObjectID());
      }
    }

    return ids;
  }

  public String executeJavascript(String using) {
    return executeJavascript(using, true);
  }

  public String executeJavascript(String using, boolean responseExpected) {
    Object result = executeScript(using, responseExpected);
    return (result == null) ? null : String.valueOf(result);
  }

  public Object executeScript(String using, boolean responseExpected) {
    Response reply = eval(using);
    return responseExpected ? parseEvalReply(parseEvalData(reply)) : null;
  }

  public Integer executeScriptOnObject(String using, int objectId) {
    Variable variable = buildVariable("locator", objectId);

    EvalResult reply = parseEvalData(eval(using, variable));
    Object object = parseEvalReply(reply);
    if (object == null
        || !(object instanceof com.opera.core.systems.scope.protos.EcmascriptProtos.Object)) {
      return null;
    }
    return ((com.opera.core.systems.scope.protos.EcmascriptProtos.Object) object).getObjectID();
  }

  public Integer getObject(String using) {
    EvalResult reply = parseEvalData(eval(using));
    return (reply.getValue().getType().equals(Type.OBJECT)) ? reply.getValue().getObject()
        .getObjectID()
                                                            : null;
  }

  public int getRuntimeId() {
    return runtime.getStamp();
  }

  public void init() {
    // we no longer need the configuration
    if (!updateRuntime()) {
      throw new WebDriverException(
          "Could not find a runtime for script injection");
    }
    // nor the dialog hack
  }

  public List<String> listFramePaths() {
    List<String> framePaths = new LinkedList<String>();
    Iterator<Entry<Integer, Runtime>> itr = runtimesList.entrySet().iterator();
    while (itr.hasNext()) {
      framePaths.add(itr.next().getValue().getHtmlFramePath());
    }
    return framePaths;
  }

  public void releaseObjects() {
    releaseObject(0);
  }

  public void removeRuntime(int runtimeId) {
    runtimesList.remove(runtimeId);
  }

  public void resetRuntimesList() {
    runtimesList.clear();
  }

  // TODO merge with eval
  public Object scriptExecutor(String script, Object... params) {
    if (windowManager.getActiveWindowId() != activeWindowId) {
      recover();
    }

    processQueues();

    List<WebElement> elements = new ArrayList<WebElement>();

    String toSend = buildEvalString(elements, script, params);
    EvalArg.Builder evalBuilder = buildEval(toSend);

    for (WebElement webElement : elements) {
      Variable variable = buildVariable(webElement.toString(),
                                        ((OperaWebElement) webElement).getObjectId());
      evalBuilder.addVariableList(variable);
    }

    Response response = executeCommand(ESCommand.EVAL, evalBuilder);

    if (response == null) {
      throw new WebDriverException(
          "Internal error while executing script");
    }

    EvalResult result = parseEvalData(response);

    Object parsed = parseEvalReply(result);
    if (parsed instanceof com.opera.core.systems.scope.protos.EcmascriptProtos.Object) {
      com.opera.core.systems.scope.protos.EcmascriptProtos.Object
          data =
          (com.opera.core.systems.scope.protos.EcmascriptProtos.Object) parsed;
      return new ScriptResult(data.getObjectID(), data.getClassName());
    } else {
      return parsed;
    }
  }

  private EvalArg.Builder buildEval(String toSend) {
    EvalArg.Builder builder = EvalArg.newBuilder();
    builder.setRuntimeID(getRuntimeId());
    builder.setScriptData(toSend);
    return builder;
  }

  /**
   * Build the script to send with arguments
   *
   * @param elements The web elements to send with the script as argument
   * @param script   The script to execute, can have references to argument(s)
   * @param params   Params to send with the script, will be parsed in to arguments
   * @return The script to be sent to Eval command for execution
   */
  private String buildEvalString(List<WebElement> elements, String script,
                                 Object... params) {
    String toSend;
    if (params != null && params.length > 0) {
      StringBuilder builder = new StringBuilder();
      for (Object object : params) {
        if (builder.toString().length() > 0) {
          builder.append(",");
        }

        if (object instanceof Collection<?>) {
          builder.append("[");
          Collection<?> collection = (Collection<?>) object;
          for (Object argument : collection) {
            processArgument(argument, builder, elements);
          }
          builder.append("]");
        } else {
          processArgument(object, builder, elements);
        }
      }

      String arguments = builder.toString();
      toSend = "(function(){" + script + "})(" + arguments + ")";
    } else {
      toSend = script;
    }
    return toSend;
  }

  protected void processArgument(Object object, StringBuilder builder,
                                 List<WebElement> elements) {
    if (object instanceof WebElement) {
      elements.add((WebElement) object);
      builder.append(String.valueOf(object));
    } else if (object instanceof String) {
      builder.append("'" + String.valueOf(object) + "'");
    } else if (object instanceof Integer || object instanceof Long
               || object instanceof Boolean || object instanceof Float
               || object instanceof Double) {
      builder.append(String.valueOf(object));
    } else {
      throw new IllegalArgumentException("The argument type is not supported");
    }
  }

  public void setRuntime(
      com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo runtime) {
    throw new UnsupportedOperationException(
        "Not suppported without ecmascript-debugger");
  }

  public boolean updateRuntime() {
    createAllRuntimes();
    Runtime activeRuntime = findRuntime();
    if (activeRuntime != null) {
      setRuntime(activeRuntime);
      return true;
    }
    return false;
  }

  public void setRuntime(Runtime runtime) {
    this.runtime.set(runtime, runtime.getRuntimeID());
    activeWindowId = runtime.getWindowID();
  }

  protected Runtime findRuntime() {
    int windowId = windowManager.getActiveWindowId();
    Runtime runtime = (Runtime) xpathPointer(runtimesList.values(),
                                             "/.[htmlFramePath='_top' and windowID='" + windowId
                                             + "']").getValue();
    return runtime;
  }

  protected void createAllRuntimes() {
    ListRuntimesArg.Builder builder = ListRuntimesArg.newBuilder();
    builder.setCreate(true);
    Response response = executeCommand(ESCommand.LIST_RUNTIMES, builder);
    runtimesList.clear();
    RuntimeList.Builder runtimeListBuilder = RuntimeList.newBuilder();
    buildPayload(response, runtimeListBuilder);
    List<Runtime> runtimes = runtimeListBuilder.build().getRuntimeListList();
    for (Runtime runtime : runtimes) {
      runtimesList.put(runtime.getRuntimeID(), runtime);
    }
  }

  public void readyStateChanged(ReadyStateChange change) {
    // we should probably wait for AFTER_LOAD but it is reported to
    // be broken, so we will just wait until DOM_CONTENT_LOADED
    // so we can interact with the page as soon as possible
    if (!runtimesList.containsKey(change.getRuntimeID())) {
      // a new runtime that we dont know about has been loaded
      runtimesQueue.add(change.getRuntimeID());
    }
  }

  private void processQueues() {
    if (!garbageQueue.isEmpty()) {
      processGcObjects();
    }

    if (!runtimesQueue.isEmpty()) {
      processNewRuntimes();
    }

    if (runtimesList.isEmpty()) {
      updateRuntime();
    }
  }

  private void processNewRuntimes() {
    while (!runtimesQueue.isEmpty()) {
      Runtime runtime = getRuntime(runtimesQueue.poll());
      if (runtime != null) {
        if (runtime.getHtmlFramePath().equals("_top")
            && windowManager.getActiveWindowId() == runtime.getWindowID()) {
          setRuntime(runtime);
        }
        runtimesList.putIfAbsent(runtime.getRuntimeID(), runtime);
      }
    }
  }

  private void processGcObjects() {
    ReleaseObjectsArg.Builder builder = ReleaseObjectsArg.newBuilder();
    builder.addAllObjectList(garbageQueue);
    executeCommand(ESCommand.RELEASE_OBJECTS, builder);
    garbageQueue.clear();
  }

  /**
   * Queries for the given runtime ID
   *
   * @param runtimeID The runtime id to query for
   * @return {@link Runtime} object if found, <code>null</code> otherwise
   */
  private Runtime getRuntime(int runtimeID) {
    ListRuntimesArg.Builder builder = ListRuntimesArg.newBuilder();
    builder.addRuntimeList(runtimeID);
    builder.setCreate(true);

    Response response = executeCommand(ESCommand.LIST_RUNTIMES, builder);
    RuntimeList.Builder runtimeListBuilder = RuntimeList.newBuilder();
    buildPayload(response, runtimeListBuilder);
    List<Runtime> runtimes = runtimeListBuilder.build().getRuntimeListList();
    return (runtimes.isEmpty()) ? null : runtimes.get(0);
  }

  public void releaseObject(int objectId) {
    ReleaseObjectsArg.Builder builder = ReleaseObjectsArg.newBuilder();

    if (objectId != 0) {
      garbageQueue.add(objectId);
    } else {
      executeCommand(ESCommand.RELEASE_OBJECTS, builder);
    }
  }

  public void resetFramePath() {
    // FIXME implement

  }

  public void changeRuntime(int index) {
    // TODO Auto-generated method stub

  }

  public void changeRuntime(String frameName) {
    // TODO Auto-generated method stub

  }

  public String executeJavascript(String using, Integer windowId) {
    // TODO Auto-generated method stub
    return null;
  }

  public Object examineScriptResult(Integer id) {
    // TODO Auto-generated method stub
    return null;
  }

}
