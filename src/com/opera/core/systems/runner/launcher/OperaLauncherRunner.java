/*
Copyright 2008-2011 Opera Software ASA

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

package com.opera.core.systems.runner.launcher;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.protobuf.GeneratedMessage;
import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.runner.launcher.OperaLauncherProtocol.MessageType;
import com.opera.core.systems.runner.launcher.OperaLauncherProtocol.ResponseEncapsulation;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherHandshakeRequest;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherScreenshotRequest;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherScreenshotResponse;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherStartRequest;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherStatusRequest;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherStatusResponse;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherStopRequest;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherStatusResponse.StatusType;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.settings.OperaDriverSettings;

public class OperaLauncherRunner implements OperaRunner {

  private static Logger logger = Logger.getLogger(OperaLauncherRunner.class.getName());

  private OperaLauncherBinary launcherRunner = null;

  private DesiredCapabilities capabilities;
  private OperaLauncherProtocol launcherProtocol = null;
  private String crashlog = null;

  @Deprecated
  public OperaLauncherRunner(OperaDriverSettings settings) {
    this(settings.getCapabilities());
  }

  public OperaLauncherRunner(DesiredCapabilities capabilities) {
    logger.fine("Creating OperaLauncherRunner");
    this.capabilities = capabilities;

    if (this.capabilities.getCapability(OperaDriver.LAUNCHER) == null) {
      throw new WebDriverException("Launcher path not set");
    }

    if (this.capabilities.getCapability(OperaDriver.BINARY) == null) {
      throw new WebDriverException("You need to set Opera's path to use opera-launcher");
    }

    logger.fine("Running launcher from OperaDriver");

    List<String> stringArray = new ArrayList<String>();
    stringArray.add("-host");
    stringArray.add("127.0.0.1");
    stringArray.add("-port");
    stringArray.add("9999");  // TODO: Get a random port
    if (this.capabilities.getCapability(OperaDriver.DISPLAY) != null) {
      stringArray.add("-display");
      stringArray.add(":" + Integer.toString((Integer) this.capabilities.getCapability(OperaDriver.DISPLAY)));
    }

    if (logger.isLoggable(Level.FINEST)) {
      stringArray.add("-console");
      stringArray.add("-verbosity");
      stringArray.add("FINEST");
    }

    String binary_profile = (String) capabilities.getCapability(OperaDriver.BINARY_PROFILE);
    if (binary_profile != null && !binary_profile.isEmpty()) {
      stringArray.add("-profile");
      stringArray.add(binary_profile);
    }

    // Note any launcher arguments must be before this line!

    if ((Boolean) this.capabilities.getCapability(OperaDriver.NO_QUIT)) {
      stringArray.add("-noquit");
    }
    stringArray.add("-bin");
    stringArray.add((String) this.capabilities.getCapability(OperaDriver.BINARY));

    /*
    * We read in environmental variable OPERA_ARGS in addition to existing
    * arguments passed down from OperaDriverSettings. These are combined
    * and sent to the browser.
    *
    * Note that this is a deviation from the principle of arguments normally
    * overwriting environmental variables.
    */
    String binaryArguments = (String) this.capabilities.getCapability(OperaDriver.ARGUMENTS);
    if (binaryArguments == null) {
      binaryArguments = "";
    }
    String environmentArguments = System.getenv("OPERA_ARGS");

    if (environmentArguments != null && environmentArguments.length() > 0) {
      binaryArguments = environmentArguments + " " + binaryArguments;
    }

      // Arguments are split by single space.
      StringTokenizer tokenizer = new StringTokenizer(binaryArguments, " ");
      while (tokenizer.hasMoreTokens()) {
        stringArray.add(tokenizer.nextToken());
      }

      logger.fine("Launcher arguments: " + stringArray);

      // Enable auto test mode, always starts Opera on opera:debug and prevents
      // interrupting dialogues appearing
      if (!stringArray.contains("-autotestmode")) {
        stringArray.add("-autotestmode");

        int port = (Integer) this.capabilities.getCapability(OperaDriver.PORT);
        if (port != -1) {
          // Provide defaults if one hasn't been set
          String host = "127.0.0.1";
          stringArray.add(host+":"+port);
        }
      }

      System.out.println("command line: "+stringArray.toString());

    launcherRunner = new OperaLauncherBinary(
      (String) this.capabilities.getCapability(OperaDriver.LAUNCHER),
      stringArray.toArray(new String[stringArray.size()])
    );

    int launcherPort = 9999;  // TODO: Store port from previously
    logger.fine("Waiting for Opera Launcher connection on port " + launcherPort);

    try {
      // setup listener server
      ServerSocket listenerServer = new ServerSocket(launcherPort);
      listenerServer.setSoTimeout((int) OperaIntervals.LAUNCHER_TIMEOUT.getValue());

      // try to connect
      launcherProtocol = new OperaLauncherProtocol(listenerServer.accept());

      // we did it!
      logger.fine("Connected with Opera Launcher on port " + launcherPort);
      listenerServer.close();

      // Do the handshake!
      LauncherHandshakeRequest.Builder request = LauncherHandshakeRequest.newBuilder();
      ResponseEncapsulation res = launcherProtocol.sendRequest(
        MessageType.MSG_HELLO, request.build().toByteArray());

      // Are we happy?
      if (res.IsSuccess()) {
        logger.fine("Got Opera launcher handshake: " + res.getResponse().toString());
      } else {
        logger.fine("Did not get Opera launcher handshake: " + res.getResponse().toString());
        throw new OperaRunnerException("Did not get Opera launcher handshake");
      }
    } catch (SocketTimeoutException e) {
      throw new OperaRunnerException("Timeout waiting for Opera Launcher to connect on port " + launcherPort, e);
    } catch (IOException e) {
      throw new OperaRunnerException("Unable to listen to Opera launcher port " + launcherPort, e);
    }
  }

  public void startOpera() {
    logger.fine("Launcher starting Opera...");
    try {
      byte[] request = LauncherStartRequest.newBuilder().build().toByteArray();

      ResponseEncapsulation res = launcherProtocol.sendRequest(MessageType.MSG_START, request);

      if (handleStatusMessage(res.getResponse()) != StatusType.RUNNING) {
        throw new OperaRunnerException("Could not start Opera");
      }

      // Check Opera hasn't immediately exited (e.g. due to unknown arguments)
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
      }
      res = launcherProtocol.sendRequest(MessageType.MSG_STATUS, request);

      if (handleStatusMessage(res.getResponse()) != StatusType.RUNNING) {
        throw new OperaRunnerException("Opera exited immediately; possibly incorrect arguments?");
      }

    } catch (IOException e) {
      throw new OperaRunnerException("Could not start Opera", e);
    }
  }

  public void stopOpera() {
    logger.fine("Launcher stopping Opera...");

    try {
      LauncherStopRequest.Builder request = LauncherStopRequest.newBuilder();

      ResponseEncapsulation res = launcherProtocol.sendRequest(
        MessageType.MSG_STOP, request.build().toByteArray()
      );

      if (handleStatusMessage(res.getResponse()) == StatusType.RUNNING) {
        throw new OperaRunnerException("Could not stop Opera");
      }
    } catch (IOException e) {
      throw new OperaRunnerException("Could not stop Opera", e);
    }
  }

  public boolean isOperaRunning() {
    return isOperaRunning(0);
  }

  public boolean isOperaRunning(int processId) {
    logger.fine("Get Opera status");

    try {
      LauncherStatusRequest.Builder request = LauncherStatusRequest.newBuilder();
      if (processId > 0) request.setProcessid(processId);

      ResponseEncapsulation res = launcherProtocol.sendRequest(
        MessageType.MSG_STATUS, request.build().toByteArray());
      return handleStatusMessage(res.getResponse()) == StatusType.RUNNING;
    } catch (IOException e) {
      throw new OperaRunnerException("Could not get state of Opera", e);
    }
  }

  public boolean hasOperaCrashed() {
    return crashlog != null;
  }

  public String getOperaCrashlog() {
    return crashlog;
  }

  public void shutdown() {
    try {
      // Send a shutdown command to the launcher.
      try {
        launcherProtocol.sendRequestWithoutResponse(MessageType.MSG_SHUTDOWN, null);
      } catch (Exception e) {
        e.printStackTrace();
      }

      // Then shutdown the protocol connection.
      launcherProtocol.shutdown();
    } catch (IOException e) {
      // Don't care.
      throw new OperaRunnerException("Exception when shutting down", e);
    }

    if (launcherRunner != null) {
      launcherRunner.shutdown();
      launcherRunner = null;
    }
  }

  /**
   * Handle status message, and updates state.
   *
   * @param msg
   */
  private StatusType handleStatusMessage(GeneratedMessage msg) {
    LauncherStatusResponse response = (LauncherStatusResponse) msg;

    // LOG RESULT!
    logger.finest("[LAUNCHER] Status: " + response.getStatus().toString());
    if (response.hasExitcode()) {
      logger.finest("[LAUNCHER] Status: exitCode=" + response.getExitcode());
    }
    if (response.hasCrashlog()) {
      logger.finest("[LAUNCHER] Status: crashLog=yes");
    } else {
      logger.finest("[LAUNCHER] Status: crashLog=no");
    }
    if (response.getLogmessagesCount() > 0) {
      for (String message : response.getLogmessagesList()) {
        logger.finest("[LAUNCHER LOG] " + message);
      }
    } else {
      logger.finest("[LAUNCHER LOG] No log...");
    }

    // Handle state
    StatusType status = response.getStatus();
    if (status == StatusType.CRASHED) {
      if (response.hasCrashlog()) {
        crashlog = response.getCrashlog().toStringUtf8();
      } else {
        crashlog = ""; // != NULL :-|
      }
    } else {
      crashlog = null;
    }

    // TODO: send something to the operalistener....
    // if(launcherLastKnowStatus == StatusType.RUNNING && status !=
    // StatusType.RUNNING){
    // if(operaListener != null)
    // operaListener.operaBinaryStopped(response.getExitcode());
    // }

    return status;
  }

  /**
   * Take screenshots!
   */
  public ScreenShotReply saveScreenshot(long timeout, String... hashes) {
    String resultMd5 = null;
    byte[] resultBytes = null;
    boolean blank = false;

    logger.fine("Get opera screenshot");

    try {
      LauncherScreenshotRequest.Builder request = LauncherScreenshotRequest.newBuilder();
      for (int i = 0; i < hashes.length; i++) {
        request.addKnownMD5S(hashes[i]);
      }
      request.setKnownMD5STimeoutMs((int) timeout);

      ResponseEncapsulation res = launcherProtocol.sendRequest(
        MessageType.MSG_SCREENSHOT, request.build().toByteArray());
      LauncherScreenshotResponse response = (LauncherScreenshotResponse) res.getResponse();

      resultMd5 = response.getMd5();
      resultBytes = response.getImagedata().toByteArray();

      if (response.hasBlank()) {
        blank = response.getBlank();
      }

    } catch (SocketTimeoutException e) {
      throw new OperaRunnerException("Could not get screenshot from launcher (Socket Timeout)", e);
    } catch (IOException e) {
      throw new OperaRunnerException("Could not get screenshot from launcher with exception:" + e, e);
    }

    // This will make sure to check the status of Opera
    isOperaRunning();

    ScreenShotReply screenshotreply = new ScreenShotReply(resultMd5, resultBytes);
    screenshotreply.setBlank(blank);
    screenshotreply.setCrashed(this.hasOperaCrashed());

    return screenshotreply;
  }

}