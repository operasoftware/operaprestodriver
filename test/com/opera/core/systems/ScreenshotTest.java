package com.opera.core.systems;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.Adler32;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.opera.core.systems.model.ScreenShotReply;

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
    FileInputStream fis= new FileInputStream(filename);
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

    Assert.assertEquals(one, two);
  }

  @Test
  public void testWrittenFilesSame() throws Exception {
    String one = "one.png", two = "two.png";

    radioLittle.saveScreenshot(one);
    radioSome.saveScreenshot(two);

    Assert.assertEquals(digest(one), digest(two));
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
    Assert.assertEquals(0, two.length());
  }

  @Test
  public void testRealPng() throws Exception {
    ScreenShotReply reply = driver.saveScreenshot(0);
    byte[] png = reply.getPng();

    Assert.assertTrue("PNG magic bytes match",
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

    Assert.assertEquals(one.getMd5(), two.getMd5());
    Assert.assertTrue("PNG data is the same", Arrays.equals(one.getPng(), two.getPng()));

    Assert.assertFalse(one.isBlank());
    Assert.assertFalse(two.isBlank());
  }

  // Can cause problems on Windows, so moved to last
  @Test
  public void testFlash() throws Exception {
    getFixture("flash.html");
    OperaWebElement img = (OperaWebElement) driver.findElementById("img_container");
    OperaWebElement flash = (OperaWebElement) driver.findElementById("flash_container");

    String imgMD5 = img.saveScreenshot("one.png");
    String flashMD5 = flash.saveScreenshot("two.png");

    Assert.assertEquals(imgMD5, flashMD5);
  }
}
