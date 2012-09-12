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

package com.opera.core.systems.runner.inprocess;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;

import com.opera.core.systems.common.hash.MD5;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Utility for taking screen captures using Java.
 *
 * Example:
 *
 * <pre><code>
 *   ScreenCapture capture = ScreenCapture.of(new Dimension(100, 200));
 *   try {
 *     System.out.println(capture.getMD5());
 *   } catch (IOException e) {
 *     e.printStackTrace();
 *   }
 * </code></pre>
 *
 * @see #of()
 * @see #of(java.awt.Dimension)
 */
public class ScreenCapture {

  private final Dimension dimensions;
  private byte[] data = null;

  public ScreenCapture(Dimension dimensions) {
    this.dimensions = dimensions;
  }

  /**
   * Takes the screen capture of the designated area.  If no dimensions are specified, it will take
   * a screenshot of the full screen by default.
   */
  public void take() throws AWTException {
    Rectangle area = new Rectangle(dimensions);

    Robot robot = new Robot();
    BufferedImage image = robot.createScreenCapture(area);
    data = getByteArray(image);
  }

  /**
   * Get the MD5 hash sum of the byte array of the data of the screen capture.
   *
   * @return the MD5 hash sum of this capture
   * @throws IOException if an I/O exception occurs
   */
  public String getMD5() throws IOException {
    checkCaptureTaken();

    try {
      return MD5.of(getData());
    } catch (NoSuchAlgorithmException e) {
      throw new IOException("Algorithm is not available in your environment: " + e.getMessage(), e);
    } catch (IOException e) {
      throw new IOException("Unable to open stream or file: " + e.getMessage(), e);
    }
  }

  /**
   * Get the a byte array of the raw data of this screen capture.
   *
   * @return raw data as a byte array
   */
  public byte[] getData() {
    checkCaptureTaken();
    return data;
  }

  private void checkCaptureTaken() {
    checkState(data != null, "Screen capture not taken yet");
  }

  /**
   * Takes a screen capture of the full screen.
   *
   * @return a capture of the whole screen
   * @throws IOException if an I/O exception occurs
   */
  public static ScreenCapture of() throws IOException {
    return of(Toolkit.getDefaultToolkit().getScreenSize());
  }

  /**
   * Takes a screen capture of the given dimensions.
   *
   * @param dimensions the dimensions of which to take a screen capture
   * @return a capture of the designated dimensions
   * @throws IOException if an I/O exception occurs
   */
  public static ScreenCapture of(Dimension dimensions) throws IOException {
    ScreenCapture capture = new ScreenCapture(dimensions);

    try {
      capture.take();
    } catch (AWTException e) {
      throw new IOException(e);
    }

    return capture;
  }

  /**
   * Get a byte array of the data of a buffered image.
   *
   * @param image the image to retrieve a byte array of
   * @return a byte array of the buffered image's data
   */
  private static byte[] getByteArray(BufferedImage image) {
    checkNotNull(image);

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    try {
      ImageIO.write(image, "png", stream);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    return stream.toByteArray();
  }

}