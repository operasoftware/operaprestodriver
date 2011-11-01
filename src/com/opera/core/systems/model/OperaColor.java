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

/**
 * A simple pojo file to hold the colour values.
 *
 * @author Deniz Turkoglu <dturkoglu@opera.com>
 */
public class OperaColor {

  protected int id;
  protected Integer lowRed;
  protected Integer highRed;
  protected Integer lowGreen;
  protected Integer highGreen;
  protected Integer lowBlue;

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

  protected Integer highBlue;

}
