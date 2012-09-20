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

import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import static org.junit.Assert.assertEquals;

public class FormSubmissionTest extends OperaDriverTestCase {

  public WebElement input;
  public WebElement submit;

  @Before
  public void setup() {
    driver.get(pages.form);
    input = driver.findElement(By.name("result"));
    submit = driver.findElement(By.id("submit"));
  }

  @Test
  public void newLineSubmitsForm() {
    input.sendKeys("foo\n");
    assertPassed();
  }

  @Test
  public void keysEnterSubmitsForm() {
    input.sendKeys("foo", Keys.ENTER);
    assertPassed();
  }

  @Test
  public void keysReturnSubmitsForm() {
    input.sendKeys("foo", Keys.RETURN);
    assertPassed();
  }

  @Test
  public void submitButtonSubmitsForm() throws Exception {
    input.sendKeys("foo");
    submit.click();
    assertPassed();
  }

  @Test
  public void submitMethodSubmitsForm() {
    input.sendKeys("foo");
    input.submit();
    assertPassed();
  }

  public void assertPassed() {
    assertEquals("PASS", driver.findElement(By.id("result")).getText());
  }

}