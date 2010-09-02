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
	        
	        System.out.println("Do wait start");
	        driver.waitStart(); // wait for dialog to open
	        System.out.println("Do add to bookmarks action ");
	        driver.operaAction("Add to bookmarks");
	        
	        System.out.println("Wait for window updated ");
	        driver.waitForWindowUpdated();
	        
	        int id = driver.getWindowID("Bookmark Properties");
	        
	        System.out.println("Id of bookmark dialog is " + id);
	        
	        driver.getWidgetList(id);
	        
	        /*System.out.println("------------ Done with first part ");
	        
	        driver.waitStart();
	        
	        driver.keyDown("Control");
	        driver.key(",");
	        driver.keyUp("Control");
	        
	        System.out.println("--Wait for window to open--");
	        driver.waitForWindowUpdated();

	        driver.waitStart();
	        
	        driver.key("enter");
	        
	        System.out.println("--Wait for window to close--");
	        driver.waitForWindowClose();
	        
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
