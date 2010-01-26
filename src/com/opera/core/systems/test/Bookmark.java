package com.opera.core.systems.test;

import org.openqa.selenium.WebElement;

import com.opera.core.systems.OperaDriver;

public class Bookmark {
	
	private OperaDriver driver;
	
	public Bookmark(OperaDriver driver) {
		this.driver = driver;
	}
	
	public void clearFolder(String title) {
		driver.findElementByXPath("//button[text()='Delete'][preceding-sibling::div[child::li[child::b[text()='"+title+"']]]]").click();
	}
	
	public void addBookmark(String url, String title, String description, String nickname) {
		driver.findElementById("au").sendKeys(url);
		driver.findElementById("at").sendKeys(title);
		driver.findElementById("ad").sendKeys(description);
		driver.findElementById("as").sendKeys(nickname);
		driver.findElementByName("add_bookmark").click();
		//sleep(200);
	}
	
	public void addFolder(String title, String description, String nickname) {
		driver.findElementById("at").sendKeys(title);
		driver.findElementById("ad").sendKeys(description);
		driver.findElementById("as").sendKeys(nickname);
		driver.findElementByName("add_folder").click();
		//sleep(200);
	}
	
	public void clearAll() {
		WebElement element = null;
		try {
			element = driver.findElementByXPath("//button[text()='Delete']");
		} catch (Exception e) {
			return;
		}
		element.click();
		clearAll();
	}
	

}
