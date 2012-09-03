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
import com.opera.core.systems.scope.services.Selftest.ResultType;
import com.opera.core.systems.scope.stp.services.ScopeSelftest;
import com.opera.core.systems.testing.OperaDriverTestCase;
import com.opera.core.systems.testing.RequiresService;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;
import static com.opera.core.systems.scope.services.Selftest.SelftestResult;
import static com.opera.core.systems.scope.stp.services.ScopeSelftest.ScopeSelftestResult;

@RequiresService("selftest")
public class SelftestTest extends OperaDriverTestCase {

  public static final long DEFAULT_SELFTEST_TIMEOUT = OperaIntervals.SELFTEST_TIMEOUT.getMs();

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
    SelftestResult pass = new ScopeSelftestResult("tag", "description", ResultType.PASS);
    SelftestResult fail = new ScopeSelftestResult("tag", "description", ResultType.FAIL);
    SelftestResult skip = new ScopeSelftestResult("tag", "description", ResultType.SKIP);
    SelftestResult skipWhy = new ScopeSelftestResult("tag", "description", ResultType.SKIP, "why");

    assertEquals("PASS stringification", pass.toString(), "tag:description\tPASS");
    assertEquals("FAIL stringification", fail.toString(), "tag:description\tFAIL");
    assertEquals("SKIP stringification", skip.toString(), "tag:description\tSKIP");
    assertEquals("SKIP stringification w/reason", skipWhy.toString(), "tag:description\tSKIP\twhy");
  }

  @Test
  public void parsing() {
    String data =
        "foo:bar\tPASS\nlorem:ipsum dolor sit amet\tFAIL\tmore\nmore:tests here\tSKIP\treason\n";

    ImmutableList<ScopeSelftestResult> expected = ImmutableList.of(
        new ScopeSelftestResult("foo", "bar", ResultType.PASS),
        new ScopeSelftestResult("lorem", "ipsum dolor sit amet", ResultType.FAIL, "more"),
        new ScopeSelftestResult("more", "tests here", ResultType.SKIP, "reason")
    );

    List<SelftestResult> results = ScopeSelftest.parseSelftests(data);
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