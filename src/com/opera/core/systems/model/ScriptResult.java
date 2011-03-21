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

/**
 * Holds the result of a script execution, namely for an object, scope protocol
 * assigns an object id for each object retrieved through the protocol, this id
 * can also be used later on to release the object
 *
 * @author Deniz Turkoglu <denizt@opera.com>
 *
 */
public class ScriptResult {

  protected int objectId;
  protected String className;

  public ScriptResult(int objectId, String className) {
    this.objectId = objectId;
    this.className = className;
  }

  public int getObjectId() {
    return objectId;
  }

  public void setObjectId(int objectId) {
    this.objectId = objectId;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }
}
