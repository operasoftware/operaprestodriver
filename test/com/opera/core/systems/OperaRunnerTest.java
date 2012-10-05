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
import com.opera.core.systems.arguments.OperaDesktopArguments;
import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.runner.inprocess.OperaInProcessRunner;
import com.opera.core.systems.testing.NoDriver;
import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

@NoDriver
public class OperaRunnerTest extends OperaDriverTestCase {

  public static final String OLD_OPERA_PATH = System.getenv(OperaBinary.OPERA_PATH_ENV_VAR);

  public TestOperaRunner runner;
  public OperaSettings settings;

  @Before
  public void setupSettings() {
    settings = new OperaSettings();
  }

  @After
  public void dereferenceFields() {
    runner = null;
    settings = null;
  }

  @After
  public void resetEnvironmentPath() {
    environment.set(OperaBinary.OPERA_PATH_ENV_VAR, OLD_OPERA_PATH);
  }

  @Test
  public void construction() {
    runner = new TestOperaRunner();
    List<OperaArgument> arguments = runner.getSettings().arguments().getArguments();

    assertNotNull(runner);
    assertNotNull(runner.getSettings().profile());
    assertEquals("autotestmode", arguments.get(2).getArgument());  // 0
    assertEquals("pd", arguments.get(0).getArgument());  // 1
    assertEquals("debugproxy", arguments.get(1).getArgument());  // 2
    assertTrue(Integer.valueOf(arguments.get(1).getValue().split(":")[1]) > 0);  // 2
  }

  @Test
  public void constructionWithProductCore() {
    environment.unset(OperaBinary.OPERA_PATH_ENV_VAR);

    settings.setBinary(null);
    settings.setProduct(OperaProduct.CORE);

    try {
      runner = new TestOperaRunner(settings);
      fail("Expected OperaRunnerException");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(OperaRunnerException.class)));
      assertThat(e.getMessage(), containsString("Unable to find executable for product"));
    }
  }

  @Test
  public void constructionWithProductDesktop() {
    settings.setProduct(OperaProduct.DESKTOP);
    runner = new TestOperaRunner(settings);
    assertNotNull(runner);
    assertTrue(runner.getSettings().arguments() instanceof OperaDesktopArguments);
  }

  @Test
  public void constructionWithProfile() throws IOException {
    TemporaryFolder temporaryProfile = new TemporaryFolder();
    temporaryProfile.create();

    // Make a new copy in a temporary file system so we don't overwrite our fixture
    String profilePath;

    try {
      Files.copy(resources.locate("profile/opera.ini"), temporaryProfile.newFile("operaprefs.ini"));
    } catch (IOException e) {
      fail("Unable to copy preference fixture: " + e.getMessage());
    }

    profilePath = temporaryProfile.getRoot().getAbsolutePath();

    settings.setProfile(profilePath);
    runner = new TestOperaRunner(settings);
    assertEquals(profilePath,
                 runner.getSettings().arguments().getArguments().get(0).getValue());  // 1
  }

  @Test
  public void constructionWithPort() {
    settings.setPort(0);
    runner = new TestOperaRunner(settings);
    assertNotNull(runner);
    assertTrue(Integer.valueOf(
        runner.getSettings().arguments().getArguments().get(1).getValue().split(":")[1]) > 0);  // 2
  }

  @Test
  public void constructionWithArguments() {
    OperaArguments arguments = new OperaArguments();
    arguments.add("foo");
    arguments.add("bar", "bah");
    arguments.add("baz", "abc");

    settings.arguments().merge(arguments);
    runner = new TestOperaRunner(settings);
    assertNotNull(runner);
    assertTrue("should contain all the arguments added",
               runner.getSettings().arguments().getArguments()
                   .containsAll(arguments.getArguments()));
  }

  public class TestOperaRunner extends OperaInProcessRunner {

    public TestOperaRunner() {
      super();
    }

    public TestOperaRunner(OperaSettings settings) {
      super(settings);
    }

    public OperaSettings getSettings() {
      return this.settings;
    }

  }

}