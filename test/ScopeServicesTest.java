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
	static OperaDriverSettings settings;
	static OperaLauncherRunner runner;	
	
	public void testOperaAndLauncherInitializationForServices()
	{
		settings = new OperaDriverSettings();
		settings.setRunOperaLauncherFromOperaDriver(true);
		settings.setOperaBinaryLocation(System.getProperty("test_gogi_binary_location"));
		settings.setOperaLauncherBinary(System.getProperty("test_launcher_binary_location"));
		settings.setOperaBinaryArguments(" -geometry 800x600 -nohw");
		
		runner = new OperaLauncherRunner(settings);
		runner.startOpera();
		
		Assert.assertNotNull(runner);
	}
	
	public void testScopeServicesConstructor()
	{
		// these versions may become deprecated in the future, but should give a good backwards compatability test
		Map<String, String> versions = new HashMap<String, String>();
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
	
	public void testInitializeScopeServices()
	{		
		services.startStpThread();
		
		try
		{
			services.init();			
		}
		catch (WebDriverException e)
		{	
			fail("WebDriverException encountered while initializing Scope Services");
			e.printStackTrace();
		}					
	}
	
	public void testIsConnected()
	{		
		Assert.assertTrue(services.isConnected());
	}
	
	public void testShutdown()
	{
		services.shutdown();
		Assert.assertFalse(services.isConnected());
		
		// now shutdown the launcher and Opera
		runner.stopOpera();
		runner.shutdown();
	}

}