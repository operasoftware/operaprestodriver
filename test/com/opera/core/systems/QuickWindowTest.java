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

package com.opera.core.systems;

import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.OperaDesktopDriverTestCase;

import org.junit.Test;

import java.util.List;

import static com.opera.core.systems.OperaProduct.CORE;
import static com.opera.core.systems.OperaProduct.MINI;
import static com.opera.core.systems.OperaProduct.MOBILE;
import static com.opera.core.systems.OperaProduct.SDK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore(products = {CORE, MINI, MOBILE, SDK})
public class QuickWindowTest extends OperaDesktopDriverTestCase {

  @Test
  public void activeQuickWindowID() {
    assertTrue("getActiveQuickWindowID() should be greater than 0",
               driver.getActiveQuickWindowID() > 0);
  }

  @Test
  public void windowCount() {
    assertTrue("getQuickWindowCount() should be greater than 0",
               driver.getQuickWindowCount() > 0);
  }

  @Test
  public void windowListSize() {
    assertEquals(driver.getQuickWindowCount(), driver.getQuickWindowList().size());
  }

  @Test
  public void windowName() {
    for (QuickWindow window : driver.getQuickWindowList()) {
      assertEquals(window.getName(), driver.getQuickWindowName(window.getWindowID()));
    }
  }

  @Test
  public void findWindowById() {
    List<QuickWindow> quickWindowList = driver.getQuickWindowList();

    for (QuickWindow w : quickWindowList) {
      QuickWindow windowById = driver.findWindowById(w.getWindowID());

      assertEquals(w.getName(), windowById.getName());
      assertEquals(w.getWindowID(), windowById.getWindowID());
      assertEquals(w.getTitle(), windowById.getTitle());
    }
  }

}