package com.opera.core.systems;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class QuickWidgetTest {
	private static OperaDesktopDriver driver;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        driver = new OperaDesktopDriver();
    }
    
    @Test
    public void testSomething(){
    	
    }
   
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    	//driver.quit();
    }

}
