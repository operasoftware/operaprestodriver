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

package com.opera.core.systems;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Rotatable;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.TouchScreen;

/**
 * A WebDriver implementation for Opera Mobile.  It relies heavily on {@link OperaDriver} and
 * supplies some extra device specific functionality.
 */
public class OperaMobileDriver extends OperaDriver implements HasTouchScreen, Rotatable {

  private TouchScreen touchScreen;

  public OperaMobileDriver() {
    this(new OperaSettings.Builder().product(OperaProduct.MOBILE).get());
  }

  public OperaMobileDriver(Capabilities capabilities) {
    super(capabilities);
  }

  public OperaMobileDriver(OperaSettings settings) {
    super(settings);
    super.settings.setProduct(OperaProduct.MOBILE);
  }

  @Override
  protected void init() {
    super.init();
    touchScreen = new OperaTouchScreen(this);
  }

  public void rotate(ScreenOrientation orientation) {
    throw new UnsupportedOperationException("Not implemented");
  }

  public ScreenOrientation getOrientation() {
    return ScreenOrientation.PORTRAIT;
  }

  public TouchScreen getTouch() {
    return touchScreen;
  }

}
