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


import com.opera.core.systems.testing.InProject;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.openqa.selenium.net.NetworkUtils;
import org.openqa.selenium.net.PortProber;

import java.io.File;

import javax.servlet.Servlet;

public class Jetty7WebServer implements WebServer {

  private static final String DEFAULT_CONTEXT_PATH = "/test/fixtures";
  private static final NetworkUtils NETWORK_UTILS = new NetworkUtils();

  private int port;
  private File path;
  private final Server server;
  private WebAppContext defaultContext;

  private ContextHandlerCollection handlers;
  private final String hostName;

  public Jetty7WebServer() {
    this(getHostname());
  }

  public Jetty7WebServer(String hostName) {
    this.hostName = hostName;

    server = new Server();

    path = findRootOfWebApp();
    handlers = new ContextHandlerCollection();

    defaultContext = addWebApplication(DEFAULT_CONTEXT_PATH, path);

    server.setHandler(handlers);

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
    return InProject.locate("test/fixtures");
  }

  public String getHostName() {
    return hostName;
  }

  public String getAlternateHostName() {
    return NETWORK_UTILS.getPrivateLocalAddress();
  }

  public String whereIs(String relativeUrl) {
    relativeUrl = getCommonPath(relativeUrl);
    return "http://" + getHostName() + ":" + port + relativeUrl;
  }

  public String whereElseIs(String relativeUrl) {
    relativeUrl = getCommonPath(relativeUrl);
    return "http://" + getAlternateHostName() + ":" + port + relativeUrl;
  }

  private String getCommonPath(String relativeUrl) {
    if (!relativeUrl.startsWith("/")) {
      relativeUrl = DEFAULT_CONTEXT_PATH + "/" + relativeUrl;
    }
    return relativeUrl;
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

  public void stop() {
    try {
      server.stop();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void addServlet(String name, String url, Class<? extends Servlet> servletClass) {
    try {
      defaultContext.addServlet(servletClass, url);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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
    handlers.addHandler(app);
    return app;
  }

  public static void main(String[] args) {
    Jetty7WebServer server = new Jetty7WebServer(getHostname());
    server.listenOn(2310);
    System.out.println("Starting server on port 2310");
    server.start();
  }

  private static String getHostname() {
    return NETWORK_UTILS.getNonLoopbackAddressOfThisMachine();
  }

}