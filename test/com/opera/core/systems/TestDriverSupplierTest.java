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

import com.opera.core.systems.testing.drivers.TestDriver;
import com.opera.core.systems.testing.drivers.TestDriverSupplier;
import com.opera.core.systems.testing.drivers.TestOperaDesktopDriver;
import com.opera.core.systems.testing.drivers.TestOperaDesktopDriverSupplier;
import com.opera.core.systems.testing.drivers.TestOperaDriver;
import com.opera.core.systems.testing.drivers.TestOperaDriverSupplier;
import com.opera.core.systems.testing.drivers.TestOperaMobileDriver;
import com.opera.core.systems.testing.drivers.TestOperaMobileDriverSupplier;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.logging.Level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDriverSupplierTest {

  public static OperaSettings customSettings;

  public TestDriver driver;
  public TestDriverSupplier supplier;

  @BeforeClass
  public static void initCustomSettings() {
    customSettings = new OperaSettings();
    customSettings.setProduct(OperaProduct.SDK);
    customSettings.logging().setLevel(Level.CONFIG);
  }

  @After
  public void removeDriver() {
    try {
      if (driver != null && driver.isRunning()) {
        driver.quit();
      }
    } finally {
      driver = null;
      supplier = null;
    }
  }

  @Test
  public void testOperaDriverSupplierSuppliesTestOperaDriver() {
    supplier = new TestOperaDriverSupplier();
    assertTrue(supplier.supplies(TestOperaDriver.class));
  }

  @Test
  public void testOperaMobileDriverSupplierSuppliesTestOperaMobileDriver() {
    supplier = new TestOperaMobileDriverSupplier();
    assertTrue(supplier.supplies(TestOperaMobileDriver.class));
  }

  @Test
  public void testOperaDesktopDriverSupplierSuppliesTestOperaDesktopDriver() {
    supplier = new TestOperaDesktopDriverSupplier();
    assertTrue(supplier.supplies(TestOperaDesktopDriver.class));
  }

  @Test
  public void testOperaDriverSupplierAutostarts() {
    assertTrue(new TestOperaDriverSupplier().getSettings().autostart());
  }

  @Test
  public void testOperaMobileDriverSupplierAutostarts() {
    assertTrue(new TestOperaMobileDriverSupplier().getSettings().autostart());
  }

  @Test
  public void testOperaDesktopDriverSupplierAutostarts() {
    assertTrue(new TestOperaDesktopDriverSupplier().getSettings().autostart());
  }

  @Test
  public void testOperaDriverSupplierProduct() {
    assertEquals(OperaProduct.DESKTOP, new TestOperaDriverSupplier().getSettings().getProduct());
  }

  @Test
  public void testOperaMobileDriverSupplierProduct() {
    assertEquals(OperaProduct.MOBILE,
                 new TestOperaMobileDriverSupplier().getSettings().getProduct());
  }

  @Test
  public void testOperaDesktopDriverSupplierProduct() {
    assertEquals(OperaProduct.DESKTOP,
                 new TestOperaDesktopDriverSupplier().getSettings().getProduct());
  }

  @Test
  public void testOperaDriverSupplierOverridesProductInCustomSettings() {
    supplier = new TestOperaDriverSupplier();
    supplier.setSettings(customSettings);
    assertEquals(OperaProduct.DESKTOP, supplier.getSettings().getProduct());
  }

  @Test
  public void testDriverSupplierOnlyOverridesLoggingLevelIfDefaultValue() {
    supplier = new TestOperaDriverSupplier();
    assertEquals(Level.INFO, supplier.getSettings().logging().getLevel());

    supplier.setSettings(customSettings);
    assertEquals(Level.CONFIG, supplier.getSettings().logging().getLevel());
  }

}