/*
Copyright 2008-2012 Opera Software ASA

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

package com.opera.core.systems.scope.internal;

public class OperaDefaults {

  /**
   * Whether the ecmascript-debugger should be enabled or not.
   */
  public static boolean ENABLE_DEBUGGER = true;

  /**
   * Whether or not to append to log file instead of overwriting contents.  If true the driver will
   * append, otherwise it will overwrite.  By default it appends to the file.
   */
  public static final boolean APPEND_TO_LOGFILE = true;

  /**
   * Whether Opera Idle should be enabled by default.
   */
  public static final boolean ENABLE_IDLE = false;

  /**
   * Whether to enable autostart of the Opera browser by default.
   */
  public static final boolean ENABLE_AUTOSTART = true;

  /**
   * The default Opera debug proxy server port for OperaDriver to connect to.
   */
  public static final int SERVER_DEFAULT_PORT = 7001;

  /**
   * For backwards compatibility with Operas without <code>-debugproxy</code> support, if the user
   * specifies 0 as the port it will use a random port.
   */
  public static final int SERVER_RANDOM_PORT_IDENTIFIER = 0;

  /**
   * For backwards compatibility with Operas without <code>-debugproxy</code> support, if the user
   * specifies -1 as the port it will use the default server proxy port specified in {@link
   * #SERVER_DEFAULT_PORT}.
   */
  public static final int SERVER_DEFAULT_PORT_IDENTIFIER = -1;

  /**
   * The number of times to attempt retrieving a response from injected ECMAscript sent to the
   * browser.
   */
  public static final int SCRIPT_RETRIES = 5;

}