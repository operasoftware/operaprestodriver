package com.opera.core.systems.scope.stp;

import org.openqa.selenium.WebDriverException;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.opera.core.systems.scope.handlers.AbstractEventHandler;
import com.opera.core.systems.scope.protos.ConsoleLoggerProtos.ConsoleMessage;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeID;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo;
import com.opera.core.systems.scope.protos.UmsProtos.Event;
import com.opera.core.systems.scope.protos.WmProtos.WindowID;
import com.opera.core.systems.scope.protos.WmProtos.WindowInfo;
import com.opera.core.systems.scope.services.ums.EcmaScriptDebugger.EsdbgCommand;
import com.opera.core.systems.scope.services.ums.WindowManager.WmCommand;

public class UmsEventParser {
	
	private AbstractEventHandler eventHandler;

	public UmsEventParser(AbstractEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}
	
	public void handleEvent(Event event) {
		String service = event.getService();
		int eventId = event.getCommandID();
		
		if(service.equals("ecmascript-debugger")) {
			switch (EsdbgCommand.get(eventId)) {
			case RUNTIME_STARTED:
				RuntimeInfo.Builder infoBuilder = RuntimeInfo.newBuilder();
				buildPayload(event, infoBuilder);
				eventHandler.onRuntimeStarted(infoBuilder.build());
				break;
			case RUNTIME_STOPPED:
				RuntimeID.Builder idBuilder = RuntimeID.newBuilder();
				buildPayload(event, idBuilder);
				Integer id = idBuilder.build().getRuntimeID();
				eventHandler.onRuntimeStopped(id);
				break;
			default:
				break;
			}
		} else if(service.equals("window-manager")) {
			switch (WmCommand.get(eventId)) {
			case WINDOW_ACTIVATED:
				WindowID.Builder activeWindowIdBuilder = WindowID.newBuilder();
				buildPayload(event, activeWindowIdBuilder);
				Integer activeWindowID = activeWindowIdBuilder.build().getWindowID();
				eventHandler.onActiveWindow(activeWindowID);
				break;
			case WINDOW_CLOSED:
				WindowID.Builder closedWindowBuilder = WindowID.newBuilder();
				buildPayload(event, closedWindowBuilder);
				Integer closedWindowID = closedWindowBuilder.build().getWindowID();
				eventHandler.onWindowClosed(closedWindowID);
				break;
			case WINDOW_UPDATED:
				WindowInfo.Builder windowInfoBuilder = WindowInfo.newBuilder();
				buildPayload(event, windowInfoBuilder);
				WindowInfo info = windowInfoBuilder.build();
				eventHandler.onUpdatedWindow(info);
				break;
			case WINDOW_LOADED:
				eventHandler.onWindowLoaded();
				break;
			default:
				break;
			}
		}
		else if(service.equals("console-logger")) {
			//console logger only sends one message
			ConsoleMessage.Builder messageBuilder = ConsoleMessage.newBuilder();
			buildPayload(event, messageBuilder);
			eventHandler.onMessage(messageBuilder.build());
		} /*else if(service.equals("http-logger") && eventId == 2) {
			//console logger only sends one message
			//FIXME make generic
			Header header;
			try {
				header = Header.parseFrom(event.getPayload());
			} catch (InvalidProtocolBufferException e) {
				throw new WebDriverException("Exception while parsing event");
			}
			eventHandler.onHttpResponse(eventHandler.parseHeader(header.getHeader()));
		}
		*/
		
	}
	
	private final GeneratedMessage.Builder<?> buildPayload(Event event, GeneratedMessage.Builder<?> builder) {
		return buildMessage(builder, event.getPayload().toByteArray());
	}
	
	private final GeneratedMessage.Builder<?> buildMessage(GeneratedMessage.Builder<?> builder, byte[] message) {
		try {
			return builder.mergeFrom(message);
		} catch (InvalidProtocolBufferException ex) {
			throw new WebDriverException("Could not build " + builder.getDescriptorForType().getFullName() +  " : " + ex.getMessage());
		}
	}


}
