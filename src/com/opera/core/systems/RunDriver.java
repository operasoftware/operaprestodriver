package com.opera.core.systems;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo.MouseButton;
import com.opera.core.systems.settings.OperaDriverSettings;

class RunDriver {
	 private static OperaDesktopDriver driver;
	    
	 public static void main(String[]args) {
			OperaDriverSettings settings = new OperaDriverSettings();
			settings.setNoQuit(true);
			settings.setNoRestart(true);
		 	
			settings.setRunOperaLauncherFromOperaDriver(true);
			//settings.setOperaBinaryLocation("/home/karianne/work/auto_common/run/lib/opera/opera");
			settings.setOperaBinaryArguments("-watirtest");
			settings.setOperaLauncherBinary("/home/karianne/ui_test/launcher/opera-launcher/projects/linux/launcher");
			settings.setNoQuit(true);
			
		 
	        driver = new OperaDesktopDriver(settings);
	        System.out.println(".Driver created ......");
	        
	        for (int i = 0; i < 100; i++) {
	        	driver.waitStart();
	        	List<ModifierPressed> list = new ArrayList<ModifierPressed>();
	        	driver.keyPress("t", list);
	        	int id = driver.waitForWindowShown("Document Window");
	        	
	        	QuickWidget widget = (QuickWidget) driver.findWidgetByName(id, "pb_CloseButton");
	        	if (widget != null) {
	        		driver.waitStart();
	        		widget.click(MouseButton.LEFT, 1, list);
	        		driver.waitForWindowClose("Document Window");
	        	}
	        
	        	driver.resetOperaPrefs("new_prefs2");
	        	
	        	System.out.println("------- Runinng test no " + i + "----------------");
	        }
	        
	        // ---------------------
/*
	        driver.waitStart();
	        driver.operaDesktopAction("Show preferences");

	        int win_id = driver.waitForWindowShown("New Preferences Dialog");

	        QuickWidget qw = driver.findWidgetByName(-1, "Startpage_edit");
	        //qw.verifyText("D_STARTUP_WITH_HOMEPAGE");
	        qw.isVisible();
	*/        
	        
	        System.out.println("Done ");
	        //driver.quit();
	    }
}
