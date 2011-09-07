package com.opera.core.systems;

import junit.framework.Assert;
import org.junit.Test;
import org.openqa.selenium.Platform;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class OperaPathsTest {
  private static OperaPaths paths = new OperaPaths();
  private static String knownDir = System.getProperty("user.home");

  @Test
  public void testOperaPath() throws Exception {
    assertNotNull(paths.operaPath());
  }

  @Test
  public void testLauncherPath() throws Exception {
    assertNotNull(paths.launcherPath());
  }

  /**
   * Test that the launcher is extracted correctly
   */
  @Test
  public void testNoLauncher() throws Exception {
    File launcher = new File(paths.launcherPath());
    launcher.delete();

    Assert.assertFalse(launcher.exists());

    paths.launcherPath();

    Assert.assertTrue(launcher.exists());
  }

  /**
   * Test that the launcher is re-extracted when the hashes differ
   */
  @Test
  public void testNoMatch() throws Exception {
    File launcher = new File(paths.launcherPath());
    launcher.delete();

    Assert.assertFalse(launcher.exists());
    launcher.createNewFile();
    Assert.assertEquals(0, launcher.length());

    paths.launcherPath();

    Assert.assertNotSame(0, launcher.length());
  }

  /**
   * Test that the launcher isn't extracted when it is the same
   */
  @Test
  public void testExists() throws Exception {
    File launcher = new File(paths.launcherPath());
    long modified = launcher.lastModified();

    paths.launcherPath();
    Assert.assertEquals(modified, launcher.lastModified());
  }

  @Test
  public void testStart() throws Exception {
    OperaDriver driver = new OperaDriver();

    driver.get("http://t/core/README");

    assertEquals("http://t/core/README", driver.getCurrentUrl());

    driver.quit();
  }

  /* These tests mess with the environment variables. So they're at the end */

  @Test
  public void testOperaEnvVar() throws Exception {
    // Doesn't work on Windows
    if (Platform.getCurrent() == Platform.WINDOWS) return;

    assertNotSame(knownDir, paths.operaPath());

    setEnvVar("OPERA_PATH", knownDir);

    assertEquals(knownDir, paths.operaPath());
  }

  @Test
  public void testLauncherEnvVar() throws Exception {
    // Doesn't work on Windows
    if (Platform.getCurrent() == Platform.WINDOWS) return;

    assertNotSame(knownDir, paths.launcherPath());

    setEnvVar("OPERA_LAUNCHER", knownDir);

    assertEquals(knownDir, paths.launcherPath());
  }

  /**
   * Massive hack to set the environment variables inside this JVM. Used to
   * test if OperaPaths is checking the env vars.
   * <p/>
   * http://stackoverflow.com/questions/318239/how-do-i-set-environment-variables-from-java/496849#496849
   *
   * @param newenv The new environment to set
   */
  @SuppressWarnings("unchecked")
  private static void setEnvVar(String key, String value) throws Exception {
    Class<?>[] classes = Collections.class.getDeclaredClasses();
    Map<String, String> env = System.getenv();
    for (Class<?> cl : classes) {
      if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
        Field field = cl.getDeclaredField("m");
        field.setAccessible(true);
        Object obj = field.get(env);
        Map<String, String> map = (Map<String, String>) obj;
        map.put(key, value);
      }
    }
  }
}
