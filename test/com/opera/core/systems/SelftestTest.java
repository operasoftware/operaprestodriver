package com.opera.core.systems;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.opera.core.systems.scope.services.ISelftest.ResultType;
import com.opera.core.systems.scope.services.ISelftest.SelftestResult;

public class SelftestTest extends TestBase {
  @Test
  public void testSelftests() {
    String product = driver.getProduct();
    if(product.equals("core-gogi")) {
      List<SelftestResult> results = driver.selftest(Arrays.asList("about"), 30000);
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
}
