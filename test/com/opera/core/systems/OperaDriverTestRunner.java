/*
Copyright 2011 Opera Software ASA

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

import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.openqa.selenium.Platform;

/**
 * The OperaDriverTestRunner can be used, amongst other things, for applying advanced ignores to
 * individual test cases (methods) or test suits (classes).
 *
 * Before a test run it will launch the default Opera using {@link OperaDriver} to determine the
 * current product's name.  You can then use <code>@Ignore(product = Product.CORE_SMARTPHONE)</code>
 * in your tests to ignore a certain product.  See {@link OperaProduct} for a full list of products.
 *
 * You may also utilize Selenium's {@link Platform} enum to ignore platforms/operating systems.  To
 * combine this with ignoring a product, an ignore rule (annotation) might look like <code>@Ignore({
 * product = Product.CORE_SMARTPHONE, platform = Platform.LINUX })</code>.
 *
 * You can specify the current product manually by setting the <code>OPERA_PRODUCT</code>
 * environment variable to a value defined in {@link OperaProduct}.  This will skip the step of creating
 * a browser instance for determining the current product.
 *
 * To use this class, apply the <code>@RunWith(OperaDriverTestRunner.class)</code> annotation to
 * your class, or extend the {@link DriverTestCase} class.
 *
 * @see {@link OperaProduct}, {@link Platform}
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class OperaDriverTestRunner extends BlockJUnit4ClassRunner {

  /*
  private Product currentProduct = Product.CORE;
  private Platform currentPlatform = Platform.getCurrent();
  */

  public OperaDriverTestRunner(Class<?> klass) throws InitializationError {
    super(klass);

    /*
    String requestedProduct = System.getenv("OPERA_PRODUCT");
    if (requestedProduct == null || requestedProduct.isEmpty()) {
      OperaDriver driver = new OperaDriver();
      requestedProduct = driver.utils().getProduct();
      driver.quit();
    }

    // TODO(andreastt): core builds currently only return "core-gogi", not including the profile name
    if (requestedProduct != null && !requestedProduct.isEmpty()) {
      try {
        currentProduct = Product.valueOf(requestedProduct);
      } catch (IllegalArgumentException e) {
        // product not found
      }
    }
    */
  }

  @Override
  protected void runChild(FrameworkMethod method, RunNotifier notifier) {
    EachTestNotifier eachNotifier = makeNotifier(method, notifier);

    if (method.getAnnotation(Ignore.class) != null) {
      Ignore customIgnore = method.getAnnotation(Ignore.class);

      if (shouldIgnore(customIgnore)) {
        runIgnored(eachNotifier);
        return;
      }
    }

    runNotIgnored(method, eachNotifier);
  }

  /**
   * Determines whether a test should be ignored or not based on a JUnit ignore annotation rule.
   * The check for this is mutually exclusive, meaning that if <em>either</em> the product or the
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
    if (ignoreAnnotation.products().length == 0 && ignoreAnnotation.platforms().length == 0) {
      return true;
    }

    for (OperaProduct product : ignoreAnnotation.products()) {
      if (OperaDriverTestCase.currentProduct.is(product)) {
        return true;
      }
    }

    for (Platform platform : ignoreAnnotation.platforms()) {
      if (OperaDriverTestCase.currentPlatform.is(platform)) {
        return true;
      }
    }

    return false;
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