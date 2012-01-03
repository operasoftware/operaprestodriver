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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.Platform.WINDOWS;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class UtilsTest extends OperaDriverTestCase {

  // Make sure these tests only run if meta data is available.
  @Rule
  public MethodRule random = new MethodRule() {
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
      if (driver.services.getCoreUtils()
          .hasMetaInformation()) {  // If meta info is exposed, return tests
        return base;
      } else {
        return new Statement() {  // otherwise return an empty statement -> test doesn't run

          @Override
          public void evaluate() throws Throwable {
          }
        };
      }
    }
  };

  @Test
  public void testCoreVersion() {
    assertTrue(driver.utils().getCoreVersion().matches("\\d+\\.\\d+\\.\\d+"));
  }

  @Test
  public void testOS() {
    Assert.assertNotNull(driver.utils().getOS());
  }

  @Test
  public void testProductIsNotUnknown() {
    assertFalse(driver.utils().getProduct().contains("unknown"));
  }

  @Test
  public void testProductIsKnown() {
    assertTrue(driver.utils().getProduct().matches("core-gogi|desktop"));
  }

  @Test
  public void testBinaryPath() {
    Assert.assertNotNull(driver.utils().getBinaryPath());
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

}