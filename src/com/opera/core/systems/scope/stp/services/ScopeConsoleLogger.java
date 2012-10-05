package com.opera.core.systems.scope.stp.services;

import com.google.common.collect.Lists;

import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.ScopeServices;
import com.opera.core.systems.scope.internal.ServiceCallback;
import com.opera.core.systems.scope.protos.ConsoleLoggerProtos.ConsoleMessage;
import com.opera.core.systems.scope.protos.ConsoleLoggerProtos.ConsoleMessageList;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.ConsoleLogger;
import com.opera.core.systems.scope.stp.services.messages.ConsoleLoggerMessage;

import java.util.List;

public class ScopeConsoleLogger extends AbstractService implements ConsoleLogger {

  private final List<ConsoleMessage> messages = Lists.newArrayList();
  private ServiceCallback<ConsoleMessage> callback = null;

  public ScopeConsoleLogger(ScopeServices services) {
    super(services, SERVICE_NAME);
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
    executeMessage(ConsoleLoggerMessage.CLEAR);
  }

  public List<ConsoleMessage> listMessages() {
    Response response = executeMessage(ConsoleLoggerMessage.LIST_MESSAGES);
    ConsoleMessageList.Builder builder = ConsoleMessageList.newBuilder();
    buildPayload(response, builder);
    return builder.build().getConsoleMessageListList();
  }

  public void onConsoleMessage(ServiceCallback<ConsoleMessage> callback) {
    this.callback = callback;
  }

}