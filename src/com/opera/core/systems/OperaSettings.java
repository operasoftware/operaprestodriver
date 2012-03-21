/*
Copyright 2012 Opera Software ASA

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

package com.opera.core.systems;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableMap;

import com.opera.core.systems.runner.launcher.OperaLauncherRunner;
import com.opera.core.systems.scope.internal.OperaFlags;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import static com.opera.core.systems.OperaProduct.CORE;
import static com.opera.core.systems.OperaProduct.DESKTOP;
import static com.opera.core.systems.OperaSettings.Capability.ARGUMENTS;
import static com.opera.core.systems.OperaSettings.Capability.AUTOSTART;
import static com.opera.core.systems.OperaSettings.Capability.BACKEND;
import static com.opera.core.systems.OperaSettings.Capability.BINARY;
import static com.opera.core.systems.OperaSettings.Capability.DISPLAY;
import static com.opera.core.systems.OperaSettings.Capability.HOST;
import static com.opera.core.systems.OperaSettings.Capability.LAUNCHER;
import static com.opera.core.systems.OperaSettings.Capability.LOGGING_FILE;
import static com.opera.core.systems.OperaSettings.Capability.LOGGING_LEVEL;
import static com.opera.core.systems.OperaSettings.Capability.NO_QUIT;
import static com.opera.core.systems.OperaSettings.Capability.NO_RESTART;
import static com.opera.core.systems.OperaSettings.Capability.OPERAIDLE;
import static com.opera.core.systems.OperaSettings.Capability.PORT;
import static com.opera.core.systems.OperaSettings.Capability.PRODUCT;
import static com.opera.core.systems.OperaSettings.Capability.PROFILE;
import static com.opera.core.systems.scope.internal.OperaIntervals.SERVER_DEFAULT_PORT_IDENTIFIER;
import static com.opera.core.systems.scope.internal.OperaIntervals.SERVER_PORT;
import static com.opera.core.systems.scope.internal.OperaIntervals.SERVER_RANDOM_PORT_IDENTIFIER;
import static org.openqa.selenium.Platform.LINUX;

/**
 * Manages settings specific to {@link OperaDriver}.
 *
 * OperaSettings serves as the internal storage for OperaDriver related settings and as a converted
 * between {@link DesiredCapabilities} and other internal resources.
 *
 * Example usage:
 *
 * <pre><code>
 *   OperaSettings settings = new OperaSettings();
 *   settings.setBinary(new File("/path/to/opera"));
 *   settings.arguments().add("foo", "bar");
 *   settings.logging().level(Level.FINE);
 * </code></pre>
 *
 * For use with {@link OperaDriver}:
 *
 * <pre><code>
 *   OperaDriver driver = new OperaDriver(settings);
 * </code></pre>
 *
 * For use with {@link org.openqa.selenium.remote.RemoteWebDriver}:
 *
 * <pre><code>
 *   DesiredCapabilities capabilities = DesiredCapabilities.opera();
 *   capabilities.merge(settings.toCapabilities());
 *   RemoteWebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"),
 *                                                capabilities);
 * </code></pre>
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 * @since 0.12
 */
public class OperaSettings {

  /**
   * The capabilities specifically available to Opera.  The capabilities may be used with {@link
   * DesiredCapabilities#opera()}.
   */
  public enum Capability {

    /**
     * (String) How verbose the logging should be. Available levels are: SEVERE (highest value),
     * WARNING, INFO, CONFIG, FINE, FINER, FINEST (lowest value), ALL. Default is INFO.
     */
    LOGGING_LEVEL("logging.level") {
      Level getDefaultValue() {
        return Level.INFO;
      }
    },

    /**
     * (String) Where to send the output of the logging. Default is to not write to file.
     */
    LOGGING_FILE("logging.file"),

    /**
     * (String) Path to the Opera binary to use. If not specified, OperaDriver will guess the path
     * to your Opera installation (typically <code>/usr/bin/opera</code> or <code>C:\Program
     * Files\Opera\opera.exe</code>).
     */
    BINARY(),

    /**
     * (String) Arguments to pass to Opera, separated by spaces. See <code>opera -help</code> for
     * available command-line switches.
     */
    ARGUMENTS() {
      OperaArguments getDefaultValue() {
        return new OperaArguments();
      }
    },

    /**
     * (String) The host Opera should connect to. Unless you're starting Opera manually you won't
     * need this.
     */
    HOST() {
      String getDefaultValue() {
        return "127.0.0.1";
      }
    },

    /**
     * (Integer) The port to Opera should connect to. 0 = Random, -1 = Opera default (for use with
     * Opera < 11.60).
     */
    PORT() {
      Integer getDefaultValue() {
        return (int) SERVER_RANDOM_PORT_IDENTIFIER.getValue();
      }
    },

    /**
     * (String) Path to the launcher binary to use. The launcher is a gateway between OperaDriver
     * and the Opera browser, and is being used for controlling the binary and taking external
     * screenshots. If left blank, OperaDriver will use the launcher supplied with the package.
     */
    LAUNCHER(),

    /**
     * (Object) Directory to use for the Opera profile. If an {@link OperaProfile} object that will
     * be used when starting opera. If null a random temporary directory is used. If "", an empty
     * string, then the default <code>.autotest</code> profile directory will be used (for backwards
     * compatibility with Opera < 11.60).
     */
    PROFILE() {
      OperaProfile getDefaultValue() {
        return new OperaProfile();
      }
    },

    /**
     * (Boolean) Whether to use Opera's alternative implicit wait implementation. It will use an
     * in-browser heuristic to guess when a page has finished loading, allowing us with great
     * accuracy tell whether there are any planned events in the document. This functionality is
     * useful for very simple test cases, but not designed for real-world testing. It is disabled by
     * default.
     */
    OPERAIDLE("idle") {
      Boolean getDefaultValue() {
        return OperaFlags.ENABLE_IDLE;
      }
    },

    /**
     * (Integer) The X display to use. (Only works on *nix OSes.)
     */
    DISPLAY(),

    /**
     * (Boolean) Whether to auto-start the Opera binary. If false, OperaDriver will wait for a
     * connection from the browser. Go to "opera:debug", enter the correct port number, and hit
     * "Connect" to connect manually.
     */
    AUTOSTART() {
      Boolean getDefaultValue() {
        return OperaFlags.ENABLE_AUTOSTART;
      }
    },

    /**
     * (Boolean) Whether to restart.
     */
    NO_RESTART() {
      Boolean getDefaultValue() {
        return false;
      }
    },

    /**
     * (Boolean) Whether to quit Opera when OperaDriver is shut down. If enabled, it will keep the
     * browser running after the driver is shut down.
     */
    NO_QUIT() {
      Boolean getDefaultValue() {
        return false;
      }
    },

    /**
     * (String) The product we are using, for example "desktop" or "core".
     */
    PRODUCT() {
      OperaProduct getDefaultValue() {
        return DESKTOP;
      }
    },

    /**
     * (String) Rendering backend used by internal gogi builds of Opera.  If null or an empty
     * string, "software" is used by default.
     *
     * <strong>Warning:</strong> This is a temporary workaround for launcher not taking external
     * window-only screenshots on Windows.
     */
    BACKEND() {
      String getDefaultValue() {
        return "software";
      }
    };

    private static final String CAPABILITY = "opera.";
    //private static final ImmutableMap<String, Capability> lookup = ImmutableMap.of();
    private static final Map<String, Capability> lookup = new LinkedHashMap<String, Capability>();

    private final String identifier;
    private final String capability;
    
    static {
      for (Capability capability : values()) {
        lookup.put(capability.getCapability(), capability);
      }
    }

    private Capability() {
      identifier = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
      capability = CAPABILITY + name().toLowerCase();
    }

    private Capability(String capabilityName) {
      identifier = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
      capability = CAPABILITY + capabilityName;
    }

    public final String getIdentifier() {
      return identifier;
    }

    /**
     * Gets the string identifier of this capability for use in {@link DesiredCapabilities}.
     *
     * @return identifier of this capability
     */
    public final String getCapability() {
      return capability;
    }

    /**
     * Gets the default value connected with this capability.
     *
     * @return the default value of this capability
     */
    Object getDefaultValue() {
      return null;
    }

    /**
     * Gets the string representation of this capability.
     *
     * @return easy to read string representation
     */
    public String toString() {
      return getCapability();
    }

    /**
     * Looks up a capability by its capability name.
     *
     * @param capabilityName the string representation of the capability
     * @return the corresponding capability
     */
    public static Capability findCapability(String capabilityName) {
      return lookup.get(capabilityName);
    }

  }

  private class CapabilityInstance {

    private final String identifier;
    private final String capabilityName;
    private Object value = null;

    public CapabilityInstance(final Capability capability) {
      identifier = capability.getIdentifier();
      capabilityName = capability.getCapability();
      value = capability.getDefaultValue();
    }

    public final String getIdentifier() {
      return identifier;
    }

    public final String getCapability() {
      return capabilityName;
    }

    public Object getValue() {
      return value;
    }

    public void setValue(Object value) {
      this.value = value;
    }

    public String toString() {
      return String.format("%s=%s", getIdentifier(), getValue());
    }
  }

  private final Map<Capability, CapabilityInstance> options =
      new HashMap<Capability, CapabilityInstance>();
  private final DesiredCapabilities surplusCapabilities = new DesiredCapabilities();
  private final OperaLogging logging = new OperaLogging();

  private boolean supportsPd = true;

  public OperaSettings() {
    for (Capability capability : Capability.values()) {
      options.put(capability, new CapabilityInstance(capability));
    }
  }

  public OperaSettings(OperaProfile profile) {
    this();
    setProfile(profile);
  }

  public OperaLogging logging() {
    return logging;
  }

  public class OperaLogging {

    public Level level() {
      return (Level) options.get(LOGGING_LEVEL).getValue();
    }

    public void level(Level level) {
      options.get(LOGGING_LEVEL).setValue(level);
    }

    public File file() {
      return (File) options.get(LOGGING_FILE).getValue();
    }

    public void file(File file) {
      options.get(LOGGING_FILE).setValue(file);
    }
  }

  /**
   * Returns the browser binary executable.
   *
   * @return the Opera binary
   */
  public File getBinary() {
    return (File) options.get(BINARY).getValue();
  }

  /**
   * Sets Opera's binary executable location.
   *
   * @param binary the Opera binary
   */
  public void setBinary(File binary) {
    options.get(BINARY).setValue(binary);
  }

  /**
   * Gets the arguments passed on to Opera.
   *
   * @return arguments passed on to Opera
   */
  public OperaArguments arguments() {
    return (OperaArguments) options.get(ARGUMENTS).getValue();
  }

  /**
   * Returns the Scope server host the debugger should connect to.
   *
   * @return the host
   */
  public String getHost() {
    return (String) options.get(HOST).getValue();
  }

  /**
   * Specifies which Scope server the debugger should connect to.
   *
   * @param ip IP address (hostnames are not allowed) to the Scope server
   */
  public void setHost(String ip) {
    options.get(HOST).setValue(ip);
  }

  /**
   * Gets the port the debugger should connect to.
   *
   * @return local port number for the debugger to connect to
   */
  public int getPort() {
    // 0 = random, -1 = Opera default (7001) (for use with Opera < 11.60)
    Integer port = (Integer) options.get(PORT).getValue();

    if (port == SERVER_RANDOM_PORT_IDENTIFIER.getValue()) {
      port = PortProber.findFreePort();
    } else if (port == SERVER_DEFAULT_PORT_IDENTIFIER.getValue()) {
      port = (int) SERVER_PORT.getValue();
    }

    return port;
  }

  /**
   * Specifies the port the debugger should connect to.
   *
   * @param port local port number for the debugger to connect to
   */
  public void setPort(int port) {
    options.get(PORT).setValue(port);
  }

  public File getLauncher() {
    File environmentLauncher = new File(System.getenv("OPERA_LAUNCHER"));
    
    if (environmentLauncher.isFile() || environmentLauncher.exists()) {
      return environmentLauncher;
    } else if (options.get(LAUNCHER).getValue() != null) {
      return (File) options.get(LAUNCHER).getValue();
    } else {
      return new File(System.getProperty("user.home") + File.separator +
                      OperaLauncherRunner.launcherNameForOS());
    }
  }

  public void setLauncher(File launcherBinary) {
    options.get(LAUNCHER).setValue(launcherBinary);
  }

  public OperaProfile profile() {
    return (OperaProfile) options.get(PROFILE).getValue();
  }

  public void setProfile(String profileDirectory) {
    OperaProfile profile;

    if (profileDirectory != null && !profileDirectory.isEmpty()) {  // use this profile
      profile = new OperaProfile(profileDirectory);
    } else if (profileDirectory == null) {  // random profile
      profile = new OperaProfile();
    } else {  // "" (empty string), use ~/.autotest
      // TODO(andreastt): What are the autotest directories on Windows and Mac?
      supportsPd = false;
      profile = new OperaProfile(new File(System.getProperty("user.home") +
                                          File.separator + ".autotest"));
    }

    options.get(PROFILE).setValue(profile);
  }

  public void setProfile(OperaProfile profile) {
    options.get(PROFILE).setValue(profile);
  }

  public boolean useIdle() {
    return (Boolean) options.get(OPERAIDLE).getValue();
  }

  public void setIdle(boolean enabled) {
    options.get(OPERAIDLE).setValue(enabled);
  }

  public Integer getDisplay() {
    return (Integer) options.get(DISPLAY).getValue();
  }

  public void setDisplay(int display) {
    if (!Platform.getCurrent().is(LINUX)) {
      throw new UnsupportedOperationException("Unsupported platform: " + Platform.getCurrent());
    }

    options.get(DISPLAY).setValue(display);
  }

  public boolean autostart() {
    return (Boolean) options.get(AUTOSTART).getValue();
  }

  public void autostart(boolean enabled) {
    options.get(AUTOSTART).setValue(enabled);
  }

  public OperaProduct getProduct() {
    return (OperaProduct) options.get(PRODUCT).getValue();
  }

  public void setProduct(OperaProduct product) {
    options.get(PRODUCT).setValue(product);
  }

  @Deprecated
  public boolean noRestart() {
    return (Boolean) options.get(NO_RESTART).getValue();
  }

  @Deprecated
  public void noRestart(boolean enabled) {
    options.get(NO_RESTART).setValue(enabled);
  }

  @Deprecated
  public boolean noQuit() {
    return (Boolean) options.get(NO_QUIT).getValue();
  }

  @Deprecated
  public void noQuit(boolean enabled) {
    options.get(NO_QUIT).setValue(enabled);
  }

  @Deprecated
  public String getBackend() {
    return (String) options.get(BACKEND).getValue();
  }

  @Deprecated
  public void setBackend(String backend) {
    options.get(BACKEND).setValue(backend);
  }

  /**
   * Whether or not the currently specified Opera configuration supports the
   * <code>-debugproxy</code> command-line argument.  If the specified port <em>is not equal</em> to
   * the default proxy server port specified in {@link com.opera.core.systems.scope.internal.OperaIntervals#SERVER_PORT},
   * this will be true.
   *
   * @return true if this configuration supports the <code>-debugproxy</code> command-line argument,
   *         false otherwise
   */
  public boolean supportsDebugProxy() {
    return getPort() != SERVER_PORT.getValue();
  }

  /**
   * Whether or not the currently specified Opera configuration supports the <code>-pd</code>
   * command-line argument.
   *
   * @return true if this configuration supports the <code>-pd</code> command-line argument.
   */
  public boolean supportsPd() {
    return !getProduct().is(CORE) && supportsPd;
  }

  public OperaSettings merge(Capabilities capabilities) {
    for (Map.Entry<String, ?> capability : capabilities.asMap().entrySet()) {
      Capability capabilityReference = Capability.findCapability(capability.getKey());
      
      if (options.containsKey(capabilityReference)) {
        options.get(capabilityReference).setValue(capability.getValue());
        continue;
      }

      // Store the rest as surplus capabilities
      surplusCapabilities.setCapability(capability.getKey(), capability.getValue());
    }

    return this;
  }

  /**
   * Returns this as capabilities.  This does not copy the settings.  Further changes will be
   * reflected in the capabilities.
   *
   * @return Capabilities for Opera with these settings
   */
  public Capabilities toCapabilities() {
    DesiredCapabilities capabilities = DesiredCapabilities.opera();

    for (CapabilityInstance option : options.values()) {
      capabilities.setCapability(option.getCapability(), option.getValue());
    }

    return capabilities;
  }

  /**
   * Converts this instance to its JSON representation.
   *
   * @return the JSON representation of these settings
   * @throws IOException   if an I/O error occurs
   * @throws JSONException if an error occurs while encoding these settings as JSON
   */
  public JSONObject toJson() throws IOException, JSONException {
    JSONObject json = new JSONObject();

    for (CapabilityInstance option : options.values()) {
      if (option.getValue() == null) {
        continue;
      }

      if (option.getValue() instanceof OperaProfile) {
        json.put(option.getIdentifier(), ((OperaProfile) option.getValue()).toJson());
      } else if (option.getValue() instanceof OperaArguments
                 && ((OperaArguments) option.getValue()).size() > 0) {
        json.put(option.getIdentifier(), ((OperaArguments) option.getValue()).toJson());
      } else if (option.getValue() instanceof Boolean) {
        json.put(option.getIdentifier(), (Boolean) option.getValue() ? 1 : 0);
      } else {
        json.put(option.getIdentifier(), String.valueOf(option.getValue()));
      }
    }

    return json;
  }

  /**
   * String representation of the Opera specific settings.
   *
   * @return string representation of this
   */
  public String toString() {
    return String.format("OperaSettings %s", options);
  }

  /**
   * Supplier of an {@link OperaSettings} instance with the ability to specify which {@link
   * #usingProfile(OperaProfile)} to use.
   */
  public static class Builder {

    private final OperaSettings settings = new OperaSettings();

    /**
     * Specify which profile to base the settings on.
     *
     * @param profile profile to base the settings on
     * @return reference to self
     */
    public Builder usingProfile(OperaProfile profile) {
      settings.setProfile(profile);
      return this;
    }

    /**
     * Get a new instance of {@link OperaSettings}.
     *
     * @return new settings instance
     */
    public OperaSettings get() {
      return settings;
    }

  }

}