/*
Copyright 2011-2012 Opera Software ASA
Copyright 2011 Software Freedom Conservancy
Copyright 2011 WebDriver committers

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

package com.opera.core.systems.testing;

import com.opera.core.systems.OperaProduct;
import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.testing.drivers.OperaDriverBuilder;
import com.opera.core.systems.testing.drivers.TestOperaDriver;
import com.opera.core.systems.testing.drivers.TestOperaDriverSupplier;
import com.opera.core.systems.util.VersionUtil;

import org.junit.runners.model.FrameworkMethod;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Decides whether a test class or a method should be ignored.
 */
public class TestIgnorance {

  private final IgnoreComparator ignoreComparator = new IgnoreComparator();
  private Boolean idleEnabled = null;
  private ScopeServices services = null;

  public TestIgnorance(OperaProduct product, Platform platform) {
    ignoreComparator.addProduct(checkNotNull(product, "Product must be set"));
    ignoreComparator.setCurrentPlatform(checkNotNull(platform, "Platform must be set"));
  }

  // JUnit 4
  public boolean isIgnored(FrameworkMethod method, Object test) {
    boolean ignored = ignoreComparator.shouldIgnore(test.getClass().getAnnotation(Ignore.class)) ||
                      ignoreComparator.shouldIgnore(method.getMethod().getAnnotation(Ignore.class));

    ignored |= isIgnoredBecauseOfJUnit4Ignore(test.getClass().getAnnotation(org.junit.Ignore.class));
    ignored |= isIgnoredBecauseOfJUnit4Ignore(method.getMethod().getAnnotation(org.junit.Ignore.class));

    ignored |= isIgnoredDueToIdle(test.getClass().getAnnotation(IdleEnabled.class));
    ignored |= isIgnoredDueToIdle(method.getMethod().getAnnotation(IdleEnabled.class));

    ignored |= isIgnoredDueToLackingService(test.getClass().getAnnotation(RequiresService.class));
    ignored |= isIgnoredDueToLackingService(method.getClass().getAnnotation(RequiresService.class));

    return ignored;
  }

  private boolean isIgnoredBecauseOfJUnit4Ignore(org.junit.Ignore annotation) {
    return annotation != null;
  }

  private boolean isIgnoredDueToIdle(IdleEnabled enabled) {
    // If not specified, it should not be ignored
    if (enabled == null) {
      return false;
    }

    // If enabled (true), it should not be ignored
    if (idleEnabled != null) {
      return !idleEnabled;
    }

    // Spawn a new driver to check if idle is available.  If that fails, assume idle is not
    // available.
    TestOperaDriver driver = null;
    try {
      driver = OperaDriverTestCase.getWrappedDriver();
      if (driver == null) {
        driver = (TestOperaDriver) new OperaDriverBuilder(new TestOperaDriverSupplier()).get();
      }

      idleEnabled = driver.getServices().isOperaIdleAvailable();
    } catch (RuntimeException e) {
      idleEnabled = false;
    } finally {
      if (driver != null) {
        driver.quit();
      }
      driver = null;
    }

    return isIgnoredDueToIdle(enabled);
  }

  private boolean isIgnoredDueToLackingService(RequiresService annotation) {
    if (annotation == null) {
      return false;
    }

    if (services != null) {
      if (!services.getListedServices().contains(annotation.service())) {
        return true;
      } else if (services.getListedServices().contains(annotation.service()) &&
                 annotation.version() == null) {
        return false;
      }

      return VersionUtil.compare(annotation.version(), "maxVersion") >= 0 ||
          VersionUtil.compare(annotation.version(), services.getMinVersionFor(annotation.service())) < 0;
    }

    TestOperaDriver driver = null;
    try {
      driver = OperaDriverTestCase.getWrappedDriver();
      if (driver == null) {
        driver = (TestOperaDriver) new OperaDriverBuilder(new TestOperaDriverSupplier()).get();
      }

      services = driver.getServices();
    } catch (RuntimeException e) {
      throw new WebDriverException("Unknown error: " + e.getMessage());
    } finally {
      if (driver != null) {
        driver.quit();
      }
      driver = null;
    }

    return isIgnoredDueToLackingService(annotation);
  }

}