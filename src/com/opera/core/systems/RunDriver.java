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
	        
	        /*System.out.println("--Go to Google --");
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
