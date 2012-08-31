/*
Copyright 2008-2012 Opera Software ASA

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

package com.opera.core.systems.scope.services.stp1;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.opera.core.systems.OperaWebElement;
import com.opera.core.systems.model.RuntimeNode;
import com.opera.core.systems.model.ScriptResult;
import com.opera.core.systems.scope.AbstractEcmascriptService;
import com.opera.core.systems.scope.ScopeServices;
import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.scope.protos.EcmascriptProtos;
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
import com.opera.core.systems.scope.protos.EcmascriptProtos.SetFormElementValueArg;
import com.opera.core.systems.scope.protos.EcmascriptProtos.Value;
import com.opera.core.systems.scope.protos.EcmascriptProtos.Value.Type;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.EcmascriptDebugger;
import com.opera.core.systems.scope.services.messages.EcmascriptMessage;

import org.apache.commons.jxpath.Pointer;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicStampedReference;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.opera.core.systems.scope.internal.OperaDefaults.SCRIPT_RETRIES;
import static com.opera.core.systems.scope.internal.OperaIntervals.SCRIPT_RETRY_INTERVAL;
import static com.opera.core.systems.scope.internal.OperaIntervals.SCRIPT_TIMEOUT;

// Highlight when we're using EsdbgProtos, instead of importing them directly.  We can't import
// this, because of the java "Object" class.  Leaving this commented out to help in the future!
//import com.opera.core.systems.scope.protos.EcmascriptProtos.Object;

/**
 * ScopeEcmascriptService is a lightweight service to enable JavaScript injection.  Unlike {@link
 * ScopeEcmascriptDebugger} it does not disable JIT.
 */
public class ScopeEcmascriptService extends AbstractEcmascriptService implements
                                                                      EcmascriptDebugger {

  private final AtomicStampedReference<Runtime> runtime =
      new AtomicStampedReference<Runtime>(null, 0);
  private final ConcurrentMap<Integer, Runtime> runtimesList =
      new ConcurrentHashMap<Integer, Runtime>();
  private final Queue<Integer> runtimesQueue = Lists.newLinkedList();
  private final Queue<Integer> garbageQueue = Lists.newLinkedList();

  public ScopeEcmascriptService(ScopeServices services, String version) {
    super(services, "ecmascript", version);
  }

  public int getRuntimeId() {
    return runtime.getStamp();
  }

  public void setRuntime(Runtime runtime) {
    if (runtime != null) {
      this.runtime.set(runtime, runtime.getRuntimeID());
      activeWindowId = runtime.getWindowID();
    }
  }

  public void setRuntime(RuntimeInfo runtime) {
    throw new UnsupportedOperationException("Not supported without ecmascript-debugger");
  }

  public void addRuntime(RuntimeInfo info) {
    Runtime.Builder runtime = Runtime.newBuilder();

    runtime.setRuntimeID(info.getRuntimeID());
    runtime.setHtmlFramePath(info.getHtmlFramePath());
    runtime.setWindowID(info.getWindowID());
    runtime.setObjectID(info.getObjectID());
    runtime.setUri(info.getUri());

    runtimesList.put(info.getRuntimeID(), runtime.build());
  }

  public void removeRuntime(int runtimeId) {
    runtimesList.remove(runtimeId);
  }

  private List<Runtime> getRuntimesList() {
    int windowId = services.getWindowManager().getActiveWindowId();
    Iterator<?> iterator = xpathIterator(runtimesList.values(), "/.[windowID='" + windowId + "']");
    List<Runtime> runtimes = Lists.newArrayList();
    while (iterator.hasNext()) {
      runtimes.add((Runtime) ((Pointer) iterator.next()).getNode());
    }
    return runtimes;
  }

  public void init() {
    updateRuntime();
  }

  public boolean updateRuntime() {
    Runtime activeRuntime = findRuntime();

    if (activeRuntime != null) {
      setRuntime(activeRuntime);
      return true;
    }

    return false;
  }

  /**
   * Gets a list of runtimes and keeps the list, create runtimes for all pages so even if the pages
   * don't have script we can still inject to a 'fake' runtime.
   */
  protected void createAllRuntimes() {
    ListRuntimesArg.Builder selection = ListRuntimesArg.newBuilder();
    selection.setCreate(true);
    Response response = executeMessage(EcmascriptMessage.LIST_RUNTIMES, selection);

    runtimesList.clear();
    RuntimeList.Builder builder = RuntimeList.newBuilder();
    buildPayload(response, builder);
    List<Runtime> allRuntimes = builder.build().getRuntimeListList();

    for (Runtime info : allRuntimes) {
      runtimesList.put(info.getRuntimeID(), info);
    }
  }

  public Object scriptExecutor(String script, Object... params) {
    processQueues();

    List<WebElement> elements = Lists.newArrayList();

    String toSend = buildEvalString(elements, script, params);
    EvalArg.Builder evalBuilder = buildEval(toSend, getRuntimeId());

    for (WebElement webElement : elements) {
      Variable variable =
          buildVariable(webElement.toString(), ((OperaWebElement) webElement).getObjectId());
      evalBuilder.addVariableList(variable);
    }

    Response response = executeMessage(EcmascriptMessage.EVAL, evalBuilder);

    if (response == null) {
      throw new WebDriverException("Internal error while executing script");
    }

    EvalResult result = parseEvalData(response);

    Object parsed = parseEvalReply(result);
    if (parsed instanceof EcmascriptProtos.Object) {
      EcmascriptProtos.Object data = (EcmascriptProtos.Object) parsed;
      return new ScriptResult(data.getObjectID(), data.getClassName());
    } else {
      return parsed;
    }
  }

  private EvalResult parseEvalData(Response response) {
    EvalResult.Builder builder = EvalResult.newBuilder();
    buildPayload(response, builder);
    return builder.build();
  }

  private EvalArg.Builder buildEval(String toSend, int runtimeId) {
    EvalArg.Builder builder = EvalArg.newBuilder();
    builder.setRuntimeID(runtimeId);
    builder.setScriptData(toSend);
    return builder;
  }

  private Variable buildVariable(String name, int objectId) {
    Variable.Builder variable = Variable.newBuilder();
    variable.setName(name);
    variable.setObjectID(objectId);
    return variable.build();
  }

  /**
   * Evaluates ECMAScript in the global scope.  This method maps to {@link EcmascriptMessage#EVAL}
   * in Scope. For instance to fetch information on the window object, one can do:
   *
   * <pre><code>
   *   Object window = parseEvalReply(parseEvalData(eval("window", 1)));
   * </code></pre>
   *
   * {@link EvalResult#getValue#getObject} will be set, note however that this will not include
   * properties on the return object to conserve the amount of data that needs to be sent.  To get
   * properties use {@link #examineObjects(Integer)}.
   *
   * @param using     piece of ECMAScript to evaluate
   * @param variables a list of objects to place in the execution environment when evaluating
   *                  <code>using</code>
   * @return result of script execution
   * @see EcmascriptMessage#EVAL
   */
  private EvalResult eval(String using, Variable... variables) {
    return eval(using, getRuntimeId(), variables);
  }

  /**
   * Evaluates ECMAScript in the global scope.  This method maps to {@link EcmascriptMessage#EVAL}
   * in Scope. For instance to fetch information on the window object, one can do:
   *
   * <pre><code>
   *   Object window = parseEvalReply(parseEvalData(eval("window", 1)));
   * </code></pre>
   *
   * {@link EvalResult#getValue#getObject} will be set, note however that this will not include
   * properties on the return object to conserve the amount of data that needs to be sent.  To get
   * properties use {@link #examineObjects(Integer)}.
   *
   * @param using     piece of ECMAScript to evaluate
   * @param runtimeId ID of the runtime to execute script in
   * @param variables a list of objects to place in the execution environment when evaluating
   *                  <code>using</code>
   * @return result of script execution
   * @throws CommunicationException if browser does not respond
   * @see EcmascriptMessage#EVAL
   */
  private EvalResult eval(String using, int runtimeId, Variable... variables) {
    // This call causes us to release objects, which allows them to be garbage collected, and
    // sometimes causes this method to fail.  So I've commented it out.  But I reckon it might cause
    // high memory usage in Opera, so the method might need to be updated in the future.
    //processQueues();

    EvalArg.Builder builder = buildEval(using, runtimeId);
    builder.addAllVariableList(Arrays.asList(variables));

    Response response = executeMessage(EcmascriptMessage.EVAL, builder, SCRIPT_TIMEOUT.getMs());

    if (response == null && retries < SCRIPT_RETRIES) {
      retries++;
      sleepDuration += SCRIPT_RETRY_INTERVAL.getMs();
      sleep(sleepDuration);
      recover();
      return eval(using, variables);
    } else if (retries >= SCRIPT_RETRIES) {
      resetCounters();
      throw new CommunicationException("No response on ECMAScript evaluation command");
    }

    resetCounters();

    return parseEvalData(response);
  }

  public Object executeScript(String using, boolean responseExpected) {
    return executeScript(using, responseExpected, getRuntimeId());
  }

  private Object executeScript(String using, boolean responseExpected, int runtimeId) {
    EvalResult reply = eval(using, runtimeId);
    return responseExpected ? parseEvalReply(reply) : null;
  }

  public Integer getObject(String using) {
    EvalResult reply = eval(using);
    return (reply.getValue().getType().equals(Type.OBJECT)) ? reply.getValue().getObject()
        .getObjectID() : null;
  }

  public String callFunctionOnObject(String using, int objectId) {
    Object result = callFunctionOnObject(using, objectId, true);
    return (result == null) ? null : String.valueOf(result);
  }

  public Object callFunctionOnObject(String using, int objectId, boolean responseExpected) {
    Variable variable = buildVariable("locator", objectId);

    EvalResult reply = eval(using, variable);
    return responseExpected ? parseEvalReply(reply) : null;
  }

  public Integer executeScriptOnObject(String using, int objectId) {
    Variable variable = buildVariable("locator", objectId);
    EvalResult reply = eval(using, variable);
    Object object = parseEvalReply(reply);

    if (object == null || !(object instanceof EcmascriptProtos.Object)) {
      return null;
    }
    return ((EcmascriptProtos.Object) object).getObjectID();
  }

  public void setFormElementValue(int objectId, String value) {
    SetFormElementValueArg.Builder args = SetFormElementValueArg.newBuilder();
    args.setObjectID(objectId);
    args.setValue(value);
    executeMessage(EcmascriptMessage.SET_FORM_ELEMENT_VALUE, args);
  }

  /**
   * Parses a reply and returns the result of the script execution.  The result from EcmaScript is
   * converted based on the object types specified by Scope:
   *
   * <dl> <dt>undefined</dt> <dd> The undefined type, returned when no value was found or
   * "undefined" was references.  Will return null. </dd>
   *
   * <dt>true</dt> <dd>Boolean true value.</dd>
   *
   * <dt>false</dt> <dd>Boolean false value.</dd>
   *
   * <dt>NaN</dt> <dd> NaN value (not a number) which cannot be exported to JSON directly.  Will be
   * treated as a number, and returns a {@link Float#NaN} reference. </dd>
   *
   * <dt>Infinity</dt> <dd> Plus infinity value which cannot be exported to JSON directly.  Will be
   * treated as a number, and returns a {@link Float#POSITIVE_INFINITY} reference. </dd>
   *
   * <dt>-Infinity</dt> <dd> Negative infinity value which cannot be exported to JSON directly. Will
   * be treated as a number, and returns a {@link Float#NEGATIVE_INFINITY} reference. </dd>
   *
   * <dt>number</dt> <dd>A number, will return a long or double value depending on its value.</dd>
   *
   * <dt>string</dt> <dd>A string.</dd>
   *
   * <dt>object</dt> <dd> A non-primitive value in EcmaScript, typically a generic object.  This
   * includes functions and arrays. </dd> </dl>
   *
   * @param result the result of a script execution
   * @return the parsed result of a reply
   */
  private Object parseEvalReply(EvalResult result) {
    Status status = result.getStatus();

    switch (status) {
      case CANCELLED:
        return null;
      case EXCEPTION:
        throw new WebDriverException("EcmaScript exception");
      case NO_MEMORY:
        //releaseObjects();
        throw new WebDriverException("Out of memory");
      case FAILURE:
        throw new WebDriverException("Could not execute script");
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

  /**
   * Find the runtime for injection (default). Typically this is _top runtime with the active window
   * that has focus.
   */
  protected Runtime findRuntime() {
    return findRuntime(windowManager.getActiveWindowId());
  }

  protected Runtime findRuntime(int windowId) {
    createAllRuntimes();
    Pointer pointer =
        xpathPointer(runtimesList.values(),
                     String.format("/.[htmlFramePath='%s' and windowID='%d']", currentFramePath,
                                   windowId));
    if (pointer != null) {
      return (Runtime) pointer.getValue();
    }
    return null;
  }

  /**
   * Updates the runtimes list to most recent version
   *
   * TODO this has to be kept up to date with events and as failover we should update It builds a
   * tree to find the frame we are looking for TODO tree also must be kept up to date
   */
  private void buildRuntimeTree() {
    updateRuntime();
    Runtime rootInfo = findRuntime();
    root = new RuntimeNode();
    root.setFrameName("_top");
    root.setRuntimeID(rootInfo.getRuntimeID());

    List<Runtime> runtimeInfos = Lists.newArrayList(runtimesList.values());
    runtimeInfos.remove(rootInfo);

    for (Runtime runtimeInfo : runtimeInfos) {
      addNode(runtimeInfo, root);
    }
  }

  public void changeRuntime(int index) {
    buildRuntimeTree();

    RuntimeNode node = root.getNodes().get(index + 1);

    if (node == null) {
      throw new NoSuchFrameException("Invalid frame index " + index);
    }

    Runtime info = runtimesList.get(node.getRuntimeID());
    currentFramePath = info.getHtmlFramePath();
    setRuntime(info);
  }

  public void changeRuntime(String frameName) {
    checkNotNull(frameName);
    buildRuntimeTree();

    String[] values = frameName.split("\\.");
    RuntimeNode curr = root;

    for (String value : values) {
      curr = findNodeByName(value, curr);
      if (curr == null) {
        break;
      }
    }

    if (curr == null) {
      throw new NoSuchFrameException("Invalid frame name " + frameName);
    }

    Runtime info = runtimesList.get(curr.getRuntimeID());

    // We should only find frames underneath the current one.
    if (!info.getHtmlFramePath().startsWith(currentFramePath)) {
      throw new NoSuchFrameException("No such frame " + frameName + " in " + currentFramePath);
    }

    currentFramePath = info.getHtmlFramePath();
    setRuntime(info);
  }

  private RuntimeNode findNodeByName(String name, RuntimeNode rootNode) {
    for (Entry<Integer, RuntimeNode> entry : rootNode.getNodes().entrySet()) {
      // check if the name is a number
      if (isNumber(name) && entry.getKey().equals(Integer.valueOf(name) + 1)) {
        return entry.getValue();
      }
      // check if it is really the name
      else if (entry.getValue().getFrameName().equals(name)) {
        return entry.getValue();
      }
      // last resort is id
      else {
        try {
          if (executeScript("frameElement ? frameElement.id : ''", true,
                            entry.getValue().getRuntimeID()).equals(name)) {
            return entry.getValue();
          }
        } catch (WebDriverException e) {
          // ignore exception
        }
      }
    }
    return null;
  }

  private void addNode(Runtime info, RuntimeNode root) {
    String[] values = info.getHtmlFramePath().split("/");
    RuntimeNode curr = root;

    // first frame is always _top, so we skip it
    for (int i = 1; i < values.length; ++i) {
      int index = framePathToIndex(values[i]);
      if (curr.getNodes().get(index) == null) {
        // add to this node
        RuntimeNode node = new RuntimeNode();
        int end = values[i].indexOf('[');
        node.setFrameName(values[i].substring(0, end));
        curr.getNodes().put(index, node);
        curr = node;
      } else {
        curr = curr.getNodes().get(index);
      }
      // else we already know about this node, skip it
    }
    // last node gets the runtime id
    curr.setRuntimeID(info.getRuntimeID());
  }

  private int framePathToIndex(String framePath) {
    int begin = framePath.indexOf('[');
    int end = framePath.indexOf(']');
    return Integer.valueOf(framePath.substring(begin + 1, end));
  }

  public void cleanUpRuntimes(int windowId) {
    // if we already have a runtime listed as _top with that window id, clean all runtimes with that
    // window id
    for (Runtime runtime : runtimesList.values()) {
      if (runtime.getWindowID() == windowId) {
        runtimesList.remove(runtime.getRuntimeID());
      }
    }
  }

  public List<Integer> examineObjects(Integer id) {
    List<Integer> ids = Lists.newArrayList();

    ObjectList list = getObjectList(id);
    List<Property> objects =
        list.getPrototypeListList().get(0).getObjectListList().get(0).getPropertyListList();
    for (Property obj : objects) {
      if (obj.getValue().getType().equals(Value.Type.OBJECT)) {
        ids.add(obj.getValue().getObject().getObjectID());
      }
    }

    return ids;
  }

  public List<String> listFramePaths() {
    List<Runtime> runtimes = getRuntimesList();
    List<String> frameNames = Lists.newArrayList();
    for (Runtime runtime : runtimes) {
      frameNames.add(runtime.getHtmlFramePath());
    }
    return frameNames;
  }

  public void releaseObjects() {
    ReleaseObjectsArg.Builder builder = ReleaseObjectsArg.newBuilder();
    executeMessage(EcmascriptMessage.RELEASE_OBJECTS, builder);
  }

  public void resetRuntimesList() {
    runtimesList.clear();
  }

  public void readyStateChanged(ReadyStateChange change) {
    // We should probably wait for AFTER_LOAD but it is reported to be broken, so we will just wait
    // until DOM_CONTENT_LOADED so we can interact with the page as soon as possible.
    if (!runtimesList.containsKey(change.getRuntimeID())) {
      // A new runtime that we don't know about has been loaded
      runtimesQueue.add(change.getRuntimeID());
    }
  }

  public void releaseObject(int objectId) {
    garbageQueue.add(objectId);
  }

  public void resetFramePath() {
    currentFramePath = "_top";
    setRuntime(findRuntime());
  }

  public String executeJavascript(String using, Integer windowId) {
    // TODO: Workaround for frame issues when executing in a specific window
    String tmp = currentFramePath;
    currentFramePath = "_top";
    Runtime runtime = findRuntime(windowId);
    currentFramePath = tmp;

    // speed dial doesn't have a runtime
    if (runtime == null) {
      return "";
    }
    return (String) executeScript(using, true, runtime.getRuntimeID());
  }

  public Object examineScriptResult(Integer id) {
    return examineScriptResult(id, new HashSet<Integer>());
  }

  private Object examineScriptResult(Integer id, Set<Integer> visitedIDs) {
    if (visitedIDs.contains(id)) {
      // cyclic reference - returning null for the inner most reference
      return null;
    }
    visitedIDs.add(id);
    ObjectList list = getObjectList(id);
    EcmascriptProtos.Object obj = list.getPrototypeList(0).getObjectList(0);
    String className = obj.getClassName();

    List<Property> properties = obj.getPropertyListList();

    if (className.endsWith("Element")) {
      return new OperaWebElement(driver, id);
    } else if (className.equals("Array")) {
      List<Object> result = Lists.newArrayList();

      for (Property property : properties) {
        Type type = property.getValue().getType();
        if (type == Type.NUMBER && property.getName().equals("length")) {
          // ignore ?!?
        } else {
          result.add(parseValue(type, property.getValue(), visitedIDs));
        }
      }
      return result;
    } else {
      // we have a map
      Map<String, Object> result = Maps.newHashMap();

      for (Property property : properties) {
        Type type = property.getValue().getType();
        if (type == Type.NUMBER && property.getName().equals("length")) {
          // ignore ?!?
        } else {
          result.put(property.getName(), parseValue(type, property.getValue(), visitedIDs));
        }
      }
      return result;
    }
  }

  private Object parseValue(Type type, Value value, Set<Integer> visitedIDs) {
    switch (type) {
      case TRUE:
        return true;
      case FALSE:
        return false;
      case PLUS_INFINITY:
        return Double.POSITIVE_INFINITY;
      case MINUS_INFINITY:
        return Double.NEGATIVE_INFINITY;
      case NUMBER:
        return value.getNumber();
      case STRING:
        return value.getStr();
      case OBJECT:
        return examineScriptResult(value.getObject().getObjectID(), visitedIDs);
      case UNDEFINED:
      case NULL:
      case NAN:
      default:
        return null;
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
    builder.addAllObjectIDList(garbageQueue);
    executeMessage(EcmascriptMessage.RELEASE_OBJECTS, builder);
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
    builder.addRuntimeIDList(runtimeID);
    builder.setCreate(true);

    Response response = executeMessage(EcmascriptMessage.LIST_RUNTIMES, builder);
    RuntimeList.Builder runtimeListBuilder = RuntimeList.newBuilder();
    buildPayload(response, runtimeListBuilder);
    List<Runtime> runtimes = runtimeListBuilder.build().getRuntimeListList();
    return (runtimes.isEmpty()) ? null : runtimes.get(0);
  }

  private ObjectList getObjectList(Integer id) {
    ExamineObjectsArg.Builder builder = ExamineObjectsArg.newBuilder();
    builder.setExaminePrototypes(false);
    builder.setRuntimeID(getRuntimeId());
    builder.addObjectIDList(id);
    Response response = executeMessage(EcmascriptMessage.EXAMINE_OBJECTS, builder);

    ObjectList.Builder objListBuilder = ObjectList.newBuilder();
    buildPayload(response, objListBuilder);
    return objListBuilder.build();
  }

}