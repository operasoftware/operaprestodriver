/*
Copyright 2012 Opera Software ASA
Copyright 2007-2009 WebDriver committers
Copyright 2007-2009 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.opera.core.systems.environment.webserver;

import junit.framework.Assert;

import java.io.File;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.openqa.selenium.net.NetworkUtils;
import org.openqa.selenium.net.PortProber;

import javax.servlet.Servlet;

public class Jetty6WebServer implements WebServer {

  private static final String DEFAULT_CONTEXT_PATH = "/test/fixtures";
  private static final NetworkUtils NETWORK_UTILS = new NetworkUtils();

  private int port;
  private File path;
  private final Server server;
  private WebAppContext context;
  private final String hostName;

  public Jetty6WebServer() {
    this(NETWORK_UTILS.getNonLoopbackAddressOfThisMachine());
  }

  public Jetty6WebServer(String hostName) {
    this.hostName = hostName;

    server = new Server();

    path = findRootOfWebApp();

    context = addWebApplication(DEFAULT_CONTEXT_PATH, path);

    /*
    addServlet("Redirecter", "/redirect", RedirectServlet.class);
    addServlet("InfinitePagerServer", "/page/*", PageServlet.class);
    addServlet("Manifest", "/manifest/*", ManifestServlet.class);
    addServlet("Uploader", "/upload", UploadServlet.class);
    addServlet("Unusual encoding", "/encoding", EncodingServlet.class);
    addServlet("Sleeper", "/sleep", SleepingServlet.class);
    addFilter(MultiPartFilter.class, "/upload", Handler.DEFAULT);
    */

    listenOn(PortProber.findFreePort());
  }

  protected File findRootOfWebApp() {
    String[] possiblePaths = {
        "test/fixtures",
        "../test/fixtures",
        "../../test/fixtures"
    };

    File current;
    for (String potential : possiblePaths) {
      current = new File(potential);
      if (current.exists()) {
        return current;
      }
    }

    Assert.assertTrue("Unable to find common web files. These are located in the common directory",
                      path.exists());
    return null;
  }

  private static File findWebAppRoot(String[] possiblePaths) {
    for (String potential : possiblePaths) {
      File current = new File(potential);
      if (current.exists()) {
        return current;
      }
    }
    return null;
  }

  public String getHostName() {
    return hostName;
  }

  public String getAlternateHostName() {
    return NETWORK_UTILS.getPrivateLocalAddress();
  }

  public String whereIs(String relativeUrl) {
    if (!relativeUrl.startsWith("/")) {
      relativeUrl = DEFAULT_CONTEXT_PATH + "/" + relativeUrl;
    }
    return "http://" + getHostName() + ":" + port + relativeUrl;
  }

  public String whereElseIs(String relativeUrl) {
    if (!relativeUrl.startsWith("/")) {
      relativeUrl = DEFAULT_CONTEXT_PATH + "/" + relativeUrl;
    }
    return "http://" + getAlternateHostName() + ":" + port + relativeUrl;
  }

  public void start() {
    SelectChannelConnector connector = new SelectChannelConnector();
    connector.setPort(port);
    server.addConnector(connector);

    try {
      server.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void listenOn(int port) {
    this.port = port;
  }

  protected void addListener(Connector listener) {
    server.addConnector(listener);
  }

  public void stop() {
    try {
      server.stop();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void addServlet(String name, String url, Class<? extends Servlet> servletClass) {
    try {
      context.addServlet(servletClass, url);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void addFilter(Class<?> filter, String path, int dispatches) {
    context.addFilter(filter, path, dispatches);
  }


  public void addAdditionalWebApplication(String context, String absolutePath) {
    addWebApplication(context, absolutePath);
  }

  private WebAppContext addWebApplication(String contextPath, File rootDir) {
    return addWebApplication(contextPath, rootDir.getAbsolutePath());
  }

  private WebAppContext addWebApplication(String contextPath, String absolutePath) {
    WebAppContext app = new WebAppContext();
    app.setContextPath(contextPath);
    app.setWar(absolutePath);
    server.addHandler(app);
    return app;
  }

  public static void main(String[] args) {
    Jetty6WebServer server = new Jetty6WebServer("localhost");
    server.port = 2310;
    server.start();
  }

}