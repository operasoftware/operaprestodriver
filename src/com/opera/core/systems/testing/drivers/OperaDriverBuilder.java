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
import com.google.common.base.Throwables;

import com.opera.core.systems.OperaDriver;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * OperaDriverBuilder is a builder that supplies you with instances of {@link
 * com.opera.core.systems.OperaDriver} through the {@link #get()} method.
 *
 * You can either use it by specifying a {@link Supplier<OperaDriver>} class or the class it self as
 * a construction argument.  Call {@link #get()} to instantiate a new driver.
 *
 * The {@link Supplier} class supplies objects of a single type.  Semantically that could be,
 * factories, generators, builders, closures, or something else entirely.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class OperaDriverBuilder implements Supplier<OperaDriver> {

  private Class driverClass;
  private OperaDriverSupplier driverSupplier;
  private DesiredCapabilities capabilities;
  private Level loggingLevel = null;

  /**
   * By default, this class provides a plain {@link OperaDriver} instance object with the default
   * desired capabilities specified in Selenium's {@link DesiredCapabilities#opera()}.
   */
  public OperaDriverBuilder() {
    this(new DefaultOperaDriverSupplier(DesiredCapabilities.opera()));
  }

  /**
   * You may build a new Opera instance by specifying an implementation of the {@link
   * Supplier<OperaDriver>} interface.  The supplier may, for instance, perform pre- or post startup
   * steps needed for a custom driver configuration.
   *
   * @param supplier a supplier that implements {@link Supplier#get()}
   */
  public OperaDriverBuilder(OperaDriverSupplier supplier) {
    driverSupplier = supplier;
  }

  /**
   * You may build a new Opera instance by specifying a custom driver class.  The class must extend
   * the OperaDriver implementation.
   *
   * @param driverImplementation class reference to driver implementation
   */
  public OperaDriverBuilder(Class<? extends OperaDriver> driverImplementation) {
    driverClass = driverImplementation;
  }

  /**
   * Allows you to specify using which capabilities you'd like the driver to be instantiated with.
   *
   * @param c capabilities for the driver
   * @return a self reference
   */
  public OperaDriverBuilder using(Capabilities c) {
    capabilities = (DesiredCapabilities) c;
    return this;
  }

  /**
   * Allows you to override the logging levels specified in the capabilities.
   *
   * @param level the logging level you wish the driver to use
   * @return a self reference
   */
  public OperaDriverBuilder modifyLogLevel(Level level) {
    loggingLevel = level;
    return this;
  }

  /**
   * Retrieves an instance of {@link OperaDriver}.
   *
   * @return an instance of OperaDriver
   */
  public OperaDriver get() {
    OperaDriver driver;

    // Custom modifications based on local methods
    if (loggingLevel != null) {
      capabilities.setCapability(OperaDriver.LOGGING_LEVEL, loggingLevel);
    }

    DesiredCapabilities c = new DesiredCapabilities(getDefaultCapabilities(), capabilities);

    if (driverSupplier == null) {
      try {
        Class constructor = driverClass.getClass().getConstructor(Class.class).newInstance(c);
        driver = (OperaDriver) constructor.newInstance();
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
      driverSupplier.setCapabilities(c);
      driver = driverSupplier.get();
    }

    return driver;
  }

  private Capabilities getDefaultCapabilities() {
    DesiredCapabilities c = DesiredCapabilities.opera();

    if (driverSupplier == null) {
      try {
        Method getDefaultCapabilities;
        getDefaultCapabilities = driverClass.getClass().getMethod("getDefaultCapabilities");
        c = (DesiredCapabilities) getDefaultCapabilities.invoke(driverClass.getClass());
      } catch (NoSuchMethodException e) {
        // fall through
      } catch (InvocationTargetException e) {
        throw Throwables.propagate(e);
      } catch (IllegalAccessException e) {
        throw Throwables.propagate(e);
      }
    } else {
      c = (DesiredCapabilities) driverSupplier.getCapabilities();
    }

    return c;
  }

}