/*
Copyright 2011-2012 Opera Software ASA

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

import com.opera.core.systems.model.ICommand;

import java.util.Map;


public enum SelftestCommand implements ICommand {

  RUN_TESTS(1),
  GET_TESTSGROUPS(2),
  FINISHED(3),
  RESULT(4);

  private final int command;

  private static final Map<Integer, SelftestCommand> lookup = Maps.uniqueIndex(
      ImmutableList.copyOf(values()),
      new Function<SelftestCommand, Integer>() {
        public Integer apply(SelftestCommand command) {
          return command.getCommandID();
        }
      });

  private SelftestCommand(int command) {
    this.command = command;
  }

  public int getCommandID() {
    return command;
  }

  public String getServiceName() {
    return "selftest";
  }

  public static SelftestCommand get(int id) {
    return lookup.get(id);
  }

}