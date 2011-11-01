package com.opera.core.systems.arguments;

import com.opera.core.systems.OperaArguments;

/**
 * Handles a list of strings/arguments for the Opera core binary.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class OperaCoreArguments extends OperaArguments implements
                                com.opera.core.systems.arguments.interfaces.OperaArguments {

  public OperaCoreArguments() {
    // TODO(andreastt): gogi doesn't support -pd, and for some reason this must be sent last
    //OperaArgument argument = new OperaArgument("autotestmode");
    //super.arguments.add(argument);
  }

  public static OperaArguments parse(String string) {
    return OperaArguments.parse(string);
  }

}