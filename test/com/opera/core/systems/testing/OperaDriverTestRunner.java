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

package com.opera.core.systems.testing;

import com.opera.core.systems.OperaProduct;
import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.OperaDriverTestCase;
import com.opera.core.systems.testing.drivers.OperaDriverBuilder;
import com.opera.core.systems.testing.drivers.TestOperaDriver;
import com.opera.core.systems.testing.drivers.TestOperaDriverSupplier;

import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.openqa.selenium.Platform;

import static com.opera.core.systems.testing.OperaDriverTestCase.driver;

/**
 * The OperaDriverTestRunner can be used, amongst other things, for applying advanced ignores to
 * individual test cases (methods) or test suits (classes).
 *
 * Before a test run it will launch the default Opera using {@link com.opera.core.systems.OperaDriver} to determine the
 * current product's name.  You can then use <code>@Ignore(product = Product.CORE_SMARTPHONE)</code>
 * in your tests to ignore a certain product.  See {@link com.opera.core.systems.OperaProduct} for a full list of
 * products.
 *
 * You may also utilize Selenium's {@link Platform} enum to ignore platforms/operating systems.  To
 * combine this with ignoring a product, an ignore rule (annotation) might look like <code>@Ignore({
 * product = Product.CORE_SMARTPHONE, platform = Platform.LINUX })</code>.
 *
 * You can specify the current product manually by setting the <code>OPERA_PRODUCT</code>
 * environment variable to a value defined in {@link com.opera.core.systems.OperaProduct}.  This will skip the step of
 * creating a browser instance for determining the current product.
 *
 * To use this class, apply the <code>@RunWith(OperaDriverTestRunner.class)</code> annotation to
 * your class, or extend the {@link OperaDriverTestCase} class.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class OperaDriverTestRunner extends BlockJUnit4ClassRunner {

  public OperaDriverTestRunner(Class<? extends OperaDriverTestCase> klass) throws InitializationError {
    super(klass);
  }

  @Override
  protected void runChild(FrameworkMethod method, RunNotifier notifier) {
    EachTestNotifier eachNotifier = makeNotifier(method, notifier);

    // TODO(andreastt): Move annotations to separate classes

    if (method.getAnnotation(Ignore.class) != null) {
      Ignore customIgnore = method.getAnnotation(Ignore.class);

      if (shouldIgnore(customIgnore)) {
        runIgnored(eachNotifier);
        return;
      }
    }

    if (method.getAnnotation(FreshDriver.class) != null) {
      if (driver != null && driver.isRunning()) {
        driver.quit();
      }

      driver = (TestOperaDriver) new OperaDriverBuilder(new TestOperaDriverSupplier()).get();
    }

    runNotIgnored(method, eachNotifier);
  }

  /**
   * Determines whether a test should be ignored or not based on a JUnit ignore annotation rule. The
   * check for this is mutually exclusive, meaning that if <em>either</em> the product or the
   * platform is true, the test will be ignored.
   *
   * @param ignoreAnnotation a custom ignore annotation
   * @return true if test should be ignored, false otherwise
   */
  private boolean shouldIgnore(Ignore ignoreAnnotation) {
    if (ignoreAnnotation == null) {
      return false;
    }

    // If it's a plain old @Ignore without arguments
    if (isPlainIgnore(ignoreAnnotation)) {
      return true;
    }

    for (OperaProduct product : ignoreAnnotation.products()) {
      if (product.is(OperaProduct.ALL)) {
        break;
      } else if (product.is(OperaDriverTestCase.driver.utils().getProduct())) {
        return true;
      }
    }

    for (Platform platform : ignoreAnnotation.platforms()) {
      if (platform.is(Platform.ANY)) {
        // While ANY really should ignore any platform, ANY is also the default value for our custom
        // Ignore annotation, meaning it will ignore everything by default.  So this is an exception
        // from the rule.
        return false;
      } else if (platform.is(OperaDriverTestCase.driver.utils().getPlatform())) {
        return true;
      }
    }

    // Should not be ignored, none of the rules apply
    return false;
  }

  /**
   * Checks if provided ignore annotation is a plain old @Ignore without arguments.
   *
   * @param annotation the annotation to check
   * @return true if annotation is without arguments, false otherwise
   */
  private boolean isPlainIgnore(Ignore annotation) {
    return (annotation.products().length == 1 && annotation.products()[0].is(OperaProduct.ALL)) &&
        (annotation.platforms().length == 1 && annotation.platforms()[0].is(Platform.ANY));
  }

  // copy of BlockJUnit4ClassRunner.runNotIgnored()
  private void runNotIgnored(FrameworkMethod method, EachTestNotifier eachNotifier) {
    eachNotifier.fireTestStarted();

    try {
      methodBlock(method).evaluate();
    } catch (AssumptionViolatedException e) {
      eachNotifier.addFailedAssumption(e);
    } catch (Throwable e) {
      eachNotifier.addFailure(e);
    } finally {
      eachNotifier.fireTestFinished();
    }
  }

  // copy of BlockJUnit4ClassRunner.runIgnored()
  private void runIgnored(EachTestNotifier eachNotifier) {
    eachNotifier.fireTestIgnored();
  }

  // copy of BlockJUnit4ClassRunner.makeNotifier()
  private EachTestNotifier makeNotifier(FrameworkMethod method, RunNotifier notifier) {
    Description description = describeChild(method);
    return new EachTestNotifier(notifier, description);
  }

}