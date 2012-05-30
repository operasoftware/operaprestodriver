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

import org.junit.runners.model.FrameworkMethod;
import org.openqa.selenium.Platform;

import java.util.List;

/**
 * Decides whether a test class or a method should be ignored.
 */
public class TestIgnorance {

  private final IgnoreComparator ignoreComparator = new IgnoreComparator();
  private final List<String> services;
  private final Boolean idleEnabled;

  public TestIgnorance(List<String> services, boolean hasIdle, Platform platform,
                       OperaProduct product) {
    this.services = services;
    this.idleEnabled = hasIdle;
    ignoreComparator.setCurrentPlatform(platform);
    ignoreComparator.setCurrentProduct(product);
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
    return enabled != null && !idleEnabled;
  }

  private boolean isIgnoredDueToLackingService(RequiresService annotation) {
    if (annotation == null) {
      return false;
    }

    if (!services.contains(annotation.service())) {
      return true;
    } else if (services.contains(annotation.service()) &&
               annotation.version() == null) {
      return false;
    }

    /*
    return VersionUtil.compare(annotation.version(), "maxVersion") >= 0 ||
           VersionUtil
               .compare(annotation.version(), services.getMinVersionFor(annotation.service())) < 0;
               */
    return false;
  }

}