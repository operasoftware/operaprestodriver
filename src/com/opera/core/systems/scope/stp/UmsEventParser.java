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
package com.opera.core.systems.scope.stp;

import org.openqa.selenium.WebDriverException;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.opera.core.systems.scope.handlers.AbstractEventHandler;
import com.opera.core.systems.scope.protos.ConsoleLoggerProtos.ConsoleMessage;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuID;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuInfo;
import com.opera.core.systems.scope.protos.EcmascriptProtos.ReadyStateChange;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeID;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo;
import com.opera.core.systems.scope.protos.HttpLoggerProtos.Header;
import com.opera.core.systems.scope.protos.UmsProtos.Event;
import com.opera.core.systems.scope.protos.WmProtos.WindowID;
import com.opera.core.systems.scope.protos.WmProtos.WindowInfo;
import com.opera.core.systems.scope.CoreCommand;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;
import com.opera.core.systems.scope.ESDebuggerCommand;
import com.opera.core.systems.scope.WindowManagerCommand;
import com.opera.core.systems.scope.DesktopWindowManagerCommand;

public class UmsEventParser {

  private AbstractEventHandler eventHandler;

  public UmsEventParser(AbstractEventHandler eventHandler) {
    this.eventHandler = eventHandler;
  }

  public void handleEvent(Event event) {
    String service = event.getService();
    int eventId = event.getCommandID();

    if (service.equals("ecmascript-debugger")) {
      switch (ESDebuggerCommand.get(eventId)) {
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
    } else if (service.equals("window-manager")) {
      switch (WindowManagerCommand.get(eventId)) {
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
        WindowID.Builder loadedWindowBuilder = WindowID.newBuilder();
        buildPayload(event, loadedWindowBuilder);
        Integer loadedWindowID = loadedWindowBuilder.build().getWindowID();
        eventHandler.onWindowLoaded(loadedWindowID.intValue());
        break;
      default:
        break;
      }
    } else if (service.equals("desktop-window-manager")) {
      switch (DesktopWindowManagerCommand.get(eventId)) {
      case WINDOW_SHOWN:
        DesktopWindowInfo.Builder shownDWBuilder = DesktopWindowInfo.newBuilder();
        buildPayload(event, shownDWBuilder);
        DesktopWindowInfo info_shown = shownDWBuilder.build();
        eventHandler.onDesktopWindowShown(info_shown);
        break;
      case WINDOW_UPDATED:
        DesktopWindowInfo.Builder updatedDWBuilder = DesktopWindowInfo.newBuilder();
        buildPayload(event, updatedDWBuilder);
        DesktopWindowInfo info_updated = updatedDWBuilder.build();
        eventHandler.onDesktopWindowUpdated(info_updated);
        break;
      case WINDOW_ACTIVATED:
        DesktopWindowInfo.Builder activatedDWBuilder = DesktopWindowInfo.newBuilder();
        buildPayload(event, activatedDWBuilder);
        DesktopWindowInfo info_activated = activatedDWBuilder.build();
        eventHandler.onDesktopWindowActivated(info_activated);
        break;
      case WINDOW_CLOSED:
        DesktopWindowInfo.Builder closedDWBuilder = DesktopWindowInfo.newBuilder();
        buildPayload(event, closedDWBuilder);
        DesktopWindowInfo info_closed = closedDWBuilder.build();
        eventHandler.onDesktopWindowClosed(info_closed);
        break;
      case WINDOW_LOADED:
        DesktopWindowInfo.Builder loadedDWBuilder = DesktopWindowInfo.newBuilder();
        buildPayload(event, loadedDWBuilder);
        DesktopWindowInfo info_loaded = loadedDWBuilder.build();
        eventHandler.onDesktopWindowLoaded(info_loaded);
        break;
      case MENU_SHOWN:
    	QuickMenuInfo.Builder shownQMBuilder = QuickMenuInfo.newBuilder(); 
    	buildPayload(event, shownQMBuilder);
    	QuickMenuInfo menuInfoShown = shownQMBuilder.build();
    	eventHandler.onQuickMenuShown(menuInfoShown);
    	break;
      case MENU_CLOSED:
    	QuickMenuID.Builder closedQMBuilder = QuickMenuID.newBuilder(); 
      	buildPayload(event, closedQMBuilder);
      	QuickMenuID menuId = closedQMBuilder.build();
      	System.out.println("eventParser On Quick Menu Closed");
      	eventHandler.onQuickMenuClosed(menuId);
      	break;
      default:
        break;
      }
    } else if (service.equals("console-logger")) {
      // console logger only sends one message
      ConsoleMessage.Builder messageBuilder = ConsoleMessage.newBuilder();
      buildPayload(event, messageBuilder);
      eventHandler.onMessage(messageBuilder.build());
    } else if (service.equals("ecmascript")) {
      // we have only one event for this one
      ReadyStateChange.Builder builder = ReadyStateChange.newBuilder();
      buildPayload(event, builder);
      eventHandler.onReadyStateChange(builder.build());
    } else if (service.equals("http-logger") && eventId == 1) {
      // console logger only sends one message
      // FIXME make generic
      Header header;
      try {
        header = Header.parseFrom(event.getPayload());
      } catch (InvalidProtocolBufferException e) {
        throw new WebDriverException("Exception while parsing event");
      }
      eventHandler.onRequest(header.getWindowID());
    } else if (service.equals("core")) {
      switch (CoreCommand.get(eventId)) {
      case ONACTIVE:
        // No active event handler...
        // Opera only becomes active as reaction on other event
        // sent from operadriver, so we already know.
        break;
      case ONIDLE:
        eventHandler.onOperaIdle();
        break;
      }
    }
  }

  private final GeneratedMessage.Builder<?> buildPayload(Event event,
      GeneratedMessage.Builder<?> builder) {
    return buildMessage(builder, event.getPayload().toByteArray());
  }

  private final GeneratedMessage.Builder<?> buildMessage(
      GeneratedMessage.Builder<?> builder, byte[] message) {
    try {
      return builder.mergeFrom(message);
    } catch (InvalidProtocolBufferException ex) {
      throw new WebDriverException("Could not build "
          + builder.getDescriptorForType().getFullName() + " : "
          + ex.getMessage());
    }
  }
}
