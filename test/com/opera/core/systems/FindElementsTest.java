package com.opera.core.systems;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

public class FindElementsTest extends TestBase
{
  @Before
  public void setUp() {
    driver.get(fixture("test.html"));
  }

  @Test
  public void testActiveElement()
  {
    driver.executeScript("document.getElementById('local').focus()");
    WebElement el = driver.findActiveElement();
    Assert.assertEquals(el.getAttribute("id"), "local");
  }

  @Test
  public void testLinkText()
  {
    WebElement el = driver.findElementByLinkText("accumsan ante");
    Assert.assertEquals(el.getAttribute("id"), "local");
  }

  public void testElementsLinkText()
  {
    List<WebElement> els = driver.findElementsByLinkText("accumsan ante");

    for (WebElement el : els)
    {
      Assert.assertEquals(el.getText(), "accumsan ante");
      Assert.assertEquals(el.getTagName().toLowerCase(), "a");
    }
  }

  // Partial link text
  @Test
  public void testPartialLinkText()
  {
    WebElement el = driver.findElementByPartialLinkText("pell");
    Assert.assertEquals(el.getAttribute("id"), "external");
  }

  @Test
  public void testElementsPartialLinkText()
  {
    List<WebElement> els = driver.findElementsByPartialLinkText("te");

    for (WebElement el : els)
    {
      Assert.assertTrue(el.getText().contains("te"));
      Assert.assertEquals(el.getTagName().toLowerCase(), "a");
    }
  }

  // ID
  @Test
  public void testId()
  {
    WebElement el = driver.findElementById("call-to-action");
    Assert.assertEquals(el.getAttribute("id"), "call-to-action");
    Assert.assertEquals(el.getTagName().toLowerCase(), "p");
  }

  @Test
  public void testElementsId()
  {
    List<WebElement> els = driver.findElementsById("external");

    for (WebElement el : els)
    {
      Assert.assertEquals(el.getAttribute("id"), "external");
    }
  }

  // XPath
  @Test
  public void testXPath()
  {
    // TODO This test fails. Something is messed up in XPath I think.
    // document.evaluate("//span[1]", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue
    // = "Ante", the first span
    // document.evaluate("//span[2]", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue
    // = "ante", the LAST span?!
    WebElement el = driver.findElementByXPath("(//span)[4]");
    Assert.assertEquals(el.getText(), "ante");
  }

  public void testElementsXPath()
  {
    List<WebElement> els = driver.findElementsByXPath("//span");
    for (WebElement el : els)
    {
      Assert.assertEquals(el.getTagName().toLowerCase(), "span");
    }
  }

  // Class
  @Test
  public void testClass()
  {
    WebElement el = driver.findElementByClassName("invert");
    Assert.assertEquals(el.getText(), "Ante");
  }

  @Test
  public void testElementsClass()
  {
    List<WebElement> els = driver.findElementsByClassName("invert");

    for (WebElement el : els)
    {
      Assert.assertTrue(el.getAttribute("class").contains("invert"));
    }
  }

  // Name
  @Test
  public void testName()
  {
    WebElement el = driver.findElementByName("radios");
    Assert.assertEquals(el.getAttribute("id"), "radio_little");
  }

  @Test
  public void testElementsName()
  {
    List<WebElement> els = driver.findElementsByName("radios");

    Assert.assertEquals(els.size(), 3);
    for (WebElement el : els)
    {
      Assert.assertEquals(el.getAttribute("name"), "radios");
    }
  }

  // Tag name
  @Test
  public void testTagName()
  {
    WebElement el = driver.findElementByTagName("label");
    Assert.assertEquals(el.getAttribute("for"), "input_email");
  }

  @Test
  public void testElementsTagName()
  {
    List<WebElement> els = driver.findElementsByTagName("label");

    Assert.assertEquals(els.size(), 4);
    for (WebElement el : els)
    {
      Assert.assertEquals(el.getTagName(), "LABEL");
    }
  }

  // CSS selector
  @Test
  public void testCssSelector()
  {
    WebElement el = driver.findElementByCssSelector("p > span + a");
    Assert.assertEquals(el.getAttribute("id"), "local");
  }

  @Test
  public void testElementsCssSelector()
  {
    List<WebElement> els = driver.findElementsByCssSelector("div input[name=radios]");

    Assert.assertEquals(3, els.size());
    for (WebElement el : els)
    {
      Assert.assertEquals(el.getAttribute("name"), "radios");
    }
  }
}
