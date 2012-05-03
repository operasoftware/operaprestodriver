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

import com.opera.core.systems.OperaSettings;

import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class OperaDriverTestRunner extends BlockJUnit4ClassRunner {

  private final TestIgnorance ignorance;

  public OperaDriverTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
    ignorance = new TestIgnorance(OperaDriverTestCase.currentProduct(),
                                  OperaDriverTestCase.currentPlatform());
  }

  @Override
  protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
    Object test = null;
    Description description = describeChild(method);

    try {
      test = new ReflectiveCallable() {
        @Override
        protected Object runReflectiveCall() throws Throwable {
          return createTest();
        }
      }.run();
    } catch (Throwable e) {
      runLeaf(new Fail(e), description, notifier);
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
      statement = withFreshDriver(method, base, statement);
      statement = withSettings(base, statement);
    }

    return statement;
  }

  private Statement withNoDriver(OperaDriverTestCase test, final Statement statement) {
    final NoDriver annotation = test.getClass().getAnnotation(NoDriver.class);

    if (annotation == null) {
      OperaDriverTestCase.setCreateDriver(true);
      return statement;
    }

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        OperaDriverTestCase.setCreateDriver(false);
        OperaDriverTestCase.removeDriver();
        statement.evaluate();
      }
    };
  }

  private Statement withFreshDriver(FrameworkMethod method, final OperaDriverTestCase test,
                                    final Statement statement) {
    FreshDriver annotation = method.getAnnotation(FreshDriver.class);
    if (annotation == null) {
      return statement;
    }

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        OperaDriverTestCase.setSettings(new OperaSettings());
        OperaDriverTestCase.removeDriver();
        test.createDriver();
        statement.evaluate();
      }
    };
  }

  private Statement withSettings(OperaDriverTestCase test, final Statement statement) {
    final Settings annotation = test.getClass().getAnnotation(Settings.class);

    if (annotation == null) {
      return statement;
    }

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        OperaSettings currentSettings = OperaDriverTestCase.getSettings();

        OperaSettings newSettings = new OperaSettings();
        newSettings.setIdle(annotation.idle());
        newSettings.logging().setLevel(Level.parse(annotation.logLevel().toString()));

        // We only need to restart the driver if the settings are different
        if (currentSettings.useIdle() != newSettings.useIdle() ||
            currentSettings.logging().getLevel() != newSettings.logging().getLevel()) {
          OperaDriverTestCase.setSettings(newSettings);
          OperaDriverTestCase.removeDriver();
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