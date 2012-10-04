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

package com.opera.core.systems.scope.services;

import com.opera.core.systems.scope.Service;

import org.openqa.selenium.Cookie;

import java.util.Set;

/**
 * New cookie manager service (work in progress).
 */
public interface CookieManager extends Service {

  static final String SERVICE_NAME = "cookie-manager";

  Set<Cookie> getCookie(String domain, String path);

  void removeCookie(String domain, String path, String name);

  void removeAllCookies();

  int getMaxCookies();

  int getMaxCookiesPerDomain();

  int getMaxCookieLength();

}