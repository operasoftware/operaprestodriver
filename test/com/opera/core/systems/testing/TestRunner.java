/*
Copyright 2011-2012 Opera Software ASA
Copyright 2011 Software Freedom Conservancy
Copyright 2011 WebDriver committers

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

package com.opera.core.systems.testing;

import com.google.common.base.Throwables;

import com.opera.core.systems.OperaProduct;
import com.opera.core.systems.OperaSettings;
import com.opera.core.systems.scope.ScopeService;
import com.opera.core.systems.testing.drivers.TestDriver;

import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.openqa.selenium.Platform;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.logging.Level;

/**
 * Custom JUnit test runner that allows test igorance based on both test classes and methods based
 * on given conditions.
 */
public class TestRunner extends BlockJUnit4ClassRunner {

  private final TestIgnorance ignorance;

  private Object test;
  private Throwable testException;

  /**
   * Creates a {@link BlockJUnit4ClassRunner} to run {@code testClass}.
   *
   * @param testClass the class under test
   * @throws InitializationError if the test class is malformed
   */
  public TestRunner(Class<?> testClass) throws InitializationError {
    super(testClass);

    try {
      test = new ReflectiveCallable() {
        @Override
        protected Object runReflectiveCall() throws Throwable {
          return createTest();
        }
      }.run();
    } catch (Throwable e) {
      testException = e;
    }

    if (test instanceof TestCase) {
      TestCase base = (TestCase) test;
      TestDriver driver = base.getWrappedDriver();

      // If any driver is present we don't need a different supplier's driver to initialize
      // TestIgnorance.  This skips the supplies(klass) check in createDriverIfNecessary().
      if (driver == null || !driver.isRunning()) {
        base.createDriverIfNecessary();
        driver = base.getWrappedDriver();
      }

      ignorance = new TestIgnorance(driver.getServices().getAvailableServices(),
                                    driver.getRunner() != null,
                                    driver.getServices().isOperaIdleAvailable(),
                                    driver.utils().getPlatform(),
                                    driver.utils().getProduct());
    } else {
      ignorance = new TestIgnorance(new LinkedHashMap<ScopeService, String>(),
                                    false,
                                    false,
                                    Platform.getCurrent(),
                                    OperaProduct.ALL);
    }
  }

  @Override
  protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
    Description description = describeChild(method);

    if (test == null) {
      runLeaf(new Fail(testException), description, notifier);
    }

    // There is a difference between ignore annotations and annotations performing an action.
    // Ignore annotations are handled by the TestIgnorance, while other annotations will be handled
    // by this file in methodBlock().
    if (ignorance.isIgnored(method, test)) {
      notifier.fireTestIgnored(description);
    } else {
      runLeaf(methodBlock(method, test), description, notifier);
    }
  }

  @SuppressWarnings("deprecation")
  private Statement methodBlock(FrameworkMethod method, Object test) {
    Statement statement = methodInvoker(method, test);

    statement = possiblyExpectingExceptions(method, test, statement);
    statement = withPotentialTimeout(method, test, statement);
    statement = withBefores(method, test, statement);
    statement = withAfters(method, test, statement);
    statement = withRules(method, test, statement);

    if (test instanceof OperaDriverTestCase) {
      OperaDriverTestCase base = (OperaDriverTestCase) test;
      statement = withNoDriver(base, statement);
      statement = withNoDriverAfterTest(method, base, statement);
      statement = withFreshDriver(method, base, statement);
      statement = withSettings(base, statement);
    }

    return statement;
  }

  private Statement withNoDriver(final TestCase test, final Statement statement) {
    final NoDriver annotation = test.getClass().getAnnotation(NoDriver.class);

    if (annotation == null) {
      test.setCreateDriver(true);
      return statement;
    }

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        test.setCreateDriver(false);
        AbstractTestCase.removeDriver();
        statement.evaluate();
      }
    };
  }

  private Statement withNoDriverAfterTest(FrameworkMethod method, final TestCase test,
                                          final Statement statement) {
    NoDriverAfter annotation = method.getAnnotation(NoDriverAfter.class);
    if (annotation == null) {
      return statement;
    }

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          statement.evaluate();
        } finally {
          AbstractTestCase.removeDriver();
        }
      }
    };
  }

  private Statement withFreshDriver(FrameworkMethod method, final TestCase test,
                                    final Statement statement) {
    FreshDriver annotation = method.getAnnotation(FreshDriver.class);

    if (annotation == null) {
      return statement;
    }

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        test.setSettings(new OperaSettings());
        AbstractTestCase.removeDriver();
        test.createDriverIfNecessary();
        statement.evaluate();
      }
    };
  }

  private Statement withSettings(final TestCase test, final Statement statement) {
    final Settings annotation = test.getClass().getAnnotation(Settings.class);

    if (annotation == null) {
      return statement;
    }

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        OperaSettings currentSettings = test.getSettings();

        OperaSettings newSettings = new OperaSettings();
        newSettings.setIdle(annotation.idle());
        newSettings.logging().setLevel(Level.parse(annotation.logLevel().toString()));

        // We only need to restart the driver if the settings are different
        if (currentSettings.useIdle() != newSettings.useIdle() ||
            currentSettings.logging().getLevel() != newSettings.logging().getLevel()) {
          test.setSettings(newSettings);
          AbstractTestCase.removeDriver();
        }

        statement.evaluate();
      }
    };
  }

  private Statement withRules(FrameworkMethod method, Object target, Statement statement) {
    try {
      Method withRules = BlockJUnit4ClassRunner.class.getDeclaredMethod(
          "withRules", FrameworkMethod.class, Object.class, Statement.class);
      withRules.setAccessible(true);

      return (Statement) withRules.invoke(this, method, target, statement);
    } catch (NoSuchMethodException e) {
      throw Throwables.propagate(e);
    } catch (InvocationTargetException e) {
      throw Throwables.propagate(e);
    } catch (IllegalAccessException e) {
      throw Throwables.propagate(e);
    }
  }

}