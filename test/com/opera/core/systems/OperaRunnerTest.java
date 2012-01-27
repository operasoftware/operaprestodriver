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

package com.opera.core.systems;

import com.google.common.io.Files;

import com.opera.core.systems.arguments.OperaArgument;
import com.opera.core.systems.arguments.OperaCoreArguments;
import com.opera.core.systems.arguments.OperaDesktopArguments;
import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.runner.OperaRunnerSettings;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OperaRunnerTest extends OperaDriverTestCase {

  public static TestOperaRunner runner;
  public OperaRunnerSettings settings;
  public File iniFile;
  public String profile;

  @Rule
  public TemporaryFolder temporaryProfile = new TemporaryFolder();

  // Replace OperaDriverTestCase setup and tear down so that we don't launch Opera
  @BeforeClass
  public static void setUpBeforeClass() {
  }

  @AfterClass
  public static void tearDownAfterClass() {
  }

  @Before
  public void beforeEach() {
    settings = new OperaRunnerSettings();
    runner = null;

    // Make a new copy in a temporary file system so we don't overwrite our fixture
    // TODO(andreastt): This should be done more elegantly in OperaDriverTestCase
    try {
      iniFile = temporaryProfile.newFile("operaprefs.ini");
      Files.copy(fixtureFile("profile" + File.separator + "opera.ini"), iniFile);
    } catch (IOException e) {
      fail("Unable to copy preference fixture: " + e.getMessage());
    }

    profile = temporaryProfile.getRoot().getAbsolutePath();
  }

  @Test
  public void testConstruction() {
    runner = new TestOperaRunner();
    List<OperaArgument> arguments = runner.getSettings().getArguments().getArguments();

    // TODO(andreastt): Problems with core-gogi disallows us to have -autotestmode as the first argument
    assertNotNull(runner);
    assertNotNull(runner.getSettings().getProfile());
    assertEquals("autotestmode", arguments.get(2).getArgument());  // 0
    assertEquals("pd", arguments.get(0).getArgument());  // 1
    assertEquals("debugproxy", arguments.get(1).getArgument());  // 2
    assertTrue(Integer.valueOf(arguments.get(1).getValue().split(":")[1]) > 0);  // 2
  }

  @Test
  public void testConstructionWithProductCore() {
    settings.setProduct(OperaProduct.CORE);
    runner = new TestOperaRunner(settings);
    assertNotNull(runner);
    assertTrue(runner.getSettings().getArguments() instanceof OperaCoreArguments);
  }

  @Test
  public void testConstructionWithProductDesktop() {
    settings.setProduct(OperaProduct.DESKTOP);
    runner = new TestOperaRunner(settings);
    assertNotNull(runner);
    assertTrue(runner.getSettings().getArguments() instanceof OperaDesktopArguments);
  }

  @Test
  public void testConstructionWithProfile() {
    settings.setProfile(profile);
    runner = new TestOperaRunner(settings);
    assertEquals(profile,
                 runner.getSettings().getArguments().getArguments().get(0).getValue());  // 1
  }

  @Test
  public void testConstructionWithPort() {
    settings.setPort(0);
    runner = new TestOperaRunner(settings);
    assertNotNull(runner);
    assertTrue(Integer.valueOf(
        runner.getSettings().getArguments().getArguments().get(1).getValue().split(":")[1])
               > 0);  // 2
  }

  @Test
  public void testConstructionWithArguments() {
    OperaArguments arguments = new OperaArguments();
    arguments.add("foo");
    arguments.add("bar", "bah");
    arguments.add("baz", "abc");

    settings.setArguments(arguments);
    runner = new TestOperaRunner(settings);
    assertNotNull(runner);
    assertTrue("should contain all the arguments added",
               runner.getSettings().getArguments().getArguments()
                   .containsAll(arguments.getArguments()));
  }

  @Test
  @Ignore
  public void testStartOpera() {

  }

  @Test
  @Ignore
  public void testStopOpera() {

  }

  @Test
  @Ignore
  public void testIsOperaRunning() {

  }

  @Test
  @Ignore
  public void testHasOperaCrashed() {

  }

  @Test
  @Ignore
  public void testGetOperaCrashlog() {

  }

  @Test
  @Ignore
  public void testShutdown() {

  }

  @Test
  @Ignore
  public void testSaveScreenshot() {

  }

  public class TestOperaRunner extends OperaRunner {

    public TestOperaRunner() {
      super();
    }

    public TestOperaRunner(OperaRunnerSettings settings) {
      super(settings);
    }

    public OperaRunnerSettings getSettings() {
      return this.settings;
    }

  }

}