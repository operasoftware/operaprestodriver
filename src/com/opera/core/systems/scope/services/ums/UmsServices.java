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

package com.opera.core.systems.scope.services.ums;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.protos.ScopeProtos.HostInfo;
import com.opera.core.systems.scope.protos.ScopeProtos.Service;
import com.opera.core.systems.scope.services.IConsoleLogger;
import com.opera.core.systems.scope.services.ICookieManager;
import com.opera.core.systems.scope.services.ICoreUtils;
import com.opera.core.systems.scope.services.IDesktopUtils;
import com.opera.core.systems.scope.services.IDesktopWindowManager;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IOperaExec;
import com.opera.core.systems.scope.services.IPrefs;
import com.opera.core.systems.scope.services.ISelftest;
import com.opera.core.systems.scope.services.IWindowManager;
import com.opera.core.systems.internal.VersionUtil;

import java.util.List;
import java.util.logging.Logger;

public class UmsServices {

  protected final Logger logger = Logger.getLogger(this.getClass().getName());

  protected final IEcmaScriptDebugger debugger;
  protected final IOperaExec exec;
  protected final IWindowManager windowManager;
  protected final IConsoleLogger consoleLogger;
  protected final ICoreUtils coreUtils;
  protected final IDesktopWindowManager desktopWindowManager;
  protected final IDesktopUtils desktopUtils;
  protected final ISelftest selftest;
  protected final SystemInputManager systemInputManager;
  protected final IPrefs prefs;
  private final ICookieManager cookieManager;

  public UmsServices(ScopeServices services, HostInfo info) {
    List<Service> serviceList = info.getServiceListList();
    windowManager =
        new WindowManager(services, getVersionForService(serviceList, "window-manager"));
    consoleLogger =
        new ConsoleLogger(services, getVersionForService(serviceList, "console-logger"));

    if (findServiceNamed(serviceList, "prefs") != null && services.getVersions()
        .containsKey("prefs")) {
      prefs = new Prefs(services, getVersionForService(serviceList, "prefs"));
    } else {
      prefs = null;
    }

    if (findServiceNamed(serviceList, "core") != null && services.getVersions()
        .containsKey("core")) {
      coreUtils =
          new CoreUtils(services, getVersionForService(serviceList, "core")); //"core-utils"));
    } else {
      coreUtils = null;
    }

    if (findServiceNamed(serviceList, "desktop-utils") != null
        && services.getVersions().containsKey("desktop-utils")) {
      desktopUtils = new DesktopUtils(services, getVersionForService(serviceList, "desktop-utils"));
    } else {
      desktopUtils = null;
    }

    if (findServiceNamed(serviceList, "system-input") != null
        && services.getVersions().containsKey("system-input")) {
      systemInputManager =
          new SystemInputManager(services, getVersionForService(serviceList, "system-input"));
    } else {
      systemInputManager = null;
    }

    if (findServiceNamed(serviceList, "selftest") != null
        && services.getVersions().containsKey("selftest")) {
      selftest = new Selftest(services, getVersionForService(serviceList, "selftest"));
    } else {
      selftest = null;
    }

    // Check both the client and the Driver being created support the desktop-window-manager
    if (findServiceNamed(serviceList, "desktop-window-manager") != null
        && services.getVersions().containsKey("desktop-window-manager")) {
      desktopWindowManager = new DesktopWindowManager(desktopUtils, systemInputManager, services,
                                                      getVersionForService(serviceList,
                                                                           "desktop-window-manager"));
    } else {
      desktopWindowManager = null;
    }

    if (findServiceNamed(serviceList, "ecmascript") != null) {
      String ecmascriptVersion = getVersionForService(serviceList, "ecmascript");
      debugger = new EcmascriptService(services, ecmascriptVersion);
    } else {
      String esdbgVersion = getVersionForService(serviceList, "ecmascript-debugger");
      if (VersionUtil.compare(esdbgVersion, "6.0") >= 0) {
        debugger = new EcmaScriptDebugger6(services, esdbgVersion);
      } else {
        debugger = new EcmaScriptDebugger(services, esdbgVersion);
      }
    }

    logger.fine("Using " + debugger.getClass().getSimpleName());

    exec = new OperaExec(services, getVersionForService(serviceList, "exec"));

    if (findServiceNamed(serviceList, "cookie-manager") != null
        && services.getVersions().containsKey("cookie-manager")) {
      cookieManager = new CookieManager(
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