/*
Copyright 2010 WebDriver committers
Copyright 2010 Google Inc.
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

import org.openqa.selenium.WebDriverException;

import java.io.File;
import java.io.FileNotFoundException;

public class InProject {

  /**
   * Locates a file in the current project.
   *
   * @param path complete/full path to locate from the root of the project
   * @return file being sought, if it exists
   * @throws WebDriverException wrapped FileNotFoundException if file could not be found
   */
  public static File locate(String path) {
    File dir = new File(".").getAbsoluteFile();
    while (dir != null) {
      File needle = new File(dir, path);
      if (needle.exists()) {
        return needle;
      }
      dir = dir.getParentFile();
    }

    throw new WebDriverException(new FileNotFoundException(
        "Could not find " + path + " in the project"));
  }

}