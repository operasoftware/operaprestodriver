/*
Copyright 2012 Opera Software ASA
Copyright 2011 Software Freedom Conservancy
Copyright 2011 WebDriver committers

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

import com.google.common.collect.Sets;

import com.opera.core.systems.OperaProduct;

import org.openqa.selenium.Platform;

import java.util.Set;

/**
 * @author Simon Stewart
 */
public class IgnoreComparator {

  private final Set<OperaProduct> ignored = Sets.newHashSet();
  private Platform currentPlatform = Platform.getCurrent();

  public void addProduct(OperaProduct productToIgnore) {
    ignored.add(productToIgnore);
  }

  public void setCurrentPlatform(Platform platform) {
    currentPlatform = platform;
  }

  public boolean shouldIgnore(Ignore ignoreAnnotation) {
    if (ignoreAnnotation == null) {
      return false;
    }

    if (ignoreAnnotation.products().length == 0) {
      return true;
    }

    for (OperaProduct product : ignoreAnnotation.products()) {
      if (ignored.contains(product) || product == OperaProduct.ALL) {
        for (Platform platform : ignoreAnnotation.platforms()) {
          if (platform.is(currentPlatform)) {
            return true;
          }
        }
      }
    }

    return false;
  }

}