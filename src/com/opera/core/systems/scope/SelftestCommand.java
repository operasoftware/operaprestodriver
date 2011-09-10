package com.opera.core.systems.scope;

import java.util.EnumSet;
import java.util.HashMap;

import com.opera.core.systems.model.ICommand;

public enum SelftestCommand implements ICommand {
  // command,     event,     event
  RUN_MODULES(1), OUTPUT(2), FINISHED(3);

  private int command;
  private static HashMap<Integer, SelftestCommand> lookup = new HashMap<Integer, SelftestCommand>();

  static {
    for (SelftestCommand command : EnumSet.allOf(SelftestCommand.class)) {
      lookup.put(command.getCommandID(), command);
    }
  }

  private SelftestCommand(int command) {
    this.command = command;
  }

  public int getCommandID() {
    return command;
  }

  public String getServiceName() {
    return "selftest";
  }

  public static SelftestCommand get(int id) {
    return lookup.get(id);
  }
}
