/*
Copyright 2013 Software Freedom Conservancy

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

import com.google.common.io.Files;

import com.opera.core.systems.testing.NoDriver;
import com.opera.core.systems.testing.OperaDriverTestCase;
import com.opera.core.systems.testing.Resources;
import com.opera.core.systems.testing.drivers.TestDriver;
import com.opera.core.systems.testing.drivers.TestDriverBuilder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@NoDriver
public class OperaExtensionsTest extends OperaDriverTestCase {

  private Resources resources;
  private OperaSettings settings;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void prepare() throws IOException {
    resources = new Resources();
  }

  /**
   * Makes sure we work on a copy of the resource files to avoid modifying them.
   *
   * @param source the location of the resource file
   * @return a file object of the copied file
   */
  private File resourceCopy(File source) {
    File destination = null;

    try {
      destination = temporaryFolder.newFile(source.getName());
      Files.copy(source, destination);
      return destination;
    } catch (IOException e) {
      fail("Unable to copy resource: " + e.getMessage());
    }

    return destination;
  }

  @Before
  public void beforeEach() {
    settings = new OperaSettings();
  }

  @Test
  public void operaExtensionIsInstalled() throws IOException {
    File oexSource = resourceCopy(resources.locate("dummy-extension.oex"));

    settings.profile().addExtension(oexSource);
    TestDriver driver = new TestDriverBuilder().using(settings).get();

    assertNotNull(driver);
    assertTrue("Expected Opera to run", driver.isRunning());
    driver.navigate().to(pages.oex);

    try {
      // The dummy extension sets a HTML attribute on the <html> element.
      By by = By.cssSelector("html[isoexinstalled759='yes']");
      (new WebDriverWait(driver, 2)).until(ExpectedConditions.presenceOfElementLocated(by));
    } catch (TimeoutException e) {
      fail("Custom opera extension not detected!");
    }

    driver.quit();
  }

}
