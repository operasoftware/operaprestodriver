/*
Copyright 2008-2012 Opera Software ASA

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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.net.PortProber;

import com.google.protobuf.GeneratedMessage;
import com.opera.core.systems.OperaPaths;
import com.opera.core.systems.OperaProduct;
import com.opera.core.systems.arguments.OperaArgument;
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
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherStatusResponse.StatusType;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherStopRequest;
import com.opera.core.systems.scope.internal.OperaIntervals;

/**
 * OperaLauncherRunner implements an interface in C++ with a Java API for controlling the Opera
 * binary.
 */
public class OperaLauncherRunner extends OperaRunner
    implements com.opera.core.systems.runner.interfaces.OperaRunner {

  private static Logger logger = Logger.getLogger(OperaLauncherRunner.class.getName());

  private OperaLauncherBinary launcherRunner = null;
  private OperaLauncherProtocol launcherProtocol = null;
  private String crashlog = null;

  public OperaLauncherRunner() {
    this(OperaLauncherRunnerSettings.getDefaultSettings());
  }

  public OperaLauncherRunner(OperaLauncherRunnerSettings settings) {
    // Rely on OperaRunner to parse most of the settings
    super(settings);

    // Parse remaining launcher-related settings
    Integer launcherPort = PortProber.findFreePort();
    Integer display = settings.getDisplay();
    String binary;
    if (settings.getBinary() == null) {
      binary = OperaPaths.operaPath();
    } else {
      binary = settings.getBinary().getAbsolutePath();
    }
    OperaProduct product = settings.getProduct();
    String backend = settings.getBackend();

    List<String> launcherArguments = new ArrayList<String>();
    launcherArguments.add("-host");
    launcherArguments.add("127.0.0.1");
    launcherArguments.add("-port");
    launcherArguments.add(launcherPort.toString());
    if (display != null && display > 0) {
      launcherArguments.add("-display");
      launcherArguments.add(":" + display.toString());
    }
    if (settings.getLoggingLevel() != null && settings.getLoggingLevel() != Level.OFF) {
      launcherArguments.add("-console");  // TODO(andreastt): Allow for file logging
      launcherArguments.add("-verbosity");
      launcherArguments.add(settings.getLoggingLevel().toString());
    }
    if (!product.is(OperaProduct.ALL)) {
      launcherArguments.add("-profile");
      launcherArguments.add(product.toString());
    }
    if (backend != null && !backend.isEmpty()) {
      launcherArguments.add("-backend");
      launcherArguments.add(backend);
    }
    if (settings.getNoQuit()) {
      launcherArguments.add("-noquit");
    }
    launcherArguments.add("-bin");
    launcherArguments.add(binary);

    // The launcher will pass on any extra arguments after -bin to Opera
    for (OperaArgument argument : super.settings.getArguments().getArguments()) {
      launcherArguments.add(super.settings.getArguments().sign() + argument.getArgument());
      if (argument.getValue() != null && !argument.getValue().isEmpty()) {
        launcherArguments.add(argument.getValue());
      }
    }

    logger.config("launcher arguments: " + launcherArguments.toString());

    // Make sure that the launcher is executable
    settings.makeLauncherExecutable();

    // Setup the launcher binary
    try {
      launcherRunner = new OperaLauncherBinary(
          settings.getLauncher().getAbsolutePath(),
          launcherArguments.toArray(new String[launcherArguments.size()])
      );
    } catch (IOException e) {
      throw new OperaRunnerException("Unable to start launcher: " + e.getMessage());
    }

    logger.fine("Waiting for launcher connection on port " + launcherPort);

    try {
      // Setup listener server
      ServerSocket listenerServer = new ServerSocket(launcherPort);
      listenerServer.setSoTimeout((int) OperaIntervals.LAUNCHER_TIMEOUT.getValue());

      // Try to connect
      launcherProtocol = new OperaLauncherProtocol(listenerServer.accept());

      // We did it!
      logger.fine("Connected with launcher on port " + launcherPort);
      listenerServer.close();

      // Do the handshake!
      LauncherHandshakeRequest.Builder request = LauncherHandshakeRequest.newBuilder();
      ResponseEncapsulation res = launcherProtocol.sendRequest(
          MessageType.MSG_HELLO, request.build().toByteArray());

      // Are we happy?
      if (res.isSuccess()) {
        logger.finer("Got launcher handshake: " + res.getResponse().toString());
      } else {
        throw new OperaRunnerException(
            "Did not get launcher handshake: " + res.getResponse().toString());
      }
    } catch (SocketTimeoutException e) {
      throw new OperaRunnerException("Timeout waiting for launcher to connect on port " +
                                     launcherPort, e);
    } catch (IOException e) {
      throw new OperaRunnerException("Unable to listen to launcher port " + launcherPort, e);
    }
  }

  public void startOpera() {
    logger.fine("Instructing launcher to start Opera...");

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
        // nothing
      }

      res = launcherProtocol.sendRequest(MessageType.MSG_STATUS, request);

      if (handleStatusMessage(res.getResponse()) != StatusType.RUNNING) {
        throw new OperaRunnerException(
            "Opera exited immediately; possibly incorrect arguments?  Command: " +
            launcherRunner.getCommand());
      }
    } catch (IOException e) {
      throw new OperaRunnerException("Could not start Opera", e);
    }

    logger.fine("Opera launched through launcher");
  }

  public void stopOpera() {
    logger.fine("Instructing launcher to stop Opera...");

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

    logger.fine("Opera stopped through launcher");
  }

  public boolean isOperaRunning() {
    return isOperaRunning(0);
  }

  public boolean isOperaRunning(int processId) {
    try {
      LauncherStatusRequest.Builder request = LauncherStatusRequest.newBuilder();
      if (processId > 0) {
        request.setProcessid(processId);
      }

      ResponseEncapsulation res = launcherProtocol.sendRequest(
          MessageType.MSG_STATUS, request.build().toByteArray());
      logger.finer("Getting Opera's status from launcher: " + res.getResponse().toString());

      return handleStatusMessage(res.getResponse()) == StatusType.RUNNING;
    } catch (IOException e) {
      logger.fine("Could not get state of Opera, assuming launcher has shut down");
      return false;
    }
  }

  public boolean hasOperaCrashed() {
    return crashlog != null;
  }

  public String getOperaCrashlog() {
    return crashlog;
  }

  public void shutdown() {
    logger.fine("Shutting down launcher");

    try {
      // Send a shutdown command to the launcher
      launcherProtocol.sendRequestWithoutResponse(MessageType.MSG_SHUTDOWN, null);
    } catch (IOException e) {
      // If launcher has already been shutdown, this shouldn't create an exception, all we want to
      // do is to make sure the protocol is down
    }

    try {
      // Then shutdown the protocol connection
      launcherProtocol.shutdown();
    } catch (IOException e) {
      throw new OperaRunnerException("Unable to shut down launcher", e);
    }

    if (launcherRunner != null) {
      launcherRunner.shutdown();
      launcherRunner = null;
    }
  }

  /**
   * Handle status message, and updates state.
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

    logger.fine("Instructing launcher to take screenshot");

    try {
      LauncherScreenshotRequest.Builder request = LauncherScreenshotRequest.newBuilder();
      for (String hash : hashes) {
        request.addKnownMD5S(hash);
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
      throw new OperaRunnerException("Could not get screenshot from launcher with exception: " + e,
                                     e);
    }

    // This will make sure to check the status of Opera
    isOperaRunning();

    ScreenShotReply screenshotreply = new ScreenShotReply(resultMd5, resultBytes);
    screenshotreply.setBlank(blank);
    screenshotreply.setCrashed(this.hasOperaCrashed());

    return screenshotreply;
  }

  /**
   * The launcher allows for the following logging levels: "FINEST", "FINE", "INFO", "WARNING",
   * "SEVERE".  Since the launcher is unusually chatty, we don't want it to use the same logging
   * level as OperaDriver.  Besides, it doesn't have the same logging levels as Java.  This method
   * accepts a Java logging level and converts it to something sensible to pass on to the launcher.
   *
   * @param level the Java logging level
   * @return a sensible, non-chatty logging level
   */
  public static Level toLauncherLoggingLevel(Level level) {
    // ALL -2147483648
    // FINEST 300
    // FINER 400
    // FINE 500
    // CONFIG 700
    // INFO 800
    // WARNING 900
    // SEVERE 1000
    // OFF 2147483647

    // TODO(andreastt): Move INFO down when "--> Entering LauncherRunner::run" messages are removed
    switch (level.intValue()) {
      case 1000: // SEVERE
        return Level.SEVERE;

      case 900: // WARNING
      case 800: // INFO
        return Level.WARNING;

      case 700: // CONFIG
      case 500: // FINE
      case 400: // FINER
        return Level.FINE;

      case 300: // FINEST
      case -2147483648: // ALL
        return Level.FINEST;

      default:  // OFF
        return Level.OFF;
    }
  }

}