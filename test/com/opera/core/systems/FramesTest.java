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
    // frameset page, and two frames
    Assert.assertEquals(4, frames.size());
  }

  @Test
  public void testSwitchString() throws Exception {
    driver.switchTo().frame("a");

    // double click
    WebElement one = driver.findElementById("one");
    one.click();
    one.click();

    Assert.assertEquals("double", driver.findElementById("two").getValue());
  }

  @Test
  public void testSwitchString2() throws Exception {
    driver.switchTo().frame("b");

    // only exists in the second frame
    driver.findElementById("input_email");
  }

  @Test
  public void testSwitchInt() throws Exception {
    driver.switchTo().frame(0);

    // double click
    WebElement one = driver.findElementById("one");
    one.click();
    one.click();

    Assert.assertEquals("double", driver.findElementById("two").getValue());
  }

  @Test
  public void testSwitchIntUnnamed() throws Exception {
    System.out.println(frames);
    driver.switchTo().frame(2);

    // only exists in third frame
    driver.findElementById("img_container");
  }

}
