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

package com.opera.core.systems.scope.services;

import com.opera.core.systems.scope.Service;
import com.opera.core.systems.scope.internal.ServiceCallback;
import com.opera.core.systems.scope.protos.ConsoleLoggerProtos.ConsoleMessage;

import java.util.List;

/**
 * The console logger is a tool used to log and retrieve messages sent to Opera's console.  All
 * messages sent to the Opera console from the various modules in Opera are captured and displayed.
 */
public interface ConsoleLogger extends Service {

  static final String SERVICE_NAME = "console-logger";

  /**
   * Add a new message to the internal cache of console messages in the Scope server.
   */
  void addMessage(ConsoleMessage message);

  /**
   * Clear messages in the console backend, including messages outside the window filter (see the
   * {@link WindowManager} service for more information).  This does not necessarily clear the UI
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
   * determined by the preferences <var>Max Console Message</var> in the <var>User ScopePrefs</var>
   * section.  Use the {@link Prefs} service to read this value, if desired.
   *
   * The return value will contain only messages that match the window filter.  See the {@link
   * WindowManager} service for more information.
   *
   * @return list of console messages
   */
  List<ConsoleMessage> listMessages();

  void onConsoleMessage(ServiceCallback<ConsoleMessage> callback);

}