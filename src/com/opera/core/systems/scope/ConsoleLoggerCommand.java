package com.opera.core.systems.scope;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import com.opera.core.systems.model.ICommand;

import java.util.Map;

public enum ConsoleLoggerCommand implements ICommand {

  ON_CONSOLE_MESSAGE(1),
  CLEAR(2),
  LIST_MESSAGES(3),

  DEFAULT(-1);

  private static final Map<Integer, ConsoleLoggerCommand> lookup = Maps.uniqueIndex(
      ImmutableList.copyOf(values()),
      new Function<ConsoleLoggerCommand, Integer>() {
        public Integer apply(ConsoleLoggerCommand command) {
          return command.getCommandID();
        }
      });

  private final int code;

  ConsoleLoggerCommand(int code) {
    this.code = code;
  }

  public int getCommandID() {
    return code;
  }

  public String getServiceName() {
    return "console-logger";
  }

  public static ConsoleLoggerCommand get(int code) {
    ConsoleLoggerCommand command = lookup.get(code);
    return (command != null) ? command : DEFAULT;
  }

}