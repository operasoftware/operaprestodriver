/*
Copyright 2011 Opera Software ASA

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

  public OperaArgument get(int index);

  public List<OperaArgument> getArguments();

  public List<String> getArgumentsAsStringList();

  public String toString();

  public int size();

  public void merge(OperaArguments extraArguments);

  public String sign();

}