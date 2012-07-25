package com.opera.core.systems;

import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;

import java.io.File;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;

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
               containsString("Opera binary does not exist or is not a real file"));
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
    assertThat(exception.getMessage(), containsString("Opera binary is not an executable"));
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
    assertNull(OperaBinary.find(OperaProduct.ALL));
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

  @Test
  @Ignore("requires operamobile to be installed")
  public void findBasedOnPlatformMobile() {
    assertNotNull(OperaBinary.find(OperaProduct.MOBILE));
  }

  // TODO(andreastt): Possibly mock build*Paths() for testing?

  // TODO(andreastt): Possibly mock build*Binaries() for testing?

}