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

import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;
import org.openqa.selenium.WebDriverException;

import static com.opera.core.systems.OperaProduct.MOBILE;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

public class OperaProxyTest extends OperaDriverTestCase {

  public static final String host = "4.4.4.4";
  public static final String hostWithPort = "4.4.4.4:1234";
  public static final String nullHost = null;
  public static final String username = "foo";
  public static final String password = "bar";
  public static final String url = "http://localhost/foo.pac";
  public static final String localUrl = "file://localhost/foo.pac";

  public OperaProxy proxy;

  @Before
  public void assignProxy() {
    proxy = driver.proxy();
  }

  @After
  public void resetProxy() {
    if (driver != null && driver.isRunning()) {
      proxy.reset();
    }
  }

  @Test
  public void httpProxy() {
    proxy.setHttpProxy(host);
    assertEquals(host, proxy.getHttpProxy());
    assertTrue(proxy.isEnabled());
    assertFalse(proxy.isUsePAC());
  }

  @Test
  public void httpProxyDefault() {
    assertNull(proxy.getHttpProxy());
  }

  @Test
  public void httpsProxy() {
    proxy.setHttpsProxy(host);
    assertEquals(host, proxy.getHttpsProxy());
    assertTrue(proxy.isEnabled());
    assertFalse(proxy.isUsePAC());
  }

  @Test
  public void httpsProxyDefault() {
    assertNull(proxy.getHttpsProxy());
  }

  @Test
  public void ftpProxy() {
    proxy.setFtpProxy(host);
    assertEquals(host, proxy.getFtpProxy());
    assertTrue(proxy.isEnabled());
    assertFalse(proxy.isUsePAC());
  }

  @Test
  public void ftpProxyDefault() {
    assertNull(proxy.getFtpProxy());
  }

  @Test
  public void socksProxy() {
    if (currentProduct().is(MOBILE)) {
      try {
        proxy.setSocksProxy(host);
        fail("Expected UnsupportedOperationException");
      } catch (RuntimeException e) {
        assertThat(e, is(instanceOf(UnsupportedOperationException.class)));
        assertThat(e.getMessage(), containsString("Proxy setting not supported by product"));
      }
    } else {
      proxy.setSocksProxy(host);
      assertEquals(host, proxy.getSocksProxy());
      assertTrue(proxy.isEnabled());
      assertFalse(proxy.isUsePAC());
      assertNull(proxy.getSocksUsername());
      assertNull(proxy.getSocksPassword());
    }
  }

  @Test
  public void socksProxyDefault() {
    if (currentProduct().is(MOBILE)) {
      try {
        proxy.getSocksProxy();
        fail("Expected UnsupportedOperationException");
      } catch (RuntimeException e) {
        assertThat(e, is(instanceOf(UnsupportedOperationException.class)));
        assertThat(e.getMessage(), containsString("Proxy setting not supported by product"));
      }
    } else {
      assertNull(proxy.getSocksProxy());
    }
  }

  @Test
  public void socksUsernameDefault() {
    if (currentProduct().is(MOBILE)) {
      try {
        proxy.getSocksUsername();
        fail("Expected UnsupportedOperationException");
      } catch (RuntimeException e) {
        assertThat(e, is(instanceOf(UnsupportedOperationException.class)));
        assertThat(e.getMessage(), containsString("Proxy setting not supported by product"));
      }
    } else {
      assertNull(proxy.getSocksUsername());
    }
  }

  @Test
  public void socksPasswordDefault() {
    if (currentProduct().is(MOBILE)) {
      try {
        proxy.getSocksPassword();
        fail("Expected UnsupportedOperationException");
      } catch (RuntimeException e) {
        assertThat(e, is(instanceOf(UnsupportedOperationException.class)));
        assertThat(e.getMessage(), containsString("Proxy setting not supported by product"));
      }
    } else {
      assertNull(proxy.getSocksPassword());
    }
  }

  @Test
  @Ignore(products = MOBILE)
  public void socksProxyWithUsername() {
    proxy.setSocksProxy(host);
    proxy.setSocksUsername(username);
    assertEquals(host, proxy.getSocksProxy());
    assertEquals(username, proxy.getSocksUsername());
    assertTrue(proxy.isEnabled());
    assertFalse(proxy.isUsePAC());
    assertNull(proxy.getSocksPassword());
  }

  @Test
  @Ignore(products = MOBILE)
  public void socksProxyWithPassword() {
    proxy.setSocksProxy(host);
    proxy.setSocksPassword(password);
    assertEquals(host, proxy.getSocksProxy());
    assertEquals(password, proxy.getSocksPassword());
    assertTrue(proxy.isEnabled());
    assertFalse(proxy.isUsePAC());
    assertNull(proxy.getSocksUsername());
  }

  @Test
  @Ignore(products = MOBILE)
  public void socksProxyWithUsernameAndPassword() {
    proxy.setSocksProxy(host);
    proxy.setSocksUsername(username);
    proxy.setSocksPassword(password);
    assertEquals(host, proxy.getSocksProxy());
    assertEquals(username, proxy.getSocksUsername());
    assertEquals(password, proxy.getSocksPassword());
    assertTrue(proxy.isEnabled());
    assertFalse(proxy.isUsePAC());
  }

  @Test
  public void hostWithPort() {
    proxy.setHttpProxy(hostWithPort);
    assertEquals(hostWithPort, proxy.getHttpProxy());
  }

  @Test
  public void nullHost() {
    proxy.setHttpProxy(nullHost);
    assertNull(proxy.getHttpProxy());
  }

  @Test
  public void proxyLocalServersDefault() {
    if (currentProduct().is(MOBILE)) {
      assertTrue(proxy.isProxyLocal());
    } else {
      assertFalse(proxy.isProxyLocal());
    }
  }

  @Test
  public void proxyLocalServers() {
    if (currentProduct().is(MOBILE)) {
      proxy.setProxyLocal(false);
      assertFalse(proxy.isProxyLocal());
    } else {
      proxy.setProxyLocal(true);
      assertTrue(proxy.isProxyLocal());
    }
  }

  @Test
  public void autoconfigUrl() {
    proxy.setAutoconfigUrl(url);
    assertEquals(url, proxy.getAutoconfigUrl());
    assertFalse(proxy.isUsePAC());
    assertTrue(proxy.isEnabled());
  }

  @Test
  public void autoconfigLocalUrl() {
    proxy.setAutoconfigUrl(localUrl);
    assertEquals(localUrl, proxy.getAutoconfigUrl());
    assertFalse(proxy.isUsePAC());
    assertTrue(proxy.isEnabled());
  }

  @Test
  public void proxyDisabledByDefault() {
    assertTrue(proxy.isEnabled());
  }

  @Test
  public void proxyEnable() {
    if (currentProduct().is(MOBILE)) {
      try {
        proxy.setEnabled(false);
        fail("Expected UnsupportedOperationException");
      } catch (RuntimeException e) {
        assertThat(e, is(instanceOf(UnsupportedOperationException.class)));
        assertThat(e.getMessage(), containsString("Proxy setting not supported by product"));
      }
    } else {
      proxy.setEnabled(false);
      assertFalse(proxy.isEnabled());
    }
  }

  @Test
  public void parseManualProxies() {
    Proxy p = new Proxy()
        .setHttpProxy(host)
        .setFtpProxy(host)
        .setAutodetect(false);

    proxy.parse(p);

    assertEquals(host, proxy.getHttpProxy());
    assertEquals(host, proxy.getFtpProxy());
    assertFalse(proxy.isUsePAC());
    assertTrue(proxy.isEnabled());
  }

  @Test
  public void parseAutomaticProxies() {
    Proxy p = new Proxy().setProxyAutoconfigUrl(url);
    proxy.parse(p);

    assertTrue(proxy.isUsePAC());
    assertTrue(proxy.isEnabled());
  }

  @Test
  @Ignore(products = MOBILE)
  public void parseDirectProxyConnection() {
    Proxy p = new Proxy().setProxyType(ProxyType.DIRECT);
    proxy.parse(p);

    assertFalse(proxy.isEnabled());
    assertFalse(proxy.isUsePAC());
  }

  @Test
  public void cannotResetWhenDisconnected() {
    driver.quit();

    try {
      driver.proxy().reset();
      fail("Expected WebDriverException");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(WebDriverException.class)));
      assertThat(e.getMessage(),
                 containsString("Unable to update proxy configuration; not connected!"));
    }
  }

  @Test
  public void cannotSetProxyValueWhenDisconnected() {
    driver.quit();

    try {
      driver.proxy().setHttpProxy(url);
      fail("Expected WebDriverException");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(WebDriverException.class)));
      assertThat(e.getMessage(),
                 containsString("Unable to update proxy configuration; not connected!"));
    }
  }

}