/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.scope.services.ums;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import com.opera.core.systems.model.ScriptResult;
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.ESDebuggerCommand;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.protos.EcmascriptProtos.ReadyStateChange;
import com.opera.core.systems.scope.protos.EsdbgProtos.Configuration;
import com.opera.core.systems.scope.protos.EsdbgProtos.EvalData;
import com.opera.core.systems.scope.protos.EsdbgProtos.EvalData.Variable;
import com.opera.core.systems.scope.protos.EsdbgProtos.EvalResult;
import com.opera.core.systems.scope.protos.EsdbgProtos.ExamineList;
import com.opera.core.systems.scope.protos.EsdbgProtos.ObjectInfo.Property;
import com.opera.core.systems.scope.protos.EsdbgProtos.ObjectList;
import com.opera.core.systems.scope.protos.EsdbgProtos.ObjectValue;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeList;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeSelection;
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
	
	private int retries;
	private long sleepDuration;
	protected int activeWindowId;
	protected final IWindowManager windowManager;
	private String currentFramePath;

	private AtomicStampedReference<RuntimeInfo> runtime = new AtomicStampedReference<RuntimeInfo>(null, 0);
	
	private ConcurrentMap<Integer, RuntimeInfo> runtimesList = new ConcurrentHashMap<Integer, RuntimeInfo>();
	
	private RuntimeNode root;
	
	private static class RuntimeNode {
		private String frameName;
		private int runtimeID;
		
		private Map<Integer,RuntimeNode> nodes = new HashMap<Integer,RuntimeNode>();
		
		public String getFrameName() {
			return frameName;
		}

		public void setFrameName(String frameName) {
			this.frameName = frameName;
		}

		public int getRuntimeID() {
			return runtimeID;
		}

		public void setRuntimeID(int runtimeID) {
			this.runtimeID = runtimeID;
		}

		public Map<Integer,RuntimeNode> getNodes() {
			return nodes;
		}
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
	

	public EcmaScriptDebugger(ScopeServices services, String version) {	
		super(services, version);
		services.setDebugger(this);
		this.windowManager = services.getWindowManager();
		this.services = services;
		resetCounters();
		currentFramePath = "_top";
	}
	

	
	private List<RuntimeInfo> getRuntimesList() {
		int windowId = services.getWindowManager().getActiveWindowId();
		Iterator<?> iterator = (Iterator<?>) xpathIterator(runtimesList.values(), "/.[windowID='" + windowId + "']");
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
			runtimesList.put(info.getRuntimeID(), info);
		}

	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#scriptExecutor(java.lang.String, java.lang.Object)
	 */
	public Object scriptExecutor(String script, Object... params) {
		List<WebElement> elements = new ArrayList<WebElement>();
		
		String toSend = buildEvalString(elements, script, params);
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
        	return new ScriptResult(data.getObjectID(), data.getName());
        }
        else return parsed;
	}
	
	/**
	 * Build the script to send with arguments
	 * @param elements The web elements to send with the script as argument
	 * @param script The script to execute, can have references to argument(s)
	 * @param params Params to send with the script, will be parsed in to arguments
	 * @return The script to be sent to Eval command for execution
	 */
	private String buildEvalString(List<WebElement> elements, String script, Object... params) {
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
	
	private void recover() {
		windowManager.findDriverWindow();
		activeWindowId = windowManager.getActiveWindowId();
		updateRuntime();
	}
	
	protected Response eval(String using, int runtimeId, Variable...variables) {
		if (windowManager.getActiveWindowId() != activeWindowId) {
			recover();
		}
		
		EvalData.Builder builder = buildEval(using, runtimeId);
		builder.addAllVariableList(Arrays.asList(variables));
		
		Response response = executeCommand(ESDebuggerCommand.EVAL, builder);
		
		if(response == null && retries < OperaIntervals.SCRIPT_RETRY.getValue()) {
			retries++;
			sleepDuration += sleepDuration;
			sleep(sleepDuration);
			recover();
			return eval(using, variables);
		} else if(retries >= OperaIntervals.SCRIPT_RETRY.getValue()) {
			resetCounters();
			throw new WebDriverException("Internal error");
		}
		
		resetCounters();
		
        return response;

	}
	
	protected Response eval(String using, Variable... variables) {
		return eval(using, getRuntimeId(), variables);
	}
	
	private void resetCounters() {
		retries = 0;
		sleepDuration = OperaIntervals.SCRIPT_RETRY_INTERVAL.getValue();
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#executeScript(java.lang.String, boolean)
	 */	
	public Object executeScript(String using, boolean responseExpected) {
		return executeScript(using, responseExpected, getRuntimeId());
	}
	
	public Object executeScript(String using, boolean responseExpected, int runtimeId) {
		Response reply = eval(using, runtimeId);
		return responseExpected ? parseEvalReply(parseEvalData(reply)) : null;
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#getObject(java.lang.String)
	 */
	public Integer getObject(String using) {
		EvalResult reply = parseEvalData(eval(using));
		return (reply.getType().equals("object")) ? reply.getObjectValue().getObjectID() : null;
	}
	
	protected Integer getObject(String using, int runtimeId) {
		EvalResult reply = parseEvalData(eval(using, runtimeId));
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
		createAllRuntimes();
		RuntimeInfo runtime = (RuntimeInfo) xpathPointer(runtimesList.values(), "/.[htmlFramePath='" + currentFramePath + "' and windowID='" + windowId + "']").getValue();
		return runtime;
	}
	
	/**
	 * Updates the runtimes list to most recent version
	 * TODO this has to be kept up to date with events and
	 * as failover we should update
	 * It builds a tree to find the frame we are looking for
	 * TODO tree also must be kept up to date
	 */
	private void buildRuntimeTree() {
		updateRuntime();
		RuntimeInfo rootInfo = findRuntime();
		root = new RuntimeNode();
		root.setFrameName("_top");
		root.setRuntimeID(rootInfo.getRuntimeID());

		List<RuntimeInfo> runtimesInfos = new ArrayList<RuntimeInfo>(runtimesList.values());
		runtimesInfos.remove(rootInfo);
		
		for (RuntimeInfo runtimeInfo : runtimesInfos) {
			addNode(runtimeInfo, root);
		}
	}
	
	public void changeRuntime(int index) {
		buildRuntimeTree();
		
		RuntimeNode node = root.getNodes().get(index + 1);
		
		if(node == null) {
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
		
		for(int i = 0; i < values.length; i++) {
			curr = findNodeByName(values[i], curr);
		}
		
		if(curr == null) {
			throw new NoSuchFrameException("Invalid frame name " + frameName);
		}
		
		RuntimeInfo info = runtimesList.get(curr.getRuntimeID());
		currentFramePath = info.getHtmlFramePath();
		setRuntime(info);
	}
	
	
	private RuntimeNode findNodeByName(String name, RuntimeNode rootNode) {
		for (Entry<Integer,RuntimeNode> entry : rootNode.getNodes().entrySet()) {
			//check if the name is a number
			if(isNumber(name) && entry.getKey().equals(Integer.valueOf(name) + 1))
				return entry.getValue();
			//check if it is really the name
			else if(entry.getValue().getFrameName().equals(name))
				return entry.getValue();
			//last resort is id
			else if(executeScript("frameElement ? frameElement.id : ''", true, entry.getValue().getRuntimeID()).equals(name))
				return entry.getValue();
		}
		return null;
	}
	
	private boolean isNumber(String name) {
		boolean canParse = true;
		try {
			Integer.valueOf(name);
		} catch (NumberFormatException e) {
			canParse = false;
		}
		return canParse;
	}
	
	private void addNode(RuntimeInfo info, RuntimeNode root) {
		String[] values = info.getHtmlFramePath().split("/");
		RuntimeNode curr = root;
		//first frame is always _top, so we skip it
		for(int i = 1 ; i < values.length ; ++i) {
			int index = framePathToIndex(values[i]);
			if(curr.getNodes().get(index) == null) {
				//add to this node
				RuntimeNode node = new RuntimeNode();
				int end = values[i].indexOf('[');
				node.setFrameName(values[i].substring(0,end));
				curr.getNodes().put(index, node);
				curr = node;
			} else {
				curr = curr.getNodes().get(index);
			}
			//else we already know about this node, skip it
		}
		//last node gets the runtime id
		curr.setRuntimeID(info.getRuntimeID());
	}

	private int framePathToIndex(String framePath) {
		int begin = framePath.indexOf('[');
		int end = framePath.indexOf(']');
		return Integer.valueOf(framePath.substring(begin + 1, end));
	}


	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#cleanUpRuntimes(int)
	 */
	public void cleanUpRuntimes(int windowId) {
		// if we already have a runtime listed as _top with that window id,
		// clean all runtimes with that window id
		//if (findRuntime() != null) {
			for (RuntimeInfo runtime : runtimesList.values()) {
				if (runtime.getWindowID() == windowId) {
					runtimesList.remove(runtime.getRuntimeID());
				}
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

	public void readyStateChanged(ReadyStateChange change) {
		//not fired, silently ignore
		
	}

	public void releaseObject(int objectId) {
		//not supported, silently ignore
	}

	public void resetFramePath() {
		currentFramePath = "_top";
		setRuntime(findRuntime());
	}
	
}
