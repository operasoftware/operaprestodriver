package com.opera.core.systems;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opera.core.systems.settings.OperaDriverSettings;

public class OperaPathsTest {

  @Test
  public void testStart() throws Exception {
    OperaDriverSettings settings = new OperaDriverSettings();    
    settings.setRunOperaLauncherFromOperaDriver(true);

    // TODO make work everywhere
    settings.setOperaBinaryLocation("/usr/bin/opera");
    settings.setOperaLauncherBinary("/home/stuart/bin/opera-launcher");

    // FIXME -nosession doesn't work on Windows
    settings.setOperaBinaryArguments("opera:debug");    
    
    OperaDriver driver = new OperaDriver(settings);
    driver.get("http://example.com/");
    
    assertEquals("http://example.com/", driver.getCurrentUrl());
    
    driver.shutdown();
  }
    
}
