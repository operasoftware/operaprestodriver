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

package com.opera.core.systems.scope.stp.services;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.ScopeServices;
import com.opera.core.systems.scope.internal.ServiceCallback;
import com.opera.core.systems.scope.protos.SelftestProtos;
import com.opera.core.systems.scope.protos.SelftestProtos.RunTestsArg;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.Selftest;
import com.opera.core.systems.scope.stp.services.messages.SelftestMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebDriverException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

public class ScopeSelftest extends AbstractService implements Selftest {

  private ServiceCallback<SelftestProtos.SelftestResult> callback = null;

  public ScopeSelftest(ScopeServices services) {
    super(services, SERVICE_NAME);

    // Note: Version 2.0 of selftest is not backwards compatible with the previous version 1.1
    if (!isVersionInRange("2.0")) {
      return;
    }

    services.setSelftest(this);
  }

  public void init() {
  }

  public RunStatus runSelftests(Set<String> modules, String groups, String excludes) {
    checkNotNull(modules, "Modules cannot be empty");

    RunTestsArg.Builder argBuilder = RunTestsArg.newBuilder();

    if (groups != null) {
      argBuilder.setTestPattern(groups);
    }

    argBuilder.setModuleList(Joiner.on(',').join(modules));

    if (excludes != null) {
      argBuilder.setExcludePattern(excludes);
    }

    Response response = executeMessage(SelftestMessage.RUN_TESTS, argBuilder);

    SelftestProtos.RunStatus.Builder builder = SelftestProtos.RunStatus.newBuilder();

    buildPayload(response, builder);

    SelftestProtos.RunStatus.RunTestStatus status = builder.build().getStatus();
    switch (status) {
      case RUNNING:
        return RunStatus.RUNNING;
      case NOT_AVAILABLE:
        return RunStatus.NOT_AVAILABLE;
      default:
        throw new WebDriverException(String.format("Unknown result: %s", status));
    }
  }

  public void addResult(SelftestProtos.SelftestResult result) {
    callback.call(result);
  }

  public void onSelftestResult(ServiceCallback<SelftestProtos.SelftestResult> callback) {
    this.callback = callback;
  }

  public Map<String, List<String>> getAvailableGroups() {
    Response response = executeMessage(SelftestMessage.GET_TESTSGROUPS);
    SelftestProtos.GroupList.Builder builder = SelftestProtos.GroupList.newBuilder();
    buildPayload(response, builder);

    ImmutableMap.Builder<String, List<String>> returnModules = ImmutableMap.builder();

    for (SelftestProtos.GroupInformation groupInfo : builder.build().getGroupListList()) {
      if (returnModules.build().containsKey(groupInfo.getModule())) {
        returnModules.build().get(groupInfo.getModule()).add(groupInfo.getName());
      } else {
        List<String> group = Lists.newLinkedList();
        group.add(groupInfo.getName());
        returnModules.put(groupInfo.getModule(), group);
      }
    }

    return returnModules.build();
  }

  public static class ScopeSelftestResult implements Selftest.SelftestResult {

    private String testName = null;
    private String groupName = null;
    private String fileName = null;
    private Result result = Result.FAIL;
    private String reason = null;
    private int lineNumber = 0;

    public String getTestName() {
      return testName;
    }

    public String getGroupName() {
      return groupName;
    }

    public Result getResult() {
      return result;
    }

    public String getReason() {
      return reason;
    }

    public int getLineNumber() {
      return lineNumber;
    }

    public String getFileName() {
      return fileName;
    }

    @Override
    public String toString() {
      return String.format("%s:%s:%s:%s", testName, groupName, result, reason);
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof SelftestResult)) {
        return false;
      }

      ScopeSelftestResult result = (ScopeSelftestResult) other;
      return result.testName.equals(this.testName) &&
             result.groupName.equals(this.groupName) &&
             result.result == this.result &&
             result.lineNumber == this.lineNumber &&
             (result.reason == null || result.reason.equals(this.reason));
    }

    public JSONObject toJson() throws JSONException {
      JSONObject json = new JSONObject();
      json.put("testName", testName);
      json.put("groupName", groupName);
      json.put("result", result);
      json.put("reason", reason);
      json.put("fileName", fileName);
      json.put("lineNumber", lineNumber);
      return json;
    }

    public SelftestResult fromJson(JSONObject json) throws JSONException {
      return builder()
          .setTestName(String.valueOf(json.get("testName")))
          .setGroupName(String.valueOf(json.get("groupName")))
          .setFileName(String.valueOf(json.get("fileName")))
          .setTestResult(Result.valueOf(String.valueOf(json.get("result"))))
          .setReason(String.valueOf(json.get("reason")))
          .setLineNumber(Integer.parseInt((String.valueOf(json.get("lineNumber")))))
          .build();
    }

    public static Result convertProtoResult(SelftestProtos.SelftestResult.Result result) {
      switch (result) {
        case PASS:
          return Result.PASS;
        case SKIP:
          return Result.SKIP;
        case FAIL:
        default:
          return Result.FAIL;
      }
    }

    /**
     * Returns a new builder.  The generated builder is equivalent to the builder created by the
     * {@link ScopeSelftestResult.Builder} constructor.
     */
    public static Builder builder() {
      return new Builder();
    }

    /**
     * A builder for creating {@link SelftestResult} emulation profiles.
     */
    public static class Builder {

      private final ScopeSelftestResult result;

      /**
       * Creates a new builder.  The returned builder is equivalent to the builder generated by
       * {@link ScopeSelftestResult#builder()}.
       */
      public Builder() {
        result = new ScopeSelftestResult();
      }

      /**
       * Returns a newly-created {@link SelftestResult} based on the contents of the builder.
       */
      public SelftestResult build() {
        return result;
      }

      /**
       * Sets the name of this test.
       *
       * @param testName name of this test
       * @return self-reference
       */
      public Builder setTestName(String testName) {
        result.testName = testName;
        return this;
      }

      /**
       * Sets the group this test belongs to.
       *
       * @param groupName name of group this test belongs to
       * @return self-reference
       */
      public Builder setGroupName(String groupName) {
        result.groupName = groupName;
        return this;
      }

      /**
       * Sets the group this test belongs to.
       *
       * @param fileName filename where this test is located
       * @return self-reference
       */
      public Builder setFileName(String fileName) {
        result.fileName = fileName;
        return this;
      }

      /**
       * Sets the test result.
       *
       * @param result result of the test {@link Result}
       * @return self-reference
       */
      public Builder setTestResult(Result result) {
        this.result.result = result;
        return this;
      }

      /**
       * Sets the test reason of a FAILed or SKIPped test.
       *
       * @param reason a unique description of this profile
       * @return self-reference
       */
      public Builder setReason(String reason) {
        result.reason = reason;
        return this;
      }

      /**
       * Sets the line number where the test failed.
       *
       * @param lineNumber in filename where a test FAILed or was SKIPped.
       * @return self-reference
       */
      public Builder setLineNumber(int lineNumber) {
        result.lineNumber = lineNumber;
        return this;
      }

    }

  }

}