import java.io.*;
import java.util.*;

import junit.framework.*;
import junit.textui.*;

import com.opera.core.systems.*;
import com.opera.core.systems.runner.*;
import com.opera.core.systems.runner.launcher.*;
import com.opera.core.systems.settings.*;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.Platform;

public class ScopeServicesTest extends TestCase 
{
	static ScopeServices services;
	static Map<String, String> versions;
	static OperaDriverSettings settings;
	static OperaLauncherRunner runner;	
	
	public void testInitializeRunnerAndOpera()
	{
		settings = new OperaDriverSettings();
		settings.setRunOperaLauncherFromOperaDriver(true);
		settings.setOperaBinaryLocation(System.getProperty("test_gogi_binary_location"));
		settings.setOperaLauncherBinary(System.getProperty("test_launcher_binary_location"));		
		settings.setOperaBinaryArguments(" -geometry 1280x1024 -nohw -url www.opera.com");
		runner = new OperaLauncherRunner(settings);
		runner.startOpera();
		Assert.assertTrue(runner.isOperaRunning());			
	}
	
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
			services = new ScopeServices(versions);
		} 
		catch (IOException e) 
		{
			fail("IOException encountered while initializing new ScopeServices object");
		}		
		
		Assert.assertNotNull(services);
	}
	
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
	
	public void testIsConnected()
	{
		Assert.assertTrue(services.isConnected());
	}
	
	// this is effectively a test to see if the core service is available or not
	public void testIsOperaIdleAvailable()
	{
		Assert.assertTrue(services.isOperaIdleAvailable());		
	}
	
	public void testIsSTPConnectionObjectNull()
	{
		Assert.assertNotNull(services.getConnection());
	}
	
	public void testGetListedServices()
	{
		List<String> list = services.getListedServices();
		Assert.assertTrue(list.size() > 0);
	}
	
	public void testIsExecServiceAvailable()
	{
		List<String> list = services.getListedServices();
		Assert.assertTrue(list.contains("exec"));
	}
	
	public void testIsWindowManagerServiceAvailable()
	{
		List<String> list = services.getListedServices();
		Assert.assertTrue(list.contains("window-manager"));
	}	
	
	public void testShutdownScopeServices()
	{
		services.quit();
		Assert.assertFalse(services.isConnected());
	}
	
	public void testCloseOperaAndRunner()
	{
		runner.stopOpera();
		runner.shutdown();
	}	
}