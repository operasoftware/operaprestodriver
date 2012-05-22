/*
Copyright 2012 Opera Software ASA
Copyright 2007-2009 WebDriver committers
Copyright 2007-2009 Google Inc.
Portions copyright 2007 ThoughtWorks, Inc

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

package com.opera.core.systems.environment;

import com.opera.core.systems.environment.webserver.AppServer;

public interface TestEnvironment {

  /**
   * Sets environmental variable during runtime.
   *
   * @param key   environmental variable identifier
   * @param value value of the variable
   */
  void set(String key, String value);

  /**
   * Unsets environmental variable during runtime.
   *
   * @param key environmental variable identifier
   */
  void unset(String key);

  /**
   * Post-hook for shutting down the test environment.
   */
  void stop();

  /**
   * Gets the web server serving testing files.
   *
   * @return a web server serving testing files
   */
  AppServer getWebServer();

}