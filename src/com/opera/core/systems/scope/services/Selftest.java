/*
Copyright 2011-2012 Opera Software ASA

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

import com.opera.core.systems.scope.internal.ServiceCallback;
import com.opera.core.systems.scope.protos.SelftestProtos.SelftestResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Runs Opera's selftests through Scope. This is only compatible with Opera versions with
 * core-integration-394 and higher.
 */
public interface Selftest extends Service {

  enum RunStatus {RUNNING, NOT_AVAILABLE}

  /**
   * Add a new selftest result to the internal cache kept in Operadriver#selftest
   */
  void addResult(SelftestResult message);

  /**
   * Run the selftests for the specified modules. Optionally you can limit the tests to be run by a
   * group pattern and an exclude pattern.
   *
   * @param modules  module names
   * @param groups   group names or pattern
   * @param excludes pattern of groups or modules that should be excluded from testing
   * @return {@link RunStatus#RUNNING} if tests are running, or {@link RunStatus#NOT_AVAILABLE} if
   *         no tests matching the selection pattern are found
   * @since 2.0
   */
  RunStatus runSelftests(Set<String> modules, String groups, String excludes);

  /**
   * Retrieves a list of available groups.
   *
   * @return map of modules and the test groups it contains
   * @since 2.0
   */
  Map<String, List<String>> getAvailableGroups();

  /**
   * Specify a callback for when a selftest result is received.
   *
   * @param callback will be called on every selftest result that is received
   * @since 2.0
   */
  void onSelftestResult(ServiceCallback<SelftestResult> callback);

  /**
   * Represents a single result from a selftest.
   */
  interface ISelftestResult {

    enum Result {
      PASS, FAIL, SKIP
    }

    /**
     * @return name of selftest
     */
    String getTestName();

    /**
     * @return name of group the selftest belongs to
     */
    String getGroupName();

    /**
     * @return result of selftest, {@link Result#PASS}, {@link Result#FAIL} or {@link Result#SKIP}
     */
    Result getResult();

    /**
     * @return if result is FAIL or SKIP a reason migth be given
     */
    String getReason();

    /**
     * @return if result is FAIL or SKIP the filename where the test is located
     */
    String getFileName();

    /**
     * @return line number in selftestfile where it FAILED or was SKIPPED, this can be 0
     */
    int getLineNumber();

  }

}