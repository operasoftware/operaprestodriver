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

public class EcmaScriptDebugger6 extends EcmaScriptDebugger {
	
	public EcmaScriptDebugger6(ScopeServices services,String version) {
		super(services, version);
		if(VersionUtil.compare(version, "7.0") >= 0)
			throw new UnsupportedOperationException("ecmascript-debugger version " + version + " is not supported");
		
	}
	
	public Integer executeScriptOnObject(String using, int objectId) {
		Variable variable = buildVariable("locator", objectId);

		EvalResult reply = parseEvalData(eval(using, variable));
		Object object = parseEvalReply(reply);
		if(object == null || !(object instanceof ObjectValue))
			return null;
		return ((ObjectValue) object).getObjectID();
	}
	
	
	public Object scriptExecutor(String script, Object... params) {
		List<WebElement> elements = new ArrayList<WebElement>();
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
					if(builder.charAt(lastCharIndex) != '[')
						builder.deleteCharAt(lastCharIndex);
					
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
			Variable variable = buildVariable(webElement.toString(), ((OperaWebElement) webElement).getObjectId());
			evalBuilder.addVariableList(variable);
		}
		
		Response response = executeCommand(ESDebuggerCommand.EVAL, evalBuilder);
		
		if(response == null)
			throw new WebDriverException("Internal error while executing script");
		
		EvalResult result = parseEvalData(response);

        Object parsed = parseEvalReply(result);
        if(parsed instanceof ObjectValue) {
        	ObjectValue data = (ObjectValue) parsed;
        	return new ScriptResult(data.getObjectID(), data.getClassName());
        }
        else return parsed;
	}
	
	public List<Integer> examineObjects(Integer id) {
		List<Integer> ids = new ArrayList<Integer>();
		
		ObjectChainList list = getChainList(id);
		List<Property> properties = list.getObjectChainList(0).getObjectListList().get(0).getPropertyListList();
		for (Property property : properties) {
			if(property.getType().equals("object"))
				ids.add(property.getObjectValue().getObjectID());
		}
		
		return ids;
		
	}
	
	private ObjectChainList getChainList(Integer id) {
		ExamineList.Builder examine = ExamineList.newBuilder();
		examine.setExaminePrototypes(false);
		examine.setRuntimeID(getRuntimeId());
		examine.addObjectList(id);
		Response response = executeCommand(ESDebuggerCommand.EXAMINE_OBJECTS, examine);
		
		ObjectChainList.Builder builder = ObjectChainList.newBuilder();
		buildPayload(response, builder);
		return builder.build();

	}
	
	public Object examineScriptResult(Integer id) {
		ObjectChainList list = getChainList(id);
		List<Property> properties = list.getObjectChainList(0).getObjectListList().get(0).getPropertyListList();
		String className = list.getObjectChainListList().get(0).getObjectList(0).getValue().getClassName();
		if (className.equals("Array")) {
			List<Object> result = new ArrayList<Object>();

			for (Property property : properties) {
				if (property.getType().equals("number") && property.getName().equals("length")) {
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
				if (property.getType().equals("number") && property.getName().equals("length")) {
					// ignore ?!?
				} else if (property.getType().equals("object")) {
					result.put(property.getName(), examineScriptResult(property.getObjectValue().getObjectID()));
				} else {
					result.put(property.getName(), parseValue(property.getType(), property.getValue()));
				}
			}
			return result;
		}
	}
	
	private EvalResult parseEvalData(Response response) {
		EvalResult.Builder builder = EvalResult.newBuilder();
		buildPayload(response, builder);
		return builder.build();
	}
	
	protected Object parseEvalReply(EvalResult result) {
		
		String status = result.getStatus();
		
		if(!status.equals("completed")) {
			if(status.equals("unhandled-exception")) {
				throw new WebDriverException("Ecmascript exception");
			} 
			//FIXME what is the best approach here?
			else if(status.equals("cancelled-by-scheduler")){
				return null;
			} else if(status.equals("aborted")){
				
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
        //return null if none
        return null;
	}
	
	public Object callFunctionOnObject(String using, int objectId, boolean responseExpected) {
		Variable variable = buildVariable("locator", objectId);
		
		Response response = eval(using, variable);
		return responseExpected ? parseEvalReply(parseEvalData(response)) : null;
	}
	
	public String callFunctionOnObject(String using, int objectId) {
		Variable variable = buildVariable("locator", objectId);

		EvalResult reply = parseEvalData(eval(using, variable));
		return reply.getType().equals("null") ? null : reply.getValue();
	}
	
	public Object executeScript(String using, boolean responseExpected) {
		return executeScript(using, responseExpected, getRuntimeId());
	}
	
	public Object executeScript(String using, boolean responseExpected, int runtimeId) {
		Response reply = eval(using, runtimeId);
		return responseExpected ? parseEvalReply(parseEvalData(reply)) : null;
	}
	
        
	public Integer getObject(String using) {
		EvalResult reply = parseEvalData(eval(using));
		return (reply.getType().equals("object")) ? reply.getObjectValue().getObjectID() : null;
	}
	
	@Override
	public Integer getObject(String using, int runtimeId) {
		EvalResult reply = parseEvalData(eval(using, runtimeId));
		return (reply.getType().equals("object")) ? reply.getObjectValue().getObjectID() : null;
	}
	
	
	
}
