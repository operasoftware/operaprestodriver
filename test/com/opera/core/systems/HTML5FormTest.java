/*
Copyright 2012 Opera Software ASA

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.opera.core.systems;

import com.opera.core.systems.scope.ScopeService;
import com.opera.core.systems.testing.OperaDriverTestCase;
import com.opera.core.systems.testing.RequiresService;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RequiresService(value = ScopeService.ECMASCRIPT, version = "1.1")
public class HTML5FormTest extends OperaDriverTestCase {

  @Before
  public void setup() {
    driver.get(pages.html5form);
  }

  @Test
  public void setInputFileValue() {
    WebElement fileField = driver.findElement(By.id("file"));
    fileField.sendKeys(resources.textFile().getAbsolutePath());
    assertThat(fileField.getAttribute("value"),
               is("C:\\fakepath\\" + resources.textFile().getName()));
  }

  @Test
  public void setInputColorValue() {
    WebElement colorField = driver.findElement(By.id("color"));
    colorField.sendKeys("#cccccc");
    assertThat(colorField.getAttribute("value"), is("#cccccc"));
  }

  @Test
  public void setInputRangeValue() {
    WebElement rangeField = driver.findElement(By.id("range"));
    rangeField.sendKeys("20");
    assertThat(rangeField.getAttribute("value"), is("20"));
  }

  @Test
  public void setInputPlaceholderValue() {
    WebElement placeholderField = driver.findElement(By.id("placeholder"));
    placeholderField.sendKeys("bar");
    assertThat(placeholderField.getAttribute("value"), is("bar"));
  }

}