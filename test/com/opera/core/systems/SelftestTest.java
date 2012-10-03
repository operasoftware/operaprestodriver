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

package com.opera.core.systems;

import com.google.common.collect.ImmutableSet;

import com.opera.core.systems.scope.exceptions.ResponseNotReceivedException;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.services.ISelftest;
import com.opera.core.systems.scope.services.ISelftest.ISelftestResult;
import com.opera.core.systems.scope.services.ums.Selftest;
import com.opera.core.systems.testing.OperaDriverTestCase;
import com.opera.core.systems.testing.RequiresService;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.ui.Duration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

@RequiresService(value = "selftest", version = "2.0")
public class SelftestTest extends OperaDriverTestCase {

  public static final Duration DEFAULT_SELFTEST_TIMEOUT =
      OperaIntervals.SELFTEST_TIMEOUT.getValue();

  @Before
  public void resetTimeout() {
    driver.manage().timeouts().selftestTimeout(DEFAULT_SELFTEST_TIMEOUT.in(TimeUnit.MILLISECONDS),
                                               TimeUnit.MILLISECONDS);
  }

  @Test
  public void selftestDoesNotBlowUp() {
    List<ISelftestResult> results = driver.selftest(ImmutableSet.of("accessibility",
                                                                                  "about"), "", "");
    assertFalse("Running selftests doesn't blow up, returns non-null result",
                       results.isEmpty());
  }

  @Test
  public void selftestOnlyGroup() {
    List<ISelftestResult> results = driver.selftest(ImmutableSet.of(""), "about.generateddocuments.dom:*", "");
    assertFalse(results.isEmpty());
  }

  @Test
  public void respectsTimeout() {
    driver.manage().timeouts().selftestTimeout(10, TimeUnit.MILLISECONDS);

    try {
      driver.selftest(ImmutableSet.of("about"));
      fail("Expected selftest to time out");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(ResponseNotReceivedException.class)));
      assertThat(e.getMessage(), containsString("No response in a timely fashion"));
    }
  }

  @Test
  public void canRetrieveSelftestGroups() {
    Map<String, List<String>> groups = driver.getServices().getSelftest().getAvailableGroups();
    assertFalse(groups.isEmpty());
  }

  @Test
  public void toJsonSerializesProperly() throws JSONException {
    Selftest.SelftestResult.Builder builder = Selftest.SelftestResult.builder();

    Selftest.SelftestResult expected = builder
        .setTestName("name")
        .setGroupName("group")
        .setFileName("filename.ot")
        .setTestResult(ISelftest.ISelftestResult.Result.PASS)
        .setReason("reason")
        .setLineNumber(2)
        .build();

    JSONObject json = expected.toJson();

    assertEquals(expected.getTestName(), json.get("testName"));
    assertEquals(expected.getGroupName(), json.get("groupName"));
    assertEquals(expected.getFileName(), json.get("fileName"));
    assertEquals(expected.getResult(), json.get("result"));
    assertEquals(expected.getLineNumber(), json.get("lineNumber"));
  }

  @Test
  public void fromJsonDeserializesProperly() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("testName", "name");
    json.put("groupName", "group");
    json.put("fileName", "filename");
    json.put("reason", "reason");
    json.put("result", ISelftest.ISelftestResult.Result.PASS);
    json.put("lineNumber", 333);

    Selftest.SelftestResult expected = Selftest.SelftestResult.builder().build().fromJson(json);

    assertEquals("name", expected.getTestName());
    assertEquals("group", expected.getGroupName());
    assertEquals("filename", expected.getFileName());
    assertEquals("reason", expected.getReason());
    assertEquals(ISelftest.ISelftestResult.Result.PASS, expected.getResult());
    assertEquals(333, expected.getLineNumber());
  }

}