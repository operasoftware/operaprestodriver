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

import java.util.List;

/**
 * A wrapper object for screenshot reply, is used to overcome the problem of two different protocols
 * (XML/PB).
 */
public class ScreenShotReply {

  protected int windowId;
  protected String md5;
  protected byte[] png;
  protected boolean blank;
  protected boolean crashed;

  protected List<ColorResult> colorResult;

  public List<ColorResult> getColorResult() {
    return colorResult;
  }

  public void setColorResult(List<ColorResult> colorResult) {
    this.colorResult = colorResult;
  }

  public ScreenShotReply(String md5) {
    this.md5 = md5;
    this.png = null;
  }

  public ScreenShotReply(String md5, byte[] png) {
    this.md5 = md5;
    this.png = png.clone();
  }

  public ScreenShotReply(String md5, List<ColorResult> colorResult) {
    this.md5 = md5;
    this.colorResult = colorResult;
  }

  public int getWindowId() {
    return windowId;
  }

  public void setWindowId(int windowId) {
    this.windowId = windowId;
  }

  public String getMd5() {
    return md5;
  }

  public void setMd5(String md5) {
    this.md5 = md5;
  }

  public byte[] getPng() {
    return png;
  }

  public void setPng(byte[] png) {
    this.png = png.clone();
  }

  public boolean isBlank() {
    return blank;
  }

  public void setBlank(boolean blank) {
    this.blank = blank;
  }

  public boolean isCrashed() {
    return crashed;
  }

  public void setCrashed(boolean crashed) {
    this.crashed = crashed;
  }

}