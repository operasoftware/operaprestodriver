import java.io.*;
import java.util.*;

import junit.framework.*;
import junit.textui.*;

import com.opera.core.systems.runner.*;
import com.opera.core.systems.runner.launcher.*;
import com.opera.core.systems.settings.*;
import org.openqa.selenium.Platform;

public class OperaLauncherRunnerTest extends TestCase 
{	
	static OperaDriverSettings settings;
	static OperaLauncherRunner runner;
	
	public void testOperaDriverSettings()
	{
		settings = new OperaDriverSettings();
		Assert.assertNotNull(settings);	
	}
	
	public void testDefaultDoRunOperaLauncherFromOperaDriver()
	{
		// default is to run launcher from the driver, so this should always be true
		Assert.assertTrue(settings.doRunOperaLauncherFromOperaDriver()); 			
	}
	
	public void testSetRunOperaLauncherFromOperaDriver()
	{
		settings.setRunOperaLauncherFromOperaDriver(false);
		Assert.assertFalse(settings.doRunOperaLauncherFromOperaDriver()); 
	}
	
	public void testDefaultLauncherListeningPort()
	{
		// the listening port should default to 9999
		Assert.assertEquals(settings.getOperaLauncherListeningPort(), 9999);		
	}
	
	public void testSetLauncherListeningPort()
	{
		settings.setOperaLauncherListeningPort(5555);
		Assert.assertEquals(settings.getOperaLauncherListeningPort(), 5555);
	}
	
	public void testDefaultOperaBinaryLocation()
	{
		// the opera binary location should default to null
		Assert.assertNull(settings.getOperaBinaryLocation());		
	}	
	
	public void testSetOperaBinaryLocation()
	{
		settings.setOperaBinaryLocation("/spartan/ramdisk/launcher");
		Assert.assertEquals(settings.getOperaBinaryLocation(), "/spartan/ramdisk/launcher");
	}	
	
	public void testDefaultOperaBinaryArguments()
	{
		// the opera binary arguments should default to null
		Assert.assertEquals(settings.getOperaBinaryArguments(), "");		
	}	
	
	public void testSetOperaBinaryArguments()
	{
		settings.setOperaBinaryArguments("-host 127.0.0.1 -port 12199 -bin /spartan/ramdisk/install/launcher");
		Assert.assertEquals(settings.getOperaBinaryArguments(), "-host 127.0.0.1 -port 12199 -bin /spartan/ramdisk/install/launcher");
	}		
	
	public void testSetOperaBinaryLocationFromPropertyInBuildXML()
	{
		settings.setOperaBinaryLocation("");
		settings.setOperaBinaryLocation(System.getProperty("test_gogi_binary_location"));
		Assert.assertNotSame(settings.getOperaBinaryLocation(), "");
	}
	
	public void testThatOperaBinaryFromPropertyInBuildXMLExists()
	{
		File operaBinary = new File(System.getProperty("test_gogi_binary_location"));
		Assert.assertTrue(operaBinary.exists());
	}
	
	public void testSetOperaLauncherBinaryLocationFromPropertyInBuildXML()
	{
		settings.setOperaLauncherBinary("");
		settings.setOperaLauncherBinary(System.getProperty("test_launcher_binary_location"));
		Assert.assertNotSame(settings.getOperaLauncherBinary(), "");
	}	
	
	public void testThatOperaLauncherBinaryFromPropertyInBuildXMLExists()
	{
		File operaLauncherBinary = new File(System.getProperty("test_launcher_binary_location"));
		Assert.assertTrue(operaLauncherBinary.exists());
	}	
	
	public void testOperaLauncherRunnerConstructorWithSettings()
	{
		settings.setRunOperaLauncherFromOperaDriver(true);
		settings.setOperaBinaryArguments("");
		runner = new OperaLauncherRunner(settings);
		Assert.assertNotNull(runner);	
	}
	
	public void testDefaultCrashedState()
	{
		Assert.assertFalse(runner.hasOperaCrashed());	
	}
	
	public void testDefaultIsOperaRunning()
	{
		Assert.assertFalse(runner.isOperaRunning());
	}
	
	public void testStartOpera()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}
	
	public void testShutdownOpera()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}	
	
	public void testShutdownLauncher()
	{
		runner.shutdown();
		
		try
		{
			runner.startOpera();
			fail("This test should have generated a OperaRunnerException as we tried to start Opera when the Launcher isn't running...");
		}
		catch (OperaRunnerException e)
		{			
		}		
	}
	
	// BEGIN TESTING WITH DIFFERENT OPERA ARGUMENTS 
	
	public void testOperaLauncherRunnerConstructorWithSettings2()
	{
		settings.setRunOperaLauncherFromOperaDriver(true);
		settings.setOperaBinaryArguments("-nowindow");
		runner = new OperaLauncherRunner(settings);
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());	
	}
	
	public void testShutdown2()
	{
		runner.stopOpera();
		runner.shutdown();
		
		try
		{
			runner.isOperaRunning();
			fail("This test should have generated a OperaRunnerException as we tried to check on the Opera process after being shutdown...");
		}
		catch (OperaRunnerException e)
		{			
		}			
	}		
	
	public void testOperaLauncherRunnerConstructorWithSettings3()
	{
		settings.setRunOperaLauncherFromOperaDriver(true);
		settings.setOperaBinaryArguments(" -geometry 1024x768 -nohw");
		runner = new OperaLauncherRunner(settings);
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());	
	}
	
	public void testShutdown3()
	{
		runner.stopOpera();
		runner.shutdown();
		
		// verify that a second shutdown call doesn't do any harm (shouldn't)
		runner.shutdown();		
	}	
	
	public void testStartAndStopOperaTenTimesRoundOneStart()
	{
		settings.setOperaBinaryArguments(" -geometry 640x480 -nohw");
		runner = new OperaLauncherRunner(settings);
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}
	
	public void testStartAndStopOperaTenTimesRoundOneStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}	
	
	public void testStartAndStopOperaTenTimesRoundTwoStart()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}
	
	public void testStartAndStopOperaTenTimesRoundTwoStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}		
	
	public void testStartAndStopOperaTenTimesRoundThreeStart()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}
	
	public void testStartAndStopOperaTenTimesRoundThreeStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}		
	
	public void testStartAndStopOperaTenTimesRoundFourStart()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}
	
	public void testStartAndStopOperaTenTimesRoundFourStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}	
	
	public void testStartAndStopOperaTenTimesRoundFiveStart()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}
	
	public void testStartAndStopOperaTenTimesRoundFiveStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}	
	
	public void testStartAndStopOperaTenTimesRoundSixStart()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}
	
	public void testStartAndStopOperaTenTimesRoundSixStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}		
	
	public void testStartAndStopOperaTenTimesRoundSevenStart()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}
	
	public void testStartAndStopOperaTenTimesRoundSevenStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}		
	
	public void testStartAndStopOperaTenTimesRoundEightStart()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}
	
	public void testStartAndStopOperaTenTimesRoundEightStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}		
	
	public void testStartAndStopOperaTenTimesRoundNineStart()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}
	
	public void testStartAndStopOperaTenTimesRoundNineStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}		
	
	public void testStartAndStopOperaTenTimesRoundTenStart()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}
	
	public void testStartAndStopOperaTenTimesRoundTenStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
		runner.shutdown();
	}		
}