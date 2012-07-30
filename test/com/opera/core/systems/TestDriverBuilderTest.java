package com.opera.core.systems;

import com.opera.core.systems.testing.NoDriver;
import com.opera.core.systems.testing.OperaDriverTestCase;
import com.opera.core.systems.testing.drivers.TestDriverBuilder;
import com.opera.core.systems.testing.drivers.TestOperaDesktopDriverSupplier;
import com.opera.core.systems.testing.drivers.TestOperaDriverSupplier;
import com.opera.core.systems.testing.drivers.TestOperaMobileDriverSupplier;

import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.Level;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@NoDriver
public class TestDriverBuilderTest extends OperaDriverTestCase {

  public TestDriverBuilder builder;

  @After
  public void dereference() {
    builder = null;
  }

  @After
  public void resetTestProduct() {
    setTestProduct(null);
  }

  @Test
  public void testProductOptionKey() {
    assertEquals("selenium.browser.product", TestDriverBuilder.TEST_PRODUCT_OPTION_KEY);
  }

  @Test
  public void detectProduct() {
    assertThat(TestDriverBuilder.detect(), is(instanceOf(TestOperaDriverSupplier.class)));
  }

  @Test
  public void detectDesktop() {
    setTestProduct(OperaProduct.DESKTOP);
    assertThat(TestDriverBuilder.detect(), is(instanceOf(TestOperaDesktopDriverSupplier.class)));
  }

  @Test
  public void detectMobile() {
    setTestProduct(OperaProduct.MOBILE);
    assertThat(TestDriverBuilder.detect(), is(instanceOf(TestOperaMobileDriverSupplier.class)));
  }

  @Test
  public void detectCore() {
    setTestProduct(OperaProduct.CORE);
    assertThat(TestDriverBuilder.detect(), is(instanceOf(TestOperaDriverSupplier.class)));
  }

  @Test
  public void testProductOption() throws IOException {
    System.setProperty(TestDriverBuilder.TEST_PRODUCT_OPTION_KEY,
                       OperaProduct.MOBILE.getDescriptionString());
    driver = new TestDriverBuilder().get();
    assertEquals(OperaProduct.MOBILE, driver.utils().getProduct());
    assertEquals(OperaProduct.MOBILE, driver.getSettings().getProduct());
  }

  @Test
  public void modifyLogLevel() {
    driver = new TestDriverBuilder().modifyLogLevel(Level.CONFIG).get();
    assertEquals(Level.CONFIG, driver.getSettings().logging().getLevel());
  }

  // TODO(andreastt): using(settings)

  // TODO(andreastt): using(capabilities)

  private void setTestProduct(OperaProduct product) {
    if (product != null) {
      System.setProperty(TestDriverBuilder.TEST_PRODUCT_OPTION_KEY, product.getDescriptionString());
    } else {
      System.clearProperty(TestDriverBuilder.TEST_PRODUCT_OPTION_KEY);
    }
  }

}