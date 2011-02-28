package com.opera.core.systems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class OperaPathsTest {
  private static OperaPaths paths = new OperaPaths();
  private static String knownDir = System.getProperty("user.home");

  @Test
  public void testOperaPath() throws Exception {
    assertNotNull(paths.operaPath());
  }

  @Test
  public void testLauncherPath() throws Exception {
    assertNotNull(paths.operaPath());
  }

  @Test
  public void testStart() throws Exception {
    OperaDriver driver = new OperaDriver();

    driver.get("http://t/core/README");

    assertEquals("http://t/core/README", driver.getCurrentUrl());

    driver.shutdown();
  }

  /* These tests mess with the environment variables. So they're at the end */

  @Test
  public void testOperaEnvVar() throws Exception {
    assertNotSame(knownDir, paths.operaPath());

    Map<String, String> env = new HashMap<String, String>();
    env.put("OPERA_PATH", knownDir);
    setEnv(env);

    assertEquals(knownDir, paths.operaPath());
  }

  @Test
  public void testLauncherEnvVar() throws Exception {
    assertNotSame(knownDir, paths.launcherPath());

    Map<String, String> env = new HashMap<String, String>();
    env.put("OPERA_LAUNCHER", knownDir);
    setEnv(env);

    assertEquals(knownDir, paths.launcherPath());
  }

  /**
   * Massive hack to set the environment variables inside this JVM. Used to
   * test if OperaPaths is checking the env vars.
   *
   * http://stackoverflow.com/questions/318239/how-do-i-set-environment-variables-from-java/496849#496849
   *
   * @param newenv The new environment to set
   */
  @SuppressWarnings("unchecked")
  private static void setEnv(Map<String, String> newenv) throws Exception {
    Class[] classes = Collections.class.getDeclaredClasses();
    Map<String, String> env = System.getenv();
    for (Class cl : classes) {
      if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
        Field field = cl.getDeclaredField("m");
        field.setAccessible(true);
        Object obj = field.get(env);
        Map<String, String> map = (Map<String, String>) obj;
        map.clear();
        map.putAll(newenv);
      }
    }
  }
}
