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

import org.junit.Test;
import org.openqa.selenium.io.FileHandler;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OperaPathsTest {

  // TODO(andreastt): Abstract operaPath() so that we can provide OS to it for more testing

  @Test
  public void operaPathIsNotNull() {
    assertNotNull(OperaPaths.operaPath());
  }

  @Test
  public void operaPathExists() {
    assertTrue(new File(OperaPaths.operaPath()).exists());
  }

  @Test
  public void operaPathIsExecutable() {
    assertTrue(FileHandler.canExecute(new File(OperaPaths.operaPath())));
  }

  // TODO(andreastt): Add tests for environmental variables in operaPath()

  // TODO(andreastt): Add tests for OperaPaths.isPathValid()

}