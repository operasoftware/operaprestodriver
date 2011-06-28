/*
 * Copyright 2008-2011 Opera Software ASA
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
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.CoreUtilsCommand;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.protos.CoreProtos.BrowserInformation;
import com.opera.core.systems.scope.services.ICoreUtils;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class CoreUtils extends AbstractService implements ICoreUtils {

    private String coreVersion;
    private String operatingSystem;
    private String product;
    private String binaryPath;
    private String userAgent;
    private int    processID;

    /// Whether this core supports getting metadata
    private boolean supports_meta = false;

    public CoreUtils(ScopeServices services, String version) {
        super(services, version);

        String serviceName = "core";

        // Version 1.2 is required for browser meta information
        if (!isVersionInRange(version, "1.2", serviceName)) {
            supports_meta = true;
        }

        services.setCoreUtils(this);
    }

    public void init() {
        if (supports_meta) {
            // TODO Use factory?
            BrowserInformation browserInformation = getBrowserInformation();
            coreVersion     = browserInformation.getCoreVersion();
            operatingSystem = browserInformation.getOperatingSystem();
            product         = browserInformation.getProduct();
            binaryPath      = browserInformation.getBinaryPath();
            userAgent       = browserInformation.getUserAgent();
            processID       = browserInformation.getProcessID();
        }
    }

    public boolean supportsMetaData() {
        return supports_meta;
    }

    public String getCoreVersion() {
        return coreVersion;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public String getProduct() {
        return product;
    }

    public String getBinaryPath() {
        return binaryPath;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public int getProcessID() {
        return processID;
    }


    // Private methods follow

    private BrowserInformation getBrowserInformation() {
        Response response = executeCommand(CoreUtilsCommand.GET_BROWSER_INFORMATION, null);
        BrowserInformation.Builder builder = BrowserInformation.newBuilder();
        buildPayload(response, builder);
        return builder.build();
    }

}
