package com.opera.core.systems;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.RenderedWebElement;

import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.settings.OperaDriverSettings;

public class OperaDriverTest extends TestBase
{
	OperaDriver driver;
	TestOperaDriver testDriver;
	OperaDriverSettings settings;

  // Replace the TestBase setup and teardown so that we don't launch opera
	@BeforeClass
  public static void setUpBeforeClass() throws Exception {}
	@AfterClass
  public static void tearDownAfterClass() throws Exception {}

	@Test
	public void testConstructorWithoutSettingsObject()
	{
		testDriver = new TestOperaDriver();
		Assert.assertNotNull(driver);
	}

  @Test
	public void testOperaBinaryPathPulledFromEnv()
	{
		File operaBinary = new File(testDriver.getSettings().getOperaBinaryLocation());
		Assert.assertTrue(operaBinary.exists());
	}

  @Test
	public void testLauncherBinaryPathPulledFromEnv()
	{
		File launcherBinary = new File(testDriver.getSettings().getOperaLauncherBinary());
		Assert.assertTrue(launcherBinary.exists());
	}

  @Test
	public void testDebugPortPulledFromEnv()
	{
		Assert.assertTrue(OperaIntervals.SERVER_PORT.getValue() == 7001);
	}



  @Test
	public void testOperaDriverShutdown()
	{
    testDriver.shutdown();
	}

  @Test
	public void testConstructorWithSettingsObject()
	{
		settings = new OperaDriverSettings();
		settings.setRunOperaLauncherFromOperaDriver(true);
		settings.setOperaBinaryLocation(binary);
		settings.setOperaLauncherBinary(launcher);
		settings.setOperaBinaryArguments(" -fullscreen -nohw -url www.opera.com");
		driver = new OperaDriver(settings);
		Assert.assertNotNull(driver);
	}

  @Test
	public void testDefaultWindowCount()
	{
		Assert.assertTrue(driver.getWindowCount() == 1);
	}

  @Test
	public void testGetWindowHandle()
	{
		driver.get("http://t/core/standards/dom0/link/pathname/002.html");
		Assert.assertEquals("URL with explicit pathname and hash", driver.getWindowHandle());
	}

  @Test
	public void testGetTitle()
	{
		Assert.assertEquals("URL with explicit pathname and hash", driver.getTitle());
	}

  @Test
	public void testGetText()
	{
		driver.get("http://t/core/standards/quotes/none.html");
		Assert.assertEquals("you should see nothing below", driver.findElementByTagName("body").getText().trim());
	}

  @Test
	public void testGetScreenshot()
	{
		driver.get("http://t/core/standards/SVG/Testsuites/W3C-1_1F2/images/paths-data-04-f.png");
		ScreenShotReply png = driver.saveScreenshot(3);
		Assert.assertTrue(png.getPng().length > 0);
	}

  @Test
	public void testGetScreenshot2()
	{
		driver.get("http://t/core/standards/SVG/Testsuites/W3C-1_1F2/images/shapes-ellipse-01-f.png");
		ScreenShotReply png = driver.saveScreenshot(5, "0x81634ade48e575508756a00a8a4f3bc9");
		Assert.assertTrue(png.getMd5().length() > 0);
	}

  @Test
	public void testGetURL()
	{
		driver.get("www.ebay.co.uk");
		Assert.assertTrue(driver.getCurrentUrl().indexOf("www.ebay.co.uk") > 0);
	}

  @Test
	public void testGetURL2()
	{
		driver.get("www.nyt.com", 15000);
		Assert.assertTrue(driver.getCurrentUrl().indexOf("www.nytimes.com") > 0);
	}

  @Test
	public void testGetWindowWidth()
	{
		Dimension dimension = ((RenderedWebElement)driver.findElementByTagName("body")).getSize();
		Assert.assertTrue(dimension.getWidth() == 1024);
	}

  @Test
	public void testGetWindowHeight()
	{
		Dimension dimension = ((RenderedWebElement)driver.findElementByTagName("body")).getSize();
		Assert.assertTrue(dimension.getHeight() == 768);
	}

  @Test
	public void testOperaDriverShutdown2()
	{
		driver.shutdown();
	}
}
