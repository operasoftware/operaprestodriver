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
package com.opera.core.systems.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds a tree of references to runtimes. Used in EcmaScriptDebugger and
 * EcmascriptService.
 *
 * @author Deniz Turkoglu <denizt@opera.com>
 *
 */
public class RuntimeNode {
  private String frameName;
  private int runtimeID;

  private Map<Integer, RuntimeNode> nodes = new HashMap<Integer, RuntimeNode>();

  public String getFrameName() {
    return frameName;
  }

  public void setFrameName(String frameName) {
    this.frameName = frameName;
  }

  public int getRuntimeID() {
    return runtimeID;
  }

  public void setRuntimeID(int runtimeID) {
    this.runtimeID = runtimeID;
  }

  public Map<Integer, RuntimeNode> getNodes() {
    return nodes;
  }
}
