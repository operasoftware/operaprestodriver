package com.opera.core.systems.runner;

import com.opera.core.systems.arguments.OperaCoreArguments;
import com.opera.core.systems.arguments.OperaDesktopArguments;
import com.opera.core.systems.arguments.interfaces.OperaArguments;
import com.opera.core.systems.scope.internal.OperaIntervals;

import org.openqa.selenium.Platform;
import org.openqa.selenium.io.TemporaryFilesystem;
import org.openqa.selenium.net.PortProber;

import java.io.File;
import java.util.logging.Level;

/**
 * Defines a settings object which impacts OperaRunner, the interface for controlling the Opera
 * binary.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class OperaRunnerSettings
    implements com.opera.core.systems.runner.interfaces.OperaRunnerSettings {

  private File    operaBinary  = null;
  private Integer display      = null;
  private String  product      = null;
  private String  profile      = null;         // "" for Opera < 12
  private boolean noQuit       = false;
  private String  host         = "127.0.0.1";
  private Integer port         = 0;            // -1 for Opera < 12
  private Level   loggingLevel = Level.INFO;
  private com.opera.core.systems.arguments.interfaces.OperaArguments arguments;

  public OperaRunnerSettings() {
    // We read in environmental variable OPERA_ARGS in addition to existing arguments passed down
    // through OperaRunnerSettings/OperaArguments.  These are combined and can be retrieved using
    // OperaDriverSettings.getArguments().
    //
    // Note that this is a deviation from the principle of arguments normally overwriting
    // environmental variables.
    String environmentArguments = System.getenv("OPERA_ARGS");
    if (environmentArguments != null && !environmentArguments.isEmpty()) {
      OperaArguments
          parsedEnvironmentArguments =
          com.opera.core.systems.OperaArguments.parse(environmentArguments);
      if (arguments != null && !arguments.getArguments().isEmpty()) {
        arguments.merge(parsedEnvironmentArguments);
      } else {
        arguments = parsedEnvironmentArguments;
      }
    } else {
      arguments = new com.opera.core.systems.OperaArguments();
    }
  }

  public File getBinary() {
    return operaBinary;
  }

  public void setBinary(String path) {
    if (path != null && !path.isEmpty()) {
      File binary = new File(path);
      if (binary.exists()) {
        operaBinary = binary;
      }
    }
  }

  public Integer getDisplay() {
    return display;
  }

  public void setDisplay(Integer display) throws UnsupportedOperationException {
    if (Platform.getCurrent() != Platform.LINUX &&
        Platform.getCurrent() != Platform.UNIX) {
      throw new UnsupportedOperationException(
          "Unsupported operating system: " + Platform.getCurrent());
    }
    this.display = display;
  }

  public String getProduct() {
    return product;
  }

  public void setProduct(String product) {
    this.product = product;
  }

  public String getProfile() {
    if (profile == null) {
      profile = TemporaryFilesystem.getDefaultTmpFS().createTempDir("opera-profile", "")
          .getAbsolutePath();
    }

    return profile;
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

  public boolean getNoQuit() {
    return noQuit;
  }

  public void setNoQuit(boolean noQuit) {
    this.noQuit = noQuit;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Integer getPort() {
    // The port Opera should connect to.  0 = Random, -1 = Opera default (7001) (for use with Opera
    // < 12).
    if (port == 0) {
      port = PortProber.findFreePort();
    } else if (port == 0 && supportsDebugProxy()) {
      port = (int) OperaIntervals.SERVER_PORT.getValue();
    }

    return port;
  }

  public boolean supportsDebugProxy() {
    return port != -1;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public OperaArguments getArguments() {
    return arguments;
  }

  public void setArguments(OperaArguments arguments) {
    this.arguments = arguments;
  }

  public Level getLoggingLevel() {
    return loggingLevel;
  }

  public void setLoggingLevel(Level level) {
    loggingLevel = level;
  }

  public static OperaRunnerSettings getDefaultSettings() {
    OperaRunnerSettings settings = new OperaRunnerSettings();

    // TODO(andreastt): WTF?  This is already defined as default values to the fields during initialization
    //settings.setProduct("core-gogi");
    //settings.setPort(0);

    OperaArguments arguments;
    if (settings.getProduct().equals("desktop")) {
      arguments = new OperaDesktopArguments();
    } else {
      arguments = new OperaCoreArguments();
    }

    settings.setArguments(arguments);
    return settings;
  }

}