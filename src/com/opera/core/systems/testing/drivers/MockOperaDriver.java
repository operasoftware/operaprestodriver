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

package com.opera.core.systems.testing.drivers;

import com.google.common.collect.ImmutableList;

import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.OperaSettings;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.logging.Logger;

/**
 * A mocked OperaDriver for testing startup, shutdown and generally anything not related to the
 * WebDriver specific implementation at all.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class MockOperaDriver extends OperaDriver {

  private final Logger logger = Logger.getLogger(getClass().getName());

  public MockOperaDriver(Capabilities capabilities) {
    super(capabilities);
    logger.info("Mocking OperaDriver behaviour");
  }

  public OperaSettings getSettings() {
    return settings;
  }

  @Override
  protected void init() {
  }

  @Override
  public void get(String url) {
  }

  @Override
  public String getTitle() {
    return "";
  }

  @Override
  public List<WebElement> findElements(By by) {
    return ImmutableList.of();
  }

  @Override
  public WebElement findElement(By by) {
    return new MockOperaWebElement();
  }

  public class MockOperaWebElement implements WebElement {

    public void click() {
    }

    public void submit() {
    }

    public void sendKeys(CharSequence... keysToSend) {
    }

    public void clear() {
    }

    public String getTagName() {
      return "";
    }

    public String getAttribute(String name) {
      return "";
    }

    public boolean isSelected() {
      return false;
    }

    public boolean isEnabled() {
      return false;
    }

    public String getText() {
      return "";
    }

    public List<WebElement> findElements(By by) {
      return ImmutableList.of();
    }

    public WebElement findElement(By by) {
      return new MockOperaWebElement();
    }

    public boolean isDisplayed() {
      return false;
    }

    public Point getLocation() {
      return new Point(0, 0);
    }

    public Dimension getSize() {
      return new Dimension(0, 0);
    }

    public String getCssValue(String propertyName) {
      return "";
    }
  }

}