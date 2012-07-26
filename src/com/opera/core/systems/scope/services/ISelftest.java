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

import java.util.List;

/**
 * Runs Opera's selftests through Scope.
 */
public interface ISelftest {

  public enum ResultType {
    PASS, FAIL, SKIP
  }

  /**
   * Run the selftests for the specified modules.
   *
   * @param modules the names of the modules to test
   */
  void runSelftests(List<String> modules);

  /**
   * Represents a single result from a selftest.
   */
  interface ISelftestResult {

    String getTag();

    String getDescription();

    ResultType getResult();

    String getMore();

  }

}