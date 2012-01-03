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

package com.opera.core.systems.scope.services;

import java.util.List;

public interface ISelftest {

  /**
   * Run the selftests for the specified modules.
   *
   * @param modules the names of the modules to test
   */
  public void runSelftests(List<String> modules);

  public class SelftestResult {

    private String tag;
    private String description;
    private ResultType result;
    private String more;

    public SelftestResult(String tag, String description, ResultType result) {
      this(tag, description, result, null);
    }

    public SelftestResult(String tag, String description, ResultType result, String more) {
      this.tag = tag;
      this.description = description;
      this.result = result;
      this.more = more;
    }

    public String getTag() {
      return tag;
    }

    public String getDescription() {
      return description;
    }

    public ResultType getResult() {
      return result;
    }

    public String getMore() {
      return more;
    }

    public String toString() {
      String format;
      Object[] args;
      if (more != null) {
        format = "%s:%s\t%s\t%s";
        args = new Object[]{tag, description, result, more};
      } else {
        format = "%s:%s\t%s";
        args = new Object[]{tag, description, result};
      }

      return String.format(format, args);
    }

    public boolean equals(Object other) {
      if (!(other instanceof SelftestResult)) {
        return false;
      }

      SelftestResult result = (SelftestResult) other;
      return result.tag.equals(this.tag) &&
             result.description.equals(this.description) &&
             result.result == this.result &&
             (result.more != null ? result.more.equals(this.more) : true);
    }
  }

  public enum ResultType {
    PASS, FAIL, SKIP
  }
}