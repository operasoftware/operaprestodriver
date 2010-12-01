import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

import junit.framework.*;
import junit.textui.*;

import com.opera.core.systems.*;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.Platform;

public class ScopeServicesTest extends TestCase 
{
	static ScopeServices services;
	
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
		}					
	}
	
	public void testIsConnected()
	{
		Assert.assertTrue(services.isConnected());
	}

}