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

import com.opera.core.systems.model.ScreenShotReply;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.OutputType;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.Adler32;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScreenshotTest extends TestBase {

  private OperaWebElement text;
  private OperaWebElement radioLittle;
  private OperaWebElement radioSome;
  private OperaWebElement radioLots;

  @Before
  public void setUp() throws Exception {
    getFixture("test.html");

    text = (OperaWebElement) driver.findElementById("input_email");
    radioSome = (OperaWebElement) driver.findElementById("radio_some");
    radioLittle = (OperaWebElement) driver.findElementById("radio_little");
    radioLots = (OperaWebElement) driver.findElementById("radio_lots");
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    new File("one.png").delete();
    new File("two.png").delete();
  }

  private static int digest(String filename) throws IOException {
    FileInputStream fis = new FileInputStream(filename);
    byte[] data = new byte[fis.available()];

    int i = 0;
    int c;
    while ((c = fis.read()) != -1) {
      data[i++] = (byte) c;
    }
    fis.close();

    final Adler32 digester = new Adler32();
    digester.update(data);

    return (int) digester.getValue();
  }

  @Test
  public void testElementsSame() throws Exception {
    String one = radioLittle.saveScreenshot("one.png");
    String two = radioSome.saveScreenshot("two.png");

    assertEquals(one, two);
  }

  @Test
  public void testWrittenFilesSame() throws Exception {
    String one = "one.png", two = "two.png";

    radioLittle.saveScreenshot(one);
    radioSome.saveScreenshot(two);

    assertEquals(digest(one), digest(two));
  }

  @Test
  public void testElementsNotSame() throws Exception {
    String one = radioLittle.saveScreenshot("one.png");
    String two = radioLots.saveScreenshot("two.png");

    Assert.assertNotSame(one, two);
  }

  @Test
  public void testWrittenFilesNotSame() throws Exception {
    String one = "one.png", two = "two.png";

    radioLittle.saveScreenshot(one);
    radioLots.saveScreenshot(two);

    Assert.assertNotSame(digest(one), digest(two));
  }

  @Test
  public void testTimout() throws Exception {
    getFixture("timer.html");
    OperaWebElement text = (OperaWebElement) driver.findElementById("one");

    String original = text.saveScreenshot("one.png");
    String changed = text.saveScreenshot("two.png", 2000);

    Assert.assertNotSame(original, changed);
  }

  @Test
  public void testHashes() throws Exception {
    String md5 = text.saveScreenshot("one.png");
    text.saveScreenshot("two.png", 0, true, md5);

    File one = new File("one.png");
    File two = new File("two.png");

    // Confirm that a png is not returned when the hash matches
    Assert.assertNotSame(0, one.length());
    assertEquals(0, two.length());
  }

  @Test
  public void testRealPng() throws Exception {
    ScreenShotReply reply = driver.saveScreenshot(0);
    byte[] png = reply.getPng();

    assertTrue("PNG magic bytes match",
               png[0] == (byte) 0x89 &&
               png[1] == (byte) 0x50 &&
               png[2] == (byte) 0x4E &&
               png[3] == (byte) 0x47 &&
               png[4] == (byte) 0x0D &&
               png[5] == (byte) 0x0A &&
               png[6] == (byte) 0x1A &&
               png[7] == (byte) 0x0A
    );
  }

  /**
   * Tests the saveScreenshot method that returns a ScreenShotReply
   */
  @Test
  public void testReply() throws Exception {
    ScreenShotReply one = radioLittle.saveScreenshot(0);
    ScreenShotReply two = radioSome.saveScreenshot(0);

    assertEquals(one.getMd5(), two.getMd5());
    assertTrue("PNG data is the same", Arrays.equals(one.getPng(), two.getPng()));

    assertFalse(one.isBlank());
    assertFalse(two.isBlank());
  }

  @Test
  public void testZeroHeight() throws Exception {
    // Shouldn't throw exceptions

    getFixture("zero-height-standards.html");
    ((OperaWebElement) driver.findElementByTagName("html")).getImageHash();

    getFixture("zero-height-quirks.html");
    ((OperaWebElement) driver.findElementByTagName("html")).getImageHash();
  }

  // Can cause problems on Windows, so moved to last
  @Ignore(value = "We don't support taking single element screenshots of plugins")
  @Test
  public void testFlash() throws Exception {
    getFixture("flash.html");
    OperaWebElement img = (OperaWebElement) driver.findElementById("img_container");
    OperaWebElement flash = (OperaWebElement) driver.findElementById("flash_container");

    String imgMD5 = img.saveScreenshot("one.png");
    String flashMD5 = flash.saveScreenshot("two.png");

    assertEquals(imgMD5, flashMD5);
  }

  @Test
  public void testTakesScreenshot() throws Exception {
    getFixture("tall.html");
    File file = driver.getScreenshotAs(OutputType.FILE);

    BufferedImage img = ImageIO.read(file);
    assertEquals(5100, img.getHeight());

    // Check the top pixel
    int botcol = img.getRGB(0, 0);
    assertEquals(0xFF0000, botcol & 0xFFFFFF);
  }

  @Test
  @Ignore(value = "Opera problem. Areas outside current viewport are black.")
  public void testFullScreenshot() throws Exception {
    getFixture("tall.html");
    File file = driver.getScreenshotAs(OutputType.FILE);

    BufferedImage img = ImageIO.read(file);
    assertEquals(5100, img.getHeight());

    // Make sure the bottom colour is green, not black.
    int botcol = img.getRGB(0, 5050);
    assertEquals(0xFF00, botcol & 0xFF00);

  }

}