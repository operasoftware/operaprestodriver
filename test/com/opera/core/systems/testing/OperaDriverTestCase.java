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

package com.opera.core.systems.testing;

import com.opera.core.systems.testing.drivers.TestOperaDriver;
import com.opera.core.systems.testing.drivers.TestOperaDriverSupplier;

/**
 * Test framework for {@link com.opera.core.systems.OperaDriver}.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 * @see OperaDesktopDriverTestCase
 */
public abstract class OperaDriverTestCase extends AbstractTestCase {

  protected TestOperaDriver driver;

  public OperaDriverTestCase() {
    super(new TestOperaDriverSupplier());
  }

  @Override
  public void createDriverIfNecessary() {
    super.createDriverIfNecessary();
    driver = (TestOperaDriver) super.driver;
  }

}