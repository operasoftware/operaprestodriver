/*
Copyright 2008-2013 Opera Software ASA

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

import org.openqa.selenium.WebDriver.Timeouts;

import java.util.concurrent.TimeUnit;

public interface OperaTimeouts extends Timeouts {

  /**
   * Sets the amount of time to wait for a response from Opera/the Scope client before throwing an
   * error.
   *
   * @param time the amount of time to wait
   * @param unit the unit of measure for {@code time}
   * @return a self reference
   */
  Timeouts responseTimeout(long time, TimeUnit unit);

  /**
   * Defines the amount of time to wait before aborting a running selftest executed by {@link
   * OperaDriver#selftest(String)}.
   *
   * @param time the amount of time to wait
   * @param unit the unit of measure for {@code time}
   * @return a self reference
   */
  Timeouts selftestTimeout(long time, TimeUnit unit);

  /**
   * 
   *
   * @param time the amount of time to wait
   * @param unit the unit of measure for {@code time}
   * @return a self reference
   */
  Timeouts setDefaultResponseTimeout(long time, TimeUnit unit);

}
