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

package com.opera.core.systems.scope.stp.services;

import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.scope.Service;
import com.opera.core.systems.scope.protos.EcmascriptProtos;
import com.opera.core.systems.scope.protos.Esdbg6Protos;
import com.opera.core.systems.scope.protos.EsdbgProtos;
import com.opera.core.systems.scope.services.Debugger;
import com.opera.core.systems.scope.services.EcmascriptDebugger;

import java.util.List;
import java.util.logging.Logger;

public class MockEcmascriptDebugger implements Debugger {

  private final Logger logger = Logger.getLogger(getClass().getName());

  public void init() {
    logger.warning("Using mock ecmascript-debugger");
  }

  public String getServiceName() {
    return EcmascriptDebugger.SERVICE_NAME;
  }

  public String getServiceVersion() {
    return null;
  }

  public Object scriptExecutor(String script, Object... params) {
    return null;
  }

  public void removeRuntime(int runtimeId) {
  }

  public void addRuntime(EsdbgProtos.RuntimeInfo info) {
  }

  public List<String> listFramePaths() {
    return null;
  }

  public int getRuntimeId() {
    return 0;
  }

  public void setRuntime(EsdbgProtos.RuntimeInfo runtime) {
  }

  public Integer getObject(String using) {
    return null;
  }

  public Integer executeScriptOnObject(String using, int objectId) {
    return null;
  }

  public Object executeScript(String using, boolean responseExpected) {
    return null;
  }

  public String executeJavascript(String using, boolean responseExpected) {
    return null;
  }

  public String executeJavascript(String using) {
    return null;
  }

  public List<Integer> examineObjects(Integer id) {
    return null;
  }

  public void cleanUpRuntimes() {
  }

  public void cleanUpRuntimes(int windowId) {
  }

  public void changeRuntime(String framePath) {
  }

  public Object callFunctionOnObject(String using, int objectId, boolean responseExpected) {
    return null;
  }

  public String callFunctionOnObject(String using, int objectId) {
    return null;
  }

  public void addRuntime(Esdbg6Protos.RuntimeInfo info) {
  }

  public void releaseObjects() {
  }

  public boolean updateRuntime() {
    return false;
  }

  public void resetRuntimesList() {
  }

  public void readyStateChanged(EcmascriptProtos.ReadyStateChange change) {
  }

  public void releaseObject(int objectId) {
  }

  public void resetFramePath() {
  }

  public void changeRuntime(int index) {
  }

  public String executeJavascript(String using, Integer windowId) {
    return null;
  }

  public Object examineScriptResult(Integer id) {
    return null;
  }

  public void setFormElementValue(int objectId, String value) {
  }

  public void setDriver(OperaDriver driver) {
  }

  public boolean isScriptInjectable() {
    return false;
  }

}