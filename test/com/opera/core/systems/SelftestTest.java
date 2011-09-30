package com.opera.core.systems;

import com.opera.core.systems.scope.services.ISelftest.ResultType;
import com.opera.core.systems.scope.services.ISelftest.SelftestResult;
import com.opera.core.systems.scope.services.ums.Selftest;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SelftestTest extends TestBase {
  @Test
  public void testSelftests() {
    String product = driver.utils().getProduct();
    if("core-gogi".equals(product)) {
      String results;
      try {
        results = driver.selftest(Arrays.asList("about"), 30000);
      } catch (UnsupportedOperationException e) {
        // This service isn't available everywhere. Don't fail if we get that
        // exception.
        if (e.getMessage().contains("selftest is not supported")) return;
        else throw e;
      }

      assertNotNull("Running selftests doesn't blow up, returns non-null result.", results);
    }
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
    String data = "foo:bar\tPASS\nlorem:ipsum dolor sit amet\tFAIL\tmore\nmore:tests here\tSKIP\treason\n";
    List<SelftestResult> results;

    if(driver.utils().getProduct().equals("core-gogi")) {
      try {
        results = Selftest.parseSelftests(driver.selftest(Arrays.asList("nosuchmodule"), 30000));
      } catch (UnsupportedOperationException e) {
        // This service isn't available everywhere. Don't fail if we get that
        // exception.
        if (e.getMessage().contains("selftest is not supported")) return;
        else throw e;
      }

      assertNull("Running selftests for non-existent module parsed as null", results);
    }

    List<SelftestResult> expected = Arrays.asList(new SelftestResult("foo", "bar", ResultType.PASS),
                                                  new SelftestResult("lorem", "ipsum dolor sit amet", ResultType.FAIL, "more"),
                                                  new SelftestResult("more", "tests here", ResultType.SKIP, "reason"));
    results = Selftest.parseSelftests(data);
    assertEquals("Sample output parsed correctly", expected, results);
  }
}
