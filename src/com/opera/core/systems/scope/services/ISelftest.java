package com.opera.core.systems.scope.services;

import java.util.List;

public interface ISelftest {

  /**
   * Run the selftests for the specified modules.
   *
   * @param modules The names of the modules to test
   * @return The test output
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
