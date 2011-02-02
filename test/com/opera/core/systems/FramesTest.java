package com.opera.core.systems;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

public class FramesTest extends TestBase {

  @Before
  public void setUp() throws Exception {
    getFixture("frames.html");
  }

  @Test
  public void testList() throws Exception {
    driver.getWindowHandles();

    List<String> frames = driver.listFrames();

    Assert.assertEquals(2, frames.size());

    driver.switchTo().frame(frames.get(0));

    // double click
    WebElement one = driver.findElementById("one");
    one.click();
    one.click();

    Assert.assertEquals("double", driver.findElementById("two").getValue());
  }

}
