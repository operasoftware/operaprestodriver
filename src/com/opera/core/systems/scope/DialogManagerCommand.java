package com.opera.core.systems.scope;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.opera.core.systems.model.ICommand;

public enum DialogManagerCommand implements ICommand {
  LIST_DIALOGS(1), TYPE(2), CLICK(3), SELECT(4),
  // Events
  ON_DIALOG_CREATED(5), ON_DIALOG_CLOSED(6),
  DEFAULT(-1);

  private int code;
  private static final Map<Integer, DialogManagerCommand> lookup = new HashMap<Integer, DialogManagerCommand>();

  static {
    for (DialogManagerCommand command : EnumSet.allOf(DialogManagerCommand.class))
      lookup.put(command.getCommandID(), command);
  }

  private DialogManagerCommand(int code) {
    this.code = code;
  }

  public int getCommandID() {
    return code;
  }

  public String getServiceName() {
    return "dialog-manager";
  }

  public static DialogManagerCommand get(int code) {
    DialogManagerCommand command = lookup.get(code);
    return (command != null) ? command : DEFAULT;
  }

}
