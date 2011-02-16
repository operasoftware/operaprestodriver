package com.opera.core.systems;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

public class FramesTest extends TestBase {
  List<String> frames;

  @Before
  public void setUp() throws Exception {
    getFixture("frames.html");
    frames = driver.listFrames();
  }

  @Test
  public void testList() throws Exception {
    // frameset page, and three frames
    Assert.assertEquals(4, frames.size());
  }

  @Test
  public void testSwitchString() throws Exception {
    driver.switchTo().frame("a");

    // first frame
    Assert.assertNotNull(driver.findElementById("one"));
  }

  @Test
  public void testSwitchString2() throws Exception {
    driver.switchTo().frame("b");

    // second frame
    Assert.assertNotNull(driver.findElementById("radio_little"));
  }

  @Test
  public void testSwitchInt() throws Exception {
    driver.switchTo().frame(0);

    // first frame
    Assert.assertNotNull(driver.findElementById("one"));
  }

  @Test
  public void testSwitchIntUnnamed() throws Exception {
    driver.switchTo().frame(2);

    // only exists in third frame
    Assert.assertNotNull(driver.findElementById("img_container"));
  }

}
