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

import com.opera.core.systems.preferences.OperaScopePreferences;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriverException;

import java.util.logging.Logger;

import static com.opera.core.systems.OperaProduct.MOBILE;
import static com.opera.core.systems.OperaProxy.ProxyToPreference.AUTOMATIC_PROXY_CONFIGURATION_URL;
import static com.opera.core.systems.OperaProxy.ProxyToPreference.ENABLE_PROXY;
import static com.opera.core.systems.OperaProxy.ProxyToPreference.FTP_SERVER;
import static com.opera.core.systems.OperaProxy.ProxyToPreference.HTTPS_SERVER;
import static com.opera.core.systems.OperaProxy.ProxyToPreference.HTTP_SERVER;
import static com.opera.core.systems.OperaProxy.ProxyToPreference.PROXY_LOCAL_SERVERS;
import static com.opera.core.systems.OperaProxy.ProxyToPreference.SOCKS_PASSWORD;
import static com.opera.core.systems.OperaProxy.ProxyToPreference.SOCKS_SERVER;
import static com.opera.core.systems.OperaProxy.ProxyToPreference.SOCKS_USERNAME;
import static com.opera.core.systems.OperaProxy.ProxyToPreference.USE_AUTOMATIC_PROXY_CONFIGURATION;
import static com.opera.core.systems.OperaProxy.ProxyToPreference.USE_FTP;
import static com.opera.core.systems.OperaProxy.ProxyToPreference.USE_HTTP;
import static com.opera.core.systems.OperaProxy.ProxyToPreference.USE_HTTPS;
import static com.opera.core.systems.OperaProxy.ProxyToPreference.USE_SOCKS;

/**
 * Representation of Opera's internal proxy configuration.  Don't use this interface directly, but
 * consider using Selenium's {@link Proxy} instead.
 */
public class OperaProxy {

  protected enum ProxyToPreference {
    ENABLE_PROXY("Enable Proxy"),
    HTTP_SERVER("HTTP server"),
    USE_HTTP("Use HTTP"),
    HTTPS_SERVER("HTTPS server"),
    USE_HTTPS("Use HTTPS"),
    FTP_SERVER("FTP server"),
    USE_FTP("Use FTP"),
    SOCKS_SERVER("SOCKS server"),
    SOCKS_USERNAME("SOCKS username"),
    SOCKS_PASSWORD("SOCKS password"),
    USE_SOCKS("Use SOCKS"),
    PROXY_LOCAL_SERVERS("Use Proxy On Local Names Check"),
    AUTOMATIC_PROXY_CONFIGURATION_URL("Automatic Proxy Configuration URL"),
    USE_AUTOMATIC_PROXY_CONFIGURATION("Use Automatic Proxy Configuration");

    private final String key;

    ProxyToPreference(String preferenceName) {
      key = preferenceName;
    }

    public String getPreferenceKey() {
      return key;
    }
  }

  private final static String PROXY_SECTION = "Proxy";

  private final Logger logger = Logger.getLogger(getClass().getName());
  private final OperaDriver driver;
  private final OperaProduct product;
  private final OperaScopePreferences preferences;

  public OperaProxy(OperaDriver parent) {
    driver = parent;
    product = driver.utils().getProduct();
    preferences = driver.preferences();
  }

  /**
   * Gets the HTTP proxy.
   *
   * @return the HTTP proxy hostname if present, or null if not set
   */
  public String getHttpProxy() {
    return (String) getProxyValue(HTTP_SERVER);
  }

  /**
   * Specify which proxy to use for HTTP connections.
   *
   * @param host the proxy host, expected format is <code>hostname:1234</code>
   */
  public void setHttpProxy(String host) {
    setProxyValue(HTTP_SERVER, host);
    setProxyValue(USE_HTTP, host != null);
  }

  /**
   * Gets the HTTPS proxy.
   *
   * @return the HTTPS proxy hostname if present, or null if not set
   */
  public String getHttpsProxy() {
    return (String) getProxyValue(HTTPS_SERVER);
  }

  /**
   * Specify which proxy to use for HTTPS connections.
   *
   * @param host the proxy host, expected format is <code>hostname:1234</code>
   */
  public void setHttpsProxy(String host) {
    setProxyValue(HTTPS_SERVER, host);
    setProxyValue(USE_HTTPS, host != null);
  }

  /**
   * Gets the FTP proxy.
   *
   * @return the FTP proxy hostname if present, or null if not set
   */
  public String getFtpProxy() {
    return (String) getProxyValue(FTP_SERVER);
  }

  /**
   * Specify which proxy to use for FTP connections.
   *
   * @param host the proxy host, expected format is <code>hostname:1234</code>
   */
  public void setFtpProxy(String host) {
    setProxyValue(FTP_SERVER, host);
    setProxyValue(USE_FTP, host != null);
  }

  /**
   * Gets the SOCKS proxy.
   *
   * @return the SOCKS proxy if present, null otherwise
   */
  public String getSocksProxy() {
    assertNotMobile();
    return (String) getProxyValue(SOCKS_SERVER);
  }

  /**
   * Specifies which proxy to use for SOCKS.  Currently only supported in {@link
   * com.opera.core.systems.OperaDriver}.
   *
   * @param host the proxy host, expected format is <code>hostname.com:1234</code>
   */
  public void setSocksProxy(String host) {
    assertNotMobile();
    setProxyValue(SOCKS_SERVER, host);
    setProxyValue(USE_SOCKS, host != null);
  }

  /**
   * Gets the SOCKS proxy's username.  Supported by SOCKS v5 and above.
   *
   * @return the SOCKS proxy's username
   */
  public String getSocksUsername() {
    assertNotMobile();
    return (String) getProxyValue(SOCKS_USERNAME);
  }

  /**
   * Specifies a username for the SOCKS proxy.  Supported by SOCKS v5 and above.
   *
   * @param username username for the SOCKS proxy
   */
  public void setSocksUsername(String username) {
    assertNotMobile();
    setProxyValue(SOCKS_USERNAME, username);
  }

  /**
   * Gets the SOCKS proxy's password.  Supported by SOCKS v5 and above.
   *
   * @return the SOCKS proxy's password
   */
  public String getSocksPassword() {
    assertNotMobile();
    return (String) getProxyValue(SOCKS_PASSWORD);
  }

  /**
   * Specifies a password for the SOCKS proxy.  Supported by SOCKS v5 and above.
   *
   * @param password password for the SOCKS proxy
   */
  public void setSocksPassword(String password) {
    assertNotMobile();
    setProxyValue(SOCKS_PASSWORD, password);
  }

  /**
   * Whether to proxy local resources.  Is disabled by default.
   *
   * @return true if to proxy local resources, false otherwise
   */
  public boolean isProxyLocal() {
    return (Boolean) getProxyValue(PROXY_LOCAL_SERVERS);
  }

  /**
   * Enable or disable proxy of local resources.  By default it is disabled.
   *
   * @param proxyLocalServers true to enable of proxy local resources, false to disable
   */
  public void setProxyLocal(boolean proxyLocalServers) {
    setProxyValue(PROXY_LOCAL_SERVERS, proxyLocalServers);
  }

  /**
   * Get the URL of the automatic proxy configuration file.
   *
   * @return URL to proxy configuration file
   */
  public String getAutoconfigUrl() {
    return (String) getProxyValue(AUTOMATIC_PROXY_CONFIGURATION_URL);
  }

  /**
   * Specify the automatic proxy configuration file as a URL.
   *
   * @param url url to the automatic proxy configuration file
   */
  public void setAutoconfigUrl(String url) {
    setProxyValue(AUTOMATIC_PROXY_CONFIGURATION_URL, url);
  }

  /**
   * Whether this proxy uses PAC, short for Proxy Auto Configuration.
   *
   * @return true if PAC is in use, false otherwise
   */
  public boolean isUsePAC() {
    return (Boolean) getProxyValue(USE_AUTOMATIC_PROXY_CONFIGURATION);
  }

  /**
   * Enable or disable PAC, short for Proxy Auto Configuration.
   *
   * @param enabled set to true to enable, false to disable
   */
  public void setUsePAC(boolean enabled) {
    setProxyValue(USE_AUTOMATIC_PROXY_CONFIGURATION, enabled);
  }

  /**
   * Whether proxy is enabled.
   *
   * @return true if proxy is enabled, false otherwise
   */
  public boolean isEnabled() {
    if (product.is(MOBILE)) {
      return true;
    }
    return (Boolean) getProxyValue(ENABLE_PROXY);
  }

  /**
   * Enable or disable proxy.
   *
   * @param enabled set to true to enable, false to disable
   */
  public void setEnabled(boolean enabled) {
    assertNotMobile();
    setProxyValue(ENABLE_PROXY, enabled);
  }

  /**
   * Parse an instance of {@link Proxy}, merge and apply its configuration to the current Opera
   * instance.
   */
  public void parse(Proxy proxy) {
    if (proxy.getProxyType() == Proxy.ProxyType.UNSPECIFIED) {
      return;
    }

    reset();

    switch (proxy.getProxyType()) {
      case DIRECT:
        if (!product.is(MOBILE)) {
          setEnabled(false);
        }
        setUsePAC(false);
        break;

      case MANUAL:
        if (!product.is(MOBILE)) {
          setEnabled(true);
        }
        setUsePAC(false);

        // TODO(andreastt): HTTPS proxy
        // TODO(andreastt): SOCKS proxy

        if (proxy.getHttpProxy() != null) {
          setHttpProxy(proxy.getHttpProxy());
        }
        if (proxy.getFtpProxy() != null) {
          setFtpProxy(proxy.getFtpProxy());
        }

        break;

      case PAC:
        if (!product.is(MOBILE)) {
          setEnabled(true);
        }
        setUsePAC(true);

        if (proxy.getProxyAutoconfigUrl() != null) {
          setAutoconfigUrl(proxy.getProxyAutoconfigUrl());
        }

        break;

      default:
        logger.warning("Unsupported proxy type: " + proxy.getProxyType());
    }
  }

  protected void reset() {
    assertIsConnected();

    for (ProxyToPreference preference : ProxyToPreference.values()) {
      OperaScopePreferences.ScopePreference p = preferences.get(PROXY_SECTION,
                                                                preference.getPreferenceKey());
      if (p != null) {
        p.reset();
      }
    }
  }

  private Object getProxyValue(ProxyToPreference key) {
    Object value = preferences.get(PROXY_SECTION, key.getPreferenceKey()).getValue();
    if (value instanceof String && ((String) value).isEmpty()) {
      return null;
    }
    return value;
  }

  private void setProxyValue(ProxyToPreference key, Object value) {
    assertIsConnected();
    if (value == null) {
      value = "";
    }
    preferences.set(PROXY_SECTION, key.getPreferenceKey(), value);
  }

  private void assertIsConnected() {
    if (!driver.getScopeServices().isConnected()) {
      throw new WebDriverException("Unable to update proxy configuration; not connected!");
    }
  }

  private void assertNotMobile() {
    if (product.is(MOBILE)) {
      throw new UnsupportedOperationException(String.format(
          "Proxy setting not supported by product %s", product));
    }
  }

}