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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.protobuf.GeneratedMessage;

import com.opera.core.systems.Architecture;
import com.opera.core.systems.OperaProduct;
import com.opera.core.systems.OperaSettings;
import com.opera.core.systems.arguments.OperaArgument;
import com.opera.core.systems.common.io.Closeables;
import com.opera.core.systems.model.ScreenCaptureReply;
import com.opera.core.systems.runner.AbstractOperaRunner;
import com.opera.core.systems.runner.OperaLaunchers;
import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.runner.interfaces.OperaRunner;
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

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.net.PortProber;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.opera.core.systems.runner.OperaLaunchers.LAUNCHER_CHECKSUMS;

/**
 * OperaLauncherRunner implements a Java interface to communicate with the <em>opera-launcher</em>
 * utility to launch and manage Opera instances.
 */
public class OperaLauncherRunner extends AbstractOperaRunner implements OperaRunner {

  public static final String LAUNCHER_ENV_VAR = "OPERA_LAUNCHER";
  public static final String LAUNCHER_NAME = launcherNameForOS();
  public static final File LAUNCHER_DIRECTORY =
      new File(System.getProperty("user.home"), ".launcher");
  public static final File LAUNCHER_DEFAULT_LOCATION = new File(LAUNCHER_DIRECTORY, LAUNCHER_NAME);

  private final Logger logger = Logger.getLogger(getClass().getName());
  private final int launcherPort = PortProber.findFreePort();
  private final List<String> arguments;

  private OperaLauncherBinary binary = null;
  private OperaLauncherProtocol protocol = null;
  private String crashlog = null;

  public OperaLauncherRunner() {
    this(new OperaSettings());
  }

  public OperaLauncherRunner(OperaSettings s) {
    super(s);

    // Locate the bundled launcher from OperaLaunchers project and copy it to its default location
    // on users system if it's not there or outdated
    URL bundledLauncher =
        OperaLaunchers.class.getClassLoader().getResource("launchers/" + LAUNCHER_NAME);

    if (bundledLauncher == null) {
      throw new OperaRunnerException("Not able to locate bundled launcher: " + LAUNCHER_NAME);
    }

    File launcher = settings.getLauncher();
    try {
      if (launcher.getCanonicalPath().equals(LAUNCHER_DEFAULT_LOCATION.getCanonicalPath()) &&
          (!launcher.exists() || isLauncherOutdated(launcher))) {
        extractLauncher(bundledLauncher, launcher);
      }
    } catch (IOException e) {
      throw new OperaRunnerException(e);
    }

    if (!launcher.canExecute()) {
      if (!launcher.setExecutable(true)) {
        throw new OperaRunnerException("Unable to make launcher executable: " + launcher.getPath());
      }
    }

    // Create list of arguments for launcher binary
    arguments = buildArguments();
    logger.config("launcher arguments: " + arguments);

    init();
  }


  private void init() {
    try {
      binary = new OperaLauncherBinary(settings.getLauncher().getCanonicalPath(),
                                       Iterables.toArray(arguments, String.class));
    } catch (IOException e) {
      throw new OperaRunnerException("Unable to start launcher: " + e.getMessage());
    }

    logger.fine("Waiting for launcher connection on port " + launcherPort);

    ServerSocket listenerServer = null;
    try {
      // Setup listener server
      listenerServer = new ServerSocket(launcherPort);
      // TODO(andreastt): Unsafe int cast
      listenerServer.setSoTimeout((int) OperaIntervals.LAUNCHER_CONNECT_TIMEOUT.getMs());

      // Try to connect
      protocol = new OperaLauncherProtocol(listenerServer.accept());

      // We did it!
      logger.fine("Connected with launcher on port " + launcherPort);

      // Do the handshake!
      LauncherHandshakeRequest.Builder request = LauncherHandshakeRequest.newBuilder();
      ResponseEncapsulation res = protocol.sendRequest(
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
    } finally {
      Closeables.closeQuietly(listenerServer);
    }
  }

  protected List<String> buildArguments() {
    ImmutableList.Builder<String> builder = ImmutableList.builder();

    builder.add("-host").add(settings.getHost());
    builder.add("-port").add(String.format("%s", launcherPort));
    if (settings.getDisplay() != null && settings.getDisplay() > 0) {
      builder.add("-display").add(String.format(":%s", settings.getDisplay()));
    }
    if (settings.logging().getLevel() != Level.OFF) {
      builder.add("-console");  // TODO(andreastt): Allow for file logging
      builder.add("-verbosity")
          .add(toLauncherLoggingLevel(settings.logging().getLevel()).toString());
    }
    if (settings.getProduct() != OperaProduct.ALL) {
      builder.add("-profile").add(settings.getProduct().getDescriptionString());
    }
    if (settings.getBackend() != null && !settings.getBackend().isEmpty()) {
      builder.add("-backend").add(settings.getBackend());
    }
    if (settings.hasDetach()) {
      builder.add("-noquit");
    }
    builder.add("-bin").add(settings.getBinary().getAbsolutePath());

    // The launcher will pass on any extra arguments to Opera
    for (OperaArgument argument : settings.arguments()) {
      builder.add(settings.arguments().sign() + argument.getArgument());
      if (argument.getValue() != null && !argument.getValue().isEmpty()) {
        builder.add(argument.getValue());
      }
    }

    return builder.build();
  }

  /**
   * {@inheritDoc}
   *
   * @throws OperaRunnerException if launcher is shut down or not running
   */
  public void startOpera() throws OperaRunnerException {
    assertLauncherAlive();

    try {
      byte[] request = LauncherStartRequest.newBuilder().build().toByteArray();

      ResponseEncapsulation res = protocol.sendRequest(MessageType.MSG_START, request);

      if (handleStatusMessage(res.getResponse()) != StatusType.RUNNING) {
        throw new IOException("launcher unable to start binary");
      }

      // Check Opera hasn't immediately exited (e.g. due to unknown arguments)
      sleep(OperaIntervals.PROCESS_START_SLEEP);

      res = protocol.sendRequest(MessageType.MSG_STATUS, request);

      if (handleStatusMessage(res.getResponse()) != StatusType.RUNNING) {
        throw new IOException(
            "Opera exited immediately; possibly incorrect arguments?  Command: " +
            binary.getCommands());
      }
    } catch (IOException e) {
      throw new OperaRunnerException("Could not start Opera: " + e.getMessage());
    }
  }

  /**
   * {@inheritDoc}
   *
   * @throws OperaRunnerException if launcher is shut down or not running
   */
  public void stopOpera() throws OperaRunnerException {
    assertLauncherAlive();

    if (!isOperaRunning()) {
      return;
    }

    try {
      LauncherStopRequest.Builder request = LauncherStopRequest.newBuilder();

      ResponseEncapsulation res = protocol.sendRequest(
          MessageType.MSG_STOP, request.build().toByteArray()
      );

      if (handleStatusMessage(res.getResponse()) == StatusType.RUNNING) {
        throw new IOException("launcher unable to stop binary");
      }
    } catch (IOException e) {
      throw new OperaRunnerException("Could not stop Opera: " + e.getMessage());
    }
  }

  public boolean isOperaRunning() {
    return isOperaRunning(0);
  }

  public boolean isOperaRunning(int processId) {
    if (!isLauncherRunning()) {
      return false;
    }

    try {
      LauncherStatusRequest.Builder request = LauncherStatusRequest.newBuilder();
      if (processId > 0) {
        request.setProcessid(processId);
      }

      ResponseEncapsulation res = protocol.sendRequest(
          MessageType.MSG_STATUS, request.build().toByteArray());

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
    if (!isLauncherRunning()) {
      return;
    }

    try {
      // Send a shutdown command to the launcher
      protocol.sendRequestWithoutResponse(MessageType.MSG_SHUTDOWN, null);
    } catch (IOException e) {
      // If launcher has already been shutdown, this shouldn't create an exception, all we want to
      // do is to make sure the protocol is down
    }

    try {
      // Then shutdown the protocol connection
      protocol.shutdown();
    } catch (IOException e) {
      throw new OperaRunnerException("Unable to shut down launcher", e);
    } finally {
      binary.shutdown();
      protocol = null;
      binary = null;
    }
  }

  /**
   * Take screenshot using external program.  Will not trigger a screen repaint.
   *
   * @throws OperaRunnerException if runner is shutdown or not running
   * @inheritDoc
   */
  public ScreenCaptureReply captureScreen() throws OperaRunnerException {
    return captureScreen(OperaIntervals.RUNNER_SCREEN_CAPTURE_TIMEOUT.getMs());
  }

  /**
   * Take screenshot using external program.  Will not trigger a screen repaint.
   *
   * @throws OperaRunnerException if runner is shutdown or not running
   * @inheritDoc
   */
  public ScreenCaptureReply captureScreen(long timeout) throws OperaRunnerException {
    return captureScreen(timeout, (String) null);
  }

  /**
   * Take screenshot using external program.  Will not trigger a screen repaint.
   *
   * @throws OperaRunnerException if runner is shutdown or not running
   * @inheritDoc
   */
  public ScreenCaptureReply captureScreen(long timeout, String... knownMD5s)
      throws OperaRunnerException {
    assertLauncherAlive();

    String resultMd5;
    byte[] resultBytes;
    boolean blank = false;

    try {
      LauncherScreenshotRequest.Builder request = LauncherScreenshotRequest.newBuilder();
      for (String hash : knownMD5s) {
        request.addKnownMD5S(hash);
      }
      request.setKnownMD5STimeoutMs((int) timeout);

      ResponseEncapsulation res = protocol.sendRequest(
          MessageType.MSG_SCREENSHOT, request.build().toByteArray());
      LauncherScreenshotResponse response = (LauncherScreenshotResponse) res.getResponse();

      resultMd5 = response.getMd5();
      resultBytes = response.getImagedata().toByteArray();

      if (response.hasBlank()) {
        blank = response.getBlank();
      }
    } catch (SocketTimeoutException e) {
      throw new OperaRunnerException("Could not get screenshot from launcher", e);
    } catch (IOException e) {
      throw new OperaRunnerException("Could not get screenshot from launcher", e);
    }

    ScreenCaptureReply.Builder builder = ScreenCaptureReply.builder();
    builder.setMD5(resultMd5);
    builder.setPNG(resultBytes);
    builder.setBlank(blank);
    builder.setCrashed(this.hasOperaCrashed());
    return builder.build();
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

  private void extractLauncher(URL sourceLauncher, File targetLauncher) {
    checkNotNull(sourceLauncher);
    checkNotNull(targetLauncher);

    InputStream is = null;
    OutputStream os = null;

    try {
      targetLauncher.getParentFile().mkdirs();

      if (!targetLauncher.exists()) {
        Files.touch(targetLauncher);
      }

      is = sourceLauncher.openStream();
      os = new FileOutputStream(targetLauncher);

      ByteStreams.copy(is, os);
    } catch (IOException e) {
      throw new OperaRunnerException("Cannot write file to disk: " + e.getMessage());
    } finally {
      Closeables.closeQuietly(is);
      Closeables.closeQuietly(os);
    }

    logger.fine("New launcher copied to " + targetLauncher.getPath());
  }

  private boolean isLauncherOutdated(File launcher) {
    try {
      return !md5sum(md5(launcher)).equals(LAUNCHER_CHECKSUMS.get(launcher.getName()));
    } catch (NoSuchAlgorithmException e) {
      throw new OperaRunnerException(
          "Algorithm is not available in your environment: " + e.getMessage());
    } catch (IOException e) {
      throw new OperaRunnerException("Unable to open stream or file: " + e.getMessage());
    }
  }

  private void assertLauncherAlive() {
    if (!isLauncherRunning()) {
      throw new OperaRunnerException("launcher was shutdown");
    }
  }

  private boolean isLauncherRunning() {
    return binary != null && binary.isRunning();
  }

  /**
   * Asserts whether given launcher exists, is a file and that it's executable.
   *
   * @param launcher the launcher to assert
   * @throws IOException if there is a problem with the provided launcher
   */
  public static void assertLauncherGood(File launcher) throws IOException {
    if (!launcher.exists()) {
      throw new IOException("Unknown file: " + launcher.getPath());
    }

    if (!launcher.isFile()) {
      throw new IOException("Not a real file: " + launcher.getPath());
    }

    if (!FileHandler.canExecute(launcher)) {
      throw new IOException("Not executable: " + launcher.getPath());
    }
  }

  /**
   * Get the launcher's binary file name based on what flavour of operating system and what kind of
   * architecture the user is using.
   *
   * @return the launcher's binary file name
   */
  private static String launcherNameForOS() {
    // TODO(andreastt): It would be nice for OperaLaunchers and OperaDriver to both use Platform
    switch (Platform.getCurrent()) {
      case LINUX:
      case UNIX:
        // TODO(andreastt): It would be _really nice_ if OperaLaunchers and OperaDriver could both use Architecture:
        //return String.format("launcher-linux-%s", Architecture.getCurrent());

        Architecture architecture = Architecture.getCurrent();
        String launcherPrefix = "launcher-linux-%s";
        switch (architecture) {
          case X86:
            return String.format(launcherPrefix, "i386");
          case X64:
            return String.format(launcherPrefix, "amd64");
          case ARM:
            return String.format(launcherPrefix, "arm");
          default:
            throw new WebDriverException("Unsupported processor architecture: " + architecture);
        }
      case MAC:
        return "launcher-mac";
      case WINDOWS:
      case VISTA:
      case XP:
        return "launcher-win32-i86pc.exe";
      default:
        throw new WebDriverException(
            "Could not find a platform that supports bundled launchers, please set it manually");
    }
  }

  /*
   * Return the HEX sum of an MD5 byte array.
   *
   * @param bytes the md5 byte array to hex
   * @return HEX version of the byte array
   */
  private static String md5sum(byte[] bytes) {
    String result = "";
    for (byte b : bytes) {
      result += Integer.toString((b & 0xff) + 0x100, 16).substring(1);
    }
    return result;
  }

  /**
   * Get the MD5 hash of the given stream.
   *
   * @param fis the input stream to use
   * @return a byte array of the MD5 hash
   * @throws NoSuchAlgorithmException if MD5 is not available
   * @throws IOException              if an I/O error occurs
   */
  private static byte[] md5(InputStream fis) throws NoSuchAlgorithmException, IOException {
    return ByteStreams.hash(ByteStreams.newInputStreamSupplier(ByteStreams.toByteArray(fis)),
                            Hashing.md5()).asBytes();
  }

  /**
   * Get the MD5 hash of the given file.
   *
   * @param file file to compute a hash on
   * @return a byte array of the MD5 hash
   * @throws IOException              if file cannot be found
   * @throws NoSuchAlgorithmException if MD5 is not available
   */
  private static byte[] md5(File file) throws NoSuchAlgorithmException, IOException {
    return Files.hash(file, Hashing.md5()).asBytes();
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
  protected static Level toLauncherLoggingLevel(Level level) {
    // ALL -2147483648
    // FINEST 300
    // FINER 400
    // FINE 500
    // CONFIG 700
    // INFO 800
    // WARNING 900
    // SEVERE 1000
    // OFF 2147483647

    switch (level.intValue()) {
      case 1000: // SEVERE
        return Level.SEVERE;

      case 900: // WARNING
        return Level.WARNING;

      case 800: // INFO
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