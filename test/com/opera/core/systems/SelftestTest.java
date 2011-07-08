package com.opera.core.systems;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

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
}
