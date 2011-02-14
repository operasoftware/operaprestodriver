package com.opera.core.systems;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.runner.launcher.OperaLauncherRunner;
import com.opera.core.systems.settings.OperaDriverSettings;

public class OperaLauncherRunnerTest
{
	private static OperaDriverSettings settings;
	private static OperaLauncherRunner runner;

	@Test
	public void testOperaDriverSettings()
	{
		settings = new OperaDriverSettings();
		Assert.assertNotNull(settings);
	}

	@Test
	public void testDefaultDoRunOperaLauncherFromOperaDriver()
	{
		// default is to run launcher from the driver, so this should always be true
		Assert.assertTrue(settings.doRunOperaLauncherFromOperaDriver());
	}

	@Test
	public void testSetRunOperaLauncherFromOperaDriver()
	{
		settings.setRunOperaLauncherFromOperaDriver(false);
		Assert.assertFalse(settings.doRunOperaLauncherFromOperaDriver());
	}

	@Test
	public void testDefaultLauncherListeningPort()
	{
		// the listening port should default to 9999
		Assert.assertEquals(9999, settings.getOperaLauncherListeningPort());
	}

	@Test
	public void testSetLauncherListeningPort()
	{
		settings.setOperaLauncherListeningPort(5555);
		Assert.assertEquals(5555, settings.getOperaLauncherListeningPort());
	}

	@Test
	public void testDefaultOperaBinaryLocation()
	{
		// the opera binary location should default to null
		Assert.assertNull(settings.getOperaBinaryLocation());
	}

	@Test
	public void testSetOperaBinaryLocation()
	{
		settings.setOperaBinaryLocation("/spartan/ramdisk/launcher");
		Assert.assertEquals("/spartan/ramdisk/launcher", settings.getOperaBinaryLocation());
	}

	@Test
	public void testDefaultOperaBinaryArguments()
	{
		// the opera binary arguments should default to null
		Assert.assertEquals(settings.getOperaBinaryArguments(), "");
	}

	@Test
	public void testSetOperaBinaryArguments()
	{
		settings.setOperaBinaryArguments("-host 127.0.0.1 -port 12199 -bin /spartan/ramdisk/install/launcher");
		Assert.assertEquals("-host 127.0.0.1 -port 12199 -bin /spartan/ramdisk/install/launcher", settings.getOperaBinaryArguments());
	}

	@Test
	public void testOperaLauncherRunnerConstructorWithSettings()
	{
	  OperaPaths paths = new OperaPaths();
		settings.setRunOperaLauncherFromOperaDriver(true);
		settings.setOperaBinaryArguments("");
		settings.setOperaBinaryLocation(paths.operaPath());
		settings.setOperaLauncherBinary(paths.launcherPath());

		runner = new OperaLauncherRunner(settings);
		Assert.assertNotNull(runner);
	}

	@Test
	public void testDefaultCrashedState()
	{
		Assert.assertFalse(runner.hasOperaCrashed());
	}

	@Test
	public void testDefaultIsOperaRunning()
	{
		Assert.assertFalse(runner.isOperaRunning());
	}

	@Test
	public void testStartOpera()
	{
	  Assert.assertNotNull(runner);
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}

	@Test
	public void testShutdownOpera()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}

	@Test
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

	@Test
	public void testOperaLauncherRunnerConstructorWithSettings2()
	{
		settings.setRunOperaLauncherFromOperaDriver(true);
		settings.setOperaBinaryArguments("-geometry 1024x768");
		runner = new OperaLauncherRunner(settings);
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}

	@Test
	public void testShutdown2()
	{
		runner.stopOpera();
		runner.shutdown();

		// verify that a second shutdown call doesn't do any harm (shouldn't)
		runner.shutdown();
	}

	@Test
	public void testStartAndStopOperaTenTimesRoundOneStart()
	{
		settings.setOperaBinaryArguments("-geometry 640x480");
		runner = new OperaLauncherRunner(settings);
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}

	@Test
	public void testStartAndStopOperaTenTimesRoundOneStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}

	@Test
	public void testStartAndStopOperaTenTimesRoundTwoStart()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}

	@Test
	public void testStartAndStopOperaTenTimesRoundTwoStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}

	@Test
	public void testStartAndStopOperaTenTimesRoundThreeStart()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}

	@Test
	public void testStartAndStopOperaTenTimesRoundThreeStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}

	@Test
	public void testStartAndStopOperaTenTimesRoundFourStart()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}

  @Test
	public void testStartAndStopOperaTenTimesRoundFourStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}

  @Test
	public void testStartAndStopOperaTenTimesRoundFiveStart()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}

  @Test
	public void testStartAndStopOperaTenTimesRoundFiveStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}

  @Test
	public void testStartAndStopOperaTenTimesRoundSixStart()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}

  @Test
	public void testStartAndStopOperaTenTimesRoundSixStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}

  @Test
	public void testStartAndStopOperaTenTimesRoundSevenStart()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}

  @Test
	public void testStartAndStopOperaTenTimesRoundSevenStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}

  @Test
	public void testStartAndStopOperaTenTimesRoundEightStart()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}

  @Test
	public void testStartAndStopOperaTenTimesRoundEightStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}

  @Test
	public void testStartAndStopOperaTenTimesRoundNineStart()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}

  @Test
	public void testStartAndStopOperaTenTimesRoundNineStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
	}

  @Test
	public void testStartAndStopOperaTenTimesRoundTenStart()
	{
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());
	}

  @Test
	public void testStartAndStopOperaTenTimesRoundTenStop()
	{
		runner.stopOpera();
		Assert.assertFalse(runner.isOperaRunning());
		runner.shutdown();
	}
}