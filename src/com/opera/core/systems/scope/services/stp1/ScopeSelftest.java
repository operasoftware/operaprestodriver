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

package com.opera.core.systems.scope.services.stp1;

import com.google.common.collect.ImmutableList;

import com.opera.core.systems.scope.ScopeServices;
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.services.messages.SelftestMessage;
import com.opera.core.systems.scope.protos.SelftestProtos.RunModulesArg;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.Selftest;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScopeSelftest extends AbstractService implements Selftest {

  public static final String SERVICE_NAME = "selftest";

  private static Pattern errorPattern =
      Pattern.compile("Warning: Pattern '([^']+)' did not match any tests\n" +
                      "Warning: There is no module named '([^']+)'\n");

  private final Logger logger = Logger.getLogger(getClass().getName());

  public ScopeSelftest(ScopeServices services, String version) {
    super(services, SERVICE_NAME, version);

    if (!isVersionInRange(version, "2.0", SERVICE_NAME)) {
      logger.warning(String.format("%s version %s is not supported", SERVICE_NAME, version));
    }

    services.setSelftest(this);
  }

  public void init() {
  }

  public void runSelftests(List<String> modules) {
    logger.finest(String.format("runSelftests: %s", modules));

    RunModulesArg.Builder builder = RunModulesArg.newBuilder();
    builder.addAllModuleList(modules);
    builder.setOutputType(RunModulesArg.OutputType.MACHINE_READABLE);

    Response response = executeMessage(SelftestMessage.RUN_MODULES, builder);
    logger.finest(String.format("ScopeSelftest response: %s", response));
  }

  public static List<SelftestResult> parseSelftests(String output) {
    ImmutableList.Builder<SelftestResult> results = ImmutableList.builder();

    // Check for non-existent module.
    Matcher matcher = errorPattern.matcher(output);
    if (matcher.matches()) {
      return null;
    }

    String[] lines = output.split("\\n");
    for (String line : lines) {

      // Each line has the following format:
      //   tag:description result more
      //   result is PASS, FAIL, or SKIP.
      String[] pieces = line.split("\\t");
      String tagAndDescription = pieces[0];
      String resultString = pieces[1];
      String more = pieces.length > 2 ? pieces[2] : null;

      String[] otherPieces = tagAndDescription.split(":", 2);
      String tag = otherPieces[0];
      String description = otherPieces[1];

      ResultType result;
      if ("PASS".equals(resultString)) {
        result = ResultType.PASS;
      } else if ("FAIL".equals(resultString)) {
        result = ResultType.FAIL;
      } else if ("SKIP".equals(resultString)) {
        result = ResultType.SKIP;
      } else {
        throw new RuntimeException(String.format("Unknown test result %s", resultString));
      }

      results.add(new ScopeSelftestResult(tag, description, result, more));
    }

    return results.build();
  }

  public static class ScopeSelftestResult implements SelftestResult {

    private final String tag;
    private final String description;
    private final ResultType result;
    private final String more;

    public ScopeSelftestResult(String tag, String description, ResultType result) {
      this(tag, description, result, null);
    }

    public ScopeSelftestResult(String tag, String description, ResultType result, String more) {
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

    @Override
    public String toString() {
      StringBuilder format = new StringBuilder();

      format.append("%s:%s\t%s");
      if (more != null) {
        format.append("\t%s");
      }

      return String.format(format.toString(), tag, description, result, more);
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof ScopeSelftestResult)) {
        return false;
      }

      ScopeSelftestResult result = (ScopeSelftestResult) other;
      return result.tag.equals(this.tag) &&
             result.description.equals(this.description) &&
             result.result == this.result &&
             (result.more == null || result.more.equals(this.more));
    }

    // TODO(andreastt): Should match equals()
    @Override
    public int hashCode() {
      return super.hashCode();
    }

  }

}