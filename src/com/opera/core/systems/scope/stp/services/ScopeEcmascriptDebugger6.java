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

package com.opera.core.systems.scope.stp.services;

import com.google.common.collect.Lists;

import com.opera.core.systems.OperaWebElement;
import com.opera.core.systems.model.ScriptResult;
import com.opera.core.systems.scope.ScopeServices;
import com.opera.core.systems.scope.exceptions.ScopeException;
import com.opera.core.systems.scope.protos.Esdbg6Protos.EvalResult;
import com.opera.core.systems.scope.protos.Esdbg6Protos.ExamineList;
import com.opera.core.systems.scope.protos.Esdbg6Protos.ObjectChainList;
import com.opera.core.systems.scope.protos.Esdbg6Protos.ObjectInfo.Property;
import com.opera.core.systems.scope.protos.Esdbg6Protos.ObjectValue;
import com.opera.core.systems.scope.protos.EsdbgProtos.EvalData;
import com.opera.core.systems.scope.protos.EsdbgProtos.EvalData.Variable;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.stp.services.messages.EcmascriptDebuggerMessage;

import org.openqa.selenium.WebElement;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * EcmaScript debugger 6.0 implementation handles injection and runtime management to the new
 * version of debugger.
 */
public class ScopeEcmascriptDebugger6 extends ScopeEcmascriptDebugger {

  public ScopeEcmascriptDebugger6(ScopeServices services) {
    super(services, "6.10");
  }

  @Override
  public Object scriptExecutor(String script, Object... params) {
    List<WebElement> elements = Lists.newArrayList();
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
            builder.append(",");
          }
          int lastCharIndex = builder.length() - 1;
          if (builder.charAt(lastCharIndex) != '[') {
            builder.deleteCharAt(lastCharIndex);
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

    EvalData.Builder evalBuilder = buildEval(toSend, getRuntimeId());

    for (WebElement webElement : elements) {
      Variable variable =
          buildVariable(webElement.toString(), ((OperaWebElement) webElement).getObjectId());
      evalBuilder.addVariableList(variable);
    }

    Response response = executeMessage(EcmascriptDebuggerMessage.EVAL, evalBuilder);

    if (response == null) {
      throw new ScopeException("Internal error while executing script");
    }

    EvalResult result = parseEvalData(response);

    Object parsed = parseEvalReply(result);
    if (parsed instanceof ObjectValue) {
      ObjectValue data = (ObjectValue) parsed;
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

  @Override
  public Object executeScript(String using, boolean responseExpected) {
    return executeScript(using, responseExpected, getRuntimeId());
  }

  @Override
  public Object executeScript(String using, boolean responseExpected, int runtimeId) {
    Response reply = eval(using, runtimeId);
    return responseExpected ? parseEvalReply(parseEvalData(reply)) : null;
  }

  @Override
  public Integer getObject(String using) {
    EvalResult reply = parseEvalData(eval(using));
    return (reply.getType().equals("object")) ? reply.getObjectValue().getObjectID() : null;
  }

  @Override
  public Integer getObject(String using, int runtimeId) {
    EvalResult reply = parseEvalData(eval(using, runtimeId));
    return (reply.getType().equals("object")) ? reply.getObjectValue().getObjectID() : null;
  }

  @Override
  public String callFunctionOnObject(String using, int objectId) {
    Variable variable = buildVariable("locator", objectId);

    EvalResult reply = parseEvalData(eval(using, variable));
    return reply.getType().equals("null") ? null : reply.getValue();
  }

  @Override
  public Object callFunctionOnObject(String using, int objectId, boolean responseExpected) {
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
        String message;
        try {
          message =
              (String) callFunctionOnObject("return locator.name+': '+locator.message;", result
                  .getObjectValue().getObjectID(), true);
        } catch (Exception e) {
          // If we get an exception while trying to get the message just throw
          // a generic Ecmascript exception.
          throw new ScopeException("Ecmascript exception");
        }
        // Throw the ecmascript exception
        throw new ScopeException("Ecmascript exception:\n" + message);
      }
      // TODO: What is the best approach here?
      else if (status.equals("cancelled-by-scheduler")) {
        return null;
      }
    }

    String dataType = result.getType();

    if ("string".equals(dataType)) {
      return result.getValue();
    } else if ("number".equals(dataType)) {
      return parseNumber(result.getValue());
    } else if ("boolean".equals(dataType)) {
      return Boolean.valueOf(result.getValue());
    } else if ("undefined".equals(dataType)) {
      return null;
    } else if ("object".equals(dataType)) {
      return result.getObjectValue();
    }
    // return null if none
    return null;
  }

  @Override
  public List<Integer> examineObjects(Integer id) {
    List<Integer> ids = Lists.newArrayList();

    ObjectChainList list = getChainList(id);
    List<Property> properties =
        list.getObjectChainList(0).getObjectListList().get(0).getPropertyListList();
    for (Property property : properties) {
      if (property.getType().equals("object")) {
        ids.add(property.getObjectValue().getObjectID());
      }
    }

    return ids;

  }

  @Override
  public Object examineScriptResult(Integer id) {
    return examineScriptResult(id, new HashSet<Integer>());
  }

  private Object examineScriptResult(Integer id, Set<Integer> visitedIDs) {
    if (visitedIDs.contains(id)) {
      // cyclic reference - returning null for the inner most reference
      return null;
    }
    visitedIDs.add(id);
    ObjectChainList list = getChainList(id);
    List<Property> properties =
        list.getObjectChainList(0).getObjectListList().get(0).getPropertyListList();

    String className =
        list.getObjectChainListList().get(0).getObjectList(0).getValue().getClassName();

    if (className.endsWith("Element")) {
      return new OperaWebElement(driver, id);
    } else if (className.equals("Array")) {
      List<Object> result = Lists.newArrayList();

      for (Property property : properties) {
        if (property.getType().equals("number") && property.getName().equals("length")) {
          // ignore these, they just tell us about the length of the result
        } else if (property.getType().equals("object")) {
          result.add(examineScriptResult(property.getObjectValue().getObjectID(), visitedIDs));
        } else {
          result.add(parseValue(property.getType(), property.getValue()));
        }
      }
      return result;
    } else {
      // we have a map
      Map<String, Object> result = new HashMap<String, Object>();

      for (Property property : properties) {
        if (property.getType().equals("number") && property.getName().equals("length")) {
          // ignore ?!?
        } else if (property.getType().equals("object")) {
          result.put(property.getName(),
                     examineScriptResult(property.getObjectValue().getObjectID(), visitedIDs));
        } else {
          result.put(property.getName(), parseValue(property.getType(), property.getValue()));
        }
      }
      return result;
    }
  }

  private ObjectChainList getChainList(Integer id) {
    ExamineList.Builder examine = ExamineList.newBuilder();
    examine.setExaminePrototypes(false);
    examine.setRuntimeID(getRuntimeId());
    examine.addObjectList(id);
    Response response = executeMessage(EcmascriptDebuggerMessage.EXAMINE_OBJECTS, examine);

    ObjectChainList.Builder builder = ObjectChainList.newBuilder();
    buildPayload(response, builder);
    return builder.build();

  }

}