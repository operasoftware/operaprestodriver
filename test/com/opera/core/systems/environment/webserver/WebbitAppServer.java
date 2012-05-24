/*
Copyright 2012 Opera Software ASA
Copyright 2012 Selenium committers
Copyright 2012 Software Freedom Conservancy

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

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.net.NetworkUtils;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;

import java.util.concurrent.Future;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.openqa.selenium.net.PortProber.findFreePort;

/**
 * A simple web app server to host test files, using Webbit as the underlying web server.
 */
public class WebbitAppServer implements AppServer {

  private static final String HOSTNAME_FOR_TEST_ENV_NAME = "HOSTNAME";
  private static final String ALTERNATIVE_HOSTNAME_FOR_TEST_ENV_NAME = "ALTERNATIVE_HOSTNAME";
  private static final String FIXED_HTTP_PORT_ENV_NAME = "TEST_HTTP_PORT";

  private static final int DEFAULT_HTTP_PORT = 2310;
  private static final String DEFAULT_CONTEXT_PATH = "/";

  private final String hostname;
  private int httpPort;
  private WebServer httpServer;

  public WebbitAppServer() {
    this(detectHostname());
  }

  public WebbitAppServer(String hostname) {
    this.hostname = hostname;
    listenOn(getHttpPort());
  }

  public String getHostName() {
    return hostname;
  }

  private int getHttpPort() {
    if (httpPort == 0) {
      String port = System.getenv(FIXED_HTTP_PORT_ENV_NAME);
      httpPort = port == null ? findFreePort() : Integer.parseInt(port);
    }

    return httpPort;
  }

  public String getAlternateHostName() {
    String alternativeHostnameFromProperty = System.getenv(ALTERNATIVE_HOSTNAME_FOR_TEST_ENV_NAME);
    return alternativeHostnameFromProperty == null ? "localhost" : alternativeHostnameFromProperty;
  }

  public String whereIs(String relativeUrl) {
    return String.format("http://%s:%d%s",
                         getHostName(), getHttpPort(), getCommonPath(relativeUrl));
  }

  public String whereElseIs(String relativeUrl) {
    return String.format("https//%s:%d%s",
                         getAlternateHostName(), getHttpPort(), getCommonPath(relativeUrl));
  }

  private String getCommonPath(String relativeUrl) {
    if (!relativeUrl.startsWith("/")) {
      relativeUrl = DEFAULT_CONTEXT_PATH + relativeUrl;
    }
    return relativeUrl;
  }

  public void start() {
    httpServer = configureServer(httpPort);
    waitFor(httpServer.start());
  }

  public void stop() {
    httpServer.stop();
  }

  private WebServer configureServer(int port) {
    WebServer server = WebServers.createWebServer(newFixedThreadPool(5), port);

    // Note: Does first matching prefix matching, so /common/foo must be set up before /common
    // Delegating to a PathMatchHandler can be used to limit this
    server.add("/redirect", new RedirectHandler("result_page.html"));
    server.add("/basicAuth", new BasicAuthHandler("test:test"));
    server.add(new PathAugmentingStaticFileHandler(InProject.locate("test/fixtures"),
                                                   DEFAULT_CONTEXT_PATH));

    return server;
  }

  private void waitFor(Future<? extends WebServer> server) {
    long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() - startTime < 10000) {
      if (server.isCancelled()) {
        throw new TimeoutException("Timed out waiting for server to start");
      }
      if (server.isDone()) {
        return;
      }
    }
    throw new TimeoutException("Timed out waiting for server to start");
  }

  public void listenOn(int port) {
    httpPort = port;
  }

  /**
   * Gets the hostname of this machine.  By default it will attempt to retrieve the non-loopback
   * address so that remote browsers can connect to the web server, but in the event that this
   * fails, we will fall back to the internal loopback address.
   *
   * The hostname can be overriden using the {@link #HOSTNAME_FOR_TEST_ENV_NAME} environmental
   * variable.
   *
   * @return hostname to use for web server
   */
  private static String detectHostname() {
    String hostnameFromProperty = System.getenv(HOSTNAME_FOR_TEST_ENV_NAME);

    if (hostnameFromProperty != null) {
      return hostnameFromProperty;
    }

    NetworkUtils networkUtils = new NetworkUtils();
    try {
      return networkUtils.getNonLoopbackAddressOfThisMachine();
    } catch (WebDriverException e) {
      return networkUtils.getIpOfLoopBackIp4();
    }
  }

  public static void main(String[] args) {
    WebbitAppServer server = new WebbitAppServer();
    server.listenOn(DEFAULT_HTTP_PORT);
    server.start();
    System.out.printf("Started server on port %d\n", server.getHttpPort());
  }

}