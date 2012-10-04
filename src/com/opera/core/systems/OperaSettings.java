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
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

import com.opera.core.systems.OperaLogs.DriverLogsHandler;
import com.opera.core.systems.arguments.OperaArgument;
import com.opera.core.systems.arguments.OperaCoreArguments;
import com.opera.core.systems.arguments.OperaDesktopArguments;
import com.opera.core.systems.common.lang.OperaBoolean;
import com.opera.core.systems.mobile.MobileDevices;
import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.runner.launcher.OperaLauncherRunner;
import com.opera.core.systems.internal.OperaDefaults;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.opera.core.systems.OperaProduct.CORE;
import static com.opera.core.systems.OperaProduct.DESKTOP;
import static com.opera.core.systems.OperaSettings.Capability.ARGUMENTS;
import static com.opera.core.systems.OperaSettings.Capability.AUTOSTART;
import static com.opera.core.systems.OperaSettings.Capability.BACKEND;
import static com.opera.core.systems.OperaSettings.Capability.BINARY;
import static com.opera.core.systems.OperaSettings.Capability.DETACH;
import static com.opera.core.systems.OperaSettings.Capability.DISPLAY;
import static com.opera.core.systems.OperaSettings.Capability.EMULATION_PROFILE;
import static com.opera.core.systems.OperaSettings.Capability.HOST;
import static com.opera.core.systems.OperaSettings.Capability.LAUNCHER;
import static com.opera.core.systems.OperaSettings.Capability.LOGGING_FILE;
import static com.opera.core.systems.OperaSettings.Capability.LOGGING_LEVEL;
import static com.opera.core.systems.OperaSettings.Capability.NO_RESTART;
import static com.opera.core.systems.OperaSettings.Capability.OPERAIDLE;
import static com.opera.core.systems.OperaSettings.Capability.PORT;
import static com.opera.core.systems.OperaSettings.Capability.PRODUCT;
import static com.opera.core.systems.OperaSettings.Capability.PROFILE;
import static com.opera.core.systems.OperaSettings.Capability.PROXY;
import static com.opera.core.systems.OperaSettings.Capability.RUNNER;
import static com.opera.core.systems.runner.launcher.OperaLauncherRunner.LAUNCHER_ENV_VAR;
import static com.opera.core.systems.internal.OperaDefaults.SERVER_DEFAULT_PORT;
import static com.opera.core.systems.internal.OperaDefaults.SERVER_DEFAULT_PORT_IDENTIFIER;
import static com.opera.core.systems.internal.OperaDefaults.SERVER_RANDOM_PORT_IDENTIFIER;
import static org.openqa.selenium.Platform.LINUX;

/**
 * Manages settings specific to {@link OperaDriver}.
 *
 * OperaSettings serves as the internal storage for OperaDriver related settings and as a converter
 * between {@link DesiredCapabilities} and other internal resources.
 *
 * Example usage:
 *
 * <pre><code>
 *   OperaSettings settings = new OperaSettings();
 *   settings.setBinary(new File("/path/to/opera"));
 *   settings.arguments().add("foo", "bar");
 *   settings.logging().getLevel(Level.FINE);
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
 * @since 0.12
 */
public class OperaSettings {

  /**
   * The capabilities specifically available to Opera.  The capabilities may be used with {@link
   * DesiredCapabilities#opera()}.
   */
  public enum Capability {

    /**
     * (String/Level/Integer) How verbose the logging should be.  Available levels are: SEVERE
     * (highest value), WARNING, INFO, CONFIG, FINE, FINER, FINEST (lowest value), ALL, OFF. Default
     * is INFO.
     *
     * The argument may consist of either a level name as a string, an integer value, a {@link
     * Level} reference, or null.  If the value is neither of a known name nor an integer, an {@link
     * IllegalArgumentException} will be thrown.
     */
    LOGGING_LEVEL("opera.logging.level") {
      Level getDefaultValue() {
        return Level.INFO;
      }

      Object sanitize(Object value) {
        if (value == null || value instanceof Level) {
          return value;
        }

        return Level.parse(String.valueOf(value).toUpperCase());
      }
    },

    /**
     * (String/File) Where to send the output of the logging.  Default is to not write to file.
     */
    LOGGING_FILE("opera.logging.file") {
      Object sanitize(Object path) {
        if (path != null && path instanceof String) {
          return new File(String.valueOf(path));
        }

        return path;
      }
    },

    /**
     * (String/File) Path to the Opera binary to use.  If not specified, OperaDriver will guess the
     * path to your Opera installation (typically <code>/usr/bin/opera</code>, <code>C:\Program
     * Files\Opera\opera.exe</code>, or similar).
     */
    BINARY() {
      Object sanitize(Object binary) {
        if (binary == null) {
          return binary;
        }

        if (binary instanceof String) {
          return new OperaBinary(String.valueOf(binary)).getFile();
        } else if (binary instanceof File) {
          return new OperaBinary((File) binary).getFile();
        }

        throw new IllegalArgumentException("Unknown type: " + binary.getClass().getSimpleName());
      }
    },

    /**
     * (String) Arguments to pass to Opera, separated by spaces.  See <code>opera -help</code> for
     * available command-line switches.
     */
    ARGUMENTS() {
      OperaArguments getDefaultValue() {
        return new OperaCoreArguments();
      }

      OperaArguments sanitize(Object arguments) {
        if (arguments instanceof String) {
          return new OperaArguments(String.valueOf(arguments));
        } else if (arguments instanceof OperaArguments) {
          return (OperaArguments) arguments;
        }

        return new OperaArguments();
      }
    },

    /**
     * (String) The host Opera should connect to.  Since OperaDriver works in a client-server
     * relationship to Opera (where Opera is the client, driver the server) you can also run remote
     * instances of Opera on other devices; that be a phone, a TV or another computer.
     */
    HOST() {
      String getDefaultValue() {
        return "127.0.0.1";
      }

      Object sanitize(Object host) {
        if (host == null) {
          return host;
        }

        return String.valueOf(host);
      }
    },

    /**
     * (Integer) The port to Opera should connect to.  Setting this capability to 0 will probe for a
     * free, random port, setting it to -1 will ensure compatibility mode using port the default
     * port 7001 for Operas version 11.52 or older.
     */
    PORT() {
      Integer getDefaultValue() {
        return PortProber.findFreePort();
      }

      Integer sanitize(Object value) {
        checkNotNull(value);
        int port = Integer.valueOf(String.valueOf(value));

        // 0 = random, -1 = Opera default (7001) (for use with Opera < 11.60)
        if (port == SERVER_RANDOM_PORT_IDENTIFIER) {
          port = PortProber.findFreePort();
        } else if (port == SERVER_DEFAULT_PORT_IDENTIFIER) {
          port = SERVER_DEFAULT_PORT;
        }

        return port;
      }
    },

    /**
     * (String) Path to the launcher binary to use.  The launcher is an external wrapper around the
     * browser, and is used for controlling the binary and taking external screenshots.  If left
     * blank, OperaDriver will use a launcher supplied with the package.
     */
    LAUNCHER() {
      File getDefaultValue() {
        if (System.getenv(LAUNCHER_ENV_VAR) != null && !System.getenv(LAUNCHER_ENV_VAR).isEmpty()) {
          return new File(System.getenv(LAUNCHER_ENV_VAR));
        }

        return OperaLauncherRunner.LAUNCHER_DEFAULT_LOCATION;
      }

      Object sanitize(Object path) {
        if (path != null) {
          return new File(String.valueOf(path));
        }

        return path;
      }
    },

    /**
     * (OperaProfile/String) Directory of the profile to use, or an {@link OperaProfile} instance
     * object representing a profile.  If null is given, a random temporary directory is used.  If
     * "", an empty string, then the default <code>~/.autotest</code> profile directory will be used
     * (for backwards compatibility with Opera < 11.60).
     */
    PROFILE() {
      OperaProfile getDefaultValue() {
        return new OperaProfile();
      }

      Object sanitize(Object value) {
        checkNotNull(value);

        if (value instanceof String && String.valueOf(value) != null) {
          String profileDirectory = String.valueOf(value);
          if (!profileDirectory.isEmpty()) {
            return new OperaProfile(profileDirectory);
          }
        } else if (value instanceof OperaProfile) {
          return value;
        } else if (value instanceof Map) {
          @SuppressWarnings("rawtypes")
          Map map = (Map) value;
          if (map.containsKey("base64")) {
            try {
              return OperaProfile.fromJson((String) map.get("base64"));
            } catch (IOException e) {
              throw new WebDriverException(e);
            }
          }
        }

        return new OperaProfile();
      }
    },

    /**
     * (Boolean) Whether to use Opera's alternative implicit wait implementation.  It will use an
     * in-browser heuristic to guess when a page has finished loading, allowing us with great
     * accuracy tell whether there are any planned events in the document.  This functionality is
     * useful for very simple test cases, but not designed for real-world testing.  It is disabled
     * by default.
     */
    OPERAIDLE("opera.idle") {
      Boolean getDefaultValue() {
        return OperaDefaults.ENABLE_IDLE;
      }

      Boolean sanitize(Object enabled) {
        checkNotNull(enabled);
        return OperaBoolean.parseBoolean(String.valueOf(enabled));
      }
    },

    /**
     * (Integer) The X display to use.  If set, Opera will be started on the specified display.
     * (Only works on GNU/Linux.)
     */
    DISPLAY() {
      Integer sanitize(Object display) {
        checkNotNull(display);
        return Integer.parseInt(String.valueOf(display));
      }
    },

    /**
     * (Boolean) Whether to auto-start the Opera binary.  If false, OperaDriver will wait for a
     * connection from the browser.  Go to "opera:debug", enter the correct host/port information,
     * and hit "Connect" to connect manually.
     */
    AUTOSTART() {
      Boolean getDefaultValue() {
        return OperaDefaults.ENABLE_AUTOSTART;
      }

      Boolean sanitize(Object enabled) {
        checkNotNull(enabled);
        return OperaBoolean.parseBoolean(String.valueOf(enabled));
      }
    },

    /**
     * (Proxy/Proxy JSON object) Details of any proxy to use.  If no proxy is specified, whatever
     * the system's current or default state is used.  Accepted types are {@link Proxy} or a proxy
     * JSON object like this:
     *
     * <pre><dl>
     *   <dt>proxyType (string)</dt>
     *   <dd>(Required) The type of proxy being used.  Possible values are: <em>direct</em>, a
     * direct connection where no proxy in use; <em>manual</em>, a manual proxy settings
     * configured,
     * e.g. setting a proxy for HTTP, a proxy for FTP, etc; <em>pac</em>, proxy auto configuration
     * from a URL); <em>autodetect</em>, proxy auto detection, probably with WPAD; <em>system</em>,
     * use system settings.</dd>
     *
     *   <dt>proxyAutoconfigUrl (string)</dt>
     *   <dd>(Required if <em>proxyType</em> is "pac", ignored otherwise) Specifies the URL to be
     * used for proxy auto configuration.  Expected format example:
     * <code>http://hostname.com:1234/pacfile</code></dd>
     *
     *   <dt>ftpProxy, httpProxy, sslProxy (string)</dt>
     *   <dd>(Optional, ignored if proxyType is not "manual") Specifies the proxies to be used for
     * FTP, HTTP and HTTPS requests respectively.  Behaviour is undefined if a request is made,
     * where the proxy for the particular protocol is undefined, if <em>proxyType</em> is manual.
     * Expected format example: <code>hostname.com:1234</code></dd>
     * </dl></pre>
     */
    @SuppressWarnings("unchecked")
    PROXY("proxy") {
      Proxy getDefaultValue() {
        return new Proxy();
      }

      Proxy sanitize(Object proxy) {
        if (proxy != null) {
          if (proxy instanceof Proxy) {
            return (Proxy) proxy;
          } else if (proxy instanceof Map) {
            return new Proxy((Map<String, ?>) proxy);
          }
        }

        return null;
      }
    },

    /**
     * (Class/String) Allows you to specify which mechanism to use for managing and controlling the
     * Opera binary process from OperaDriver.  The value must be set to the qualified classpath for
     * a class implementing the {@link OperaRunner} interface.
     *
     * OperaDriver ships with two implementations that can be used:
     *
     * <pre><dl>
     *   <dt><code>com.opera.core.systems.runner.launcher.OperaLauncherRunner</code> (default)</dt>
     *   <dd>Uses an external C++ wrapper to control the Opera binary.</dd>
     *
     *   <dt><code>com.opera.core.systems.runner.inprocess.OperaInProcessRunner</code></dt>
     *   <dd>Uses a Java wrapper implementation to control the Opera binary.</dd>
     * </dl></pre>
     */
    RUNNER("opera.runner") {
      Class getDefaultValue() {
        return OperaLauncherRunner.class;
      }

      Class sanitize(Object runner) {
        if (runner instanceof Class) {
          return (Class) runner;
        } else if (runner instanceof String) {
          try {
            return (Class) Class.forName(String.valueOf(runner));
          } catch (ClassNotFoundException e) {
            // caught below
          }
        }

        throw new WebDriverException("Unknown runner: " + runner);
      }
    },

    /**
     * (EmulationProfile/MobileDevices/String) Allows emulation of specific devices for (currently
     * only) Opera Mobile.  You can either provide your own profile configuration using the
     * following JSON object:
     *
     * <pre><code>
     *   {
     *     width: INTEGER,
     *     height: INTEGER,
     *     ppi: INTEGER,
     *     input: "native", "touch", "keypad", or "tablet"
     *     userAgent: "Android", "Desktop", "MeeGo", or "S60"
     *   }
     * </code></pre>
     *
     * Or use one of the predefined emulation profiles found in {@link MobileDevices}:
     *
     * <pre><code>
     *   capabilities.set("opera.emulationProfile", MobileDevices.SAMSUNG_GALAXY_NEXUS);
     * </code></pre>
     *
     * Or provide your own profile using the Java builder:
     *
     * <pre><code>
     *   capabilities.set("opera.emulationProfile", OperaMobileEmulation.builder()
     *     .setResolution(new Dimension(480, 800)
     *     .setPPI(123)
     *     .setUserAgent(UserAgent.ANDROID)
     *     .setIME(IME.TABLET)
     *     .build());
     * </code></pre>
     */
    EMULATION_PROFILE("opera.emulationProfile") {
      EmulationProfile sanitize(Object config) {
        if (config instanceof String) {
          return MobileDevices.find(String.valueOf(config)).getProfile();
        } else if (config instanceof EmulationProfile) {
          return (EmulationProfile) config;
        } else if (config instanceof MobileDevices) {
          return ((MobileDevices) config).getProfile();
        }

        return null;
      }
    },

    /**
     * (Boolean) Whether to restart Opera on {@link OperaDesktopDriver} reinitialization.  This
     * option only applies to the Desktop Driver.
     *
     * @deprecated
     */
    @Deprecated
    NO_RESTART() {
      Boolean getDefaultValue() {
        return false;
      }

      Boolean sanitize(Object enabled) {
        checkNotNull(enabled);
        return OperaBoolean.parseBoolean(String.valueOf(enabled));
      }
    },

    /**
     * (Boolean) Whether to quit Opera when OperaDriver is shut down.  If enabled, it will keep the
     * browser running after the driver is shut down.
     *
     * @deprecated replaced by {@link #DETACH}
     */
    @Deprecated
    NO_QUIT() {
      Boolean getDefaultValue() {
        return (Boolean) DETACH.getDefaultValue();
      }

      Boolean sanitize(Object enabled) {
        return (Boolean) DETACH.sanitize(enabled);
      }
    },

    /**
     * (Boolean) Whether to detach the Opera browser when the driver shuts down.  This will leave
     * Opera running.
     *
     * @since 0.12
     */
    DETACH() {
      Boolean getDefaultValue() {
        return false;
      }

      Boolean sanitize(Object enabled) {
        checkNotNull(enabled);
        return OperaBoolean.parseBoolean(String.valueOf(enabled));
      }
    },

    /**
     * (String) The product we are using, for example {@link OperaProduct#DESKTOP} or {@link
     * OperaProduct#CORE}.
     */
    PRODUCT() {
      OperaProduct getDefaultValue() {
        return DESKTOP;
      }

      OperaProduct sanitize(Object product) {
        if (product == null) {
          return null;
        } else if (product instanceof OperaProduct) {
          return (OperaProduct) product;
        }

        return OperaProduct.get(String.valueOf(product));
      }
    },

    /**
     * (String) Rendering backend used by internal builds of Opera.  If null or an empty string,
     * "software" is used by default.
     *
     * <strong>Warning:</strong> This is a temporary workaround for launcher not taking external
     * window-only screenshots on Windows.
     *
     * @deprecated
     */
    @Deprecated
    BACKEND() {
      String getDefaultValue() {
        return "software";
      }

      Object sanitize(Object backend) {
        return backend;
      }
    };

    private static final String CAPABILITY_PREFIX = "opera.";
    private static final ImmutableMap<String, Capability> lookup;

    private final String identifier;
    private final String capability;

    static {
      ImmutableMap.Builder<String, Capability> builder = ImmutableMap.builder();

      for (Capability capability : values()) {
        builder.put(capability.getCapability(), capability);
      }

      lookup = builder.build();
    }

    private Capability() {
      identifier = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
      capability = CAPABILITY_PREFIX + name().toLowerCase();
    }

    private Capability(String capabilityName) {
      identifier = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
      capability = capabilityName;
    }

    /**
     * Returns the identifier of the capability (used in {@link OperaSettings} without it's {@link
     * #CAPABILITY_PREFIX}.
     *
     * @return the unique identifier of a capability
     */
    public String getIdentifier() {
      return identifier;
    }

    /**
     * Gets the string representation of this capability for use in {@link DesiredCapabilities}.
     *
     * @return capability name for use with {@link DesiredCapabilities}
     */
    public String getCapability() {
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
     * Sanitizes value set through {@link DesiredCapabilities} so that they become compatible with
     * {@link OperaSettings} for use in {@link OperaDriver}.
     *
     * The input from {@link DesiredCapabilities} is always a string value.  The result of the
     * sanitization could potentially be any object.
     *
     * @param value value to sanitize
     * @return sanitized value
     */
    Object sanitize(Object value) {
      checkNotNull(value);
      return value;
    }

    /**
     * Gets the string representation of this capability.
     *
     * @return easy to read string representation
     */
    @Override
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

  private final Map<Capability, CapabilityInstance> options = Maps.newHashMap();
  private final DesiredCapabilities surplusCapabilities = new DesiredCapabilities();
  private final OperaLogging logging = new OperaLogging();

  private boolean supportsPd = true;

  /**
   * Constructs a new set of settings for {@link OperaDriver} and Opera to use.  The default
   * settings are populated from {@link Capability}.
   */
  public OperaSettings() {
    for (Capability capability : Capability.values()) {
      options.put(capability, new CapabilityInstance(capability));
    }

    initializeLogging();
  }

  /**
   * Constructs a new set of settings for {@link OperaDriver} and Opera to use based on the given
   * profile.
   *
   * @param profile the profile to use
   */
  public OperaSettings(OperaProfile profile) {
    this();
    setProfile(profile);
  }

  /**
   * Returns the {@link OperaLogging} interface for managing logging levels and logging to file for
   * {@link OperaDriver}.
   *
   * @return relevant methods for getting and setting logging settings
   */
  public OperaLogging logging() {
    return logging;
  }

  /**
   * Manages settings related to logging in {@link OperaDriver} and related processes, such as
   * {@link com.opera.core.systems.runner.OperaRunner} and Opera itself.
   */
  public class OperaLogging {

    /**
     * Returns the logging level used in OperaDriver.
     *
     * Note that the individual logging levels for sub-processes used within OperaDriver, such as
     * {@link com.opera.core.systems.runner.OperaRunner} and for Opera itself, might
     * differ due to an unspecified heuristic of what the general desired level of chattiness the
     * user wants based on the value set here.
     *
     * @return the logging level used
     */
    public Level getLevel() {
      return (Level) options.get(LOGGING_LEVEL).getValue();
    }

    /**
     * Sets the logging level used in OperaDriver.  For this to have any effect, it must be set
     * before it is passed in to OperaDriver's constructor.
     *
     * Note that the individual logging levels for sub-processes used within OperaDriver, such as
     * {@link com.opera.core.systems.runner.OperaRunner} and for Opera itself, might
     * differ due to an unspecified heuristic of what the general desired level of chattiness the
     * user wants based on the value set here.
     *
     * @param level the logging level to use
     */
    public void setLevel(Level level) {
      checkNotNull(level);
      options.get(LOGGING_LEVEL).setValue(level);

      Logger root = Logger.getLogger(OperaDriver.class.getPackage().getName());

      // We only need one console handler
      if (!Iterators.any(Arrays.asList(root.getHandlers()).iterator(), new Predicate<Handler>() {
        public boolean apply(Handler handler) {
          return handler instanceof ConsoleHandler;
        }
      })) {
        root.addHandler(new ConsoleHandler());
      }

      // We only need one WebDriver logs handler
      if (!Iterators.any(Arrays.asList(root.getHandlers()).iterator(), new Predicate<Handler>() {
        public boolean apply(Handler handler) {
          return handler instanceof DriverLogsHandler;
        }
      })) {
        root.addHandler(new DriverLogsHandler(OperaDriver.logs));
      }

      root.setLevel(level);
      root.setUseParentHandlers(false);

      // Set logging levels on all handlers
      for (Handler handler : root.getHandlers()) {
        if (handler instanceof DriverLogsHandler) {
          handler.setLevel(Level.ALL);
        } else {
          handler.setLevel(level);
        }
      }
    }

    /**
     * Returns the logging file, if any, used by Opera.  If no logging file has been specified, it
     * will return null.
     *
     * @return logging file used, or null
     */
    public File getFile() {
      return (File) options.get(LOGGING_FILE).getValue();
    }

    /**
     * Specifies the logging file to use for the OperaDriver loggers.  If no logging file is
     * specified (null), logging to file is disabled.  By default, the logging file will be
     * appended.
     *
     * @param file logging file to use
     */
    public void setFile(File file) {
      options.get(LOGGING_FILE).setValue(file);

      if (file != null) {
        FileHandler logFile;

        try {
          logFile = new FileHandler(file.getPath(), OperaDefaults.APPEND_TO_LOGFILE);
          logFile.setFormatter(new SimpleFormatter());
        } catch (IOException e) {
          throw new WebDriverException("Unable to write to log file: " + e.getMessage(), e);
        }

        logFile.setLevel(getLevel());
        Logger.getLogger(OperaDriver.class.getPackage().getName()).addHandler(logFile);

        OperaDriver.logFile = logFile;
      }
    }
  }

  /**
   * Returns the browser binary executable.  If binary is set to null, it will attempt to find a
   * suitable Opera binary on your system based on {@link #getProduct()} (which defaults to {@link
   * OperaProduct#DESKTOP}.
   *
   * @return the Opera binary
   */
  public File getBinary() {
    return (File) options.get(BINARY).getValue();
  }

  /**
   * Sets Opera's binary executable location.  If set to null, it will attempt to find a suitable
   * Opera binary on your system based on {@link #getProduct()} (which defaults to {@link
   * OperaProduct#DESKTOP}.
   *
   * @param binary the Opera binary
   */
  public void setBinary(File binary) {
    options.get(BINARY).setValue(BINARY.sanitize(binary));
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
    return (Integer) options.get(PORT).getValue();
  }

  /**
   * Specifies the port the debugger should connect to.
   *
   * @param port local port number for the debugger to connect to
   */
  public void setPort(int port) {
    options.get(PORT).setValue(PORT.sanitize(String.valueOf(port)));
  }

  /**
   * Gets the launcher to use with {@link OperaLauncherRunner}, if any.  If {@link #autostart()} is
   * false the launcher will not be used.  If not set, this method will return the default location
   * of the launcher on your system.
   *
   * @return location of launcher
   */
  public File getLauncher() {
    if (System.getenv(LAUNCHER_ENV_VAR) != null && !System.getenv(LAUNCHER_ENV_VAR).isEmpty()) {
      options.get(LAUNCHER).setValue(new File(System.getenv(LAUNCHER_ENV_VAR)));
    }

    return (File) options.get(LAUNCHER).getValue();
  }

  /**
   * Specifies launcher to use.  If {@link #autostart()} is false, it will not be used.
   *
   * @param launcherBinary the launcher binary
   * @throws IOException if there is a problem with the provided launcher
   */
  public void setLauncher(File launcherBinary) throws IOException {
    OperaLauncherRunner.assertLauncherGood(launcherBinary);
    options.get(LAUNCHER).setValue(launcherBinary);
  }

  /**
   * Returns an {@link OperaProfile} object containing all settings related to the Opera profile to
   * be used.  This  might be anything from the path to the profile directory, to preferences used
   * in Opera.
   *
   * @return the Opera profile
   */
  public OperaProfile profile() {
    return (OperaProfile) options.get(PROFILE).getValue();
  }

  /**
   * Sets the directory to use for the Opera profile.  If string is not empty use the given
   * directory.  If string is empty, the <code>-debugproxy</code> command-line argument will be
   * disabled for backwards compatibility reasons with older products, such as Opera < 11.60 and
   * core-mini.
   *
   * @param profileDirectory the path to the profile directory
   * @throws NullPointerException if argument is null
   */
  public void setProfile(String profileDirectory) {
    options.get(PROFILE).setValue(PROFILE.sanitize(profileDirectory));
    supportsPd = !(profileDirectory != null && profileDirectory.isEmpty());
  }

  /**
   * Sets the profile to use as an Opera profile represented as an object.
   *
   * @param profile the Opera profile to use
   * @throws NullPointerException if argument is null
   */
  public void setProfile(OperaProfile profile) {
    options.get(PROFILE).setValue(PROFILE.sanitize(profile));
    supportsPd = true;
  }

  /**
   * Whether to use Opera's alternative implicit wait implementation.  It will use an in-browser
   * heuristic to guess when a page has finished loading, allowing us with great accuracy tell
   * whether there are any planned events in the document.  This functionality is useful for very
   * simple test cases, but not designed for real-world testing.
   *
   * @return whether or not idle is in use
   */
  public boolean useIdle() {
    return (Boolean) options.get(OPERAIDLE).getValue();
  }

  /**
   * Whether to use Opera's alternative implicit wait implementation.  It will use an in-browser
   * heuristic to guess when a page has finished loading, allowing us with great accuracy tell
   * whether there are any planned events in the document.  This functionality is useful for very
   * simple test cases, but not designed for real-world testing.
   *
   * Idle can be switch on or off during runtime.
   *
   * @param enabled to enable or disable idle
   */
  public void setIdle(boolean enabled) {
    options.get(OPERAIDLE).setValue(enabled);
  }

  /**
   * Gets the X display used.  Only works on GNU/Linux.
   *
   * @return the X display
   */
  public Integer getDisplay() {
    return (Integer) options.get(DISPLAY).getValue();
  }

  /**
   * Sets the X display to use.  Only works on GNU/Linux.  Typically this allows you to open Opera
   * in an X virtual framebuffer.
   *
   * @param display the X display to use
   * @throws UnsupportedOperationException if on a non-GNU/Linux operating system
   */
  public void setDisplay(int display) {
    if (!Platform.getCurrent().is(LINUX)) {
      throw new UnsupportedOperationException("Unsupported platform: " + Platform.getCurrent());
    }

    options.get(DISPLAY).setValue(display);
  }

  /**
   * Whether or not Opera should be launched on initialization.  If disabled, {@link OperaDriver}
   * will wait and listen for an incoming Scope client connection.
   *
   * @return true if Opera is to be started automatically, false otherwise
   */
  public boolean autostart() {
    return (Boolean) options.get(AUTOSTART).getValue();
  }

  /**
   * Enable or disable whether Opera should be launched on initialization.  If disabled, {@link
   * OperaDriver} will wait and listen for incoming an incoming Scope client connection.  This
   * cannot be set during runtime.  This is true by default.
   *
   * @param enabled true if Opera should be started automatically, false otherwise
   */
  public void autostart(boolean enabled) {
    options.get(AUTOSTART).setValue(enabled);

    if (!enabled) {
      setPort(SERVER_DEFAULT_PORT_IDENTIFIER);
    }
  }

  /**
   * Returns current proxy settings
   *
   * @return current proxy settings
   */
  public Proxy getProxy() {
    return (Proxy) options.get(PROXY).getValue();
  }

  /**
   * Sets proxy settings
   *
   * @param proxy proxy settings
   */
  public void setProxy(Proxy proxy) {
    options.get(PROXY).setValue(proxy);
  }

  /**
   * Returns the product currently used, for example {@link OperaProduct#DESKTOP} or {@link
   * OperaProduct#CORE}.
   *
   * @return the profile configuration set
   */
  public OperaProduct getProduct() {
    return (OperaProduct) options.get(PRODUCT).getValue();
  }

  /**
   * Sets the product currently used, for example desktop or core.
   *
   * @param product the profile configuration to use
   */
  public void setProduct(OperaProduct product) {
    options.get(PRODUCT).setValue(product);

    // Switch context of arguments
    OperaArguments arguments;

    if (getProduct().is(OperaProduct.DESKTOP)) {
      arguments = new OperaDesktopArguments();
    } else {
      arguments = new OperaCoreArguments();
    }

    options.get(ARGUMENTS).setValue(arguments.merge(arguments()));
  }

  /**
   * Get the runner to use for starting and managing the Opera instance.
   *
   * @return the current runner
   */
  public OperaRunner getRunner() {
    Class klass = (Class) options.get(RUNNER).getValue();

    // If no runner is set, use the default one
    if (klass == null) {
      setRunner(OperaLauncherRunner.class);
      return getRunner();
    }

    Constructor constructor;
    try {
      constructor = klass.getDeclaredConstructor(OperaSettings.class);
    } catch (NoSuchMethodException e) {
      throw new WebDriverException("Invalid constructor in runner: " + klass.getName());
    }

    OperaRunner runner;
    try {
      runner = (OperaRunner) constructor.newInstance(this);
    } catch (InstantiationException e) {
      throw new WebDriverException("Unable to create new instance of runner", e);
    } catch (IllegalAccessException e) {
      throw new WebDriverException("Denied access to runner: " + klass.getName());
    } catch (InvocationTargetException e) {
      throw new WebDriverException("Runner threw exception on construction", e);
    }

    return runner;
  }

  /**
   * Specify which runner to use for starting and managing the Opera instance.
   *
   * @param runner the runner to use
   */
  public void setRunner(Class<? extends OperaRunner> runner) {
    options.get(RUNNER).setValue(runner);
  }

  /**
   * Whether to restart or not.  Only used in {@link OperaDesktopDriver}.  It is false by default.
   *
   * @return true if Opera should restart, false otherwise
   * @deprecated
   */
  @Deprecated
  public boolean noRestart() {
    return (Boolean) options.get(NO_RESTART).getValue();
  }

  /**
   * Whether to restart or not.  Only affects {@link OperaDesktopDriver}.
   *
   * @param enabled true if Opera should be restart, false otherwise
   * @deprecated
   */
  @Deprecated
  public void noRestart(boolean enabled) {
    options.get(NO_RESTART).setValue(enabled);
  }

  /**
   * Whether to detach the Opera browser when the driver shuts down.  If true, this will leave Opera
   * running.
   *
   * @return true if Opera should keep running, false if Opera should be shut down
   */
  public boolean hasDetach() {
    return (Boolean) options.get(DETACH).getValue();
  }

  /**
   * Specifies whether to detach the Opera browser when the driver shuts down.  If true, this will
   * leave Opera running.
   *
   * @param enabled true will leave Opera running, false will shut down Opera along with driver
   */
  public void setDetach(boolean enabled) {
    options.get(DETACH).setValue(enabled);
  }

  /**
   * Gets the mobile configuration specified in this settings object.
   *
   * @return mobile configuration
   */
  public EmulationProfile getEmulationProfile() {
    return (EmulationProfile) options.get(EMULATION_PROFILE).getValue();
  }

  /**
   * Shorthand for {@link #emulate(EmulationProfile)}.  The {@link EmulationProfile} of the given
   * device will be extracted.
   *
   * @param device the device to emulate
   */
  public void emulate(MobileDevices device) {
    emulate(device.getProfile());
  }

  /**
   * Specify a prepared configuration for Opera Mobile to use.  For this configuration to be
   * recognized, {@link #getProduct()} must evaluate to {@link OperaProduct#MOBILE}.
   *
   * @param profile the mobile configuration to use
   */
  public void emulate(EmulationProfile profile) {
    options.get(EMULATION_PROFILE).setValue(profile);

    if (profile != null) {
      Dimension resolution = profile.getResolution();
      arguments().add("-windowsize",
                      String.format("%dx%d", resolution.getWidth(), resolution.getHeight()));
      arguments().add(new OperaArgument("-ppi", profile.getPPI()));
      if (profile.getIME() == EmulationProfile.IME.KEYPAD) {
        arguments().add("-notouchwithtouchevents");
      } else if (profile.getIME() == EmulationProfile.IME.TABLET) {
        arguments().add(new OperaArgument("-tabletui"));
      }
      arguments().add("-user-agent-string", profile.getUserAgent());
      arguments().add(new OperaArgument("-profile-name", profile.getProfileName()));
    }
  }

  /**
   * Gets rendering backend used.  If null or an empty string, "software" is used by default. This
   * will only be used if using {@link OperaLauncherRunner} (i.e. if {@link #autostart()} and {@link
   * #getLauncher()} are specified).
   *
   * <strong>Warning:</strong> This is a temporary workaround for launcher not taking external
   * window-only screenshots on Windows.
   *
   * @return the backend to use for gogi
   * @deprecated
   */
  @Deprecated
  public String getBackend() {
    return (String) options.get(BACKEND).getValue();
  }

  /**
   * Specifies the rendering backend used by internal gogi builds of Opera.  If null or an empty
   * string, "software" is used by default.  This will only be used if using {@link
   * OperaLauncherRunner} (i.e. if {@link #autostart()} and {@link #getLauncher()} are specified).
   *
   * <strong>Warning:</strong> This is a temporary workaround for launcher not taking external
   * window-only screenshots on Windows.
   *
   * @param backend the backend to use for gogi, either "software" or "hardware"
   * @deprecated
   */
  @Deprecated
  public void setBackend(String backend) {
    options.get(BACKEND).setValue(backend);
  }

  /**
   * Whether or not the currently specified Opera configuration supports the
   * <code>-debugproxy</code> command-line argument.  If the specified port is not equal to the
   * default proxy server port specified in {@link OperaDefaults#SERVER_DEFAULT_PORT}, this will be
   * true.
   *
   * @return true if this configuration supports the <code>-debugproxy</code> command-line argument,
   *         false otherwise
   */
  public boolean supportsDebugProxy() {
    return getPort() != SERVER_DEFAULT_PORT;
  }

  /**
   * Whether or not the currently specified Opera configuration supports the <code>-pd</code>
   * command-line argument.  <em>PD</em> stands for <em>personal directory</em>, and refers to a
   * profile for Opera.
   *
   * @return true if this configuration supports the <code>-pd</code> command-line argument.
   */
  public boolean supportsPd() {
    return !getProduct().is(CORE) && supportsPd;
  }

  /**
   * Merge a set of capabilities in to these settings.  The settings corresponding to the
   * <code>opera.</code> capability prefix will be updated, while surplus/unknown capabilities will
   * be stored.  They can later be retrieved using the {@link #toCapabilities()} method on this
   * object.
   *
   * @param capabilities capabilities to merge into these settings
   * @return reference to self
   */
  public OperaSettings merge(Capabilities capabilities) {
    for (Map.Entry<String, ?> capability : capabilities.asMap().entrySet()) {
      Capability capabilityReference = Capability.findCapability(capability.getKey());

      if (options.containsKey(capabilityReference)) {
        options.get(capabilityReference)
            .setValue(capabilityReference.sanitize(capability.getValue()));
        continue;
      }

      // Store the rest as surplus capabilities
      surplusCapabilities.setCapability(capability.getKey(), capability.getValue());
    }

    initializeLogging();
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
      if (option.getValue() == null) {
        continue;
      }

      capabilities.setCapability(option.getCapability(), option.getValue());
    }

    return capabilities.merge(surplusCapabilities);
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
      Object value = option.getValue();

      if (value == null) {
        continue;
      }

      if (value instanceof OperaProfile) {
        value = ((OperaProfile) value).toJson();
      } else if (value instanceof OperaArguments && ((OperaArguments) value).size() > 0) {
        value = ((OperaArguments) value).toJson();
      } else if (value instanceof Boolean) {
        value = (Boolean) value ? 1 : 0;
      } else {
        value = String.valueOf(value);
      }

      json.put(option.getIdentifier(), value);
    }

    return json;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof OperaSettings)) {
      return false;
    }

    OperaSettings compareTo = (OperaSettings) o;

    for (Map.Entry<Capability, CapabilityInstance> option : options.entrySet()) {
      if (!compareTo.options.get(option.getKey()).equals(option.getValue())) {
        return false;
      }
    }

    for (Map.Entry<String, ?> capability : surplusCapabilities.asMap().entrySet()) {
      if (!compareTo.surplusCapabilities.getCapability(capability.getKey())
          .equals(capability.getValue())) {
        return false;
      }
    }

    return true;
  }

  /**
   * String representation of the Opera specific settings.
   *
   * @return string representation of this
   */
  @Override
  public String toString() {
    return String.format("OperaSettings %s", options.values());
  }

  private void initializeLogging() {
    logging().setLevel(logging().getLevel());
    logging().setFile(logging().getFile());
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

    public Builder product(OperaProduct product) {
      settings.setProduct(product);
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