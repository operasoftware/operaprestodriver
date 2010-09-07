package com.opera.core.systems;

import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebElement;

import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.OperaWebElement;

class RunDriver {
	 private static OperaDesktopDriver driver;
	    
	 public static void main(String[]args) {
	    	System.out.println("Create Driver");
	        driver = new OperaDesktopDriver();
	        System.out.println(".Driver created ......");
	        /*
	        System.out.println("Do wait start");
	        driver.waitStart(); // wait for dialog to open
	        System.out.println("Do add to bookmarks action ");
	        driver.operaAction("Add to bookmarks");
	        
	        //System.out.println("Wait for window activated ");
	        //driver.waitForWindowActivated();
	        
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
	        
	        //driver.keyDown("Control");
	        //driver.key(",");
	        //driver.keyUp("Control");
	        operaDesktopAction("Show preferences");

	        System.out.println("--Wait for window to open--");
//	        driver.waitForWindowUpdated();
	        driver.waitForWindowUpdated("Preferences");

	        driver.waitStart();
	        
//	        driver.key("enter");
	        operaDesktopAction("Cancel");
//	        driver.operaAction("Ok");
	        
	        System.out.println("--Wait for window to close--");
	        driver.waitForWindowClose("Preferences");
*/
/*
	        driver.waitStart();
	        System.out.println("--Wait for window to open--");
	        driver.operaDesktopAction("Open url in new page", "http://t/security/bts/164110/scary_file-keyboard.kini");

	        driver.waitForWindowUpdated("Potential Security Risk");

	        driver.waitStart();
	        System.out.println("--Wait for window to close--");
//	        driver.key("enter");
//	        driver.operaAction("Close window");
	        driver.operaDesktopAction("Cancel");
	        driver.waitForWindowClose("Potential Security Risk");
*/

	        driver.waitStart();
	        System.out.println("--Wait for window to open--");
	        driver.operaDesktopAction("Add to bookmarks");
	        driver.waitForWindowUpdated("Add Bookmark");

	        QuickWidget qw = driver.findWidgetByName(-1, "label_for_Name_edit");
	        System.out.println(qw.getText());
	        		
	    	driver.waitStart();
	        System.out.println("--Wait for window to close--");
	        driver.operaDesktopAction("Cancel");
	        driver.waitForWindowClose("Add Bookmark");

	        
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
