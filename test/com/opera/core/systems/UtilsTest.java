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

package com.opera.core.systems;

import com.opera.core.systems.OperaDriver.OperaUtils;
import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.OperaDriverTestCase;
import com.opera.core.systems.testing.RequiresService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.Platform.WINDOWS;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
@RequiresService(service = "core", version = "1.2")
public class UtilsTest extends OperaDriverTestCase {

  public OperaUtils utils;

  @Before
  public void beforeEach() {
    utils = driver.utils();
  }

  @Test
  public void testCoreVersion() {
    assertTrue(driver.utils().getCoreVersion().matches("\\d+\\.\\d+\\.\\d+"));
  }

  @Test
  public void testOS() {
    Assert.assertNotNull(driver.utils().getOS());
  }

  @Test
  public void testProductIsKnown() {
    OperaProduct product = driver.utils().getProduct();
    assertTrue(product.is(OperaProduct.CORE) || product.is(OperaProduct.DESKTOP));
  }

  @Test
  public void testBinaryPath() {
    assertNotNull(driver.utils().getBinaryPath());
  }

  @Test
  public void testUserAgent() {
    assertTrue(driver.utils().getUserAgent().matches("Opera/\\d+\\.\\d+.+"));
  }

  @Test
  @Ignore(platforms = WINDOWS)
  public void testPID() {
    assertTrue(driver.utils().getPID() > 0);
  }

  @Test
  public void testClearWithOneFlag() {
    utils.clearPrivateData(OperaDriver.PrivateData.DISK_CACHE);
  }

  @Test
  public void testClearWithMultipleFlags() {
    utils.clearPrivateData(OperaDriver.PrivateData.IMAGE_CACHE,
                           OperaDriver.PrivateData.MEMORY_CACHE);
  }

  @Test
  public void testClearAll() {
    utils.clearPrivateData(OperaDriver.PrivateData.ALL);
  }

}