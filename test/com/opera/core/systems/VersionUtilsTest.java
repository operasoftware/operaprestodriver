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

package com.opera.core.systems;

import com.opera.core.systems.util.VersionUtil;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class VersionUtilsTest {

  private final String[] biggerVersions = {"2.0.0", "2.0.1", "2.1", "2.1.0", "3.0", "3.0.1"};
  private final String[] smallerVersions = {"1.0.0", "1.0.1", "1.1", "1.1.0", "1.0", "1.9.9"};

  @Test
  public void testShouldSupportSmallerThan() {
    traverseVersions(biggerVersions, 1, ">");
  }

  @Test
  public void testShouldSupportBiggerThan() {
    traverseVersions(smallerVersions, -1, "<");
  }

  @Test
  public void testShouldSupportEquals() {

    for (String biggerVersion : biggerVersions) {
      int result = VersionUtil.compare(biggerVersion, biggerVersion);
      assertTrue("Expected equals on " + biggerVersion, result == 0);
    }

    for (String smallerVersion : smallerVersions) {
      int result = VersionUtil.compare(smallerVersion, smallerVersion);
      assertTrue("Expected equals on " + smallerVersion, result == 0);
    }
  }

  private void traverseVersions(String[] arr, int expected, String sign) {
    String version = "2.0";
    for (String anArr : arr) {
      int result = VersionUtil.compare(anArr, version);
      assertTrue("Expected " + anArr + " " + sign + " " + version, result == expected);
    }
  }

}