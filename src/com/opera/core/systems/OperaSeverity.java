package com.opera.core.systems;

import com.opera.core.systems.scope.internal.ServiceCallback;
import com.opera.core.systems.scope.protos.ConsoleLoggerProtos.ConsoleMessage;

import org.openqa.selenium.logging.LocalLogs;
import org.openqa.selenium.logging.LogEntry;

import java.util.logging.Level;

public enum OperaSeverity {

  DEBUG,
  VERBOSE,
  INFORMATION,
  ERROR,
  CRITICAL;

  public Level toLevel() {
    switch (this) {
      case DEBUG:
        return Level.FINE;

      case VERBOSE:
        return Level.FINER;

      case ERROR:
        return Level.WARNING;

      case CRITICAL:
        return Level.SEVERE;

      case INFORMATION:
      default:
        return Level.INFO;
    }
  }

  @Override
  public String toString() {
    return name().toLowerCase();
  }

  public static OperaSeverity get(String name) {
    for (OperaSeverity severity : values()) {
      if (severity.toString().equals(name)) {
        return severity;
      }
    }

    return OperaSeverity.INFORMATION;
  }

}