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
import com.opera.core.systems.internal.VersionUtil;
import com.opera.core.systems.scope.ScopeService;

import org.junit.runners.model.FrameworkMethod;
import org.openqa.selenium.Platform;

import java.util.Map;

/**
 * Decides whether a test class or a method should be ignored.
 */
public class TestIgnorance {

  private final IgnoreComparator ignoreComparator = new IgnoreComparator();
  private final Map<ScopeService, String> services;
  private final boolean hasLauncher;
  private final boolean idleEnabled;

  public TestIgnorance(Map<ScopeService, String> availableServices, boolean hasLauncher, boolean hasIdle,
                       Platform platform, OperaProduct product) {
    services = availableServices;
    idleEnabled = hasIdle;
    this.hasLauncher = hasLauncher;
    ignoreComparator.setCurrentPlatform(platform);
    ignoreComparator.setCurrentProduct(product);
  }

  // JUnit 4
  public boolean isIgnored(FrameworkMethod method, Object test) {
    return ignoreComparator.shouldIgnore(test.getClass().getAnnotation(Ignore.class)) ||
           ignoreComparator.shouldIgnore(method.getMethod().getAnnotation(Ignore.class)) ||

           isIgnoredBecauseOfJUnit4Ignore(test.getClass().getAnnotation(org.junit.Ignore.class)) ||
           isIgnoredBecauseOfJUnit4Ignore(method.getMethod().getAnnotation(org.junit.Ignore.class)) ||

           isIgnoredDueToIdle(test.getClass().getAnnotation(IdleEnabled.class)) ||
           isIgnoredDueToIdle(method.getMethod().getAnnotation(IdleEnabled.class)) ||

           isIgnoredDueToLackingService(test.getClass().getAnnotation(RequiresService.class)) ||
           isIgnoredDueToLackingService(method.getClass().getAnnotation(RequiresService.class)) ||

           isIgnoredBecauseOfLackingLocalEnvironment(test.getClass().getAnnotation(NeedsLocalEnvironment.class)) ||
           isIgnoredBecauseOfLackingLocalEnvironment(method.getClass().getAnnotation(NeedsLocalEnvironment.class));
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

    // If service is not available, ignore test.  If a version requirement has not been specified,
    // ignore the test regardless of version.
    if (!services.containsKey(annotation.value())) {
      return true;
    } else if (services.containsKey(annotation.value()) &&
               annotation.version() == null) {
      return false;
    }

    // Is available service version greater than the test's required version?
    return VersionUtil.compare(services.get(annotation.value()), annotation.version()) < 0;
  }

  private boolean isIgnoredBecauseOfLackingLocalEnvironment(NeedsLocalEnvironment annotation) {
    return annotation != null && hasLauncher;
  }

}