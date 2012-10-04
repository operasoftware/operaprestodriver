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

package com.opera.core.systems.scope;

/**
 * Represents a Scope service in Opera.
 */
public interface Service {

  /**
   * Initialize the service.
   */
  public void init();

  /**
   * Gets the name of the service as represented in Opera.
   *
   * @return the name of the service, as represented in Opera
   */
  public String getServiceName();

  /**
   * Gets the current version of the service.  Versions are defined as "X.Y", where X is the major
   * version, and Y the minor version.
   *
   * @return the current version of the service
   */
  public String getServiceVersion();

}