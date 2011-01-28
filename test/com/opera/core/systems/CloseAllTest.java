package com.opera.core.systems;

import org.junit.Test;
import org.junit.Assert;

public class CloseAllTest extends TestBase {
  @Test
  public void testCloseAll() throws Exception {
    driver.closeAll();

    driver.get(fixture("test.html"));
    Assert.assertEquals(fixture("test.html"), driver.getCurrentUrl());
  }
}
