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

package com.opera.core.systems;

import com.opera.core.systems.scope.services.ums.Selftest;

import java.util.List;

/**
 * Indicates that an OperaDriver can run selftests.
 *
 * @see Selftest.SelftestResult
 */
public interface RunsSelftest {

  /**
   * Executes the selftests for the given module.  WebDriver generally has a blocking API so this
   * method will return when the test is complete, or when {@link com.opera.core.systems.scope.internal.OperaIntervals#SELFTEST_TIMEOUT}
   * is reached.
   */
  List<Selftest.SelftestResult> selftest(String module);

}