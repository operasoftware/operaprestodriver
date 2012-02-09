/*
Copyright 2011-2012 Opera Software ASA

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

package com.opera.core.systems.arguments.interfaces;

import com.opera.core.systems.arguments.OperaArgument;

import java.util.Iterator;
import java.util.List;

/**
 * Defines a joint interface for interacting with arguments to various kinds of Operas.
 * OperaArguments represents a command-line argument list and contains individual arguments
 * represented by {@link OperaArgument}'s.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 * @see com.opera.core.systems.arguments.OperaDesktopArguments , com.opera.core.systems.runner.arguments.OperaCoreArguments,
 *      OperaArgument
 */
public interface OperaArguments extends Iterable<OperaArgument> {

  /**
   * Adds a new argument to the command-line argument list representation.  A new {@link
   * OperaArgument} will be created with an empty value.
   *
   * @param argument the argument key to add
   */
  public void add(String argument);

  /**
   * Adds a new argument with a value to the command-line argument list representation.  A new
   * {@link OperaArgument} will be created with specified value.
   *
   * @param argument the argument key to add
   * @param value    the value the argument should hold
   */
  public void add(String argument, String value);

  /**
   * Adds a new argument to the command-line argument list representation.  The {@link
   * OperaArgument} can optionally hold a value.
   *
   * @param argument the argument to add
   */
  public void add(OperaArgument argument);

  /**
   * Convenience method to access the given indexed argument in the command-line list
   * representation.  This maps to <code>OperaArguments.getArguments().get(x)</code>.
   *
   * @param index the argument to fetch
   * @return an argument
   */
  public OperaArgument get(int index);

  /**
   * Gets the list of all arguments, each represented by {@link OperaArgument}.
   *
   * @return list of all arguments
   */
  public List<OperaArgument> getArguments();

  /**
   * Gets an iterator for all arguments present in this collection.
   *
   * @return iterator of all arguments
   */
  public Iterator<OperaArgument> iterator();

  /**
   * Gets all arguments as a string list, including the argument's values.
   *
   * @return list of all arguments, including values
   */
  public List<String> getArgumentsAsStringList();

  /**
   * Gets the string representation of the command-line, as it would be used externally.
   *
   * @return full command-line including argument keys and values
   */
  public String toString();

  /**
   * Fetches the number of arguments in the command-line list representation.  Convenience method
   * for <code>OperaArguments.getArguments().size()</code>.
   *
   * @return number of arguments (not including values)
   */
  public int size();

  /**
   * Merge this command-line list representation with another representation.  Note that this will
   * alter the state of the current object as it does not return a new {@link OperaArguments}
   * object.
   *
   * @param extraArguments the {@link OperaArguments} to merge into this
   * @return a reference to self
   */
  public OperaArguments merge(OperaArguments extraArguments);

  /**
   * Gets the command-line argument sign used by this implementation of {@link OperaArguments}.
   * Depending on which implementation ({@link com.opera.core.systems.arguments.OperaCoreArguments},
   * {@link com.opera.core.systems.arguments.OperaDesktopArguments}) you are using, you will get a
   * {@link com.opera.core.systems.arguments.OperaArgument.OperaArgumentSign}.
   *
   * @return a command-line argument sign
   * @see com.opera.core.systems.arguments.OperaArgument.OperaArgumentSign
   */
  public String sign();

}