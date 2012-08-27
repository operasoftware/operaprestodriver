/*
Copyright 2012 Opera Software ASA

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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.opera.core.systems.scope.internal.ServiceCallback;
import com.opera.core.systems.scope.protos.ConsoleLoggerProtos.ConsoleMessage;

import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

// TODO(andreastt): Extend StoringLocalLogs when Selenium 2.26 is out
public class OperaLogs implements Logs {

  private final Map<String, List<LogEntry>> localLogs = Maps.newHashMap();
  private final Set<String> logTypesToIgnore;

  public OperaLogs() {
    this(new HashSet<String>());
  }

  public OperaLogs(Set<String> logTypesToIgnore) {
    this.logTypesToIgnore = logTypesToIgnore;
  }

  public LogEntries get(String logType) {
    Iterable<LogEntry> toReturn = getLocalLogs(logType);
    localLogs.remove(logType);
    return new LogEntries(toReturn);
  }

  private Iterable<LogEntry> getLocalLogs(String logType) {
    if (localLogs.containsKey(logType)) {
      return localLogs.get(logType);
    }

    return Lists.newArrayList();
  }

  /**
   * Add a new log entry to the local storage.
   *
   * @param logType the log type to store
   * @param entry   the entry to store
   */
  public void addEntry(String logType, LogEntry entry) {
    if (logTypesToIgnore.contains(logType)) {
      return;
    }

    if (!localLogs.containsKey(logType)) {
      localLogs.put(logType, Lists.newArrayList(entry));
    } else {
      localLogs.get(logType).add(entry);
    }
  }

  public Set<String> getAvailableLogTypes() {
    return localLogs.keySet();
  }

  /**
   * Converts the console messages we receive through Scope to {@link LogEntry}'s for use with
   * WebDriver's {@link Logs} interface.
   */
  public static class ConsoleMessageConverter implements ServiceCallback<ConsoleMessage> {

    private final OperaLogs logs;

    public ConsoleMessageConverter(OperaLogs logs) {
      this.logs = logs;
    }

    public void call(ConsoleMessage message) {
      LogEntry entry = new LogEntry(OperaSeverity.get(message.getSeverity()).toLevel(),
                                    message.getTime(),
                                    message.getDescription());
      logs.addEntry(message.getSource(), entry);
    }

  }

  /**
   * Forwards log entries from {@link OperaDriver} to the WebDriver {@link Logs} interface for
   * remote retrieval.
   */
  public static class DriverLogsHandler extends Handler {

    private final OperaLogs logs;

    public DriverLogsHandler(OperaLogs logs) {
      this.logs = logs;
    }

    public void publish(LogRecord record) {
      LogEntry entry = new LogEntry(record.getLevel(),
                                    System.currentTimeMillis() / 1000L,
                                    record.getMessage());
      logs.addEntry(LogType.DRIVER, entry);
    }

    public void flush() {
    }

    public void close() throws SecurityException {
    }

  }

}