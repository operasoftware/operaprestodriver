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
package com.opera.core.systems.scope.stp;

/**
 * An enumarator for possible states that a scope
 * command can return
 *
 * @author Deniz Turkoglu <denizt@opera.com>
 */
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum Status {
  OK(0), CONFLICT(1), UNSUPPORTED_TYPE(2), BAD_REQUEST(3), INTERNAL_ERROR(4), COMMAND_NOT_FOUND(
      5), SERVICE_NOT_FOUND(6), OOM(7), SERVICE_NOT_ENABLED(8), SERVICE_ALREADY_ENABLED(
      9);

  private int code;
  private static final Map<Integer, Status> lookup = new HashMap<Integer, Status>();

  static {
    for (Status status : EnumSet.allOf(Status.class))
      lookup.put(status.getCode(), status);
  }

  private Status(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public static Status get(int code) {
    return lookup.get(code);
  }

}
