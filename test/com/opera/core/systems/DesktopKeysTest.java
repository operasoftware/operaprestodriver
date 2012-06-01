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

import com.google.common.collect.ImmutableList;

import com.opera.core.systems.scope.protos.SystemInputProtos;
import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.OperaDesktopDriverTestCase;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.opera.core.systems.OperaProduct.CORE;
import static com.opera.core.systems.OperaProduct.MINI;
import static com.opera.core.systems.OperaProduct.MOBILE;
import static com.opera.core.systems.OperaProduct.SDK;
import static org.junit.Assert.assertEquals;

@Ignore(products = {CORE, MINI, MOBILE, SDK})
public class DesktopKeysTest extends OperaDesktopDriverTestCase {

  public final List<SystemInputProtos.ModifierPressed> ctrlModifier =
      ImmutableList.of(SystemInputProtos.ModifierPressed.CTRL);
  public int windowCountBefore;

  @Before
  public void beforeEach() {
    windowCountBefore = driver.getQuickWindowCount();
  }

  @Test
  public void keyPress() {
    driver.keyPress("t", ctrlModifier);
    driver.waitForWindowActivated();
    assertEquals(windowCountBefore + 1, driver.getQuickWindowCount());
  }

  @Test
  public void keyDownUp() {
    driver.keyDown("w", ctrlModifier);
    driver.keyUp("w", ctrlModifier);
    assertEquals(windowCountBefore - 1, driver.getQuickWindowCount());
  }

}