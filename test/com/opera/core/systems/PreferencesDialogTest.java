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

    	  int win_id = driver.waitForWindowShown("New Preferences Dialog");
    	  QuickWidget qw = driver.findWidgetByName(-1, "Startup_mode_dropdown");
    	  assertTrue("Drop down is visible", qw.isVisible());
    	  assertTrue("Chosen entry is startup with homepage", qw.verifyText("D_STARTUP_WITH_HOMEPAGE"));
    	  assertTrue("Startup w/home is selected", qw.isSelected("D_STARTUP_WITH_HOMEPAGE"));
    	  QuickWidget cancel = driver.findWidgetByName(win_id, "button_Cancel"); 
    	  
    	  
    	  driver.waitStart();
    	  cancel.click(0,1,0);
    	  //driver.operaDesktopAction("Cancel");
    	  driver.waitForWindowClose("New Preferences Dialog");
    }
   
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    	//driver.quit();
    }

	
  

	
}
