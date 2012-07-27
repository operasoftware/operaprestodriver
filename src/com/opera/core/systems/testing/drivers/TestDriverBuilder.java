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

import com.google.common.base.Supplier;

import com.opera.core.systems.OperaProduct;
import com.opera.core.systems.OperaSettings;

import org.openqa.selenium.Capabilities;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TestDriverBuilder is a builder that supplies you with instances of {@link TestDriver} through the
 * {@link #get()} method.
 *
 * You can either use it by specifying a {@link Supplier<TestDriver>} class or the class it self as
 * a construction argument.  Call {@link #get()} to instantiate a new driver.
 *
 * The {@link Supplier} class supplies objects of a single type.  Semantically that could be
 * factories, generators, builders, closures, or something else entirely.
 */
public class TestDriverBuilder implements Supplier<TestDriver> {

  private Class<? extends TestDriver> driverClass = null;
  private TestDriverSupplier driverSupplier;
  private OperaSettings settings = new OperaSettings();
  private Level loggingLevel = null;

  /**
   * By default, this class provides a plain {@link TestOperaDriver} instance object with the
   * default capabilities.
   */
  public TestDriverBuilder() {
    this(detect());
  }

  /**
   * You may build a new Opera instance by specifying an implementation of the {@link
   * Supplier<TestDriver>} interface.  The supplier may, for instance, perform pre- or post startup
   * steps needed for a custom driver configuration.
   *
   * @param supplier a supplier that implements {@link Supplier#get()}
   */
  public TestDriverBuilder(TestDriverSupplier supplier) {
    driverSupplier = supplier;
  }

  /**
   * You may build a new Opera instance by specifying a custom driver class.  The class must extend
   * the {@link TestDriver} interface.
   *
   * @param driverImplementation class reference to driver implementation
   */
  public TestDriverBuilder(Class<? extends TestDriver> driverImplementation) {
    driverClass = driverImplementation;
  }

  /**
   * Specify which settings you'd like the driver to be instantiated with.
   *
   * @param settings settings for the driver
   * @return a self reference
   */
  public TestDriverBuilder using(OperaSettings settings) {
    this.settings = settings;
    return this;
  }

  /**
   * Specify which capabilities you'd like the driver to be instantiated with.
   *
   * @param capabilities capabilities for the driver
   * @return a self reference
   */
  public TestDriverBuilder using(Capabilities capabilities) {
    this.settings = new OperaSettings().merge(capabilities);
    return this;
  }

  /**
   * Allows you to override the logging levels specified in the capabilities.
   *
   * @param level the logging level you wish the driver to use
   * @return a self reference
   */
  @SuppressWarnings("unused")
  public TestDriverBuilder modifyLogLevel(Level level) {
    loggingLevel = level;
    return this;
  }

  /**
   * Retrieves an instance.
   *
   * @return an instance of TestDriver
   */
  public TestDriver get() {
    TestDriver driver;

    // Overrides defined by builder
    if (loggingLevel != null) {
      settings.logging().setLevel(loggingLevel);
    }

    if (driverSupplier == null) {
      checkNotNull(driverClass, "No driver class specified");

      try {
        Class constructor = driverClass
            .getClass().getConstructor(Class.class).newInstance(settings);
        driver = (TestDriver) constructor.newInstance();
      } catch (NoSuchMethodException e) {
        throw new RuntimeException("Unable to recognize implementation's constructor");
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Illegal access: " + e.getMessage());
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      }
    } else {
      driverSupplier.setSettings(settings);
      driver = driverSupplier.get();
    }

    return driver;
  }

  public static TestDriverSupplier detect() {
    OperaProduct product = OperaProduct.get(System.getProperty("test.opera.product"));

    switch (product) {
      case DESKTOP:
        return new TestOperaDesktopDriverSupplier();

      case MOBILE:
        return new TestOperaMobileDriverSupplier();

      default:
        return new TestOperaDriverSupplier();
    }
  }

}