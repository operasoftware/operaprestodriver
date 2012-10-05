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

import com.google.common.collect.ImmutableSet;

import com.opera.core.systems.scope.ScopeService;
import com.opera.core.systems.scope.services.Selftest;
import com.opera.core.systems.testing.OperaDriverTestCase;
import com.opera.core.systems.testing.RequiresService;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RequiresService(value = ScopeService.SELFTEST, version = "2.0")
public class ScopeSelftestTest extends OperaDriverTestCase {

  @Test
  public void startNotExistingTests() {
    assertEquals(startModuleSelftest("bogusModuleThatMostProbablyNeverWillExist"),
                 Selftest.RunStatus.NOT_AVAILABLE);
  }

  @Test
  public void startExistingTests() {
    assertEquals(startModuleSelftest("accessibility"), Selftest.RunStatus.RUNNING);
  }

  @Test
  public void checkAvailableTests() {
    assertFalse(getAvailableGroups().isEmpty());
  }

  private Selftest.RunStatus startModuleSelftest(String module) {
    return driver.getServices().getSelftest().runSelftests(ImmutableSet.of(module), null, null);
  }

  private Map<String, List<String>> getAvailableGroups() {
    return driver.getServices().getSelftest().getAvailableGroups();
  }

}