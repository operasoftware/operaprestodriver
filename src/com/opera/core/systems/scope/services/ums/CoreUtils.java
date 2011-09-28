/*
Copyright 2008-2011 Opera Software ASA

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

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.CoreUtilsCommand;
import com.opera.core.systems.scope.protos.CoreProtos.BrowserInformation;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.ICoreUtils;

import org.openqa.selenium.UnsupportedCommandException;

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

  // Private methods follow

  private BrowserInformation getBrowserInformation() {
    Response response = executeCommand(CoreUtilsCommand.GET_BROWSER_INFORMATION, null);
    BrowserInformation.Builder builder = BrowserInformation.newBuilder();
    buildPayload(response, builder);
    return builder.build();
  }

}