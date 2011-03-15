/*
Copyright 2008-2011 Opera Software ASA

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
package com.opera.core.systems.scope.handlers;

import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.stp.StpConnection;

public interface IConnectionHandler {

  /**
   * This event is emitted if the STP connection thread has accepted a
   * connection. If an STP connection has already been accepted the return value
   * can be used to close the newly accepted connection.
   * 
   * @param connection The connection that has been accepted.
   * @return false if the connection should be closed immediately, or true to
   *         continue.
   */
  boolean onConnected(StpConnection connection);

  /**
   * This event is triggered when the webdriver received an initial service list
   * from Opera.
   */
  void onServiceList(java.util.List<String> services);

  /**
   * This event is triggered when we have received a response to enableStp1().
   * 
   * @param stp1 true if handshake was an STP/1 response.
   */
  void onHandshake(boolean stp1);

  /**
   * This event is triggered whenever the STP connection is closed.
   */
  void onDisconnect();

  /**
   * This event is triggered if an exception is thrown on the STP connection
   * thread. Once this event has been triggered then the STP connection should
   * be considered closed, and no further read/write operations are allowed.
   */
  void onException(Exception ex);

  /**
   * This event is triggered whenever a command error response is received.
   * 
   * @param tag the internal command tag corresponging to the request..
   * @param response null if the response was an error.
   */
  void onResponseReceived(int tag, Response response);

}
