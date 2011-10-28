package com.opera.core.systems.arguments;

public class OperaArgument {

  private String argument;
  private String value;

  public enum OperaArgumentSign {
    GNU_SIGN("--"),
    POSIX_SIGN("-"),
    WINDOWS_SIGN("/");

    private final String sign;

    private OperaArgumentSign(String sign) {
      this.sign = sign;
    }
  }

  public OperaArgument(String argument) {
    this.argument = OperaArgument.sanitize(argument);
  }

  public OperaArgument(String argument, String value) {
    this.argument = OperaArgument.sanitize(argument);
    this.value = value;
  }

  public String getArgument() {
    return argument;
  }

  public String getValue() {
    return value;
  }

  public static String sanitize(String key) {
    for (OperaArgumentSign sign : OperaArgumentSign.values()) {
      if (isSwitch(key, sign.sign)) {
        return key.substring(sign.sign.length());
      }
    }

    return key;
  }

  public static Boolean isSwitch(String key, String sign) {
    return key.substring(0, sign.length()).equals(sign);
  }

}