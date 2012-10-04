package com.opera.core.systems.scope.stp.services.messages;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import com.opera.core.systems.scope.Message;
import com.opera.core.systems.scope.services.ConsoleLogger;

import java.util.Map;

public enum ConsoleLoggerMessage implements Message {

  ON_CONSOLE_MESSAGE(1),
  CLEAR(2),
  LIST_MESSAGES(3),

  DEFAULT(-1);

  private static final Map<Integer, ConsoleLoggerMessage> lookup =
      Maps.uniqueIndex(ImmutableList.copyOf(values()),
                       new Function<ConsoleLoggerMessage, Integer>() {
                         public Integer apply(ConsoleLoggerMessage message) {
                           return message.getID();
                         }
                       });

  private final int code;

  ConsoleLoggerMessage(int code) {
    this.code = code;
  }

  public int getID() {
    return code;
  }

  public String getServiceName() {
    return ConsoleLogger.SERVICE_NAME;
  }

  public static ConsoleLoggerMessage get(int code) {
    ConsoleLoggerMessage message = lookup.get(code);
    return (message != null) ? message : DEFAULT;
  }

}