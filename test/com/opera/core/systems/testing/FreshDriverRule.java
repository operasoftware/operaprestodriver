package com.opera.core.systems.testing;

//import static com.opera.core.systems.testing.OperaDriverTestCase.driver;

/*
public final class FreshDriverRule implements TestRule {

  public Statement apply(Statement statement, Description description) {
    if (driver != null && driver.isRunning()) {
      driver.quit();
    }

    driver = (TestOperaDriver) new TestDriverBuilder(new TestOperaDriverSupplier()).get();

    return statement;
  }

}
*/