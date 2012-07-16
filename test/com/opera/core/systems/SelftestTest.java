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

import com.google.common.collect.ImmutableList;

import com.opera.core.systems.scope.exceptions.ResponseNotReceivedException;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.services.ISelftest.ResultType;
import com.opera.core.systems.scope.services.ums.Selftest;
import com.opera.core.systems.testing.OperaDriverTestCase;
import com.opera.core.systems.testing.RequiresService;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.opera.core.systems.scope.services.ums.Selftest.SelftestResult;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

@RequiresService("selftest")
public class SelftestTest extends OperaDriverTestCase {

  public static final long DEFAULT_SELFTEST_TIMEOUT = OperaIntervals.SELFTEST_TIMEOUT.getValue();

  @Before
  public void resetTimeout() {
    driver.manage().timeouts().selftestTimeout(DEFAULT_SELFTEST_TIMEOUT, TimeUnit.MILLISECONDS);
  }

  @Test
  public void selftestDoesNotBlowUp() {
    assertNotNull("Running selftests doesn't blow up, returns non-null result",
                  driver.selftest("about"));
  }

  @Test
  public void stringification() {
    SelftestResult pass = new SelftestResult("tag", "description", ResultType.PASS);
    SelftestResult fail = new SelftestResult("tag", "description", ResultType.FAIL);
    SelftestResult skip = new SelftestResult("tag", "description", ResultType.SKIP);
    SelftestResult skipWhy = new SelftestResult("tag", "description", ResultType.SKIP, "why");

    assertEquals("PASS stringification", pass.toString(), "tag:description\tPASS");
    assertEquals("FAIL stringification", fail.toString(), "tag:description\tFAIL");
    assertEquals("SKIP stringification", skip.toString(), "tag:description\tSKIP");
    assertEquals("SKIP stringification w/reason", skipWhy.toString(), "tag:description\tSKIP\twhy");
  }

  @Test
  public void parsing() {
    String data =
        "foo:bar\tPASS\nlorem:ipsum dolor sit amet\tFAIL\tmore\nmore:tests here\tSKIP\treason\n";

    List<SelftestResult> expected = ImmutableList.of(
        new SelftestResult("foo", "bar", ResultType.PASS),
        new SelftestResult("lorem",
                           "ipsum dolor sit amet",
                           ResultType.FAIL,
                           "more"),
        new SelftestResult("more", "tests here",
                           ResultType.SKIP,
                           "reason"));

    List<SelftestResult> results = Selftest.parseSelftests(data);
    assertEquals("Sample output parsed correctly", expected, results);
  }

  private static void assertEmptyList(final String message, final Collection<?> collection) {
    assertNotNull(message + " null", collection);
    assertTrue(message + " nonempty " + collection, collection.isEmpty());
  }

  @Test
  public void respectsTimeout() {
    driver.manage().timeouts().selftestTimeout(10, TimeUnit.MILLISECONDS);

    try {
      driver.selftest("about");
      fail("Expected selftest to time out");
    } catch (Exception e) {
      assertThat(e, is(instanceOf(ResponseNotReceivedException.class)));
      assertThat(e.getMessage(), containsString("No response in a timely fashion"));
    }
  }

}