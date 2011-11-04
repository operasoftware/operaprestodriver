/*
Copyright 2011 Opera Software ASA

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

/**
 * A list of all Opera products.  Use the {@link #is(OperaProduct)} to compare and check products.  Some
 * of the products, such as the general definition CORE might map to several sub-products; in this
 * case CORE_DESKTOP, CORE_SMARTPHONE and CORE_TV.
 */
public enum OperaProduct {

  CORE_DESKTOP("core-desktop"),
  CORE_SMARTPHONE("core-smartphone"),
  CORE_TV("core-tv"),
  DESKTOP("desktop"),
  SDK("sdk"),
  MOBILE("mobile"),
  MINI("mini"),

  /**
   * Most core products are quite similar.  <code>CORE</code> will match <code>CORE_DESKTOP</code>,
   * <code>CORE_SMARTPHONE</code> and <code>CORE_TV</code>.
   */
  CORE("core") {
    @Override
    public boolean is(OperaProduct compareWith) {
      return compareWith == CORE || compareWith == CORE_DESKTOP || compareWith == CORE_SMARTPHONE
             || compareWith == CORE_TV;
    }
  },

  /**
   * Never returned, but can be used to request any product.
   */
  ALL("") {
    @Override
    public boolean is(OperaProduct compareWith) {
      return true;
    }
  };

  OperaProduct(String a) {
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