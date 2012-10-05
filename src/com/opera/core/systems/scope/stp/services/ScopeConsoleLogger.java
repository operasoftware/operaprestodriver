/*
Copyright 2012 Opera Software ASA

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