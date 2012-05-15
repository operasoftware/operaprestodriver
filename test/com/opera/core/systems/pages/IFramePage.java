/*
Copyright 2011-2012 Opera Software ASA

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

package com.opera.core.systems.pages;

import com.opera.core.systems.testing.Pages;
import com.opera.core.systems.testing.drivers.TestOperaDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

public class IFramePage extends Page {

  @FindBy(name = "the-iframe")
  WebElement iFrame;

  public IFramePage(TestOperaDriver driver, Pages pages) {
    super(driver, pages);
    driver.navigate().to(pages.iframes);
  }

  public WebElement enterIFrame() {
    driver.switchTo().frame(iFrame);
    return driver.switchTo().activeElement();
  }

  public void leaveIFrame() {
    driver.switchTo().defaultContent();
  }

  public String clickButton() {
    enterIFrame();
    driver.findElement(By.id("test")).click();
    //new Actions(driver).moveToElement(driver.findElement(By.id("test"))).moveByOffset(5,5).click().perform();
    leaveIFrame();
    return driver.findElement(By.id("result")).getText();
  }

}