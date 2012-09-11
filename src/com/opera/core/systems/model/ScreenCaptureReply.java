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
 * Represents a screen capture reply.
 *
 * A wrapper object for screenshot reply, is used to overcome the problem of two different protocols
 * (XML/PB).
 */
public class ScreenCaptureReply {

  private String md5 = null;
  private byte[] png = null;
  private boolean blank = false;
  private boolean crashed = false;

  protected List<ColorResult> colorResults;

  public ScreenCaptureReply() {
    this.md5 = null;
    this.png = null;
  }

  public ScreenCaptureReply(String md5) {
    this.md5 = md5;
    this.png = null;
  }

  public ScreenCaptureReply(String md5, byte[] png) {
    this.md5 = md5;
    this.png = png.clone();
  }

  public ScreenCaptureReply(String md5, List<ColorResult> colorResult) {
    this.md5 = md5;
    this.colorResults = colorResult;
    this.png = null;
  }

  /**
   * Gets the MD5 hash sum of the screen capture's png.
   *
   * @return the MD5 hash sum, if available, of the screen capture's data
   */
  public String getMd5() {
    return md5;
  }

  /**
   * Gets the png of the screen capture as a byte array.
   *
   * @return the data of the capture
   */
  public byte[] getPng() {
    return png;
  }

  /**
   * Gets the color results of the screen capture if available.
   *
   * @return list of color results
   */
  public List<ColorResult> getColorResults() {
    return colorResults;
  }

  /**
   * Returns true if the screen capture taken evaluates to a blank capture.
   *
   * @return true if the capture resembles the state of a blank screen capture, false otherwise
   */
  public boolean isBlank() {
    return blank;
  }

  /**
   * Returns true if the screen capture taken evaluates to a crashed screen capture.
   *
   * @return true if the capture resembles the state of a crashed Opera build, false otherwise
   */
  public boolean isCrashed() {
    return crashed;
  }

  /**
   * Returns a new builder.  The generated builder is equivalent to the builder created by the
   * {@link ScreenCaptureReply.Builder} constructor.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * A builder for creating screen capture reply instances.
   *
   * Example:
   *
   * <pre><code>
   *   ScreenCaptureReply.Builder builder = ScreenCaptureReply.builder();
   *   builder.setBlank(true);
   *   builder.setCrashed(true);
   *   ScreenCaptureReply reply = builder.build();
   * </code></pre>
   */
  public static class Builder {

    private final ScreenCaptureReply reply = new ScreenCaptureReply();

    /**
     * Specify the hash sum of the capture.
     *
     * @param md5 the MD5 hash sum of the data
     * @return a self-reference
     */
    public Builder setMD5(String md5) {
      reply.md5 = md5;
      return this;
    }

    /**
     * Specify the byte array of png of the screen capture.
     *
     * @param data a byte array
     * @return a self-reference
     */
    public Builder setPNG(byte[] data) {
      reply.png = data.clone();
      return this;
    }

    /**
     * Specify the color results of the screen capture.
     *
     * @param colorResults the color results
     * @return a self-reference
     */
    @SuppressWarnings("unused")
    public Builder setColorResult(List<ColorResult> colorResults) {
      reply.colorResults = colorResults;
      return this;
    }

    /**
     * Specify if the {@link ScreenCaptureReply} should evaluate to a blank screen capture.
     *
     * @param blank true if the reply is a blank screen capture, false otherwise
     * @return a self-reference
     */
    public Builder setBlank(boolean blank) {
      reply.blank = blank;
      return this;
    }

    /**
     * Specify if the {@link ScreenCaptureReply} should evaluate to a crash.
     *
     * @param crashed true if the reply is a crash, false otherwise
     * @return a self-reference
     */
    public Builder setCrashed(boolean crashed) {
      reply.crashed = crashed;
      return this;
    }

    /**
     * Returns a newly created {@link ScreenCaptureReply} based on the contents of the builder.
     */
    public ScreenCaptureReply build() {
      return reply;
    }

  }

}