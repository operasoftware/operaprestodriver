package com.opera.core.systems;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.opera.core.systems.scope.services.ISelftest.ResultType;
import com.opera.core.systems.scope.services.ISelftest.SelftestResult;
import com.opera.core.systems.scope.services.ums.Selftest;

public class SelftestTest extends TestBase {
  @Test
  public void testSelftests() {
    String product = driver.getProduct();
    if(product.equals("core-gogi")) {
      String results;
      try {
        results = driver.selftest(Arrays.asList("about"), 30000);
      } catch (UnsupportedOperationException e) {
        // This service isn't available everywhere. Don't fail if we get that
        // exception.
        if (e.getMessage().contains("selftest is not supported")) return;
        else throw e;
      }

      Assert.assertNotNull("Running selftests doesn't blow up, returns non-null result.", results);
    }
  }

  @Test
  public void stringification() {
    SelftestResult pass = new SelftestResult("tag", "description", ResultType.PASS);
    SelftestResult fail = new SelftestResult("tag", "description", ResultType.FAIL);
    SelftestResult skip = new SelftestResult("tag", "description", ResultType.SKIP);
    SelftestResult skipWhy = new SelftestResult("tag", "description", ResultType.SKIP, "why");

    Assert.assertEquals("PASS stringification",          pass.toString(),    "tag:description\tPASS");
    Assert.assertEquals("FAIL stringification",          fail.toString(),    "tag:description\tFAIL");
    Assert.assertEquals("SKIP stringification",          skip.toString(),    "tag:description\tSKIP");
    Assert.assertEquals("SKIP stringification w/reason", skipWhy.toString(), "tag:description\tSKIP\twhy");
  }

  @Test
  public void parsing() {
    String data = "foo:bar\tPASS\nlorem:ipsum dolor sit amet\tFAIL\tmore\nmore:tests here\tSKIP\treason\n";
    List<SelftestResult> results;

    if(driver.getProduct().equals("core-gogi")) {
      try {
        results = Selftest.parseSelftests(driver.selftest(Arrays.asList("nosuchmodule"), 30000));
      } catch (UnsupportedOperationException e) {
        // This service isn't available everywhere. Don't fail if we get that
        // exception.
        if (e.getMessage().contains("selftest is not supported")) return;
        else throw e;
      }

      Assert.assertNull("Running selftests for non-existent module parsed as null", results);
    }

    List<SelftestResult> expected = Arrays.asList(new SelftestResult("foo", "bar", ResultType.PASS),
                                                  new SelftestResult("lorem", "ipsum dolor sit amet", ResultType.FAIL, "more"),
                                                  new SelftestResult("more", "tests here", ResultType.SKIP, "reason"));
    results = Selftest.parseSelftests(data);
    Assert.assertEquals("Sample output parsed correctly", expected, results);
  }
}
