/*
Copyright 2008-2012 Opera Software ASA

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

package com.opera.core.systems.model;

import java.awt.*;

/**
 * The OperaColor class is used to encapsulate colours in the default sRGB colour space.
 * Specifically OperaColor extends {@link Color} and adds the functionality of parsing internal
 * Opera RGB strings to the {@link Color} object.
 *
 * It also holds colour values.
 *
 * It accepts a string for the format "rgb(120, 120, 120)" as a constructor argument.
 *
 * @author Deniz Turkoglu <dturkoglu@opera.com>, Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class OperaColor {

  private int id;
  private Integer lowRed;
  private Integer highRed;
  private Integer lowGreen;
  private Integer highGreen;
  private Integer lowBlue;
  private Integer highBlue;

  public OperaColor(int r, int g, int b) {
    setLowRed(r);
    setHighRed(r);
    setLowGreen(g);
    setHighGreen(g);
    setLowBlue(b);
    setHighBlue(b);
  }

  public Integer getLowRed() {
    return lowRed;
  }

  public void setLowRed(Integer lowRed) {
    this.lowRed = lowRed;
  }

  public Integer getHighRed() {
    return highRed;
  }

  public void setHighRed(Integer highRed) {
    this.highRed = highRed;
  }

  public Integer getLowGreen() {
    return lowGreen;
  }

  public void setLowGreen(Integer lowGreen) {
    this.lowGreen = lowGreen;
  }

  public Integer getHighGreen() {
    return highGreen;
  }

  public void setHighGreen(Integer highGreen) {
    this.highGreen = highGreen;
  }

  public Integer getLowBlue() {
    return lowBlue;
  }

  public void setLowBlue(Integer lowBlue) {
    this.lowBlue = lowBlue;
  }

  public Integer getHighBlue() {
    return highBlue;
  }

  public void setHighBlue(Integer highBlue) {
    this.highBlue = highBlue;
  }

}