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

package com.opera.core.systems.scope;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import com.opera.core.systems.scope.exceptions.ScopeException;
import com.opera.core.systems.scope.services.ConsoleLogger;
import com.opera.core.systems.scope.services.CookieManager;
import com.opera.core.systems.scope.services.Core;
import com.opera.core.systems.scope.services.Ecmascript;
import com.opera.core.systems.scope.services.EcmascriptDebugger;
import com.opera.core.systems.scope.services.Exec;
import com.opera.core.systems.scope.services.Prefs;
import com.opera.core.systems.scope.services.Selftest;
import com.opera.core.systems.scope.services.WindowManager;
import com.opera.core.systems.scope.services.desktop.DesktopUtils;
import com.opera.core.systems.scope.services.desktop.DesktopWindowManager;
import com.opera.core.systems.scope.services.desktop.SystemInput;
import com.opera.core.systems.scope.stp.services.ScopeConsoleLogger;
import com.opera.core.systems.scope.stp.services.ScopeCookieManager;
import com.opera.core.systems.scope.stp.services.ScopeCore;
import com.opera.core.systems.scope.stp.services.ScopeEcmascriptDebugger6;
import com.opera.core.systems.scope.stp.services.ScopeEcmascriptService;
import com.opera.core.systems.scope.stp.services.ScopeExec;
import com.opera.core.systems.scope.stp.services.ScopePrefs;
import com.opera.core.systems.scope.stp.services.ScopeSelftest;
import com.opera.core.systems.scope.stp.services.ScopeWindowManager;
import com.opera.core.systems.scope.stp.services.desktop.ScopeDesktopUtils;
import com.opera.core.systems.scope.stp.services.desktop.ScopeDesktopWindowManager;
import com.opera.core.systems.scope.stp.services.desktop.ScopeSystemInputManager;

import java.util.Map;

public enum ScopeService {

  CORE(Core.SERVICE_NAME),
  EXEC(Exec.SERVICE_NAME),
  WINDOW_MANAGER(WindowManager.SERVICE_NAME),
  CONSOLE_LOGGER(ConsoleLogger.SERVICE_NAME),
  COOKIE_MANAGER(CookieManager.SERVICE_NAME),
  ECMASCRIPT_DEBUGGER(EcmascriptDebugger.SERVICE_NAME),
  ECMASCRIPT(Ecmascript.SERVICE_NAME),
  PREFS(Prefs.SERVICE_NAME),
  SELFTEST(Selftest.SERVICE_NAME),

  // desktop
  DESKTOP_UTILS(DesktopUtils.SERVICE_NAME),
  DESKTOP_WINDOW_MANAGER(DesktopWindowManager.SERVICE_NAME),
  SYSTEM_INPUT(SystemInput.SERVICE_NAME);

  private static final Map<String, ScopeService> lookup =
      Maps.uniqueIndex(ImmutableList.copyOf(values()), new Function<ScopeService, String>() {
        public String apply(ScopeService service) {
          return service.toString();
        }
      });

  private final String serviceName;

  ScopeService(String stringRepresentation) {
    serviceName = stringRepresentation;
  }

  public Service newInstance(ScopeServices scope) {
    return newInstance(this, scope);
  }

  public String toOpera() {
    return serviceName;
  }

  @Override
  public String toString() {
    return toOpera();
  }

  public static ScopeService get(String name) {
    return lookup.get(name);
  }

  private static Service newInstance(ScopeService service, ScopeServices scope) {
    switch (service) {
      case CONSOLE_LOGGER:
        return new ScopeConsoleLogger(scope);
      case COOKIE_MANAGER:
        return new ScopeCookieManager(scope);
      case CORE:
        return new ScopeCore(scope);
      case ECMASCRIPT_DEBUGGER:
        return new ScopeEcmascriptDebugger6(scope);
      case ECMASCRIPT:
        return new ScopeEcmascriptService(scope);
      case EXEC:
        return new ScopeExec(scope);
      case PREFS:
        return new ScopePrefs(scope);
      case SELFTEST:
        return new ScopeSelftest(scope);
      case WINDOW_MANAGER:
        return new ScopeWindowManager(scope);

      // desktop
      case DESKTOP_UTILS:
        return new ScopeDesktopUtils(scope);
      case DESKTOP_WINDOW_MANAGER:
        return new ScopeDesktopWindowManager(scope);
      case SYSTEM_INPUT:
        return new ScopeSystemInputManager(scope);
    }

    throw new ScopeException("Unknown service: " + service);
  }

}