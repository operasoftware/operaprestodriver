package com.opera.core.systems;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

public class JavascriptTest extends TestBase
{

  @Before
  public void setUp()
  {
    driver.get(fixture("javascript.html"));
    // Click to focus the document
    driver.mouseEvent(1, 1, 1);
  }

  @Test
  public void testTyping()
  {
	  String text = "Hello, world!";

	  driver.executeScript("document.getElementById('one').focus()");
	  driver.type(text);

	  Assert.assertEquals(text, driver.findElementById("one").getValue());
  }

  // Make sure that typing actually happens. When the focus switches half way
  // through typing we should continue typing on the other textbox
  @Test
  public void testTypingKeyEvents()
  {
	  driver.get(fixture("keys.html"));

    driver.type("hi");

    String log = driver.findElementById("log").getValue();
    Assert.assertTrue(log.contains("press, 104, h,"));
    Assert.assertTrue(log.contains("up, 73, I,"));
  }

  // TODO finish this test
  /*
oper  public void testConsoleListener()
  {
    driver.addConsoleListener(new IConsoleListener() {
      public List<FilterRule> getFilters() {
        List<FilterRule> list = new ArrayList<FilterRule>();
        list.add(new FilterRule("*", FilterRule.FilterType.STARTS_WITH, ""));
        return list;
      }

      public void onMessage(String message) {
        Assert.assertEquals("one", message);
      }
    });
  }
  */

  @Test
  public void testDoubleClick()
  {
    WebElement one = driver.findElementById("one");
    one.click();
    one.click();
    Assert.assertEquals(driver.findElementById("two").getValue(), "double");
  }
}
