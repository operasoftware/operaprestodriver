/**
 *
 */
package com.opera.core.systems;


import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.Adler32;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    String one = radioLittle.saveScreenshot("/tmp/one.png");
    String two = radioSome.saveScreenshot("/tmp/two.png");

    Assert.assertEquals(one, two);
  }

  @Test
  public void testWrittenFilesSame() throws Exception {
    String one = "/tmp/one.png", two = "/tmp/two.png";

    radioLittle.saveScreenshot(one);
    radioSome.saveScreenshot(two);

    Assert.assertEquals(digest(one), digest(two));
  }

  @Test
  public void testElementsNotSame() throws Exception {
    String one = radioLittle.saveScreenshot("/tmp/one.png");
    String two = radioLots.saveScreenshot("/tmp/two.png");

    Assert.assertNotSame(one, two);
  }

  @Test
  public void testWrittenFilesNotSame() throws Exception {
    String one = "/tmp/one.png", two = "/tmp/two.png";

    radioLittle.saveScreenshot(one);
    radioLots.saveScreenshot(two);

    Assert.assertNotSame(digest(one), digest(two));
  }

  @Test
  public void testFlash() throws Exception {
    getFixture("flash.html");
    OperaWebElement img = (OperaWebElement) driver.findElementById("img_container");
    OperaWebElement flash = (OperaWebElement) driver.findElementById("flash_container");

    String imgMD5 = img.saveScreenshot("/tmp/one.png");
    String flashMD5 = flash.saveScreenshot("/tmp/two.png");

    Assert.assertEquals(imgMD5, flashMD5);
  }

  @Test
  public void testTimout() throws Exception {
    getFixture("timer.html");
    OperaWebElement text = (OperaWebElement) driver.findElementById("one");

    driver.saveScreenshot(timeout, hashes)

    String original = text.saveScreenshot("/tmp/one.png");
    String changed = text.saveScreenshot("/tmp/two.png", 2000);

    Assert.assertNotSame(original, changed);
  }

  @Test
  public void testHashes() throws Exception {
    String md5 = text.saveScreenshot("/tmp/one.png");
    text.saveScreenshot("/tmp/two.png", 0, true, md5);

    File one = new File("/tmp/one.png");
    File two = new File("/tmp/two.png");

    Assert.assertNotSame(0, one.length());
    Assert.assertEquals(0, two.length());
  }
}
