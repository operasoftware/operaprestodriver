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

import com.opera.core.systems.testing.NoDriver;
import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.After;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;

@NoDriver
public class OperaBinaryTest extends OperaDriverTestCase {

  @After
  public void resetEnvironment() {
    environment.unset(OperaBinary.OPERA_PATH_ENV_VAR);
  }

  @Test
  public void constructWithProduct() {
    OperaBinary binary = new OperaBinary(OperaProduct.DESKTOP);
    assertTrue(binary.getFile().exists());
    assertTrue(binary.getFile().isFile());
    assertTrue(binary.getFile().canExecute());
  }

  // TODO(andreastt): Test when unsetting PATH here

  @Test
  public void constructWithUserSpecifiedPath() {
    OperaBinary binary = new OperaBinary(resources.executableBinary().getPath());
    assertTrue(binary.getFile().exists());
    assertTrue(binary.getFile().isFile());
    assertTrue(binary.getFile().canExecute());
  }

  @Test
  public void constructWithUserSpecifiedBinary() {
    OperaBinary binary = new OperaBinary(resources.executableBinary());
    assertTrue(binary.getFile().exists());
    assertTrue(binary.getFile().isFile());
    assertTrue(binary.getFile().canExecute());
  }

  @Test
  public void constructionWithInvalidPath() {
    Exception exception = null;

    try {
      new OperaBinary("does/not/exist");
    } catch (RuntimeException e) {
      exception = e;
    }

    assertThat(exception, is(instanceOf(IllegalStateException.class)));
    assertThat(exception.getMessage(),
               containsString("The executable does not exist: does/not/exist"));
  }

  @Test
  public void constructionWithNonExecutableBinary() {
    Exception exception = null;

    try {
      new OperaBinary(resources.textFile());
    } catch (RuntimeException e) {
      exception = e;
    }

    assertThat(exception, is(instanceOf(IllegalStateException.class)));
    assertThat(exception.getMessage(), containsString("The file is not executable"));
  }

  @Test
  public void findBasedOnEnvironmentalVariable() {
    environment.set(OperaBinary.OPERA_PATH_ENV_VAR, resources.executableBinary().getPath());
    assertNotNull(OperaBinary.find(OperaProduct.DESKTOP));
  }

  // TODO(andreastt): Test for endings on Windows and Mac

  @Test
  public void findBasedOnPlatform() {
    assertNotNull(OperaBinary.find(OperaProduct.DESKTOP));
  }

  @Test
  public void findBasedOnPlatformAll() {
    assertNotNull(OperaBinary.find(OperaProduct.ALL));
  }

  @Test
  public void findBasedOnPlatformCore() {
    assertNull(OperaBinary.find(OperaProduct.CORE));
  }

  @Test
  public void findBasedOnPlatformSDK() {
    assertNull(OperaBinary.find(OperaProduct.SDK));
  }

  @Test
  public void findBasedOnPlatformMini() {
    assertNull(OperaBinary.find(OperaProduct.MINI));
  }

  @Test  // Important: Requires operamobile to be installed!
  public void findBasedOnPlatformMobile() {
    assertNotNull(OperaBinary.find(OperaProduct.MOBILE));
  }

  // TODO(andreastt): Possibly mock build*Paths() for testing?

  // TODO(andreastt): Possibly mock build*Binaries() for testing?

}