package com.opera.core.systems.example;

import com.opera.core.systems.OperaDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Adapted from http://code.google.com/p/selenium/wiki/GettingStarted
 */
public class Example {

  public static void main(String[] args) {
    // Create a new instance of the OperaDriver.  Notice that the remainder of the code relies on
    // the interface, not the implementation.
    WebDriver driver = new OperaDriver();

    // And now use this to visit Google
    driver.navigate().to("http://www.google.com");

    // Find the text input element by its name
    WebElement element = driver.findElement(By.name("q"));

    // Enter something to search for
    element.sendKeys("Cheese!");

    // Now submit the form. WebDriver will find the form for us from the element
    element.submit();

    // Check the title of the page
    System.out.println("Page title is: " + driver.getTitle());

    driver.quit();
  }

}