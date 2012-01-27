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

import org.openqa.selenium.Capabilities;

/**
 * OperaDriverSupplier extends the Google Guava {@link Supplier<OperaDriver>} interface to make it
 * possible to specify capabilities on a supplier.
 *
 * A driver supplier allows easy construction of differently configured drivers.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 * @see OperaDriverBuilder
 */
public interface OperaDriverSupplier extends Supplier<OperaDriver> {

  /**
   * Changes the capabilities for this supplier.
   *
   * @param capabilities new capabilities
   */
  public void setCapabilities(Capabilities capabilities);

  /**
   * Gets the capabilities of this supplier.
   *
   * @return a set of capabilities
   */
  @SuppressWarnings("unused")
  public Capabilities getCapabilities();

}