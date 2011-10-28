package com.opera.core.systems;

import com.opera.core.systems.OperaArguments;
import com.opera.core.systems.OperaArgumentsTest;
import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.runner.OperaRunnerSettings;
import com.opera.core.systems.arguments.OperaArgument;
import com.opera.core.systems.arguments.OperaCoreArguments;
import com.opera.core.systems.arguments.OperaDesktopArguments;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OperaRunnerTest {

  private static OperaRunner runner;
  private OperaRunnerSettings settings;

  @Before
  public void beforeEach() {
    settings = new OperaRunnerSettings();
  }

  @Test
  public void testConstruction() {
    runner = new OperaRunner();
    List<OperaArgument> arguments = runner.settings.getArguments().getArguments();

    assertNotNull(runner);
    assertNotNull(runner.settings.getProfile());
    assertEquals("autotestmode", arguments.get(0).getArgument());
    assertEquals("pd", arguments.get(1).getArgument());
    assertEquals("debugproxy", arguments.get(2).getArgument());
    assertTrue(Integer.valueOf(runner.settings.getArguments().getArguments().get(2).getValue().split(":")[1]) > 0);
  }

  @Test
  public void testConstructionWithProductCore() {
    runner = null;  // Cleaning up from previous test

    settings.setProduct("core-gogi");
    runner = new OperaRunner(settings);
    assertNotNull(runner);
    assertTrue(runner.settings.getArguments() instanceof OperaCoreArguments);
  }

  @Test
  public void testConstructionWithProductDesktop() {
    runner = null;  // Cleaning up from previous test

    settings.setProduct("desktop");
    runner = new OperaRunner(settings);
    assertNotNull(runner);
    assertTrue(runner.settings.getArguments() instanceof OperaDesktopArguments);
  }

  @Test
  public void testConstructionWithProfile() {
    runner = null;  // Cleaning up from previous test

    String profile = "/my/profile";
    settings.setProfile(profile);
    runner = new OperaRunner(settings);
    assertNotNull(runner);
    assertEquals(profile, runner.settings.getArguments().getArguments().get(1).getValue());
  }

  @Test
  public void testConstructionWithPort() {
    runner = null;  // Cleaning up from previous test

    settings.setPort(0);
    runner = new OperaRunner(settings);
    assertNotNull(runner);
    assertTrue(Integer.valueOf(runner.settings.getArguments().getArguments().get(2).getValue().split(":")[1]) > 0);
  }

  @Test
  public void testConstructionWithArguments() {
    runner = null;  // Cleaning up from previous test

    OperaArguments arguments = new OperaArguments();
    arguments.add("foo");
    arguments.add("bar", "bah");
    arguments.add("baz", "abc");

    settings.setArguments(arguments);
    runner = new OperaRunner(settings);
    assertNotNull(runner);
    assertTrue(runner.settings.getArguments().getArguments().containsAll(arguments.getArguments()));
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

}