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

package com.opera.core.systems;

import com.opera.core.systems.pages.IFramePage;
import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.PageFactory;

import static org.junit.Assert.assertNotNull;

public class IFramesTest extends OperaDriverTestCase {
  IFramePage iFramePage;
  @Before
  public void bindPage() {
    getFixture("iframes.html");
    iFramePage = PageFactory.initElements(driver, IFramePage.class);
    iFramePage.driver = driver;
  }

  @Test
  public void testIFrameFocus() {
    assertNotNull(iFramePage.getIFrame());
    iFramePage.leaveIFrame();
  }
}
