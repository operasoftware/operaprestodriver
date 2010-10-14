package com.opera.core.systems;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.OperaWebElement;
import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo.MouseButton;

class RunDriver {
	 private static OperaDesktopDriver driver;
//	 private static OperaDriver driver;
	    
	 public static void main(String[]args) {
	    	System.out.println("Create Driver");
	        driver = new OperaDesktopDriver();
//	        driver = new OperaDriver();
	        System.out.println(".Driver created ......");

	        
	        String window_name = "New Preferences Dialog", action_name = "Show preferences";
//	        String window_name = "", action_name = "Delete private data";

	        driver.waitStart(); // wait for dialog to open
	        System.out.println("-- wait for dialog -- ");
	        driver.operaDesktopAction(action_name);
	        int window_id = driver.waitForWindowShown(window_name);

	        QuickWidget ti = driver.findWidgetByStringId(window_id, "D_NEW_PREFS_PROGRAMS", "Advanced_treeview");

/*	        
	        QuickWindow qw = driver.findWindowByName("Browser Window");
	        QuickWidget tb = driver.findWidgetByName(qw.getWindowID(), "Status Toolbar Head");
	        QuickWidget b = driver.findWidgetByName(qw.getWindowID(), "tbb_Panel", "Status Toolbar Head");

			ArrayList mods = new ArrayList();
			mods.add(ModifierPressed.NONE);
			
	        b.click(MouseButton.LEFT, 1, mods);
	*/        
/*	        
	        driver.waitStart(); // wait for dialog to open
	        System.out.println("-- wait for dialog -- ");
	        driver.operaDesktopAction(action_name);
	        if (driver.waitForWindowShown(window_name) != 0)
	        {
	        	List<QuickWidget> qw_list = driver.getQuickWidgetList("New Preferences Dialog");
	        	
//	        	QuickWidget qw = qw_list.get(84);

	        	System.out.println("Widgets\n=======\n\n");
	        	
	        	for (Iterator<QuickWidget> it = qw_list.iterator(); it.hasNext(); ) {
	        		QuickWidget qw = it.next(); 

	        		System.out.println("   Name:" + qw.getName());
	        		System.out.println("   Text:" + qw.getText());
	        		System.out.println("   Type:" + qw.getType().toString());
	        		System.out.println("Visible:" + qw.isVisible());
	        		System.out.println("Enabled:" + qw.isEnabled());
	        		System.out.println("");
	        	} 
	        	
		        driver.waitStart();
		        driver.operaDesktopAction("Cancel");
		        System.out.println("--Wait for window to close--");
		        driver.waitForWindowClose(window_name);
	        }
	        else
	        {
	        	System.out.println("Time out waiting for dialog");
		    }
		    
		    
	 */       
	        
/*	        
	        driver.waitStart();
	        driver.operaDesktopAction("Show preferences");
//	        driver.keyPress(",", ModifierPressed.CTRL);

	        int win_id = driver.waitForWindowShown("New Preferences Dialog");

	        QuickWidget qw = driver.findWidgetByName(-1, "Startpage_edit");
	        System.out.println("By name: " + qw.getText());
	        //qw.verifyText("D_STARTUP_WITH_HOMEPAGE");
	        qw.isVisible();
	        
			ArrayList mods = new ArrayList();
			mods.add(ModifierPressed.NONE);
	        
	        qw.click(MouseButton.LEFT, 1, mods);
	        
	        driver.keyPress("h", ModifierPressed.NONE);
	        driver.keyPress("t", ModifierPressed.NONE);
	        driver.keyPress("t", ModifierPressed.NONE);
	        driver.keyPress("p", ModifierPressed.NONE);
	        
	        
	        driver.sleep(5000);
	        

//	        driver.get("http://www.google.com");
	*/        
	        /*
	        System.out.println("Do wait start");
	        driver.waitStart(); // wait for dialog to open
	        System.out.println("Do add to bookmarks action ");
	        driver.operaAction("Add to bookmarks");
	        
	        System.out.println("Wait for window activated ");
	        driver.waitForWindowShown();
	        
        	// ---- Internal stuff to check text on the dialog -------
	        int id = driver.getWindowID("Bookmark Properties");
	        System.out.println("Id of bookmark dialog is " + id);
	        driver.getWidgetList(id);
	        System.out.println("Get Widget label_for_Nick_edit");
	        int widget_id = driver.getQuickWidgetID(id, "name", "label_for_Nick_edit");
	        System.out.println("widget_id returned is " + widget_id);
	        System.out.println("-- Done --");
	        // _-----------------------
*/
/*
	        driver.waitStart(); // wait for dialog to open
	        System.out.println("-- wait for dialog -- ");
	        driver.operaDesktopAction("Add to bookmarks");
	        if (driver.waitForWindowShown("Add Bookmark Dialog") != 0)
	        {
        		QuickWidget widget = driver.findWidgetByName(-1, "label_for_Nick_edit");
        		if (widget != null)
        			System.out.println("Widget: " + widget.getWidgetID());
		        
		        driver.waitStart();
		        driver.operaDesktopAction("Cancel");
		        System.out.println("--Wait for window to close--");
		        driver.waitForWindowClose("Add Bookmark Dialog");
	        }
	        else
	        {
	        	System.out.println("Time out waiting for dialog");
		    }
		    
*/		    
		    // IN ruby:
        	//driver.label("name", "label_for_Nick_edit").verify_contains("Nickname");
	        /*
	        QuickWidget widget = (QuickWidget) driver.findWidgetByName("label_for_Nick_edit");
	        if (widget.getText().equals("Nickname"))
	        	System.out.println("Success");
	        else
	        	System.out.println("Failure");
	        */
	        // ---------------------
/*
	        driver.waitStart();
	        driver.operaDesktopAction("Show preferences");

	        System.out.println("--Wait for window to open--");
	        //int win_id = driver.waitForWindowShown("New Preferences Dialog");

	        QuickWidget qw = driver.findWidgetByName(-1, "Startpage_edit");
	        System.out.println("By name: " + qw.getText());
	        //qw.verifyText("D_STARTUP_WITH_HOMEPAGE");
	        qw.isVisible();
	        */
	       // qw.click(MouseButton.LEFT, 0, ModifierPressed.NONE);
/*	        
	        System.out.println("Window name: " + driver.getWindowName(win_id));
	        
	        driver.waitStart();
	        
	        driver.operaDesktopAction("Cancel");
	        System.out.println("--Wait for window to close--");
	        driver.waitForWindowClose("New Preferences Dialog");
*/
/*	        
	        driver.waitStart();
	        System.out.println("--Wait for window to open--");
	        driver.operaDesktopAction("Open url in new page", "http://t/security/bts/164110/scary_file-keyboard.kini");
	        driver.waitForWindowShown("Setup Apply Dialog Confirm Dialog");

	        QuickWidget qw = driver.findWidgetByName(-1, "Simple_message");
	        System.out.println("Text: " + qw.getText());
	        
	        System.out.println("Contains Text D_SECURITYALERT_SETUPDOWNLOAD: " + qw.verifyContainsText("D_SECURITYALERT_SETUPDOWNLOAD"));
	        
	        QuickWidget qw_button = driver.findWidgetByName(-1, "button_Cancel");

	        qw_button.click();
*/	        
/*	        
	        driver.waitStart();
	        System.out.println("--Wait for window to close--");
	        driver.operaDesktopAction("Cancel");
	        driver.waitForWindowClose("Setup Apply Dialog Confirm Dialog");
	        driver.operaDesktopAction("Close page", "1");
*/
//	        System.out.println("Text: " + driver.getString("D_SECURITYALERT_SETUPDOWNLOAD_TITLE"));
	        
/*
	        driver.waitStart();
	        System.out.println("--Wait for window to open--");
	        driver.operaDesktopAction("Add to bookmarks");
	        driver.waitForWindowShown("Add Bookmark Dialog");

	        QuickWidget qw = driver.findWidgetByName(-1, "label_for_Name_edit");
	        System.out.println("By name: " + qw.getText());
	        qw.verifyText("DI_ID_HLFILEPROP_FNAME_LABEL");

	    	driver.waitStart();
	        System.out.println("--Wait for window to close--");
	        driver.operaDesktopAction("Cancel");
	        driver.waitForWindowClose("Add Bookmark Dialog");
*/
	        
	        /*
	        System.out.println("--Go to Google --");
	        driver.get("http://www.google.com");
	        System.out.println("Gone to google ");
	        
	        OperaWebElement element = (OperaWebElement) driver.findElementByName("q");
	        element.sendKeys("Opera Software");
	        element.submit();
	        */
	        System.out.println("Done ");
	        //driver.quit();
	    }
}
