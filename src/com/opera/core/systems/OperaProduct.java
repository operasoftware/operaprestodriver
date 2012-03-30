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

import org.openqa.selenium.WebDriverException;

import java.util.HashMap;
import java.util.Map;

/**
 * A list of all Opera products.  Use the {@link #is(OperaProduct)} to compare and check products.
 */
public enum OperaProduct {

  CORE("core-gogi"),
  DESKTOP("desktop"),
  SDK("sdk"),
  MOBILE("mobile"),
  MINI("mini"),

  /**
   * Never returned, but can be used to request any product.
   */
  ALL("") {
    @Override
    public boolean is(OperaProduct compareWith) {
      return true;
    }
  };

  private final String product;
  private static final Map<String, OperaProduct> lookup = new HashMap<String, OperaProduct>();

  static {
    for (OperaProduct p : OperaProduct.values()) {
      lookup.put(p.toString(), p);
    }
  }

  OperaProduct(String product) {
    this.product = product;
  }

  public String toString() {
    return product;
  }

  public static OperaProduct get(String product) {
    if (!lookup.containsKey(product)) {
      throw new WebDriverException("Unknown product: " + product);
    }

    return lookup.get(product);
  }

  /**
   * Compares current product with given product.  Some products, such as the general product CORE,
   * might map to several different products, such as CORE_DESKTOP, CORE_SMARTPHONE and CORE_TV.
   *
   * @param compareWith the product to compare the current one with
   * @return true if the products are the same, false otherwise
   */
  public boolean is(OperaProduct compareWith) {
    return this.equals(compareWith);
  }

}