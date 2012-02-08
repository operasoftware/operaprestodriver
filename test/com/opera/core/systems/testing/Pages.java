package com.opera.core.systems.testing;

import com.opera.core.systems.environment.webserver.WebServer;

public class Pages {

  public String windows;

  public Pages(WebServer server) {
    windows = server.whereIs("windows.html");
  }

}