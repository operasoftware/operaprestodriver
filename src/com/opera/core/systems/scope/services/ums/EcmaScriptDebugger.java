/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.scope.services.ums;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicStampedReference;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.opera.core.systems.OperaWebElement;
import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.model.ScriptResult;
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.ESDebuggerCommand;
import com.opera.core.systems.scope.beans.Runtime;
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
import com.opera.core.systems.scope.services.IWindowManager;

/**
 * Manages the ecmascript-debugger service
 * Handles runtime management and script injection
 * @author Deniz Turkoglu
 *
 */
public class EcmaScriptDebugger extends AbstractService implements IEcmaScriptDebugger {
	
	protected int activeWindowId;
	protected final IWindowManager windowManager;

	private AtomicStampedReference<RuntimeInfo> runtime = new AtomicStampedReference<RuntimeInfo>(null, 0);
	
	private Map<Integer, RuntimeInfo> runtimesList = new ConcurrentHashMap<Integer, RuntimeInfo>();

	
	public int getRuntimeId() {
		return runtime.getStamp();
	}

	public void setRuntime(RuntimeInfo runtime) {
		this.runtime.set(runtime, runtime.getRuntimeID());
		activeWindowId = runtime.getWindowID();
	}
	
	public void addRuntime(RuntimeInfo runtime) {
		runtimesList.put(runtime.getWindowID(), runtime);
	}
	
	public void removeRuntime(int runtimeId) {
		runtimesList.remove(runtimeId);
	}
	
	public void addRuntime(Runtime runtime) {
		throw new UnsupportedOperationException("Not suppported in STP/1");
	}

	public void setRuntime(Runtime runtime) {
		throw new UnsupportedOperationException("Not suppported in STP/1");
	}
	

	public EcmaScriptDebugger(ScopeServices services, String serviceName, String version) {	
		super(services, serviceName, version);
		services.setDebugger(this);
		this.windowManager = services.getWindowManager();
		this.services = services;
	}
	

	
	private List<RuntimeInfo> getRuntimesList() {
		int windowId = services.getWindowManager().getActiveWindowId();
		Iterator<?> iterator = (Iterator<?>) xpathIterator(runtimesList.values(), ".[windowId='" + windowId + "']");
		List<RuntimeInfo> runtimes = new ArrayList<RuntimeInfo>();
		while (iterator.hasNext()) {
			runtimes.add((RuntimeInfo) ((Pointer)iterator.next()).getNode());
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
		
		if(!updateRuntime())
			throw new WebDriverException("Could not find a runtime for script injection");
		//FIXME workaround for internal dialog
		executeJavascript("return 1;", true);
	}
	
	public boolean updateRuntime() {
		createAllRuntimes();
		RuntimeInfo activeRuntime = findRuntime();
		if(activeRuntime != null) {
			setRuntime(activeRuntime);
			return true;
		}
		return false;
	}

	/**
	 * Gets a list of runtimes and keeps the list,
	 * create runtimes for all pages so even if the pages dont
	 * have script we can still inject to a 'fake' runtime
	 */
	protected void createAllRuntimes() {
		RuntimeSelection.Builder selection = RuntimeSelection.newBuilder().setAllRuntimes(true);
		Response response = this.executeCommand(ESDebuggerCommand.LIST_RUNTIMES, selection);
		runtimesList.clear();
		RuntimeList.Builder builder = RuntimeList.newBuilder();
		buildPayload(response, builder);
		List<RuntimeInfo> allRuntimes = builder.build().getRuntimeListList();
		for(RuntimeInfo info : allRuntimes) {
			runtimesList.put(info.getWindowID(), info);
		}

	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#scriptExecutor(java.lang.String, java.lang.Object)
	 */
	public Object scriptExecutor(String script, Object... params) {
		List<WebElement> elements = new ArrayList<WebElement>();
		String toSend;
		if (params != null && params.length > 0) {
			StringBuilder builder = new StringBuilder();
			for (Object object : params) {
				if (!builder.toString().isEmpty()) {
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
		
		EvalData.Builder evalBuilder = buildEval(toSend);
		
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
        	return new ScriptResult(data.getObjectID(), data.getName());
        }
        else return parsed;
	}
	
	private EvalResult parseEvalData(Response response) {
		EvalResult.Builder builder = EvalResult.newBuilder();
		buildPayload(response, builder);
		return builder.build();
	}
	
	protected void processArgument(Object object, StringBuilder builder, List<WebElement> elements) {
		if(object instanceof WebElement) {
			elements.add((WebElement) object);
			builder.append(String.valueOf(object));
		} else if(object instanceof String) {
			builder.append("'" + String.valueOf(object) + "'");
		} else if(object instanceof Integer || object instanceof Long || object instanceof Boolean || object instanceof Float || object instanceof Double){
			builder.append(String.valueOf(object));
		} else {
			throw new IllegalArgumentException("The argument type is not supported");
		}
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#executeJavascript(java.lang.String)
	 */
	public String executeJavascript(String using) {
		return executeJavascript(using, true);
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#executeJavascript(java.lang.String, boolean)
	 */
	public String executeJavascript(String using, boolean responseExpected){
		Object result = executeScript(using, responseExpected);
		return (result == null) ? null : String.valueOf(result);
	}
	
	protected final EvalData.Builder buildEval(String using) {
		EvalData.Builder eval = EvalData.newBuilder();
		eval.setRuntimeID(getRuntimeId());
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
	
	private void recover() {
		windowManager.findDriverWindow();
		activeWindowId = windowManager.getActiveWindowId();
		updateRuntime();
	}
	
	protected Response eval(String using, Variable... variables) {
		
		if (windowManager.getActiveWindowId() != activeWindowId) {
			recover();
		}
		
		EvalData.Builder builder = buildEval(using);
		builder.addAllVariableList(Arrays.asList(variables));
		
		return executeCommand(ESDebuggerCommand.EVAL, builder);
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#executeScript(java.lang.String, boolean)
	 */	
	public Object executeScript(String using, boolean responseExpected) {
		Response reply = eval(using);
		return responseExpected ? parseEvalReply(parseEvalData(reply)) : null;
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#getObject(java.lang.String)
	 */
	public Integer getObject(String using) {
		EvalResult reply = parseEvalData(eval(using));
		return (reply.getType().equals("object")) ? reply.getObjectValue().getObjectID() : null;
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#callFunctionOnObject(java.lang.String, int)
	 */
	public String callFunctionOnObject(String using, int objectId) {
		Variable variable = buildVariable("locator", objectId);

		EvalResult reply = parseEvalData(eval(using, variable));
		return reply.getType().equals("null") ? null : reply.getValue();
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#callFunctionOnObject(java.lang.String, int, boolean)
	 */
	public Object callFunctionOnObject(String using, int objectId, boolean responseExpected) {
		Variable variable = buildVariable("locator", objectId);
		
		Response response = eval(using, variable);
		return responseExpected ? parseEvalReply(parseEvalData(response)) : null;
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#executeScriptOnObject(java.lang.String, int)
	 */
	
	public Integer executeScriptOnObject(String using, int objectId) {
		Variable variable = buildVariable("locator", objectId);

		EvalResult reply = parseEvalData(eval(using, variable));
		Object object = parseEvalReply(reply);
		if(object == null || !(object instanceof ObjectValue))
			return null;
		return ((ObjectValue) object).getObjectID();
	}
    
    
	/**
	 * Parses a reply and returns the following types
	 * String presentation of number, boolean or string
	 * FIXME investigate why JAXB doesnt create enums
	 * @param reply
	 * @return
	 */
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
	
	protected Object parseNumber(String value) {
		Number number;
		try {
			number = NumberFormat.getInstance().parse(value);
		if(number instanceof Long)
			return number.longValue();
		else
			return number.doubleValue();
		} catch (ParseException e) {
			throw new WebDriverException("The result from the script can not be parsed");
		}
	}
	
	/**
	 * Find the runtime for injection (default)
	 * Typically this is _top runtime with the active window that has focus
	 * @return
	 */
	protected RuntimeInfo findRuntime() {
		int windowId = windowManager.getActiveWindowId();
		RuntimeInfo runtime = (RuntimeInfo) xpathPointer(runtimesList.values(), "/.[htmlFramePath='_top' and windowID='" + windowId + "']").getValue();
		return runtime;
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#changeRuntime(java.lang.String)
	 */
	public void changeRuntime(String framePath){
		
		updateRuntime();
		int windowId = windowManager.getActiveWindowId();
		RuntimeInfo info = null;
		//TODO review :  get nodes and then from the nodes find the one that starts with second frame
		if(framePath.indexOf('.') > -1) {
			//we have a nested frame request
			String[] frames = framePath.split("\\.");
			//get the relative pointer we can start from
			
			JXPathContext pathContext = JXPathContext.newContext(runtimesList);
			Pointer result = findPointer(pathContext, "/.[htmlFramePath[starts-with(.,'_top/"+ frames[0] + "')] and windowID='" + windowId + "']", framePath);	
			JXPathContext relativeContext = pathContext.getRelativeContext(result);
			
			for (int i = 1; i < frames.length; i++) {
				String path = (String) relativeContext.getValue("htmlFramePath");
				result = findPointer(pathContext, "/.[htmlFramePath[starts-with(.,'"+ path + "/" + frames[i] +"')] and windowID='" + windowId + "']", path);
				
				relativeContext = pathContext.getRelativeContext(result);
				setRuntime(runtimesList.get((Integer) relativeContext.getValue("runtimeId")));
			}
		} else {
			framePath = framePath.isEmpty() ? "" : "/" + framePath;
			info = (RuntimeInfo) xpathPointer(runtimesList.values(), "/.[htmlFramePath[starts-with(.,'_top"+ framePath + "')] and windowID='" + windowId + "']").getValue();
			if(runtime == null) {
				throw new NoSuchFrameException("Frame with name " + framePath + " not found");
			}
			setRuntime(info);
		}

	}
	
	
	private Pointer findPointer(JXPathContext context, String query, String framePath) {
		Pointer result = context.getPointer(query);
		if(result instanceof NullPointer)
			throw new NoSuchFrameException("Frame with name " + framePath + " not found");
		return result;
	}
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#changeRuntime(int)
	 */
	public void changeRuntime(int index){
		if(index < 0 || index >= runtimesList.size()) {
			throw new NoSuchFrameException("Invalid frame index " + index);
		}
		
		List<RuntimeInfo> runtimes = getRuntimesList();
		RuntimeInfo frame = null;
		
		if((frame = runtimes.get(index)) != null) {
			setRuntime(frame);
		} else {
			throw new NoSuchFrameException("Frame with index " + index + " not found");
		}
		
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#cleanUpRuntimes(int)
	 */
	public void cleanUpRuntimes(int windowId) {
		// if we already have a runtime listed as _top with that window id,
		// clean all runtimes with that window id
		//if (findRuntime() != null) {
			for (RuntimeInfo runtime : runtimesList.values()) {
				if (runtime.getWindowID() == windowId)
					runtimesList.remove(runtime.getRuntimeID());
			}
		//}
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#cleanUpRuntimes()
	 */
	public void cleanUpRuntimes() {
		int windowId = services.getWindowManager().getActiveWindowId();
		cleanUpRuntimes(windowId);
	}

	//TODO needs retry approach?
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#examineObjects(java.lang.Integer)
	 */
	public List<Integer> examineObjects(Integer id) {
		ExamineList.Builder examine = ExamineList.newBuilder();
		examine.setRuntimeID(getRuntimeId());
		examine.addObjectList(id);
		Response response = executeCommand(ESDebuggerCommand.EXAMINE_OBJECTS, examine);
		
		ObjectList.Builder builder = ObjectList.newBuilder();
		buildPayload(response, builder);
		ObjectList list = builder.build();
		
		List<Integer> ids = new ArrayList<Integer>();
		List<Property> properties = list.getObjectList(0).getPropertyListList();
		for (Property property : properties) {
			if(property.getType().equals("object"))
				ids.add(property.getObjectValue().getObjectID());
		}
		
		return ids;
		
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#listFramePaths()
	 */
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
	
}
