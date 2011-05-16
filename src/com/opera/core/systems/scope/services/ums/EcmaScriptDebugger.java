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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicStampedReference;

import org.apache.commons.jxpath.Pointer;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.opera.core.systems.OperaWebElement;
import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.model.RuntimeNode;
import com.opera.core.systems.model.ScriptResult;
import com.opera.core.systems.scope.AbstractEcmascriptService;
import com.opera.core.systems.scope.ESDebuggerCommand;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.protos.EcmascriptProtos.ReadyStateChange;
import com.opera.core.systems.scope.protos.EsdbgProtos.Configuration;
import com.opera.core.systems.scope.protos.EsdbgProtos.EvalData;
import com.opera.core.systems.scope.protos.EsdbgProtos.EvalResult;
import com.opera.core.systems.scope.protos.EsdbgProtos.ExamineList;
import com.opera.core.systems.scope.protos.EsdbgProtos.ObjectList;
import com.opera.core.systems.scope.protos.EsdbgProtos.ObjectValue;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeList;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeSelection;
import com.opera.core.systems.scope.protos.EsdbgProtos.EvalData.Variable;
import com.opera.core.systems.scope.protos.EsdbgProtos.ObjectInfo.Property;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;

/**
 * Manages the ecmascript-debugger service Handles runtime management and script
 * injection
 *
 * @author Deniz Turkoglu <denizt@opera.com>
 *
 */
public class EcmaScriptDebugger extends AbstractEcmascriptService implements
    IEcmaScriptDebugger {

  private AtomicStampedReference<RuntimeInfo> runtime = new AtomicStampedReference<RuntimeInfo>(
      null, 0);
  private ConcurrentMap<Integer, RuntimeInfo> runtimesList = new ConcurrentHashMap<Integer, RuntimeInfo>();

  public EcmaScriptDebugger(ScopeServices services, String version) {
    super(services, version);
  }

  public int getRuntimeId() {
    return runtime.getStamp();
  }

  public void setRuntime(RuntimeInfo runtime) {
    this.runtime.set(runtime, runtime.getRuntimeID());
    activeWindowId = runtime.getWindowID();
  }

  public void addRuntime(RuntimeInfo runtime) {
    runtimesList.put(runtime.getRuntimeID(), runtime);
  }

  public void removeRuntime(int runtimeId) {
    runtimesList.remove(runtimeId);
  }

  private List<RuntimeInfo> getRuntimesList() {
    int windowId = services.getWindowManager().getActiveWindowId();
    Iterator<?> iterator = xpathIterator(runtimesList.values(), "/.[windowID='"
        + windowId + "']");
    List<RuntimeInfo> runtimes = new ArrayList<RuntimeInfo>();
    while (iterator.hasNext()) {
      runtimes.add((RuntimeInfo) ((Pointer) iterator.next()).getNode());
    }
    return runtimes;
  }

  public void init() {
    Configuration.Builder configuration = Configuration.newBuilder();
    configuration.setStopAtScript(false);
    configuration.setStopAtAbort(false);
    configuration.setStopAtException(false);
    configuration.setStopAtError(false);
    configuration.setStopAtDebuggerStatement(false);
    configuration.setStopAtGc(false);

    executeCommand(ESDebuggerCommand.SET_CONFIGURATION, configuration);

    if (!updateRuntime()) throw new WebDriverException(
        "Could not find a runtime for script injection");
    // FIXME workaround for internal dialog
    // The dialog is finally removed but just keeping this here
    // until every platform upgrades to core 2.7+
    executeJavascript("return 1;", true);
  }

  public boolean updateRuntime() {
    RuntimeInfo activeRuntime = findRuntime();
    if (activeRuntime != null) {
      setRuntime(activeRuntime);
      return true;
    }
    return false;
  }

  /**
   * Gets a list of runtimes and keeps the list, create runtimes for all pages
   * so even if the pages dont have script we can still inject to a 'fake'
   * runtime
   */
  protected void createAllRuntimes() {
    RuntimeSelection.Builder selection = RuntimeSelection.newBuilder();
    selection.setAllRuntimes(true);
    Response response = executeCommand(ESDebuggerCommand.LIST_RUNTIMES,
        selection);

    runtimesList.clear();
    RuntimeList.Builder builder = RuntimeList.newBuilder();
    buildPayload(response, builder);
    List<RuntimeInfo> allRuntimes = builder.build().getRuntimeListList();

    for (RuntimeInfo info : allRuntimes) {
      runtimesList.put(info.getRuntimeID(), info);
    }
  }

  public Object scriptExecutor(String script, Object... params) {
    List<WebElement> elements = new ArrayList<WebElement>();

    String toSend = buildEvalString(elements, script, params);
    EvalData.Builder evalBuilder = buildEval(toSend, getRuntimeId());

    for (WebElement webElement : elements) {
      Variable variable = buildVariable(webElement.toString(),
          ((OperaWebElement) webElement).getObjectId());
      evalBuilder.addVariableList(variable);
    }

    Response response = executeCommand(ESDebuggerCommand.EVAL, evalBuilder);

    if (response == null) throw new WebDriverException(
        "Internal error while executing script");

    EvalResult result = parseEvalData(response);

    Object parsed = parseEvalReply(result);
    if (parsed instanceof ObjectValue) {
      ObjectValue data = (ObjectValue) parsed;
      return new ScriptResult(data.getObjectID(), data.getName());
    } else return parsed;
  }

  private EvalResult parseEvalData(Response response) {
    EvalResult.Builder builder = EvalResult.newBuilder();
    buildPayload(response, builder);
    return builder.build();
  }

  protected final EvalData.Builder buildEval(String using, int runtimeId) {
    EvalData.Builder eval = EvalData.newBuilder();
    eval.setRuntimeID(runtimeId);
    eval.setFrameIndex(0);
    eval.setThreadID(0);
    eval.setScriptData(using);
    return eval;
  }

  protected final Variable buildVariable(String name, int objectId) {
    Variable.Builder variable = Variable.newBuilder();
    variable.setName(name);
    variable.setObjectID(objectId);
    return variable.build();
  }

  protected Response eval(String using, int runtimeId, Variable... variables) {
    /*
    if (windowManager.getActiveWindowId() != activeWindowId) {
      recover();
    }
    */

    EvalData.Builder builder = buildEval(using, runtimeId);
    builder.addAllVariableList(Arrays.asList(variables));

    Response response = executeCommand(ESDebuggerCommand.EVAL, builder,
        OperaIntervals.SCRIPT_TIMEOUT.getValue());

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

  protected Response eval(String using, Variable... variables) {
    return eval(using, getRuntimeId(), variables);
  }


  public Object executeScript(String using, boolean responseExpected) {
    return executeScript(using, responseExpected, getRuntimeId());
  }

  public Object executeScript(String using, boolean responseExpected,
      int runtimeId) {
    Response reply = eval(using, runtimeId);
    return responseExpected ? parseEvalReply(parseEvalData(reply)) : null;
  }

  public Integer getObject(String using) {
    EvalResult reply = parseEvalData(eval(using));
    return (reply.getType().equals("object")) ? reply.getObjectValue().getObjectID()
        : null;
  }

  protected Integer getObject(String using, int runtimeId) {
    EvalResult reply = parseEvalData(eval(using, runtimeId));
    return (reply.getType().equals("object")) ? reply.getObjectValue().getObjectID()
        : null;
  }

  public String callFunctionOnObject(String using, int objectId) {
    Variable variable = buildVariable("locator", objectId);

    EvalResult reply = parseEvalData(eval(using, variable));
    return reply.getType().equals("null") ? null : reply.getValue();
  }

  public Object callFunctionOnObject(String using, int objectId,
      boolean responseExpected) {
    Variable variable = buildVariable("locator", objectId);

    Response response = eval(using, variable);
    return responseExpected ? parseEvalReply(parseEvalData(response)) : null;
  }

  public Integer executeScriptOnObject(String using, int objectId) {
    Variable variable = buildVariable("locator", objectId);

    EvalResult reply = parseEvalData(eval(using, variable));
    Object object = parseEvalReply(reply);
    if (object == null || !(object instanceof ObjectValue)) return null;
    return ((ObjectValue) object).getObjectID();
  }

  /**
   * Parses a reply and returns the following types String presentation of
   * number, boolean or string
   *
   * @param reply
   * @return
   */
  protected Object parseEvalReply(EvalResult result) {

    String status = result.getStatus();

    if (!status.equals("completed")) {
      if (status.equals("unhandled-exception")) {
        throw new WebDriverException("Ecmascript exception");
      }
      // FIXME what is the best approach here?
      else if (status.equals("cancelled-by-scheduler")) {
        return null;
      } else if (status.equals("aborted")) {

      }

    }

    String dataType = result.getType();

    if (dataType.equals("object")) {
      return result.getObjectValue();
    } else return parseValue(dataType, result.getValue());
  }

  protected Object parseValue(String dataType, String value) {
    if (dataType.equals("string")) {
      return value;
    } else if (dataType.equals("number")) {
      return parseNumber(value);
    } else if (dataType.equals("boolean")) {
      return Boolean.valueOf(value);
    } else if (dataType.equals("undefined")) {
      return null;
    }
    return null;
  }

  /**
   * Find the runtime for injection (default) Typically this is _top runtime
   * with the active window that has focus
   *
   * @return
   */
  protected RuntimeInfo findRuntime() {
    return findRuntime(windowManager.getActiveWindowId());
  }

  protected RuntimeInfo findRuntime(int windowId) {
    createAllRuntimes();
    RuntimeInfo runtime = (RuntimeInfo) xpathPointer(
        runtimesList.values(),
        "/.[htmlFramePath='" + currentFramePath + "' and windowID='" + windowId
            + "']").getValue();
    return runtime;
  }

  /**
   * Updates the runtimes list to most recent version
   * TODO this has to be kept up to date with events and as failover we should
   * update
   * It builds a tree to find the frame we are looking for
   * TODO tree also must be kept up to date
   */
  private void buildRuntimeTree() {
    updateRuntime();
    RuntimeInfo rootInfo = findRuntime();
    root = new RuntimeNode();
    root.setFrameName("_top");
    root.setRuntimeID(rootInfo.getRuntimeID());

    List<RuntimeInfo> runtimesInfos = new ArrayList<RuntimeInfo>(
        runtimesList.values());
    runtimesInfos.remove(rootInfo);

    for (RuntimeInfo runtimeInfo : runtimesInfos) {
      addNode(runtimeInfo, root);
    }
  }

  public void changeRuntime(int index) {
    buildRuntimeTree();

    RuntimeNode node = root.getNodes().get(index + 1);

    if (node == null) {
      throw new NoSuchFrameException("Invalid frame index " + index);
    }

    RuntimeInfo info = runtimesList.get(node.getRuntimeID());
    currentFramePath = info.getHtmlFramePath();
    setRuntime(info);
  }

  public void changeRuntime(String frameName) {
    buildRuntimeTree();

    String[] values = frameName.split("\\.");
    RuntimeNode curr = root;

    for (int i = 0; i < values.length; i++) {
      curr = findNodeByName(values[i], curr);
      if (curr == null) break;
    }

    if (curr == null) {
      throw new NoSuchFrameException("Invalid frame name " + frameName);
    }

    RuntimeInfo info = runtimesList.get(curr.getRuntimeID());
    currentFramePath = info.getHtmlFramePath();
    setRuntime(info);
  }

  private RuntimeNode findNodeByName(String name, RuntimeNode rootNode) {
    for (Entry<Integer, RuntimeNode> entry : rootNode.getNodes().entrySet()) {
      // check if the name is a number
      if (isNumber(name) && entry.getKey().equals(Integer.valueOf(name) + 1)) return entry.getValue();
      // check if it is really the name
      else if (entry.getValue().getFrameName().equals(name)) return entry.getValue();
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

  private void addNode(RuntimeInfo info, RuntimeNode root) {
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
    // if we already have a runtime listed as _top with that window id,
    // clean all runtimes with that window id
    for (RuntimeInfo runtime : runtimesList.values()) {
      if (runtime.getWindowID() == windowId) {
        runtimesList.remove(runtime.getRuntimeID());
      }
    }
  }

  // TODO needs retry approach?
  public List<Integer> examineObjects(Integer id) {
    ObjectList list = getObjectList(id);
    List<Integer> ids = new ArrayList<Integer>();
    List<Property> objects = list.getObjectList(0).getPropertyListList();
    for (Property obj : objects) {
      if (obj.getType().equals("object")) ids.add(obj.getObjectValue().getObjectID());
    }

    return ids;
  }

  public List<String> listFramePaths() {
    List<RuntimeInfo> runtimes = getRuntimesList();
    List<String> frameNames = new ArrayList<String>();
    for (RuntimeInfo runtime : runtimes) {
      frameNames.add(runtime.getHtmlFramePath());
    }
    return frameNames;
  }

  public void releaseObjects() {
    executeCommand(ESDebuggerCommand.RELEASE_OBJECTS, null);
  }

  public void resetRuntimesList() {
    runtimesList.clear();
  }

  public void readyStateChanged(ReadyStateChange change) {
    // not fired, silently ignore

  }

  public void releaseObject(int objectId) {
    // not supported, silently ignore
  }

  public void resetFramePath() {
    currentFramePath = "_top";
    setRuntime(findRuntime());
  }

  public String executeJavascript(String using, Integer windowId) {
    // FIXME workaround for frame issues when executing in a specific window
    String tmp = currentFramePath;
    currentFramePath = "_top";
    RuntimeInfo runtime = findRuntime(windowId);
    currentFramePath = tmp;
    if (runtime == null) // speed dial doesnt have a runtime
    return "";
    return (String) executeScript(using, true, runtime.getRuntimeID());
  }

  public Object examineScriptResult(Integer id) {
    ObjectList list = getObjectList(id);
    String className = list.getObjectList(0).getValue().getName();
    List<Property> properties = list.getObjectList(0).getPropertyListList();
    if (className.equals("Array")) {
      List<Object> result = new ArrayList<Object>();

      for (Property property : properties) {
        if (property.getType().equals("number")
            && property.getName().equals("length")) {
          // ignore ?!?
        } else if (property.getType().equals("object")) {
          result.add(examineScriptResult(property.getObjectValue().getObjectID()));
        } else {
          result.add(parseValue(property.getType(), property.getValue()));
        }
      }
      return result;
    } else {
      // we have a map
      Map<String, Object> result = new HashMap<String, Object>();

      for (Property property : properties) {
        if (property.getType().equals("number")
            && property.getName().equals("length")) {
          // ignore ?!?
        } else if (property.getType().equals("object")) {
          result.put(property.getName(),
              examineScriptResult(property.getObjectValue().getObjectID()));
        } else {
          result.put(property.getName(), parseValue(property.getType(),
              property.getValue()));
        }
      }
      return result;
    }
  }

  private ObjectList getObjectList(Integer id) {
    ExamineList.Builder builder = ExamineList.newBuilder();
    builder.setRuntimeID(getRuntimeId());
    builder.addObjectList(id);
    Response response = executeCommand(ESDebuggerCommand.EXAMINE_OBJECTS,
        builder);

    ObjectList.Builder objListBuilder = ObjectList.newBuilder();
    buildPayload(response, objListBuilder);
    return objListBuilder.build();
  }

}
