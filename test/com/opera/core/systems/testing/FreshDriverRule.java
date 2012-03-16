package com.opera.core.systems.testing;

import com.opera.core.systems.testing.drivers.OperaDriverBuilder;
import com.opera.core.systems.testing.drivers.TestOperaDriver;
import com.opera.core.systems.testing.drivers.TestOperaDriverSupplier;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static com.opera.core.systems.testing.OperaDriverTestCase.driver;

public final class FreshDriverRule implements TestRule {

  public Statement apply(Statement statement, Description description) {
    if (driver != null && driver.isRunning()) {
      driver.quit();
    }

    driver = (TestOperaDriver) new OperaDriverBuilder(new TestOperaDriverSupplier()).get();

    return statement;
  }

}