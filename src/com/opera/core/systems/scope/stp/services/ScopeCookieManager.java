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

package com.opera.core.systems.scope.stp.services;

import com.google.common.collect.Sets;

import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.ScopeServices;
import com.opera.core.systems.scope.protos.CookieMngProtos;
import com.opera.core.systems.scope.protos.CookieMngProtos.CookieList;
import com.opera.core.systems.scope.protos.CookieMngProtos.CookieSettings;
import com.opera.core.systems.scope.protos.CookieMngProtos.GetCookieArg;
import com.opera.core.systems.scope.protos.CookieMngProtos.RemoveCookieArg;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.CookieManager;
import com.opera.core.systems.scope.stp.services.messages.CookieManagerMessage;

import org.openqa.selenium.Cookie;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Cookie manager to manage cookies via Scope.
 */
public class ScopeCookieManager extends AbstractService implements CookieManager {

  private int maxCookies;
  private int maxCookiesPerDomain;
  private int maxCookieLength;

  public ScopeCookieManager(ScopeServices services) {
    super(services, SERVICE_NAME);
    services.setCookieManager(this);
  }

  public void init() {
    CookieSettings settings = getCookieSettings();

    maxCookies = settings.getMaxCookies();
    maxCookiesPerDomain = settings.getMaxCookiesPerDomain();
    maxCookieLength = settings.getMaxCookieLength();
  }

  public int getMaxCookies() {
    return maxCookies;
  }

  public int getMaxCookiesPerDomain() {
    return maxCookiesPerDomain;
  }

  public int getMaxCookieLength() {
    return maxCookieLength;
  }

  public Set<Cookie> getCookie(String domain, String path) {
    checkNotNull(domain, "Domain cannot be null");

    GetCookieArg.Builder arg = GetCookieArg.newBuilder();
    arg.setDomain(domain);

    if (path != null) {
      arg.setPath(path);
    }

    Response response = executeMessage(CookieManagerMessage.GET_COOKIE, arg);
    CookieList.Builder builder = CookieList.newBuilder();
    buildPayload(response, builder);
    CookieList list = builder.build();

    List<CookieMngProtos.Cookie> cookies = list.getCookieListList();
    Set<Cookie> result = Sets.newHashSet();

    for (CookieMngProtos.Cookie cookie : cookies) {
      result.add(new Cookie(cookie.getName(), cookie.getValue(),
                            cookie.getDomain(), cookie.getPath(), new Date(cookie.getExpires()),
                            cookie.getIsSecure()));
    }

    return result;

  }

  public void removeCookie(String domain, String path, String name) {
    if (domain == null) {
      throw new NullPointerException("Domain can not be null");
    }

    RemoveCookieArg.Builder arg = RemoveCookieArg.newBuilder();
    arg.setDomain(domain);

    if (path != null) {
      arg.setPath(path);
    }

    if (name != null) {
      arg.setName(name);
    }

    executeMessage(CookieManagerMessage.REMOVE_COOKIE, arg);
  }

  public void removeAllCookies() {
    executeMessage(CookieManagerMessage.REMOVE_ALL_COOKIES, null);
  }

  private CookieSettings getCookieSettings() {
    Response response = executeMessage(CookieManagerMessage.GET_COOKIE_SETTINGS, null);
    CookieSettings.Builder builder = CookieSettings.newBuilder();
    buildPayload(response, builder);
    return builder.build();
  }

}