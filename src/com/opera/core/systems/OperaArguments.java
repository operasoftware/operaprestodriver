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

package com.opera.core.systems;

import com.google.common.base.Joiner;

import com.opera.core.systems.arguments.OperaArgument;
import com.opera.core.systems.arguments.OperaArgument.OperaArgumentSign;
import com.opera.core.systems.preferences.OperaPreferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OperaArguments implements com.opera.core.systems.arguments.interfaces.OperaArguments {

  public static String DEFAULT_SIGN = OperaArgumentSign.POSIX_SIGN.getValue();

  protected List<OperaArgument> arguments = new ArrayList<OperaArgument>();

  public void add(String key) {
    OperaArgument argument = new OperaArgument(key);
    arguments.add(argument);
  }

  public void add(String key, String value) {
    OperaArgument argument = new OperaArgument(key, value);
    arguments.add(argument);
  }

  public void add(OperaArgument argument) {
    arguments.add(argument);
  }

  public OperaArgument get(int index) {
    return arguments.get(index);
  }

  public List<OperaArgument> getArguments() {
    return arguments;
  }

  public Iterator<OperaArgument> iterator() {
    return getArguments().iterator();
  }

  public List<String> getArgumentsAsStringList() {
    List<String> stringList = new ArrayList<String>();

    for (OperaArgument argument : this) {
      stringList.add(sign() + argument.getArgument());
      if (argument.getValue() != null && !argument.getValue().isEmpty()) {
        stringList.add(argument.getValue());
      }
    }

    return stringList;
  }

  public String toString() {
    Joiner joiner = Joiner.on(" ").skipNulls();
    return joiner.join(getArgumentsAsStringList());
  }

  public int size() {
    return arguments.size();
  }

  public static OperaArguments parse(String string) {
    OperaArguments parsed = new OperaArguments();

    if (string == null || string.isEmpty()) {
      return parsed;
    }

    Pattern p = Pattern.compile("(?:-{1,2}|\\/)([\\w]+)(?:=|\\s*)(?:\"?([^-][\\w:=\\-_\\.\\/\\\\]*)\"?)?");
    Matcher m = p.matcher(string);

    while (m.find()) {
      parsed.add(m.group(1), m.group(2));
    }

    return parsed;
  }

  public void merge(com.opera.core.systems.arguments.interfaces.OperaArguments extraArguments) {
    if (extraArguments.getArguments() == null || extraArguments.getArguments().isEmpty()) {
      return;
    }
    arguments.addAll(extraArguments.getArguments());
  }

  public String sign() {
    return DEFAULT_SIGN;
  }

}