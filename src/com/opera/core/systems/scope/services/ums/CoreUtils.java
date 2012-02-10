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

package com.opera.core.systems.scope.services.ums;

import com.opera.core.systems.OperaDriver.PrivateData;
import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.CoreUtilsCommand;
import com.opera.core.systems.scope.protos.CoreProtos;
import com.opera.core.systems.scope.protos.CoreProtos.BrowserInformation;
import com.opera.core.systems.scope.protos.CoreProtos.ClearFlags;
import com.opera.core.systems.scope.protos.CoreProtos.ClearPrivateDataArg;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.ICoreUtils;

import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriverException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class CoreUtils extends AbstractService implements ICoreUtils {

  private boolean supportsMeta = false;
  private BrowserInformation browserInformation;

  public CoreUtils(ScopeServices services, String version) {
    super(services, version);

    String serviceName = "core";

    // Version 1.2 is required for browser meta information
    if (!isVersionInRange(version, "1.2", serviceName)) {
      supportsMeta = true;
    }

    services.setCoreUtils(this);
  }

  public void init() {
    if (supportsMeta) {
      browserInformation = getBrowserInformation();
    }
  }

  public boolean hasMetaInformation() {
    return supportsMeta;
  }

  public String getCoreVersion() {
    if (!browserInformation.hasCoreVersion()) {
      throw new UnsupportedCommandException("not available in this product");
    }
    return browserInformation.getCoreVersion();
  }

  public String getOperatingSystem() {
    if (!browserInformation.hasOperatingSystem()) {
      throw new UnsupportedCommandException("not available in this product");
    }
    return browserInformation.getOperatingSystem();
  }

  public String getProduct() {
    if (!browserInformation.hasProduct()) {
      throw new UnsupportedCommandException("not available in this product");
    }
    return browserInformation.getProduct();
  }

  public String getBinaryPath() {
    if (!browserInformation.hasBinaryPath()) {
      throw new UnsupportedCommandException("not available in this product");
    }
    return browserInformation.getBinaryPath();
  }

  public String getUserAgent() {
    if (!browserInformation.hasUserAgent()) {
      throw new UnsupportedCommandException("not available in this product");
    }
    return browserInformation.getUserAgent();
  }

  public Integer getProcessID() {
    if (!browserInformation.hasProcessID()) {
      throw new UnsupportedCommandException("not available in this product");
    }
    return browserInformation.getProcessID();
  }

  public void clearPrivateData(List<ClearFlags> flags) {
    ClearPrivateDataArg.Builder arg = ClearPrivateDataArg.newBuilder();
    arg.addAllClearList(flags);
    executeCommand(CoreUtilsCommand.CLEAR_PRIVATE_DATA, arg);
  }

  public void clearPrivateData(PrivateData... flags) {
    clearPrivateData(privateDataFlagsToScope(flags));
  }

  // Private methods follow

  private BrowserInformation getBrowserInformation() {
    Response response = executeCommand(CoreUtilsCommand.GET_BROWSER_INFORMATION, null);
    BrowserInformation.Builder builder = BrowserInformation.newBuilder();
    buildPayload(response, builder);
    return builder.build();
  }

  private List<CoreProtos.ClearFlags> privateDataFlagsToScope(PrivateData... flags) {
    List<ClearFlags> clearFlags = new ArrayList<CoreProtos.ClearFlags>();

    for (PrivateData flag : flags) {
      switch (flag) {
        case ALL:
          clearFlags.add(CoreProtos.ClearFlags.CLEAR_ALL);
          break;
        case VISITED_LINKS:
          clearFlags.add(CoreProtos.ClearFlags.CLEAR_VISITED_LINKS);
          break;
        case DISK_CACHE:
          clearFlags.add(CoreProtos.ClearFlags.CLEAR_DISK_CACHE);
          break;
        case IMAGE_CACHE:
          clearFlags.add(CoreProtos.ClearFlags.CLEAR_IMAGE_CACHE);
          break;
        case MEMORY_CACHE:
          clearFlags.add(CoreProtos.ClearFlags.CLEAR_MEMORY_CACHE);
          break;
        case SENSITIVE_DATA:
          clearFlags.add(CoreProtos.ClearFlags.CLEAR_SENSITIVE_DATA);
          break;
        case SESSION_COOKIES:
          clearFlags.add(CoreProtos.ClearFlags.CLEAR_SESSION_COOKIES);
          break;
        case ALL_COOKIES:
          clearFlags.add(CoreProtos.ClearFlags.CLEAR_ALL_COOKIES);
          break;
        case GLOBAL_HISTORY:
          clearFlags.add(CoreProtos.ClearFlags.CLEAR_GLOBAL_HISTORY);
          break;
        case CONSOLE:
          clearFlags.add(CoreProtos.ClearFlags.CLEAR_CONSOLE);
          break;
        case THUMBNAILS:
          clearFlags.add(CoreProtos.ClearFlags.CLEAR_THUMBNAILS);
          break;
        case WEBDATABASES:
          clearFlags.add(CoreProtos.ClearFlags.CLEAR_WEBDATABASES);
          break;
        case WEBSTORAGE:
          clearFlags.add(CoreProtos.ClearFlags.CLEAR_WEBSTORAGE);
          break;
        case APPCACHE:
          clearFlags.add(CoreProtos.ClearFlags.CLEAR_APPCACHE);
          break;
        case GEOLOCATION_PERMISSIONS:
          clearFlags.add(CoreProtos.ClearFlags.CLEAR_GEOLOCATION_PERMISSIONS);
          break;
        case SITE_PREFS:
          clearFlags.add(CoreProtos.ClearFlags.CLEAR_SITE_PREFS);
          break;
        default:
          throw new WebDriverException("Unhandled private data flag: " + flag);
      }
    }

    return clearFlags;
  }

}