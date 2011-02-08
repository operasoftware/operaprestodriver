package com.opera.core.systems;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.settings.OperaDriverSettings;

public class ScopeServicesTest extends TestBase
{
	static ScopeServices services;
	static Map<String, String> versions;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    /**
     * This class overrides the init() method to prevent the scop services
     * being initialised. We then do this in the tests below
     * @author stuartk
     */
    class ScopeTestOperaDriver extends TestOperaDriver {
      public ScopeTestOperaDriver(OperaDriverSettings settings) {
        super(settings);
      }

      @Override
      protected void init() {}
    }

    // Start up the driver
    OperaDriverSettings settings = new OperaDriverSettings();
    settings.setRunOperaLauncherFromOperaDriver(true);
    // TODO make work everywhere
    settings.setOperaBinaryLocation(binary);
    settings.setOperaLauncherBinary(launcher);

    driver = new ScopeTestOperaDriver(settings);
    Assert.assertNotNull(driver);
  }

	@Test
	public void testScopeServicesConstructor()
	{
		// these versions may become deprecated in the future, but should give a good backwards compatability test
		versions = new HashMap<String, String>();
		versions.put("ecmascript-debugger", "5.0");
		versions.put("window-manager", "2.0");
		versions.put("exec", "2.0");
		versions.put("core", "1.0");

		try
		{
			services = new ScopeServices(versions, false);
		}
		catch (IOException e)
		{
			fail("IOException encountered while initializing new ScopeServices object");
		}

		Assert.assertNotNull(services);
	}

	@Test
	public void testStartSTPThread()
	{
		try
		{
			services.startStpThread();
		}
		catch (WebDriverException e)
		{
			fail("WebDriverException encountered while starting the STP thread");
		}
	}

	@Test
	public void testInitializeScopeServices()
	{
		try
		{
			services.init();
		}
		catch (WebDriverException e)
		{
			fail("WebDriverException encountered while initializing Scope Services");
		}
	}

	// this is effectively a test to see if the core service is available or not
	@Test
	public void testIsOperaIdleAvailable()
	{
		Assert.assertTrue(services.isOperaIdleAvailable());
	}

	@Test
	public void testIsSTPConnectionObjectNull()
	{
		Assert.assertNotNull(services.getConnection());
	}

	@Test
	public void testGetListedServices()
	{
		List<String> list = services.getListedServices();
		Assert.assertTrue(list.size() > 0);
	}

	@Test
	public void testIsExecServiceAvailable()
	{
		List<String> list = services.getListedServices();
		Assert.assertTrue(list.contains("exec"));
	}

	@Test
	public void testIsWindowManagerServiceAvailable()
	{
		List<String> list = services.getListedServices();
		Assert.assertTrue(list.contains("window-manager"));
	}

}