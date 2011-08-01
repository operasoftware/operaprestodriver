/*
 * Copyright 2011 Opera Software ASA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opera.core.systems;

import static org.junit.Assert.*;

import java.util.List;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

public class EcmaScriptTest extends TestBase {

  String xpath =
    ".//input[(not(@type) or (@type!=\"file\" and @type!=\"radio\" and @typ" +
      "e!=\"checkbox\" and @type!=\"submit\" and @type!=\"reset\" and @type!=\"" +
      "image\" and @type!=\"button\" and @type!=\"hidden\" and @type!=\"url\" " +
      "and @type!=\"datetime\" and @type!=\"date\" and @type!=\"month\" and @" +
      "type!=\"week\" and @type!=\"time\" and @type!=\"datetime-local\" and @" +
      "type!=\"range\" and @type!=\"color\")) and @class='foo'] | .//textarea" +
      "[@class='foo']";

  @Before
  public void setUp() {
    driver.navigate().to(
      "data:text/html;charset=utf-8,%3Cinput%20id%3D%22wrong%22%20class%3" +
        "D%22foo%22%20%2F%3E%3Cinput%20id%3D%22correct%22%20class%3D%22foo%" +
        "22%20%2F%3E"
    );
  }

  @Test
  public void testXPath() {
    Exception exception = null;

    try {
      driver.findElementsByXPath(xpath);
    } catch (WebDriverException e) {
      exception = e;
    }

    Assert.assertNull(exception);
  }

  @Test
  public void testObjectOfElements() throws Exception {
    List<WebElement> divs = (List<WebElement>) driver.executeScript("return [document.createElement('div'), document.createElement('div')]");
    for (WebElement el : divs) {
      Assert.assertEquals("DIV", el.getTagName());
    }
  }

}
