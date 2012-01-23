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

package com.opera.core.systems.scope.services.ums;

import com.opera.core.systems.OperaWebElement;
import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.model.RuntimeNode;
import com.opera.core.systems.model.ScriptResult;
import com.opera.core.systems.scope.AbstractEcmascriptService;
import com.opera.core.systems.scope.ESCommand;
import com.opera.core.systems.scope.internal.OperaIntervals;
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
import com.opera.core.systems.scope.protos.EcmascriptProtos.Value;
import com.opera.core.systems.scope.protos.EcmascriptProtos.Value.Type;
import com.opera.core.systems.scope.protos.EsdbgProtos;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;

import org.apache.commons.jxpath.Pointer;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicStampedReference;

// Highlight when we're using EsdbgProtos, instead of importing them directly.  We can't import
// this, because of the java "Object" class.  Leaving this commented out to help in the future!
//import com.opera.core.systems.scope.protos.EcmascriptProtos.Object;

/**
 * EcmascriptService is a lightweight service to enable JavaScript injection.  Unlike
 * {@link EcmaScriptDebugger} it does not disable JIT.
 */
public class EcmascriptService extends AbstractEcmascriptService implements
 IEcmaScriptDebugger {

  private AtomicStampedReference<Runtime> runtime = new AtomicStampedReference<Runtime>(null, 0);
  private ConcurrentMap<Integer, Runtime> runtimesList = new ConcurrentHashMap<Integer, Runtime>();

  private Queue<Integer> runtimesQueue = new LinkedList<Integer>();
  private Queue<Integer> garbageQueue = new LinkedList<Integer>();

  public EcmascriptService(ScopeServices services, String version) {
    super(services, version);
  }

  public int getRuntimeId() {
    return runtime.getStamp();
  }

  public void setRuntime(Runtime runtime) {
    this.runtime.set(runtime, runtime.getRuntimeID());
    activeWindowId = runtime.getWindowID();
  }

  public void setRuntime(EsdbgProtos.RuntimeInfo runtime) {
    throw new UnsupportedOperationException("Not suppported without ecmascript-debugger");
  }

  public void addRuntime(EsdbgProtos.RuntimeInfo info) {
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
    List<Runtime> runtimes = new ArrayList<Runtime>();
    while (iterator.hasNext()) {
      runtimes.add((Runtime) ((Pointer) iterator.next()).getNode());
    }
    return runtimes;
  }

  public void init() {
    // We no longer need the configuration
    if (!updateRuntime()) {
      throw new WebDriverException("Could not find a runtime for script injection");
    }
    // Nor the dialogue hack
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
    Response response = executeCommand(ESCommand.LIST_RUNTIMES, selection);

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

    List<WebElement> elements = new ArrayList<WebElement>();

    String toSend = buildEvalString(elements, script, params);
    EvalArg.Builder evalBuilder = buildEval(toSend, getRuntimeId());

    for (WebElement webElement : elements) {
      Variable variable =
          buildVariable(webElement.toString(), ((OperaWebElement) webElement).getObjectId());
      evalBuilder.addVariableList(variable);
    }

    Response response = executeCommand(ESCommand.EVAL, evalBuilder);

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

  protected final Variable buildVariable(String name, int objectId) {
    Variable.Builder variable = Variable.newBuilder();
    variable.setName(name);
    variable.setObjectID(objectId);
    return variable.build();
  }

  private Response eval(String using, int runtimeId, Variable... variables) {
    // This call causes us to release objects, which allows them to be garbage collected, and
    // sometimes causes this method to fail. So I've commented it out. But I reckon it might cause
    // high memory usage in Opera, so the method might need to be updated in the future.
    // processQueues();

    EvalArg.Builder builder = buildEval(using, runtimeId);
    builder.addAllVariableList(Arrays.asList(variables));

    Response response =
        executeCommand(ESCommand.EVAL, builder, OperaIntervals.SCRIPT_TIMEOUT.getValue());

    if (response == null && retries < OperaIntervals.SCRIPT_RETRY.getValue()) {
      retries++;
      sleepDuration += sleepDuration;
      sleep(sleepDuration);
      recover();
      return eval(using, variables);
    } else if (retries >= OperaIntervals.SCRIPT_RETRY.getValue()) {
      resetCounters();
      throw new WebDriverException("No response on executing JS command");
    }

    resetCounters();

    return response;
  }

  private Response eval(String using, Variable... variables) {
    return eval(using, getRuntimeId(), variables);
  }


  public Object executeScript(String using, boolean responseExpected) {
    return executeScript(using, responseExpected, getRuntimeId());
  }

  private Object executeScript(String using, boolean responseExpected, int runtimeId) {
    Response reply = eval(using, runtimeId);
    return responseExpected ? parseEvalReply(parseEvalData(reply)) : null;
  }

  public Integer getObject(String using) {
    EvalResult reply = parseEvalData(eval(using));
    return (reply.getValue().getType().equals(Type.OBJECT)) ? reply.getValue().getObject()
        .getObjectID() : null;
  }


  public String callFunctionOnObject(String using, int objectId) {
    Object result = callFunctionOnObject(using, objectId, true);
    return (result == null) ? null : String.valueOf(result);
  }

  public Object callFunctionOnObject(String using, int objectId, boolean responseExpected) {
    Variable variable = buildVariable("locator", objectId);

    Response response = eval(using, variable);
    return responseExpected ? parseEvalReply(parseEvalData(response)) : null;
  }

  public Integer executeScriptOnObject(String using, int objectId) {
    Variable variable = buildVariable("locator", objectId);

    EvalResult reply = parseEvalData(eval(using, variable));
    Object object = parseEvalReply(reply);
    if (object == null || !(object instanceof EcmascriptProtos.Object)) {
      return null;
    }
    return ((EcmascriptProtos.Object) object).getObjectID();
  }

  private Object parseEvalReply(EvalResult result) {
    Status status = result.getStatus();

    switch (status) {
      case CANCELLED:
        return null;
      case EXCEPTION:
        throw new WebDriverException("EcmaScript exception");
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

  /**
   * Find the runtime for injection (default). Typically this is _top runtime with the active window
   * that has focus.
   */
  protected Runtime findRuntime() {
    return findRuntime(windowManager.getActiveWindowId());
  }

  protected Runtime findRuntime(int windowId) {
    createAllRuntimes();
    Runtime runtime =
        (Runtime) xpathPointer(runtimesList.values(),
            "/.[htmlFramePath='" + currentFramePath + "' and windowID='" + windowId + "']")
            .getValue();
    return runtime;
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

    List<Runtime> runtimesInfos = new ArrayList<Runtime>(runtimesList.values());
    runtimesInfos.remove(rootInfo);

    for (Runtime runtimeInfo : runtimesInfos) {
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
    List<Integer> ids = new ArrayList<Integer>();

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
    List<String> frameNames = new ArrayList<String>();
    for (Runtime runtime : runtimes) {
      frameNames.add(runtime.getHtmlFramePath());
    }
    return frameNames;
  }

  public void releaseObjects() {
    ReleaseObjectsArg.Builder builder = ReleaseObjectsArg.newBuilder();
    executeCommand(ESCommand.RELEASE_OBJECTS, builder);
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
    if (runtime == null) // speed dial doesn't have a runtime
    {
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
      List<Object> result = new ArrayList<Object>();

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
      Map<String, Object> result = new HashMap<String, Object>();

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
        return Boolean.valueOf(true);
      case FALSE:
        return Boolean.valueOf(false);
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
    builder.addRuntimeIDList(runtimeID);
    builder.setCreate(true);

    Response response = executeCommand(ESCommand.LIST_RUNTIMES, builder);
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
    Response response = executeCommand(ESCommand.EXAMINE_OBJECTS, builder);

    ObjectList.Builder objListBuilder = ObjectList.newBuilder();
    buildPayload(response, objListBuilder);
    return objListBuilder.build();
  }

}