package com.opera.core.systems.runner.interfaces;

import com.opera.core.systems.arguments.interfaces.OperaArguments;

import java.io.File;
import java.util.logging.Level;

/**
 * Defines a settings object which impacts OperaRunner, the interface for controlling the Opera
 * binary.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public interface OperaRunnerSettings {

  /**
   * Returns the full path to the browser binary.
   *
   * @return the absolute path to Opera's binary
   */
  public File getBinary();

  /**
   * Sets Opera's location.
   *
   * @param path the absolute path to the Opera binary
   */
  public void setBinary(String path);

  /**
   * Gets the X display.  Only works on *nix operating systems.
   *
   * @return the X display
   */
  public Integer getDisplay();

  /**
   * Sets the X display to use.  Only works on *nix operating systems.
   *
   * @param display the X display to use
   * @throws UnsupportedOperationException if on a non-nix operating system
   */
  public void setDisplay(Integer display) throws UnsupportedOperationException;

  /**
   * Returns the profile configuration specified, for example "desktop" or "core-gogi".
   *
   * @return the profile configuration set
   */
  public String getProduct();

  /**
   * Sets the profile configuration, for example "desktop" or "core-gogi".
   *
   * @param product the profile configuration to use
   */
  public void setProduct(String product);

  /**
   * Sets the directory to use for the Opera profile.  If null, generate a temporary directory.  If
   * not empty use the given directory.  To not create a temporary directory for backwards
   * compatibility reasons, set it to an empty string (""), such as for Opera < 12.
   *
   * @return the absolute path to the profile directory
   */
  public String getProfile();

  /**
   * Returns the path to the directory to use for the Opera profile.  If null a random temporary
   * directory is used.  If "", an empty string, then the default autotest profile directory is
   * used.
   *
   * @param profile the absolute path to the profile directory
   */
  public void setProfile(String profile);

  /**
   * Returns the whether Opera should quit when OperaRunner is shut down.  If enabled, it will keep
   * the browser running after the runner is shut down.
   *
   * @return true if browser should keep running, false otherwise
   */
  public boolean getNoQuit();

  /**
   * Specifies whether Opera should keep running after OperaRunner is shut down.  If enabled, it
   * will keep the browser running after the runner is shut down.
   *
   * @param noQuit true if browser should keep running, false otherwise
   */
  public void setNoQuit(boolean noQuit);

  /**
   * The host Opera should connect to.
   *
   *   0 = Random
   *   -1 = Opera default (for use with Opera < 12)
   *
   * @return the host Opera should connect to
   */
  public String getHost();

  /**
   * The host Opera should connect to.
   *
   *   0 = Random
   *   -1 = Opera default (for use with Opera < 12)
   *
   * @param host the host Opera should connect to
   */
  public void setHost(String host);

  /**
   * Returns the port Opera should connect to.  0 = Random, -1 = Opera default (for use with Opera <
   * 12).
   *
   * @return the port Opera should connect to
   */
  public Integer getPort();

  /**
   * Specifies the port Opera should connect to.  0 = Random, -1 = Opera default (for use with Opera
   * < 12).
   *
   * @param port the port Opera should connect to
   */
  public void setPort(Integer port);

  /**
   * Gets the arguments passed on to Opera.
   *
   * @return the arguments passed on to Opera
   */
  public OperaArguments getArguments();

  /**
   * Specifies what arguments to pass on to Opera.
   *
   * @param arguments which arguments to pass on to Opera
   */
  public void setArguments(OperaArguments arguments);

  /**
   * Returns the logging level of the started Opera process and the process handling mechanism.
   *
   * @return the logging level
   */
  public Level getLoggingLevel();

  /**
   * Specifies the logging level of the started Opera process and the process handling mechanism.
   *
   * @param level the logging level to use
   */
  public void setLoggingLevel(Level level);

}