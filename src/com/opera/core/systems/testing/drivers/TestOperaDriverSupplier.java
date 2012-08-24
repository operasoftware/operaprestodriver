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

package com.opera.core.systems.testing.drivers;

import org.openqa.selenium.remote.LocalFileDetector;

import java.io.File;

public class TestOperaDriverSupplier extends AbstractTestDriverSupplier {

  public TestOperaDriver get() {
    // Uncomment this if you wish to connect Opera manually:
    //settings.autostart(false);

    settings.setBinary(new File("/home/andreastt/operas/opera-core-integration-242-buildsetid_40299-core_2_11-lingogi64/profiles/release_desktop/lingogi_release_desktop"));
    TestOperaDriver driver = new TestOperaDriver(settings);
    driver.setFileDetector(new LocalFileDetector());
    return driver;
  }

  public boolean supplies(Class<? extends TestDriver> klass) {
    return TestOperaDriver.class.equals(klass);
  }

}