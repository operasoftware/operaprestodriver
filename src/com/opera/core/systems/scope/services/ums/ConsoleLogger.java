package com.opera.core.systems.scope.services.ums;

import com.google.common.collect.Lists;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.ConsoleLoggerCommand;
import com.opera.core.systems.scope.internal.ServiceCallback;
import com.opera.core.systems.scope.protos.ConsoleLoggerProtos.ConsoleMessage;
import com.opera.core.systems.scope.protos.ConsoleLoggerProtos.ConsoleMessageList;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.IConsoleLogger;

import java.util.List;

public class ConsoleLogger extends AbstractService implements IConsoleLogger {

  private final List<ConsoleMessage> messages = Lists.newArrayList();
  private ServiceCallback<ConsoleMessage> callback = null;

  public ConsoleLogger(ScopeServices services, String version) {
    super(services, version);
    services.setConsoleLogger(this);
  }

  public void init() {
  }

  public void addMessage(ConsoleMessage message) {
    messages.add(message);

    if (callback != null) {
      callback.call(message);
    }
  }

  public void clear() {
    executeCommand(ConsoleLoggerCommand.CLEAR);
  }

  public List<ConsoleMessage> listMessages() {
    Response response = executeCommand(ConsoleLoggerCommand.LIST_MESSAGES);
    ConsoleMessageList.Builder builder = ConsoleMessageList.newBuilder();
    buildPayload(response, builder);
    return builder.build().getConsoleMessageListList();
  }

  public void onConsoleMessage(ServiceCallback<ConsoleMessage> callback) {
    this.callback = callback;
  }

}