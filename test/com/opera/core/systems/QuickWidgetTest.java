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
import static com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo.QuickWidgetType.BUTTON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * The tests assume the there is a following widget available after starting Opera with a clean
 * profile:
 *
 * <pre>
 * Name: tbb_Stop_Reload
 * Type: BUTTON
 * Text: Stop Visible: true
 * </pre>
 */
@Ignore(products = {CORE, MINI, MOBILE, SDK})
public class QuickWidgetTest extends OperaDesktopDriverTestCase {

  @Test
  public void findByName() {
    QuickWidget widget = driver.findWidgetByName(BUTTON,
                                                 driver.getActiveQuickWindowID(),
                                                 "tbb_Stop_Reload");
    assertNotNull(widget);
    assertEquals("Stop", widget.getText());
  }

  @Test
  public void findByText() {
    QuickWidget widget = driver.findWidgetByText(BUTTON,
                                                 driver.getActiveQuickWindowID(),
                                                 "Stop");
    assertNotNull(widget);
    assertEquals("tbb_Stop_Reload", widget.getName());
  }

  @Test
  public void findByStringId() {
    QuickWidget widget = driver.findWidgetByStringId(BUTTON,
                                                     driver.getActiveQuickWindowID(),
                                                     "SI_STOP_BUTTON_TEXT");
    assertNotNull(widget);
    assertEquals("tbb_Stop_Reload", widget.getName());
  }

  @Test
  @Ignore
  public void findByPosition() {
    // TODO
  }

  @Test
  public void listSize() {
    assertTrue("Expected there to be 1 or more widgets",
               driver.getQuickWidgetList(driver.getActiveQuickWindowID()).size() > 0);
  }

  @Test
  //@Ignore
  // TODO(andreastt): Wouldn't it be better to match two lists here?
  public void getQuickWidgetListHasSameInformationAsFindWidgetByName() {
    Integer windowID = driver.getActiveQuickWindowID();
    List<QuickWidget> quickWidgetList = driver.getQuickWidgetList(windowID);

    for (QuickWidget quickWidget: quickWidgetList) {
      String quickWidgetName = quickWidget.getName();

      if (quickWidgetName.isEmpty()) {
        QuickWidget reference = driver.findWidgetByName(quickWidget.getType(),
                                                        windowID,
                                                        quickWidgetName);

        assertNotNull(reference);
        assertEquals(reference.toFullString(), quickWidget.toFullString());
      }
    }
  }

}