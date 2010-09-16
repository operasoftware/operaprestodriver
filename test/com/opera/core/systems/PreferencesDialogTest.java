package com.opera.core.systems;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class PreferencesDialogTest {
	private static OperaDesktopDriver driver;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        driver = new OperaDesktopDriver();
    }
    
    @Test
    public void testPreferencesDialog(){
    	  driver.waitStart();
    	  driver.operaDesktopAction("Show preferences");

    	  driver.waitForWindowShown("New Preferences Dialog");
    	  QuickWidget qw = driver.findWidgetByName(-1, "Startup_mode_dropdown");
    	  assertTrue("Drop down is visible", qw.isVisible());
    	  assertTrue("Chosen entry is startup with homepage", qw.verifyText("D_STARTUP_WITH_HOMEPAGE"));
    	  assertTrue("Startup w/home is selected", qw.isSelected("D_STARTUP_WITH_HOMEPAGE"));
    	  
    	  qw.click();
    	  
    	  driver.waitStart();
    	  driver.operaDesktopAction("Cancel");
    	  driver.waitForWindowClose("New Preferences Dialog");
    }
   
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    	//driver.quit();
    }

	
  

	
}
