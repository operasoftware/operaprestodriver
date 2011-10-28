package com.opera.core.systems.arguments.interfaces;

import com.opera.core.systems.arguments.OperaArgument;

import java.util.List;

/**
 * Defines a joint interface for interacting with arguments to various kinds of Operas.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 * @see com.opera.core.systems.arguments.OperaDesktopArguments , com.opera.core.systems.runner.arguments.OperaCoreArguments
 */
public interface OperaArguments {

  public void add(String argument);

  public void add(String argument, String value);

  public void add(OperaArgument argument);

  public List<OperaArgument> getArguments();

  public List<String> getArgumentsAsStrings();

  public void merge(OperaArguments extraArguments);

  public String sign();

}