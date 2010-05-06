/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.scope.services.xml;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
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
import com.opera.core.systems.scope.beans.CreateAllRuntimes;
import com.opera.core.systems.scope.beans.Eval;
import com.opera.core.systems.scope.beans.EvalReply;
import com.opera.core.systems.scope.beans.ExamineObjects;
import com.opera.core.systems.scope.beans.ExamineReply;
import com.opera.core.systems.scope.beans.No;
import com.opera.core.systems.scope.beans.Property;
import com.opera.core.systems.scope.beans.Runtime;
import com.opera.core.systems.scope.beans.Runtimes;
import com.opera.core.systems.scope.beans.RuntimesReply;
import com.opera.core.systems.scope.beans.SetConfiguration;
import com.opera.core.systems.scope.beans.StopAt;
import com.opera.core.systems.scope.beans.ValueData;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.internal.Parser;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IWindowManager;

/**
 * Manages the ecmascript-debugger service
 * Handles runtime management and script injection
 * @author Deniz Turkoglu
 *
 */
public class EcmaScriptDebugger extends AbstractService implements IEcmaScriptDebugger {
	
	private int tag;
	private AtomicStampedReference<Runtime> runtime = new AtomicStampedReference<Runtime>(null, 0);

	private int retries;
	private Parser parser;
	private long sleepDuration;
	private int activeWindowId;
	private final IWindowManager windowManager;

	private Map<Integer, Runtime> runtimesList = new ConcurrentHashMap<Integer, Runtime>();
	
	public int getRuntimeId() {
		return runtime.getStamp();
	}

	public void setRuntime(Runtime runtime) {
		this.runtime.set(runtime, runtime.getRuntimeId());
		this.activeWindowId = runtime.getWindowId();
	}
	

	public void addRuntime(Runtime started) {
		runtimesList.put(started.getRuntimeId(), started);
	}
	
	public void removeRuntime(int runtimeId) {
		runtimesList.remove(runtimeId);
	}
	
	public void addRuntime(RuntimeInfo info) {
		throw new UnsupportedOperationException("Not supported in STP/0");
	}

	public void setRuntime(RuntimeInfo runtime) {
		throw new UnsupportedOperationException("Not supported in STP/0");
	}

	public EcmaScriptDebugger(ScopeServices services, String serviceName) {
		super(services, serviceName, "1.0");
		services.setDebugger(this);
		this.parser = services.getParser();
		this.services = services;
		this.windowManager = services.getWindowManager();
		resetCounters();
	}
	
	//FIXME should only return for active window
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#getRuntimesList()
	 */
	private List<Runtime> getRuntimesList() {
		int windowId = windowManager.getActiveWindowId();
		Iterator<?> iterator = (Iterator<?>) xpathIterator(runtimesList.values(), ".[windowId='" + windowId + "']");
		List<Runtime> runtimes = new ArrayList<Runtime>();
		while (iterator.hasNext()) {
			runtimes.add((Runtime) ((Pointer)iterator.next()).getNode());
		}
		return runtimes;
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#init()
	 */
	public void init() {
		SetConfiguration configuration = new SetConfiguration();
		
		String[] stopTypes = {"script", "exception", "error", "abort"};
		
		for (String stopType : stopTypes) {
			StopAt stopAt = new StopAt();
			stopAt.setNo(new No());
			stopAt.setStopType(stopType);
			configuration.getStopAt().add(stopAt);
		}
		
		this.postRequest(parser.createRequest(configuration, SetConfiguration.class));
		
		if(!updateRuntime())
			throw new WebDriverException("Could not find a runtime for script injection");
		//FIXME workaround for internal dialog
		executeJavascript("return 1;", true);
	}

	/**
	 * Gets a list of runtimes and keeps the list,
	 * create runtimes for all pages so even if the pages dont
	 * have script we can still inject to a 'fake' runtime
	 */
	private void createAllRuntimes(){
		Runtimes runtimes = new Runtimes();
		runtimes.setTag(++tag);
		runtimes.setCreateAllRuntimes(new CreateAllRuntimes());
		
		this.postRequest(parser.createRequest(runtimes, Runtimes.class));
				
		String response = waitForResponse("<runtimes-reply>");
		RuntimesReply result = parser.parseResponse(RuntimesReply.class, response);
		
		runtimesList.clear();
		List<Runtime> allRuntimes = result.getRuntime();
		for (Runtime runtime : allRuntimes) {
			runtimesList.put(runtime.getRuntimeId(), runtime);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#scriptExecutor(java.lang.String, java.lang.Object)
	 */
	public Object scriptExecutor(String script, Object... params) {
		String toSend;
		List<WebElement> elements = new ArrayList<WebElement>();
		
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
		
		Eval eval = buildEval(toSend);
		
		for (WebElement webElement : elements) {
			Property property =  buildProperty(webElement.toString(), ((OperaWebElement) webElement).getObjectId());
			eval.getProperty().add(property);
		}
		
		this.postRequest(parser.createRequest(eval, Eval.class));

		String evalResponse = waitForResponse("<eval-reply><tag>" + tag + "</tag>");

		//FIXME retries
		if(evalResponse.startsWith("<error>Internal error</error>", messageOffset)) {
			throw new WebDriverException("Internal error");
		}
		
        EvalReply result = parser.parseResponse(EvalReply.class, evalResponse);
        Object response = parseEvalReply(result);
        if(response instanceof ValueData) {
        	ValueData data = (ValueData) response;
        	return new ScriptResult(data.getObjectValue().getObjectId(), data.getObjectValue().getClassName());
        }
        else return response;
	}
	
	private void processArgument(Object object, StringBuilder builder, List<WebElement> elements) {
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
	
	private final Eval buildEval(String using) {
		Eval eval = new Eval();
		eval.setTag(++tag);
		eval.setRuntimeId(getRuntimeId());
		eval.setScriptData(using);
		return eval;
	}
	
	private final Property buildProperty(String name, int objectId) {
		Property property =  new Property();
		property.setPropertyName(name);
		ValueData data = new ValueData();
		data.setDataType("object-id");
		data.setObjectId(objectId);
		property.setValueData(data);
		return property;
	}
	
	private EvalReply eval(String using, boolean responseExpected, Property... properties) {
		//TODO : remove
		if(windowManager.getActiveWindowId() != activeWindowId) {
			windowManager.filterActiveWindow();
			createAllRuntimes();
			findRuntime();
		}
		
		Eval eval = buildEval(using);
		
		for (Property property : properties) {
			eval.getProperty().add(property);
		}
		
		this.postRequest(parser.createRequest(eval, Eval.class));

		String evalResponse = waitForResponse("<eval-reply><tag>" + tag + "</tag>");
		
		if(evalResponse.startsWith("<error>Internal error</error>", messageOffset) && retries < OperaIntervals.SCRIPT_RETRY.getValue()) {
			retries++;
			sleepDuration += sleepDuration;
			sleep(sleepDuration);
			return eval(using, responseExpected, properties);
		} else if(retries >= OperaIntervals.SCRIPT_RETRY.getValue()) {
			resetCounters();
			throw new WebDriverException("Internal error");
		}
		
		resetCounters();
		
        return responseExpected ? parser.parseResponse(EvalReply.class, evalResponse) : null;

	}
	
	private void resetCounters() {
		retries = 0;
		sleepDuration = OperaIntervals.SCRIPT_RETRY_INTERVAL.getValue();
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#executeScript(java.lang.String, boolean)
	 */
	public Object executeScript(String using, boolean responseExpected) {
		EvalReply reply = eval(using, responseExpected);
		return responseExpected ? parseEvalReply(reply) : null;
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#getObject(java.lang.String)
	 */
	public Integer getObject(String using) {
		EvalReply reply = eval(using, true);
		return (reply.getValueData().getDataType().equals("object")) ? reply.getValueData().getObjectValue().getObjectId() : null;
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#callFunctionOnObject(java.lang.String, int)
	 */
	public String callFunctionOnObject(String using, int objectId) {
		Property property = buildProperty("locator", objectId);

		EvalReply reply = eval(using, true, property);
		String dataType = reply.getValueData().getDataType();
		return (dataType.equals("null") || dataType.equals("undefined"))? null : reply.getValueData().getString().getContent();
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#callFunctionOnObject(java.lang.String, int, boolean)
	 */
	public Object callFunctionOnObject(String using, int objectId, boolean responseExpected) {
		Property property = buildProperty("locator", objectId);

		EvalReply reply = eval(using, responseExpected, property);
		return responseExpected ? parseEvalReply(reply) : null;
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#executeScriptOnObject(java.lang.String, int)
	 */
	public Integer executeScriptOnObject(String using, int objectId) {
		Property property = buildProperty("locator", objectId);

		EvalReply reply = eval(using, true, property);
		Object object = parseEvalReply(reply);
		if(object == null || !(object instanceof ValueData))
			return null;
		return ((ValueData) object).getObjectValue().getObjectId();
	}
    
    
	/**
	 * Parses a reply and returns the following types
	 * String presentation of number, boolean or string
	 * FIXME investigate why JAXB doesnt create enums
	 * @param reply
	 * @return
	 */
	private Object parseEvalReply(EvalReply reply) {
		
		String status = reply.getStatus();
		
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

		String dataType = reply.getValueData().getDataType();
		
        if (dataType.equals("string")) {
            return reply.getValueData().getString().getContent();
        } else if (dataType.equals("number")) {
            return parseNumber(reply.getValueData().getString().getContent());
        } else if (dataType.equals("boolean")) {
            return Boolean.valueOf(reply.getValueData().getString().getContent());
        } else if (dataType.equals("undefined")) {
            return null;
        } else if (dataType.equals("object")) {
            return reply.getValueData();
        }
        //return null if none
        return null;
	}
	
	private Object parseNumber(String value) {
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
	private Runtime findRuntime() {	
		int windowId = windowManager.getActiveWindowId();
		Runtime runtime = (Runtime) xpathPointer(runtimesList.values(), "//.[htmlFramePath='_top' and windowId='" + windowId + "']").getValue();
		return runtime;
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#changeRuntime(java.lang.String)
	 */
	public void changeRuntime(String framePath){
		
		int windowId = windowManager.getActiveWindowId();
		Runtime runtime;
		//TODO review :  get nodes and then from the nodes find the one that starts with second frame
		//FIXME use runtimes instead of runtime ID
		createAllRuntimes();
		if(framePath.indexOf('.') > -1) {
			//we have a nested frame request
			String[] frames = framePath.split("\\.");
			//get the relative pointer we can start from
			
			JXPathContext pathContext = JXPathContext.newContext(runtimesList.values());
			Pointer result = findPointer(pathContext, "//.[htmlFramePath[starts-with(.,'_top/"+ frames[0] + "')] and windowId='" + windowId + "']", framePath);	
			JXPathContext relativeContext = pathContext.getRelativeContext(result);
			
			for (int i = 1; i < frames.length; i++) {
				String path = (String) relativeContext.getValue("htmlFramePath");
				result = findPointer(pathContext, "//.[htmlFramePath[starts-with(.,'"+ path + "/" + frames[i] +"')] and windowId='" + windowId + "']", path);
				
				relativeContext = pathContext.getRelativeContext(result);
				setRuntime(runtimesList.get((Integer) relativeContext.getValue("runtimeId")));
			}
		} else {
			framePath = framePath.isEmpty() ? "" : "/" + framePath;
			runtime = (Runtime) xpathPointer(runtimesList.values(), "//.[htmlFramePath[starts-with(.,'_top"+ framePath + "')] and windowId='" + windowId + "']").getValue();
			if(runtime == null) {
				throw new NoSuchFrameException("Frame with name " + framePath + " not found");
			}
			setRuntime(runtime);
		}

	}
	
	
	private Pointer findPointer(JXPathContext context, String query, String framePath) {
		Pointer result = context.getPointer(query);
		if(result instanceof NullPointer)
			throw new NoSuchFrameException("Frame with name " + framePath + " not found");
		return result;
	}

	public void changeRuntime(int index) {
		if(index < 0 || index >= runtimesList.size()) {
			throw new NoSuchFrameException("Invalid frame index " + index);
		}
		
		List<Runtime> runtimes = getRuntimesList();
		Runtime frame = null;
		
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
			for (Runtime runtime : runtimesList.values()) {
				if (runtime.getWindowId() == windowId)
					runtimesList.remove(runtime.getRuntimeId());
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
		ExamineObjects examine = new ExamineObjects();
		examine.setTag(++tag);
		examine.setRuntimeId(getRuntimeId());
		examine.getObjectId().add(id);
		postRequest(parser.createRequest(examine, ExamineObjects.class));
		
		String response = waitForResponse("<examine-reply><tag>" + tag + "</tag>");
		ExamineReply reply = parser.parseResponse(ExamineReply.class, response);
		
		List<Integer> ids = new ArrayList<Integer>();
		List<Property> properties = reply.getObject().get(0).getProperty();
		for (Property property : properties) {
			if(property.getValueData().getObjectValue() != null)
				ids.add(property.getValueData().getObjectValue().getObjectId());
		}

		return ids;
		
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IEcmaScriptDebugger#listFramePaths()
	 */
	public List<String> listFramePaths() {
		List<Runtime> runtimes = getRuntimesList();
		List<String> frameNames = new ArrayList<String>();
		for (Runtime runtime : runtimes) {
			frameNames.add(runtime.getHtmlFramePath());
		}
		return frameNames;
	}

	public void releaseObjects() {
		//this method is not supported in STP/0, ignore silently
	}

	public boolean updateRuntime() {
		createAllRuntimes();
		Runtime activeRuntime = findRuntime();
		if(activeRuntime != null) {
			setRuntime(activeRuntime);
			return true;
		}
		return false;
	}
	
	public void resetRuntimesList() {
		runtimesList.clear();
	}
	
}
