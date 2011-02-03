package com.opera.core.systems;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OperaPathsTest {

  @Test
  public void testStart() throws Exception {
    OperaDriver driver = new OperaDriver();

    driver.get("http://t/core/README");

    assertEquals("http://t/core/README", driver.getCurrentUrl());

    driver.shutdown();
  }

}
