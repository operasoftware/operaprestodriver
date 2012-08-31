/*
 * Copyright 2008-2012 Opera Software ASA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opera.core.systems.scope.services.stp1;

import com.opera.core.systems.scope.ScopeServices;
import com.opera.core.systems.scope.protos.ScopeProtos.HostInfo;
import com.opera.core.systems.scope.protos.ScopeProtos.Service;
import com.opera.core.systems.scope.services.ConsoleLogger;
import com.opera.core.systems.scope.services.CookieManager;
import com.opera.core.systems.scope.services.Core;
import com.opera.core.systems.scope.services.desktop.DesktopUtils;
import com.opera.core.systems.scope.services.desktop.DesktopWindowManager;
import com.opera.core.systems.scope.services.EcmascriptDebugger;
import com.opera.core.systems.scope.services.Exec;
import com.opera.core.systems.scope.services.Prefs;
import com.opera.core.systems.scope.services.Selftest;
import com.opera.core.systems.scope.services.WindowManager;
import com.opera.core.systems.internal.VersionUtil;
import com.opera.core.systems.scope.services.stp1.desktop.ScopeDesktopUtils;
import com.opera.core.systems.scope.services.stp1.desktop.ScopeDesktopWindowManager;
import com.opera.core.systems.scope.services.stp1.desktop.ScopeSystemInputManager;

import java.util.List;
import java.util.logging.Logger;

public class UmsServices {

  protected final Logger logger = Logger.getLogger(this.getClass().getName());

  protected final EcmascriptDebugger debugger;
  protected final Exec exec;
  protected final WindowManager windowManager;
  protected final ConsoleLogger consoleLogger;
  protected final Core core;
  protected final DesktopWindowManager desktopWindowManager;
  protected final DesktopUtils desktopUtils;
  protected final Selftest selftest;
  protected final ScopeSystemInputManager systemInputManager;
  protected final Prefs prefs;
  private final CookieManager cookieManager;

  public UmsServices(ScopeServices services, HostInfo info) {
    List<Service> serviceList = info.getServiceListList();
    windowManager =
        new ScopeWindowManager(services, getVersionForService(serviceList, "window-manager"));
    consoleLogger =
        new ScopeConsoleLogger(services, getVersionForService(serviceList, "console-logger"));

    if (findServiceNamed(serviceList, "prefs") != null && services.getVersions()
        .containsKey("prefs")) {
      prefs = new ScopePrefs(services, getVersionForService(serviceList, "prefs"));
    } else {
      prefs = null;
    }

    if (findServiceNamed(serviceList, "core") != null && services.getVersions()
        .containsKey("core")) {
      core =
          new ScopeCore(services, getVersionForService(serviceList, "core")); //"core-utils"));
    } else {
      core = null;
    }

    if (findServiceNamed(serviceList, "desktop-utils") != null
        && services.getVersions().containsKey("desktop-utils")) {
      desktopUtils = new ScopeDesktopUtils(services, getVersionForService(serviceList, "desktop-utils"));
    } else {
      desktopUtils = null;
    }

    if (findServiceNamed(serviceList, "system-input") != null
        && services.getVersions().containsKey("system-input")) {
      systemInputManager =
          new ScopeSystemInputManager(services, getVersionForService(serviceList, "system-input"));
    } else {
      systemInputManager = null;
    }

    if (findServiceNamed(serviceList, "selftest") != null
        && services.getVersions().containsKey("selftest")) {
      selftest = new ScopeSelftest(services, getVersionForService(serviceList, "selftest"));
    } else {
      selftest = null;
    }

    // Check both the client and the Driver being created support the desktop-window-manager
    if (findServiceNamed(serviceList, "desktop-window-manager") != null
        && services.getVersions().containsKey("desktop-window-manager")) {
      desktopWindowManager = new ScopeDesktopWindowManager(desktopUtils, systemInputManager, services,
                                                      getVersionForService(serviceList,
                                                                           "desktop-window-manager"));
    } else {
      desktopWindowManager = null;
    }

    if (findServiceNamed(serviceList, "ecmascript") != null) {
      String ecmascriptVersion = getVersionForService(serviceList, "ecmascript");
      debugger = new ScopeEcmascriptService(services, ecmascriptVersion);
    } else {
      String esdbgVersion = getVersionForService(serviceList, "ecmascript-debugger");
      if (VersionUtil.compare(esdbgVersion, "6.0") >= 0) {
        debugger = new ScopeEcmascriptDebugger6(services, esdbgVersion);
      } else {
        debugger = new ScopeEcmascriptDebugger(services, esdbgVersion);
      }
    }

    logger.fine("Using " + debugger.getClass().getSimpleName());

    exec = new ScopeExec(services, getVersionForService(serviceList, "exec"));

    if (findServiceNamed(serviceList, "cookie-manager") != null
        && services.getVersions().containsKey("cookie-manager")) {
      cookieManager = new ScopeCookieManager(
          services, getVersionForService(serviceList, "cookie-manager"));
    } else {
      cookieManager = null;
    }
  }

  private String getVersionForService(List<Service> serviceList, String name) {
    Service service = findServiceNamed(serviceList, name);
    if (service != null) {
      return service.getVersion();
    } else {
      return "0.0";
    }
  }

  private Service findServiceNamed(List<Service> services, String name) {
    Service found = null;

    for (Service service : services) {
      if (service.getName().equals(name)) {
        found = service;
        break;
      }
    }

    return found;
  }

}