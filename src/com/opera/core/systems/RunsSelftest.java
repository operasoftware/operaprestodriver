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

import com.opera.core.systems.scope.services.Selftest.SelftestResult;

import java.util.List;
import java.util.Set;

/**
 * Indicates that an OperaDriver can run selftests.
 *
 * @see SelftestResult
 */
public interface RunsSelftest {

  /**
   * {@inheritDoc #selftest(Set<String>, String, String)}
   *
   * @param modules a set of modules to run the selftests of
   */
  List<SelftestResult> selftest(Set<String> modules);

  /**
   * {@inheritDoc #selftest(Set<String>, String, String)}
   *
   * @param modules      a set of modules to run the selftests of
   * @param groupPattern glob-style pattern to match specific test groups (ie. "group:testname*")
   */
  List<SelftestResult> selftest(Set<String> modules, String groupPattern);

  /**
   * Makes a collection of selftests based on the parameters in and runs them.  WebDriver generally
   * has a blocking API so this method will return when the test is complete, or when {@link
   * com.opera.core.systems.scope.internal.OperaIntervals#SELFTEST_TIMEOUT} is reached.
   *
   * @param modules        a set of modules to run the selftests of
   * @param groupPattern   glob-style pattern to match specific test groups (ie. "group:testname*")
   * @param excludePattern glob-style pattern to create an exclusion rule as in `groupPattern`
   */
  List<SelftestResult> selftest(Set<String> modules, String groupPattern, String excludePattern);

}