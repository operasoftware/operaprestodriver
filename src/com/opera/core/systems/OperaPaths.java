package com.opera.core.systems;

import java.io.File;

import org.openqa.selenium.Platform;

/**
 * @author stuartk
 */
public class OperaPaths {

  /**
   * This method will try and find Opera on any system. It takes the following
   * steps:
   *  1. Check the environment variable "OPERA_PATH". If it exists, and the
   *     file it points to exists, then return
   *  2. (Unix) Call `which opera` to find the location
   *  3. Check if Opera exists at the default location on the respective OS
   *  4. Give up and return null
   * @return The path to Opera, or null
   */
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

  /**
   * This method will try and find Opera Launcher on any system.
   * @return The path to Opera Launcher, or null
   */
  public String launcherPath() {
    return "/home/stuart/bin/opera-launcher";
  }

  /**
   * Check that the given path is not null, and exists.
   * @param path The path to check.
   * @return null if the path is null, or does not exist. Otherwise it returns
   * the given path.
   */
  private String checkPath(String path) {
    if (path != null && new File(path).exists()) return path;
    else return null;
  }

}
