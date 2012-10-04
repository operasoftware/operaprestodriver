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

import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.runner.inprocess.OperaInProcessRunner;
import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.testing.NoDriver;
import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

@NoDriver
public class OperaInProcessRunnerTest extends OperaDriverTestCase {

  public OperaSettings settings;
  public OperaRunner runner;

  @Before
  public void setupSettings() {
    settings = new OperaSettings();
    settings.setRunner(OperaInProcessRunner.class);
  }

  @After
  public void stopRunner() {
    if (runner != null) {
      runner.stopOpera();
      runner.shutdown();
    }

    runner = null;
  }

  @Test
  public void construction() {
    assertNotNull(new OperaInProcessRunner());
  }

  @Test
  public void constructionWithSettings() {
    assertNotNull(new OperaInProcessRunner(settings));
  }

  @Test
  public void startOpera() {
    runner = new OperaInProcessRunner();

    runner.startOpera();
    assertTrue(runner.isOperaRunning());
  }

  @Test
  public void startOperaAfterStartedIsNoOp() {
    runner = new OperaInProcessRunner();
    runner.startOpera();
    runner.startOpera();
  }

  @Test
  public void stopOpera() {
    runner = new OperaInProcessRunner();

    runner.startOpera();
    assertTrue(runner.isOperaRunning());

    runner.stopOpera();
    assertFalse(runner.isOperaRunning());
  }

  @Test
  public void stopOperaAfterStoppedIsNoOp() {
    runner = new OperaInProcessRunner();
    runner.stopOpera();
    runner.stopOpera();
  }

  @Test
  public void isOperaRunning() {
    runner = new OperaInProcessRunner();

    assertFalse(runner.isOperaRunning());
    runner.startOpera();
    assertTrue(runner.isOperaRunning());
    runner.stopOpera();
    assertFalse(runner.isOperaRunning());
  }

  @Test
  public void hasOperaCrashedNotImplemented() {
    runner = new OperaInProcessRunner();

    try {
      runner.hasOperaCrashed();
      fail("Expected UnsupportedOperationException");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(UnsupportedOperationException.class)));
    }
  }

  @Test
  public void getOperaCrashlogNotImplemented() {
    runner = new OperaInProcessRunner();

    try {
      runner.getOperaCrashlog();
      fail("Expected UnsupportedOperationException");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(UnsupportedOperationException.class)));
    }
  }

  @Test
  public void shutdown() {
    runner = new OperaInProcessRunner();
    runner.shutdown();
  }

  @Test
  public void saveScreenshot() {
    runner = new OperaInProcessRunner();
    assertNotNull(runner.captureScreen());
  }

  @Test
  public void saveScreenshotAfterShutdownShouldThrow() {
    runner = new OperaInProcessRunner();
    runner.shutdown();

    try {
      runner.captureScreen();
      fail("Expected OperaRunnerException");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(OperaRunnerException.class)));
      assertThat(e.getMessage(), containsString("Opera was shutdown"));
    }
  }

}