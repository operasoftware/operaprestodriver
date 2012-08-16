package com.opera.core.systems.scope.services;

import com.opera.core.systems.scope.internal.ServiceCallback;
import com.opera.core.systems.scope.protos.ConsoleLoggerProtos.ConsoleMessage;

import java.util.List;

/**
 * The console logger is a tool used to log and retrieve messages sent to Opera's console.  All
 * messages sent to the Opera console from the various modules in Opera are captured and displayed.
 */
public interface IConsoleLogger {

  void init();

  /**
   * Add a new message to the internal cache of console messages in the Scope server.
   */
  void addMessage(ConsoleMessage message);

  /**
   * Clear messages in the console backend, including messages outside the window filter (see the
   * {@link IWindowManager} service for more information).  This does not necessarily clear the UI
   * error console used by Desktop or other products, as they may choose to store the message
   * information separately.
   *
   * It <em>will</em>, however, ensure that the next call to {@link #listMessages} contain
   * <em>only</em> the messages that occurred between the call to <code>clear()</code> and the call
   * to {@link #listMessages}.
   */
  void clear();

  /**
   * Gets messages previously dispatched to the console from Opera.
   *
   * Note that Opera does not store all console messages forever.  There is a set history size
   * determined by the preferences <var>Max Console Message</var> in the <var>User Prefs</var>
   * section.  Use the {@link IPrefs} service to read this value, if desired.
   *
   * The return value will contain only messages that match the window filter.  See the {@link
   * IWindowManager} service for more information.
   *
   * @return list of console messages
   */
  List<ConsoleMessage> listMessages();

  void onConsoleMessage(ServiceCallback<ConsoleMessage> callback);

}