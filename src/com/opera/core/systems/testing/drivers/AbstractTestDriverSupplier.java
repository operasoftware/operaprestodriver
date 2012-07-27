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

import com.opera.core.systems.OperaProduct;
import com.opera.core.systems.OperaSettings;

import java.util.logging.Level;

/**
 * Implements shared functionality across the various {@link TestDriverSupplier} implementations.
 * Specifically it allows you to get and set the capabilities for a particular driver supplier.
 */
public abstract class AbstractTestDriverSupplier implements TestDriverSupplier {

  protected OperaSettings settings;

  private final OperaProduct product;

  public AbstractTestDriverSupplier() {
    this(new OperaSettings());
  }

  public AbstractTestDriverSupplier(OperaProduct product) {
    this(new OperaSettings.Builder().product(product).get());
  }

  public AbstractTestDriverSupplier(OperaSettings settings) {
    setSettings(settings);
    product = settings.getProduct();
  }

  // TODO(andreastt): Consider using reflection for get() as well, although inheritance is bad

  public void setSettings(OperaSettings newSettings) {
    settings = newSettings;

    // Only override logging level if it has the default value
    if (new OperaSettings().logging().getLevel() == settings.logging().getLevel()) {
      settings.logging().setLevel(Level.FINE);
    }
  }

  public OperaSettings getSettings() {
    settings.setProduct(product);
    return settings;
  }

}