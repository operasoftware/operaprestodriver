package com.opera.core.systems.runner;

import com.opera.core.systems.arguments.OperaCoreArguments;
import com.opera.core.systems.arguments.OperaDesktopArguments;
import com.opera.core.systems.arguments.interfaces.OperaArguments;

import org.openqa.selenium.Platform;

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

  private File operaBinary;
  private Integer display = null;
  private String product;
  private String profile;
  private boolean noQuit;
  private String host = "127.0.0.1";
  private Integer port = 0;
  private com.opera.core.systems.arguments.interfaces.OperaArguments arguments;
  private Level loggingLevel = Level.INFO;

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
    operaBinary = new File(path);
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
    return port;
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