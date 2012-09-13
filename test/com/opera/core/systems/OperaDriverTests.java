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

package com.opera.core.systems;

import com.opera.core.systems.environment.GlobalTestEnvironment;
import com.opera.core.systems.environment.InProcessTestEnvironment;
import com.opera.core.systems.testing.AbstractTestCase;
import com.opera.core.systems.testing.ParallelSuite;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(ParallelSuite.class)
@Suite.SuiteClasses(
    {
        UnitTests.class,

        ConsoleLoggerTest.class,
        CrashTest.class,
        DriverKeysTest.class,
        EcmaScriptTest.class,
        ElementsTest.class,
        ExecKeyTest.class,
        FindElementsTest.class,
        FramesTest.class,
        HTML5FormTest.class,
        IdleTest.class,
        IFramesTest.class,
        JavascriptTest.class,
        LogsTest.class,
        MouseTest.class,
        NavigationTest.class,
        OperaArgumentsTest.class,
        OperaDriverTest.class,
        OperaFilePreferencesTest.class,
        OperaInProcessRunnerTest.class,
        OperaLauncherRunnerTest.class,
        OperaProxyTest.class,
        OperaRunnerTest.class,
        OperaScopePreferencesTest.class,
        OperaSettingsIntegrationTest.class,
        ScopeWindowTest.class,
        ScreenshotTest.class,
        SelftestTest.class,
        SessionTest.class,
        StaleElementsDifferentPagesTest.class,
        StaleElementsSamePageTest.class,
        StaleElementsTest.class,
        TestDriverBuilderTest.class,
        TestDriverSupplierTest.class,
        UtilsTest.class,
        WindowTest.class
    }
)
public class OperaDriverTests {

  @BeforeClass
  public static void prepareCommonEnvironment() {
    GlobalTestEnvironment.get(InProcessTestEnvironment.class);
  }

  @AfterClass
  public static void cleanUpDriver() {
    AbstractTestCase.removeDriver();
  }

}