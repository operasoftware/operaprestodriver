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

package com.opera.core.systems.pages;

import com.opera.core.systems.testing.Pages;
import com.opera.core.systems.testing.drivers.TestOperaDriver;

/**
 * Helper functionality for holding commonly used fields for page object pages.  Extend this class
 * and call <code>super(driver, pages)</code> to instantiate the fields <code>driver</code> and
 * <code>pages</code> to your page object.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public abstract class Page {

  protected TestOperaDriver driver;
  protected Pages pages;

  public Page(TestOperaDriver driver, Pages pages) {
    this.driver = driver;
    this.pages = pages;
  }

}