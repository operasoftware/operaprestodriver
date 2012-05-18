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

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OperaDesktopDriverStartStopStressTest extends OperaDesktopDriverTestCase {

  /**
   * This test is supposed to stress test starting/stopping of Opera.
   * This has known to be a problem with the desktop Opera due to unpredictable startup/shutdown times,
   * different computer speeds, crashes and not the best synchronization between the driver and the launcher,
   * that may depend on some sleep calls.
   *
   * NOTE: This test is known to fail you and this problem needs to be approached by the desktop. This test should
   * be ignored until the problem is fixed.
   */
  @Test
  @Ignore
  public void testStartQuitOpera() throws IOException, InterruptedException {
    setOperaPath();
    driver = new TestOperaDesktopDriver(getDefaultCaps());

    browserPid = driver.getPid();
    assertTrue("PID reported by browser should > 0", browserPid > 0);
    assertTrue("Driver doesn't report the Opera as running even thou it should.", driver.isOperaRunning());
    assertTrue("The browser process should BE running.", isPidRunning(browserPid));

    for (int i = 0; i < 5; i ++)
    {
      driver.quitOpera();
      assertFalse("Driver doesn't report the Opera as stopped even thou it should.", driver.isOperaRunning());
      assertFalse("The browser process should NOT be running.", isPidRunning(browserPid));

      driver.resetOperaPrefs("");
      browserPid = driver.getPid();
      assertTrue("PID reported by browser should > 0", browserPid > 0);
      assertTrue("Driver doesn't report the Opera as running even thou it should.", driver.isOperaRunning());
      assertTrue("The browser process should BE running.", isPidRunning(browserPid));
    }

    driver.quit();
  }
}
