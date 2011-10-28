package com.opera.core.systems;

import com.opera.core.systems.arguments.OperaArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OperaArguments implements com.opera.core.systems.arguments.interfaces.OperaArguments {

  public static String DEFAULT_SIGN = "-";

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

  public List<OperaArgument> getArguments() {
    return arguments;
  }

  public List<String> getArgumentsAsStrings() {
    List<String> stringList = new ArrayList<String>();

    for (OperaArgument argument : arguments) {
      stringList.add(argument.getArgument());
      if (argument.getValue() != null && !argument.getValue().isEmpty()) {
        stringList.add(argument.getValue());
      }
    }

    return stringList;
  }

  public static OperaArguments parse(String string) {
    OperaArguments parsed = new OperaArguments();

    Pattern p = Pattern.compile("(-{1,2}[\\w]+)\\s*=*([\\w]*)");
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