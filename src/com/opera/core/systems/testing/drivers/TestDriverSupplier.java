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

import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.OperaSettings;

/**
 * A driver supplier allows easy construction of differently configured OperaDriver's.
 *
 * TestDriverSupplier extends {@link Supplier<OperaDriver>} interface to make it possible to specify
 * settings on a supplier and query programmatically whether it supplies a driver of the given
 * type.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 * @see TestDriverBuilder
 */
public interface TestDriverSupplier extends Supplier<TestDriver> {

  /**
   * Changes the settings for this supplier.
   *
   * @param settings new settings
   */
  void setSettings(OperaSettings settings);

  /**
   * Does this supplier supply a driver of the given class?
   *
   * @param klass class to to check if this supplier supplies
   * @return true if supplier supplies <code>klass</code>, false otherwise
   */
  boolean supplies(Class<? extends TestDriver> klass);

}