package com.opera.core.systems;

import com.opera.core.systems.scope.internal.ServiceCallback;
import com.opera.core.systems.scope.protos.ConsoleLoggerProtos.ConsoleMessage;

import org.openqa.selenium.logging.LocalLogs;
import org.openqa.selenium.logging.LogEntry;

public class OperaLogs {

  private static LocalLogs localLogs;

  private final ConsoleMessageConverter converter = new ConsoleMessageConverter(this);

  public OperaLogs() {
    clear();
  }

  public LocalLogs get() {I
    return localLogs;
  }

  public ServiceCallback<ConsoleMessage> getConverter() {
    return converter;
  }

  public void clear() {
    localLogs = null;
    localLogs = new LocalLogs();
  }

  public static class ConsoleMessageConverter implements ServiceCallback<ConsoleMessage> {

    private final OperaLogs logs;

    public ConsoleMessageConverter(OperaLogs logs) {
      this.logs = logs;
    }

    public void call(ConsoleMessage message) {
      LogEntry entry = new LogEntry(OperaSeverity.get(message.getSeverity()).toLevel(),
                                    message.getTime(),
                                    message.getDescription());
      logs.get().addEntry(message.getSource(), entry);
    }

  }

}