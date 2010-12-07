import java.io.*;
import java.util.*;
import java.awt.Dimension;

import junit.framework.*;
import junit.textui.*;

import com.opera.core.systems.*;
import com.opera.core.systems.runner.*;
import com.opera.core.systems.model.*;
import com.opera.core.systems.runner.launcher.*;
import com.opera.core.systems.settings.*;
import com.opera.core.systems.scope.internal.OperaIntervals;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.Platform;

public class OperaDriverTest extends TestCase 
{
	static OperaDriver driver;
	static OperaDriverSettings settings;	
	
	public void testConstructorWithoutSettingsObject()
	{
		driver = new OperaDriver();
		Assert.assertNotNull(driver);		
	}
	
	public void testIsOperaDriverConnected()
	{
		Assert.assertTrue(driver.isConnected());
	}
	
	public void testOperaBinaryPathPulledFromEnv()
	{
		File operaBinary = new File(driver.getSettings().getOperaBinaryLocation());
		Assert.assertTrue(operaBinary.exists());
	}
	
	public void testLauncherBinaryPathPulledFromEnv()
	{
		File launcherBinary = new File(driver.getSettings().getOperaLauncherBinary());
		Assert.assertTrue(launcherBinary.exists());
	}	
	
	public void testDebugPortPulledFromEnv()
	{
		Assert.assertTrue(OperaIntervals.SERVER_PORT.getValue() == 7001);
	}
	
	public void testIsECMAscriptDebuggerAvailable()
	{
		Assert.assertTrue(driver.isECMAscriptDebuggerAvailable());
	}
	
	public void testIsOperaIdleAvailable()
	{
		Assert.assertTrue(driver.isOperaIdleAvailable());
	}	
	
	public void testGetActionList()
	{
		Set<String> actionHash = driver.getOperaActionList();
		Assert.assertTrue(actionHash.size() > 0);
	}
	
	public void testOperaDriverShutdown()
	{
		driver.shutdown();
	}
	
	public void testConstructorWithSettingsObject()
	{
		settings = new OperaDriverSettings();
		settings.setRunOperaLauncherFromOperaDriver(true);
		settings.setOperaBinaryLocation(System.getProperty("test_gogi_binary_location"));
		settings.setOperaLauncherBinary(System.getProperty("test_launcher_binary_location"));			
		settings.setOperaBinaryArguments(" -fullscreen -nohw -url www.opera.com");
		driver = new OperaDriver(settings);
		Assert.assertNotNull(driver);			
	}
	
	public void testDefaultWindowCount()
	{			
		Assert.assertTrue(driver.getWindowCount() == 1);
	}
	
	public void testGetWindowHandle()
	{
		driver.get("http://t/core/standards/dom0/link/pathname/002.html");
		Assert.assertEquals("URL with explicit pathname and hash", driver.getWindowHandle());
	}	
	
	public void testGetTitle()
	{
		Assert.assertEquals("URL with explicit pathname and hash", driver.getTitle());
	}
	
	public void testGetText()
	{
		driver.get("http://t/core/standards/quotes/none.html");	
		Assert.assertEquals("you should see nothing below", driver.getText().trim());
	}
	
	public void testGetScreenshot()
	{
		driver.get("http://t/core/standards/SVG/Testsuites/W3C-1_1F2/images/paths-data-04-f.png");
		ScreenShotReply png = driver.saveScreenshot(3);
		Assert.assertTrue(png.getPng().toString().length() > 0);
	}
	
	public void testGetScreenshot2()
	{
		driver.get("http://t/core/standards/SVG/Testsuites/W3C-1_1F2/images/shapes-ellipse-01-f.png");
		ScreenShotReply png = driver.saveScreenshot(5, "0x81634ade48e575508756a00a8a4f3bc9");
		Assert.assertTrue(png.getMd5().length() > 0);
	}	
			
	public void testGetURL()
	{
		driver.get("www.ebay.co.uk");
		Assert.assertTrue(driver.getCurrentUrl().indexOf("www.ebay.co.uk") > 0);
	}
	
	public void testGetURL2()
	{
		driver.get("www.nyt.com", 15000);
		Assert.assertTrue(driver.getCurrentUrl().indexOf("www.nytimes.com") > 0);
	}	
	
	public void testGetWindowWidth()
	{
		Dimension dimension = driver.getDimensions();
		Assert.assertTrue(dimension.getWidth() == 1024);
	}
	
	public void testGetWindowHeight()
	{
		Dimension dimension = driver.getDimensions();
		Assert.assertTrue(dimension.getHeight() == 768);
	}	
	
	public void testOperaDriverShutdown2()
	{
		driver.shutdown();
	}	
}