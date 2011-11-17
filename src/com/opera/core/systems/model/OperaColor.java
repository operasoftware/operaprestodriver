/*
Copyright 2008-2011 Opera Software ASA

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

import java.awt.Color;

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
public class OperaColor extends Color {

  protected int id;
  protected Integer lowRed;
  protected Integer highRed;
  protected Integer lowGreen;
  protected Integer highGreen;
  protected Integer lowBlue;
  protected Integer highBlue;

  public OperaColor(int r, int g, int b) {
    super(r,g,b);

    setLowRed(r);
    setHighRed(r);
    setLowGreen(g);
    setHighGreen(g);
    setLowBlue(b);
    setHighBlue(b);
  }

  /**
   * @param rgb string of the format "rgb(120, 120, 120)"
   */
  public OperaColor(String rgb) {
    this(Integer.valueOf(rgb.replace("rgb(", "").replace(")", "").split(",")[0].trim()),
         Integer.valueOf(rgb.replace("rgb(", "").replace(")", "").split(",")[1].trim()),
         Integer.valueOf(rgb.replace("rgb(", "").replace(")", "").split(",")[2].trim()));
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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

  /**
   * Returns the HEX value representing the colour in the default sRGB ColorModel.
   *
   * @return the HEX value of the colour in the default sRGB ColorModel
   */
  public String getHex() {
    return toHex(getRed(), getGreen(), getBlue());
  }

  /**
   * Returns a web browser-friendly HEX value representing the colour in the default sRGB
   * ColorModel.
   *
   * @param r red
   * @param g green
   * @param b blue
   * @return a browser-friendly HEX value
   */
  public static String toHex(int r, int g, int b) {
    return "#" + toBrowserHexValue(r) + toBrowserHexValue(g) + toBrowserHexValue(b);
  }

  private static String toBrowserHexValue(int number) {
    StringBuilder builder = new StringBuilder(Integer.toHexString(number & 0xff));
    while (builder.length() < 2) {
      builder.append("0");
    }
    return builder.toString().toUpperCase();
  }

}