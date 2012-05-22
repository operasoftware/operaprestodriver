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

package com.opera.core.systems.environment.webserver;

/**
 * Interface for a simple web server.
 */
public interface AppServer {

  /**
   * Gets the currently used hostname by the server.
   *
   * @return hostname
   */
  String getHostName();

  /**
   * Gets the currently used alternative hostname, typically the private local address, used by the
   * server.
   *
   * @return private local address
   */
  String getAlternateHostName();

  /**
   * Gets the full URL on the installed server to a resource based on the given relative URL.
   *
   * @param relativeUrl the relative path on the server to the resource you're looking for, e.g.
   *                    "/some/thing" might return "http://mymachine/some/thing"
   * @return the full URL for the requested resource on the server
   */
  String whereIs(String relativeUrl);

  /**
   * Gets the full URL with the alternate host name (see {@link #getAlternateHostName()} on the
   * installed server to a resource based on the given relative URL.
   *
   * @param relativeUrl the relative path to the server to the resource you're looking for, e.g.
   *                    "/some/thing" might reutrn "http://127.0.0.1/some/thing"
   * @return the full URL to the requested resource on the server with the alternate hostname
   */
  String whereElseIs(String relativeUrl);

  /**
   * Starts the server sockets.
   */
  void start();

  /**
   * Stops the server sockets.
   */
  void stop();

  /**
   * Which port the web server should listen on.  Cannot be set after server has been started.
   *
   * @param port the port number to listen on
   */
  void listenOn(int port);

}