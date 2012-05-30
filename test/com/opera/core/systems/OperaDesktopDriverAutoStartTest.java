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

/*
public class OperaDesktopDriverAutoStartTest extends OperaDesktopDriverTestCase {

  /**
   * Check that the driver reports Opera running after starting te driver with the default capabilities as
   * set by desktopwatir.
   *
  @Test
  public void testBrowserStarts() {
    setOperaPath();
    DesiredCapabilities caps = getDefaultCaps();
    driver = new TestOperaDesktopDriver(caps);
    browserPid = driver.getPid();

    assertTrue("Driver doesn't report the Opera as running even thou it should.", driver.isOperaRunning());
  }

  /**
   * Check that the PID reported by the driver looks fine.
   *
  @Test
  public void testPidOK() {
    assertTrue("PID reported by browser should > 0", browserPid > 0);
  }

  /**
   * Check that the PID reported by the driver refers to an actually running process.
   * @throws java.io.IOException
   * @throws InterruptedException
   *
  @Test
  public void testPidRunningOK() throws IOException, InterruptedException {
    Integer browserPid = driver.getPid();

    assertTrue("The browser process should BE running.", isPidRunning(browserPid));
  }

  /**
   * Check that the path reported by the driver is the same as the path that we set for
   * the OPERA_PATH environment variable override.
   *
   * NOTE: This test is known to fail if the OPERA_PATH path points to a shell script that
   * starts the actual Opera binary. This is what happens on Linux Debian and there is not
   * much we can do about, we'd have to parse the shell script to get the target path.
   * It was not verified how the test behaves with symlinks.
   * @throws IOException
   *
  @Test
  public void testOperaPathReportedOK() throws IOException {
    String readPath = driver.getOperaPath();
    File operaPathFile = new File(operaPath).getCanonicalFile();
    File readFile = new File(readPath).getCanonicalFile();

    assertEquals(operaPathFile, readFile);
  }

  /**
   * Check that the capabilities reported by the driver look as we expect.
   * The actual values may probably change without hurting the general driver
   * functionality, however this test will detect any changes and allow
   * inspecting them.
   *
  @Test
  public void testCapsOK() {
    Map<String, String> expectedValues = new HashMap<String, String>();
    expectedValues.put("opera.binary", operaPath);
    expectedValues.put("javascriptEnabled", "true");
    expectedValues.put("opera.host", "127.0.0.1");
    expectedValues.put("opera.idle", "false");
    expectedValues.put("browserName", "opera");
    expectedValues.put("opera.autostart", "true");
    expectedValues.put("opera.logging.file", "(null)");
    expectedValues.put("opera.guess_binary_path", "false");
    expectedValues.put("opera.logging.level", "INFO");
    expectedValues.put("opera.launcher", "(null)");
    expectedValues.put("opera.display", "(null)");
    expectedValues.put("opera.product", "desktop");
    expectedValues.put("opera.arguments", "");
    expectedValues.put("opera.no_quit", "false");
    expectedValues.put("opera.no_restart", "false");

    checkCaps(driver.getCapabilities(), expectedValues);
  }

  /**
   * Check that the PID reported by the browser refers to a *not* running process
   * after we quit the driver.
   * This test verifies that having the 'opera.no_quit' capability value set to the
   * default one set by desktopwatir (i.e. false) causes the Opera process to exit
   * after the driver has been quit.
   * @throws IOException
   * @throws InterruptedException
   *
  @Test
  public void testBrowserQuitsOK() throws IOException, InterruptedException {
    Integer browserPid = driver.getPid();
    driver.quit();
    assertFalse("The browser process should NOT be running.", isPidRunning(browserPid));
  }

}
         */