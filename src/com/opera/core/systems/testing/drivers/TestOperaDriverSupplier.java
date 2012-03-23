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

import com.opera.core.systems.OperaSettings;

import java.util.logging.Level;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class TestOperaDriverSupplier extends DefaultOperaDriverSupplier {

  public TestOperaDriverSupplier() {
    super();
  }

  public TestOperaDriverSupplier(OperaSettings settings) {
    super(settings);
  }

  public TestOperaDriver get() {
    settings.logging().level(Level.FINE);
    return new TestOperaDriver(getSettings());
  }

}