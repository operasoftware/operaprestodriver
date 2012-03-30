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

import org.openqa.selenium.Platform;

import java.io.File;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class Resources {

  private final File resourcesDirectory = InProject.locate("test/resources");
  private final File executableBinary;
  private final File fakeFile = new File("does/not/exist");

  public Resources() {
    if (Platform.getCurrent().is(Platform.WINDOWS)) {
      executableBinary = new File("C:\\WINDOWS\\system32\\find.exe");
    } else {
      executableBinary = new File("/bin/echo");
    }
  }

  public File locate(String filename) {
    return new File(resourcesDirectory.getAbsolutePath() + File.separator + filename);
  }

  public File executableBinary() {
    return executableBinary;
  }

  public File fakeFile() {
    return fakeFile;
  }

}