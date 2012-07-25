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

package com.opera.core.systems.testing;

import com.opera.core.systems.testing.drivers.TestOperaDesktopDriver;
import com.opera.core.systems.testing.drivers.TestOperaDesktopDriverSupplier;
import com.opera.core.systems.testing.drivers.TestOperaMobileDriver;
import com.opera.core.systems.testing.drivers.TestOperaMobileDriverSupplier;

/**
 * Test framework for {@link com.opera.core.systems.OperaMobileDriver}.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 * @see OperaDriverTestCase, OperaDesktopDriverTestCase
 */
public abstract class OperaMobileDriverTestCase extends AbstractTestCase {

  protected TestOperaMobileDriver driver;

  public OperaMobileDriverTestCase() {
    super(new TestOperaMobileDriverSupplier());
  }

  @Override
  public void createDriverIfNecessary() {
    super.createDriverIfNecessary();
    driver = (TestOperaMobileDriver) super.driver;
  }

}