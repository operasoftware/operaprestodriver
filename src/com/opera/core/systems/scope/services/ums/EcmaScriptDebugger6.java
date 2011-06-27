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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.opera.core.systems.OperaWebElement;
import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.model.ScriptResult;
import com.opera.core.systems.scope.ESDebuggerCommand;
import com.opera.core.systems.scope.protos.Esdbg6Protos.EvalResult;
import com.opera.core.systems.scope.protos.Esdbg6Protos.ExamineList;
import com.opera.core.systems.scope.protos.Esdbg6Protos.ObjectChainList;
import com.opera.core.systems.scope.protos.Esdbg6Protos.ObjectValue;
import com.opera.core.systems.scope.protos.Esdbg6Protos.ObjectInfo.Property;
import com.opera.core.systems.scope.protos.EsdbgProtos.EvalData;
import com.opera.core.systems.scope.protos.EsdbgProtos.EvalData.Variable;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.util.VersionUtil;

/**
 * Ecmascript debugger 6.0 implementation handles injection and runtime
 * management to the new version of debugger
 *
 * @author Deniz Turkoglu <denizt@opera.com>
 *
 */
public class EcmaScriptDebugger6 extends EcmaScriptDebugger {

  public EcmaScriptDebugger6(ScopeServices services, String version) {
    super(services, version);
    if (VersionUtil.compare(version, "7.0") >= 0) throw new UnsupportedOperationException(
        "ecmascript-debugger version " + version + " is not supported");

  }

  @Override
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
      return new ScriptResult(data.getObjectID(), data.getClassName());
    } else return parsed;
  }

  private EvalResult parseEvalData(Response response) {
    EvalResult.Builder builder = EvalResult.newBuilder();
    buildPayload(response, builder);
    return builder.build();
  }

  @Override
  public Object executeScript(String using, boolean responseExpected) {
    return executeScript(using, responseExpected, getRuntimeId());
  }

  @Override
  public Object executeScript(String using, boolean responseExpected,
      int runtimeId) {
    Response reply = eval(using, runtimeId);
    return responseExpected ? parseEvalReply(parseEvalData(reply)) : null;
  }

  @Override
  public Integer getObject(String using) {
    EvalResult reply = parseEvalData(eval(using));
    return (reply.getType().equals("object")) ? reply.getObjectValue().getObjectID()
        : null;
  }

  @Override
  protected Integer getObject(String using, int runtimeId) {
    EvalResult reply = parseEvalData(eval(using, runtimeId));
    return (reply.getType().equals("object")) ? reply.getObjectValue().getObjectID()
        : null;
  }

  @Override
  public String callFunctionOnObject(String using, int objectId) {
    Variable variable = buildVariable("locator", objectId);

    EvalResult reply = parseEvalData(eval(using, variable));
    return reply.getType().equals("null") ? null : reply.getValue();
  }

  @Override
  public Object callFunctionOnObject(String using, int objectId,
      boolean responseExpected) {
    Variable variable = buildVariable("locator", objectId);

    Response response = eval(using, variable);
    return responseExpected ? parseEvalReply(parseEvalData(response)) : null;
  }

  @Override
  public Integer executeScriptOnObject(String using, int objectId) {
    Variable variable = buildVariable("locator", objectId);

    EvalResult reply = parseEvalData(eval(using, variable));
    Object object = parseEvalReply(reply);
    if (object == null || !(object instanceof ObjectValue)) {
      return null;
    }
    return ((ObjectValue) object).getObjectID();
  }

  protected Object parseEvalReply(EvalResult result) {

    String status = result.getStatus();

    if (!status.equals("completed")) {
      if (status.equals("unhandled-exception")) {
        // Would be great give the JS error here, but it appears that by the
        // time we callFunctionOnObject the error object has gone...
        throw new WebDriverException("Ecmascript exception");
      }
      // FIXME what is the best approach here?
      else if (status.equals("cancelled-by-scheduler")) {
        return null;
      } else if (status.equals("aborted")) {

      }

    }

    String dataType = result.getType();

    if (dataType.equals("string")) {
      return result.getValue();
    } else if (dataType.equals("number")) {
      return parseNumber(result.getValue());
    } else if (dataType.equals("boolean")) {
      return Boolean.valueOf(result.getValue());
    } else if (dataType.equals("undefined")) {
      return null;
    } else if (dataType.equals("object")) {
      return result.getObjectValue();
    }
    // return null if none
    return null;
  }

  @Override
  public List<Integer> examineObjects(Integer id) {
    List<Integer> ids = new ArrayList<Integer>();

    ObjectChainList list = getChainList(id);
    List<Property> objects = list.getObjectChainList(0).getObjectListList().get(0).getPropertyListList();
    for (Property obj : objects) {
      if (obj.getType().equals("object")) ids.add(obj.getObjectValue().getObjectID());
    }

    return ids;

  }

  @Override
  public Object examineScriptResult(Integer id) {
    ObjectChainList list = getChainList(id);
    String className = list.getObjectChainListList().get(0).getObjectList(0).getValue().getClassName();
    List<Property> properties = list.getObjectChainList(0).getObjectListList().get(0).getPropertyListList();
    if (className.equals("Array")) {
      List<Object> result = new ArrayList<Object>();

      for (Property property : properties) {
        String type = property.getType();
        if (type.equals("number") && property.getName().equals("length")) {
          // ignore ?!?
        } else if (type.equals("object")) {
          result.add(examineScriptResult(property.getObjectValue().getObjectID()));
        } else {
          result.add(parseValue(type, property.getValue()));
        }
      }
      return result;
    } else {
      // we have a map
      Map<String, Object> result = new HashMap<String, Object>();

      for (Property property : properties) {
        String type = property.getType();
        if (type.equals("number") && property.getName().equals("length")) {
          // ignore ?!?
        } else if (type.equals("object")) {
          result.put(property.getName(),
              examineScriptResult(property.getObjectValue().getObjectID()));
        } else {
          result.put(property.getName(), parseValue(type,
              property.getValue()));
        }
      }
      return result;
    }
  }

  private ObjectChainList getChainList(Integer id) {
    ExamineList.Builder builder = ExamineList.newBuilder();
    builder.setExaminePrototypes(false);
    builder.setRuntimeID(getRuntimeId());
    builder.addObjectList(id);
    Response response = executeCommand(ESDebuggerCommand.EXAMINE_OBJECTS,
        builder);

    ObjectChainList.Builder objListBuilder = ObjectChainList.newBuilder();
    buildPayload(response, objListBuilder);
    return objListBuilder.build();
  }

}
