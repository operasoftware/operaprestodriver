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

import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.DesktopUtilsCommand;
import com.opera.core.systems.ScopeServices;

//import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.scope.protos.UmsProtos.Response;

import com.opera.core.systems.scope.services.IDesktopUtils;
import com.opera.core.systems.scope.protos.DesktopUtilsProtos.DesktopPid;
import com.opera.core.systems.scope.protos.DesktopUtilsProtos.DesktopStringID;
import com.opera.core.systems.scope.protos.DesktopUtilsProtos.DesktopStringText;
import com.opera.core.systems.scope.protos.DesktopUtilsProtos.DesktopPath;

/**
 *
 * @author Adam Minchinton, Karianne Ekern
 *
 */
public class DesktopUtils extends AbstractService implements IDesktopUtils {

  public DesktopUtils(ScopeServices services, String version) {
    super(services, version);

    String serviceName = "desktop-utils";

    if (!isVersionInRange(version, "3.0", serviceName)) throw new UnsupportedOperationException(
        serviceName + " version " + version + " is not supported");

    services.setDesktopUtils(this);
  }

  public void init() {
  }

  public String getString(String enumText) {
    DesktopStringID.Builder stringBuilder = DesktopStringID.newBuilder();
    stringBuilder.setEnumText(enumText);

    Response response = executeCommand(DesktopUtilsCommand.GET_STRING,
        stringBuilder);

    DesktopStringText.Builder stringTextBuilder = DesktopStringText.newBuilder();
    buildPayload(response, stringTextBuilder);
    DesktopStringText stringText = stringTextBuilder.build();

    // Remember to remove all CRLF
    return removeCR(stringText.getText());
  }

  public String removeCR(String text) {
    // Hack to remove all the \r's as we sometimes get just \n and sometimes
    // \r\n then the string comparison doesn't work
    return text.replaceAll("(\\r)", "");
  }

  public String getOperaPath() {
    Response response = executeCommand(DesktopUtilsCommand.GET_OPERA_PATH, null);

    DesktopPath.Builder pathBuilder = DesktopPath.newBuilder();
    buildPayload(response, pathBuilder);
    DesktopPath string_path = pathBuilder.build();

    return string_path.getPath();
  }

  public String getLargePreferencesPath() {
    Response response = executeCommand(
        DesktopUtilsCommand.GET_LARGE_PREFERENCES_PATH, null);

    DesktopPath.Builder pathBuilder = DesktopPath.newBuilder();
    buildPayload(response, pathBuilder);
    DesktopPath stringPath = pathBuilder.build();

    return stringPath.getPath();
  }

  public String getSmallPreferencesPath() {
    Response response = executeCommand(
        DesktopUtilsCommand.GET_SMALL_PREFERENCES_PATH, null);

    DesktopPath.Builder pathBuilder = DesktopPath.newBuilder();
    buildPayload(response, pathBuilder);
    DesktopPath stringPath = pathBuilder.build();

    return stringPath.getPath();
  }

  public String getCachePreferencesPath() {
    Response response = executeCommand(
        DesktopUtilsCommand.GET_CACHE_PREFERENCES_PATH, null);

    DesktopPath.Builder pathBuilder = DesktopPath.newBuilder();
    buildPayload(response, pathBuilder);
    DesktopPath stringPath = pathBuilder.build();

    return stringPath.getPath();
  }

  public int getOperaPid() {
    Response response = executeCommand(DesktopUtilsCommand.GET_OPERA_PID, null);

    DesktopPid.Builder pathBuilder = DesktopPid.newBuilder();
    buildPayload(response, pathBuilder);
    DesktopPid desktopPid = pathBuilder.build();

    return desktopPid.getPid();
  }

}
