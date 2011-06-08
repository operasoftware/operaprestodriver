/*
Copyright 2008-2011 Opera Software ASA

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
package com.opera.core.systems.scope.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.model.Canvas;
import com.opera.core.systems.model.ScopeActions;
import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.scope.internal.OperaMouseKeys;

/**
 * This library handles actions for Core 2.5+
 *
 * Core 2.5 uses a different (binary) protocol and different serializers
 *
 * @author Deniz Turkoglu <denizt@opera.com>
 *
 */

// TODO Support for < Core 2.5 is deprecated, merge

public class PbActionHandler extends ScopeActions {

  public PbActionHandler(ScopeServices services) {
    super(services);
  }

  @Override
  public void click(WebElement element, String locator) {
    Point point = element.getLocation();
    services.getExec().mouseAction(point.x + 1, point.y + 1,
        OperaMouseKeys.LEFT);
  }

  @Override
  public void click(WebElement element, int x, int y) {
    Point point = element.getLocation();
    services.getExec().mouseAction(point.x + x, point.y + y,
        OperaMouseKeys.LEFT);
  }

  @Override
  public void get(String url) {
    services.getExec().action("Go", url);
  }

  @Override
  public void rightClick(WebElement element) {
    Point point = element.getLocation();
    services.getExec().mouseAction(point.x, point.y, OperaMouseKeys.RIGHT);
  }

  @Override
  public void saveScreenshot(File pngFile) {
    if (pngFile == null) {
      throw new IllegalArgumentException(
          "Method parameter pngFile must not be null");
    }

    File dir = pngFile.getParentFile();
    if (!dir.exists() && !dir.mkdirs()) {
      throw new WebDriverException("Could not create directory "
          + dir.getAbsolutePath());
    }

    Canvas canvas = new Canvas();
    canvas.setX(0);
    canvas.setY(0);

    String[] dimensions = scriptDebugger.executeJavascript(
        "return (window.innerWidth + \",\" + window.innerHeight);").split(",");
    canvas.setHeight(Integer.valueOf(dimensions[1]));
    canvas.setWidth(Integer.valueOf(dimensions[0]));
    canvas.setViewPortRelative(true);

    ScreenShotReply reply = services.getExec().screenWatcher(canvas, 1l, true);
    FileOutputStream stream;
    try {
      stream = new FileOutputStream(pngFile.getAbsolutePath());
      stream.write(reply.getPng());
      stream.close();
    } catch (FileNotFoundException e) {
      // ignore
    } catch (IOException e) {
      // TODO log
    }
  }
}
