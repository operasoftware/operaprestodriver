package com.opera.core.systems.runner;

import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.arguments.OperaCoreArguments;
import com.opera.core.systems.arguments.OperaDesktopArguments;
import com.opera.core.systems.arguments.interfaces.OperaArguments;

import org.openqa.selenium.io.TemporaryFilesystem;
import org.openqa.selenium.net.PortProber;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * OperaRunner is an interface for controlling the Opera browser binary.  OperaRunner implements a
 * pure-Java process manager, OperaLauncherRunner implements one in C++ with a Java API.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class OperaRunner implements com.opera.core.systems.runner.interfaces.OperaRunner {

  protected static Logger logger = Logger.getLogger(OperaRunner.class.getName());
  public OperaRunnerSettings settings;

  /**
   * Controls access to {@link #process}.
   */
  private final ReentrantLock lock = new ReentrantLock();

  /**
   * Used to spawn a new child process when this service is {@link #startOpera()} started}.
   */
  private ProcessBuilder processBuilder;  // TODO(andreastt): final

  /**
   * A reference to the current child process.  Will be <code>null</code> whenever this service is
   * not running.
   */
  private Process process = null;

  public OperaRunner() {
    this(OperaRunnerSettings.getDefaultSettings());
  }

  public OperaRunner(OperaRunnerSettings s) {
    settings = s;

    // Use arguments provided by user if set
    OperaArguments arguments;
    if (settings.getProduct() != null && settings.getProduct().equals("desktop")) {
      arguments = new OperaDesktopArguments();
    } else {
      arguments = new OperaCoreArguments();
    }

    // This can't be last, otherwise it might get interpreted as the page to open, and the file
    // listing page doesn't have a JS context to inject into.
    String profile = settings.getProfile();

    // If null, generate a temp directory, if not empty use the given directory.
    if (profile == null) {
      profile = TemporaryFilesystem
          .getDefaultTmpFS()
          .createTempDir("opera-profile", "")
          .getAbsolutePath();
      settings.setProfile(profile);
      arguments.add("-pd", profile);
    } else if (!profile.isEmpty()) {
      arguments.add("-pd", profile);
    }

    // The port Opera should connect to.  0 = Random, -1 = Opera default (for use with Opera < 12).
    // Currently we don't append -debugproxy if port is set to -1 because of backwards compatibility.
    Integer port = settings.getPort();
    if (port == 0) {
      arguments.add("debugproxy", settings.getHost() + ":" + PortProber.findFreePort());
    } else if (port != -1) {
      // Provide defaults if one hasn't been set
      arguments.add("debugproxy", settings.getHost() + ":" + port);
    }

    arguments.add("autotestmode");

    // We read in environmental variable OPERA_ARGS in addition to existing arguments passed down
    // from OperaArguments.  These are combined and sent to the browser.
    //
    // Note that this is a deviation from the principle of arguments normally overwriting
    // environmental variables.

    // TODO(andreastt): WTF is going on here?
    /*
    OperaArguments userArguments = settings.getArguments();
    for (OperaArgument argument : userArguments.getArguments()) {
      if (argument.getValue() != null && !argument.getValue().isEmpty()) {
        arguments.add(argument.getArgument(), argument.getValue());
      } else {
        arguments.add(argument.getArgument());
      }
    }
    */

    arguments.merge(settings.getArguments());
    settings.setArguments(arguments);

    logger.config("Opera arguments: " + settings.getArguments().getArguments().toString());

    // TODO(andreastt): Should this be abstracted into its own class?
    //processBuilder = new ProcessBuilder(settings.getArguments().getArgumentsAsStrings());
  }

  public void startOpera() {
    lock.lock();

    try {
      if (process != null) {
        return;
      }
      process = processBuilder.start();
      pipe(process.getErrorStream(), System.err);
      pipe(process.getInputStream(), System.out);
    } catch (IOException e) {
      // TODO(andreastt): Can we check this earlier?
    } finally {
      lock.unlock();
    }
  }

  public void stopOpera() {
    lock.lock();

    try {
      if (process == null) {
        return;
      }

      // TODO(andreastt): Implement
    } finally {
      process = null;
      lock.unlock();
    }
  }

  public boolean isOperaRunning() {
    return isOperaRunning(0);
  }

  public boolean isOperaRunning(int processID) {
    lock.lock();

    try {
      if (process == null) {
        return false;
      }
      process.exitValue();
      return false;
    } catch (IllegalThreadStateException e) {
      return true;
    } finally {
      lock.unlock();
    }
  }

  public boolean hasOperaCrashed() {
    return false;
  }

  public String getOperaCrashlog() {
    return "";
  }

  /**
   * Handles safe shutdown of the OperaRunner class.
   */
  public void shutdown() {
  }

  public ScreenShotReply saveScreenshot(long timeout, String... hashes) {
    throw new UnsupportedOperationException("not implemented");
  }

  // http://stackoverflow.com/questions/60302
  private static void pipe(final InputStream src, final PrintStream dest) {
    new Thread(new Runnable() {
      public void run() {
        try {
          byte[] buffer = new byte[1024];
          for (int n = 0; n != -1; n = src.read(buffer)) {
            dest.write(buffer, 0, n);
          }
        } catch (IOException e) {
          // Do nothing.
        }
      }
    }).start();
  }

}