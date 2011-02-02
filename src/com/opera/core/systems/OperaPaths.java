package com.opera.core.systems;

import java.io.File;

import org.openqa.selenium.Platform;

/**
 * @author stuartk
 */
public class OperaPaths {

  public String operaPath() {
    String path;
    if ((path = System.getenv("OPERA_PATH")) != null) return path;
    
    if (Platform.getCurrent().is(Platform.LINUX)) {
      // TODO run which to see if we can get the Opera location
      
      // Check the default location
      path = "/usr/bin/opera";     
    }
    else if (Platform.getCurrent().is(Platform.WINDOWS)) {
      String progfiles = System.getenv("PROGRAMFILES");
      if (progfiles == null) progfiles = "\\Program Files";
      
      path = progfiles + "\\Opera\\opera.exe";  
    }
    
    if (new File(path).exists()) return path;    
    return null;
  }

  public String launcherPath() {
    return "/home/stuart/bin/opera-launcher";
  }

}
