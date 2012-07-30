/*
Copyright 2012 Opera Software ASA

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

import com.opera.core.systems.testing.drivers.TestDriver;

import org.openqa.selenium.support.PageFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

public class TestingPageFactory extends PageFactory {

  private static final Logger logger = Logger.getLogger(TestingPageFactory.class.getName());

  public static <T> T initElements(TestDriver driver, Pages pages, Class<T> pageClassToProxy) {
    T page = instantiatePage(driver, pages, pageClassToProxy);
    initElements(driver, page);
    return page;
  }

  private static <T> T instantiatePage(TestDriver driver, Pages pages, Class<T> pageClassToProxy) {
    try {
      Constructor<T> constructor = null;
      try {
        constructor = pageClassToProxy.getConstructor(TestDriver.class, pages.getClass());
        return constructor.newInstance(driver, pages);
      } catch (NoSuchMethodException e) {
        logger.warning(String.format("Unable to find constructor: %s", constructor));
        return pageClassToProxy.newInstance();
      }
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getTargetException());
    }
  }

}