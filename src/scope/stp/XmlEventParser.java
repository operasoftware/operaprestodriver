package com.opera.core.systems.scope.stp;

import java.util.HashMap;
import java.util.Map;

import com.opera.core.systems.model.ICommand;
import com.opera.core.systems.scope.beans.ActiveWindow;
import com.opera.core.systems.scope.beans.Message;
import com.opera.core.systems.scope.beans.RuntimeStarted;
import com.opera.core.systems.scope.beans.RuntimeStopped;
import com.opera.core.systems.scope.beans.UpdatedWindow;
import com.opera.core.systems.scope.beans.WindowClosed;
import com.opera.core.systems.scope.handlers.AbstractEventHandler;
import com.opera.core.systems.scope.internal.Parser;
import com.opera.core.systems.scope.services.ums.EcmaScriptDebugger.EsdbgCommand;
import com.opera.core.systems.scope.services.ums.WindowManager.WmCommand;

public class XmlEventParser {
	
	private final Parser parser;
	private AbstractEventHandler eventHandler;
	private Map<String, Map<String, ICommand>> events;
	
	public XmlEventParser(AbstractEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		parser = new Parser();
		events = new HashMap<String, Map<String, ICommand>>();
		
		initEvents();
	}
	
	private void initEvents() {
		Map<String, ICommand> ecmascriptEvents = new HashMap<String,ICommand>();
		
		ecmascriptEvents.put("<thread-started>", EsdbgCommand.THREAD_STARTED);
		ecmascriptEvents.put("<thread-finished>", EsdbgCommand.THREAD_FINISHED);
		ecmascriptEvents.put("<new-script>", EsdbgCommand.NEW_SCRIPT);
		ecmascriptEvents.put("<runtime-started>", EsdbgCommand.RUNTIME_STARTED);
		ecmascriptEvents.put("<runtime-stopped>", EsdbgCommand.RUNTIME_STOPPED);
		ecmascriptEvents.put("<hello>", EsdbgCommand.DEFAULT); //doesnt exist in STP/0
		events.put("ecmascript-debugger", ecmascriptEvents);
		
		//not used for now
		//Map<String, ICommand> httpLoggerEvents = new HashMap<String,EsdbgCommand>();
		Map<String, ICommand> windowManagerEvents = new HashMap<String,ICommand>();
		windowManagerEvents.put("<window-loaded>", WmCommand.WINDOW_LOADED);
		windowManagerEvents.put("<updated-window>", WmCommand.WINDOW_UPDATED);
		windowManagerEvents.put("<active-window>", WmCommand.WINDOW_ACTIVATED);
		windowManagerEvents.put("<window-closed>", WmCommand.WINDOW_CLOSED);
		events.put("window-manager", windowManagerEvents);
	}
	
	public boolean handleIfEvent(String message) {
		int serviceNameEndIndex = message.indexOf(' ');
		
		String service = message.substring(0, serviceNameEndIndex);
		int firstNodeIndex = message.indexOf("><") + 1;
		int nodeEndIndex = message.indexOf('>', firstNodeIndex);
		String key = message.substring(firstNodeIndex,nodeEndIndex + 1);
		
		Map<String, ICommand> commands = events.get(service);
		
		if(commands == null)
			return false;
		
		ICommand command = commands.get(key);
		
		if(command == null)
			return false;
		
		if(service.equals("ecmascript-debugger")) {
			switch (EsdbgCommand.get(command.getCommandID())) {
			case RUNTIME_STARTED:
				RuntimeStarted started = parser.parseResponse(RuntimeStarted.class, message);
				eventHandler.onRuntimeStarted(started);
				break;
			case RUNTIME_STOPPED:
				RuntimeStopped stopped = parser.parseResponse(RuntimeStopped.class, message);
				eventHandler.onRuntimeStopped(stopped.getRuntimeId());
				break;
			case DEFAULT:
				break;
			}
		} else if(service.equals("window-manager")) {
			switch (WmCommand.get(command.getCommandID())) {
			case WINDOW_ACTIVATED:
				ActiveWindow window = parser.parseResponse(ActiveWindow.class, message);
				eventHandler.onActiveWindow(window.getWindowId());
				break;
			case WINDOW_CLOSED:
				WindowClosed closed = parser.parseResponse(WindowClosed.class, message);
				eventHandler.onWindowClosed(closed.getWindowId());
				break;
			case WINDOW_UPDATED:
				UpdatedWindow updatedWindow = parser.parseResponse(UpdatedWindow.class, message);
				eventHandler.onUpdatedWindow(updatedWindow);
				break;
			case WINDOW_LOADED:
				eventHandler.onWindowLoaded();
				break;
			} 
		} else if (service.equals("console-logger")) {
			eventHandler.onMessage(parser.parseResponse(Message.class, message));
		}
		
		/*
		 HttpResponse response = (HttpResponse) parser.parseResponse(message);		
		 eventHandler.onHttpResponse(eventHandler.parseHeader(response.getHeader().getContent().toString()));
		 */
		return true;
	}
}
