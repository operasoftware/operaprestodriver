2011/09/06 OperaDriver 0.7.1
============================

API changes
-----------

 * Remove deprecated method OperaDriver.stop(). (andreastt)
 * Remove deprecated method OperaDriver.closeAll(). (andreastt)
 * Remove OperaDriver.convertByToAtom() as the WebDriver atoms now support
   getting locators by nice wire strings. (andreastt)
 * Remove OperaDriver.getProduct(), .getOS(), .getBinaryPath(),
   .getCoreVersion(), .getUserAgent() and .getPID() and replace with
   OperaDriver.utils().*  (andreastt)

Bug fixes
---------

 * Make OperaWebElement.sendKeys enter text at the end when an input has
   default text. (stuartk)
 * Clear Should only clear editable and enabled elements. (ajayk)
 * Call services.shutdown(), as .quit() will also shut down Opera even if
   'opera.no_quit' is set to true. (andreastt)

New features
------------

 * Add backwards-compat support for Window Manager 2.0 (for Opera Mobile).
   (stuartk)

Other
-----

 * Cleaning up logging in OperaLauncherRunner, OperaLauncherBinary,
   OperaLauncherProtocol, StpConnection, ScopeServices (andreastt)
 * Documenation and formatting fixes. (andreastt)
 * Updated the atoms. (andreastt)


2011/09/02 OperaDriver 0.7
==========================

API changes
-----------

 * Support the use of DesiredCapabilities, instead of OperaDriverSettings.
   OperaDriverSettings is still supported for backward compatibility, but
   will be removed in the next point version (0.8). (stuartk)
 * Update Selenium jar to 2.5.0.
 * Remove OperaDriver.mouseEvent() method. Use the advanced interaction API
   instead. Update OperaExec to allow several mouse keys to be pressed at once.
   (andreastt)
 * Updated SocketMonitor to not be a singleton, so that connections to
   multiple instances of Opera can be maintained. (stuartk)
 * Renamed OperaPaths.getOperaLauncherPath() to extractLauncher(path) to better match
   what the method is actually doing. (andreastt)
 * Remove OperaDriver.shutdown(). (stuartk)

New features
------------

 * Support DesiredCapabilities. Capabilities include "opera.binary" and
   "opera.arguments" amongst others. See the top of the OperaDriver class
   for all of them. (stuartk)
 * Support the "personal directory", `-pd`, flag with the "opera.profile"
   capability. Currently only available on Linux Opera builds.
 * Add "opera.port" capability to set the port that Opera communicates with
   driver on. Allows multiple instances of Opera to be run at once. Requires
   the `-debugproxy` flag, which is not yet available in current versions
   of Opera. (stuartk)
 * Add capability to set the logging level and file with "opera.logging.level"
   and "opera.logging.file". (andreastt)
 * Updated Linux launcher to support the `-profile` argument, to specify
   which build profile we are using (Note: this ISN'T the Opera user profile).
   (Iaroslav Tymchenko)
 * Display the command used to start Opera when it exits immediately (usually
   due to bad command line arguments). (stuartk)

Bug fixes
---------

 * Correct LEFT_CONTROL enum in OperaKeys to make code such as
   `action.sendKeys(Keys.CONTROL + "c")` work. (stuartk)
 * Escape double quotes instead of single quotes in findElementsBy* methods.
   Fixes the problems with pre-escaped strings sent from selenium-webdriver.
   (andreastt)
 * Fix for bug DSK-344905; Opera should not quit after testrun with noquit
   option (Karianne Ekern)
 * Wait between two double clicks so that Opera doesn't interpret it as as a
   quadruple-click. (andreastt)
 * Throw InvalidSelectorException when selectors are invalid. (stuartk)
 * Use OpenURL command in new Window Manager service. Fixes case where loading
   a page in a new window would cause a ResponseNotReceivedException. (stuartk)
 * Don't timeout when a window is closed by Javascript. (stuartk)
 * Check services isn't null before shutting it down. (stuartk)

Tests
-----

 * Add tests for modifier keys. (stuartk)
 * Ignore some launcher tests on Windows. (stuartk)
 * Skip selftest tests when service isn't supported. (stuartk)
 * Add tests for Profile and Port capabilities. (stuartk)

Other
-----

 * Update build.xml to target Java 5/1.5. (stuartk)
 * Compile tests to the bin/ directory, rather than the tests/ directory.
   (andreastt)
 * Reduce the logging level from FINE to FINER or FINEST for noisy log
   messages. (stuartk)
 * Update Window Manager service from 2.0 to 2.1. (stuartk)


2011/08/29 OperaDriver 0.6.1
============================

API changes
-----------

 * Remove OperaDriver.mouseEvent(). (andreastt)
 * Switch order and type of arguments to OperaExec.mouseAction(). (andreastt)
 * Update Selenium jar to r13628, to include double-click fix in r13592.
   (andreastt)

Bug fixes
---------

 * Wait in between sending double-click events so that Opera does not think
   that it is a quadruple-click. (andreastt)

Tests
-----

 * Fix tests to not use OperaDriver.mouseEvent(). (andreastt)


2011/08/03 OperaDriver 0.6
==========================

API changes
-----------

 * Add complete support for the Advanced User Interactions API
   http://code.google.com/p/selenium/wiki/AdvancedUserInteractions
   * OperaWebElement.getLocation() now returns the location in the DOM, rather
     than in the viewport. To get viewport location use
     `getCoordinates().getLocationInViewPort()`.
   (andreastt, stuartk)
 * Use Selenium atoms (see below).
 * Update Selenium jar to 2.3.0.


New features
------------

 * Added ability to navigate menus in Opera Desktop. (Karianne Ekern,
   Adam Minchinton)
 * Add selftest service. (Arne Skjærholt)
 * Use atoms from the Selenium project instead of our own custom JS for:
   * Finding elements from OperaDriver and OperaWebElement
   * Getting attributes, style, location and size of OperaWebElement
   * Getting displayed, enabled and selected state of OperaWebElement
   * Submitting and toggling OperaWebElement
   * Getting the text of OperaWebElement
   (stuartk) TODO list all methods?
 * If available show Ecmascript error message in the exception. (stuartk)
 * Correctly return WebElements when they are in another JS object, such as
   an array. (stuartk)

Bug fixes
---------

 * OperaDriver.navigate().frame(String) now switches to frames with "."s in
   rather than sub-frames. (stuartk)

Tests
-----

 * Add tests for selftest. (Arne Skjærholt)
 * Add test for finding sub-elements on a OperaWebElement. (stuartk)
 * Add test for executing Javascript returning an array of elements. (stuartk)

Other
-----

 * Add UMS protobuf file. Regenerate the Java code (no effective changes).
   (stuartk)
 * Move OperaMouse and OperaKeyboard into separate classes. (andreastt)
 * Update pom.xml to contain all needed info. (stuartk)



2011/07/01 OperaDriver 0.5.2
============================

Bug fixes
---------

 * Escape quotes for strings passed to find*By* methods on OperaWebElement
   (andreastt)
 * Fix for click()ing/toggling multiple-select <option> elements (stuartk)


2011/06/27 OperaDriver 0.5.1
============================

Bug fixes
---------

 * Core Scope service no longer required, to support earlier Opera versions.
   (stuartk)
 * Shutdown Scope services in alternate way to support earlier Opera versions.
   (stuartk)


2011/06/26 OperaDriver 0.5
==========================

Known issues
------------

 * Requires Administrator privileges on Windows Vista and 7 when Opera is
   installed in the default location (Program Files). See README.txt for
   workarounds.
 * getScreenshotAs() only returns image data of the current viewport, other
   parts of the image will be black.

API changes
-----------

 * Add getOS(), getProduct(), getBinaryPath(), getUserAgent(), getPID() to
   OperaDriver. (andreastt)
 * Implement TakesScreenshot. (stuartk)
 * Extend RemoteWebDriver. (stuartk)
 * Implement HasDevices (Advanced interaction API). Not fully tested, use
   with caution! (stuartk)
 * Add listAllPrefs() method on OperaDriver. (stuartk)
 * Change listPrefs() to return a map of preference name to Pref object,
   instead of a list of Pref objects.
 * Update to selenium-java-2.0rc3

New features
------------

 * Add ability to get meta-information about Opera when using Opera 11.50 or
   greater (see API changes). (andreastt)
 * Selenium APIs implemented: RemoteWebDriver, TakesScreenshot, HasDevices
   (stuartk)
 * Added maven/pom.xml. (michael tamm)

Enhancements
------------

 * Forward crash status along with the screenshot reply (danielb)
 * Capture more logging (danielb)
 * Add RemoteWebDriver example. (stuartk)

Bug fixes
---------

 * Fix click() method in OperaWebElement for <option> elements. (stuartk)
 * Escape quotes for strings passed to find*By* methods. (stuartk, andreastt)

Tests
-----

 * Ignore failing screenshot test. (stuartk)
 * Add test for quote escaping. (andreastt)
 * Added tests for getting preferences. (andreastt)

Other
-----

 * Rename released jar from webdriver-opera-${version}.jar to
   operadriver-${version}.jar (stuartk)
 * Include source jar in release zip file (stuartk)


2011/06/14 OperaDriver 0.4.3
============================

API changes
-----------

 * Update to a snapshot of selenium post 2.0rc2. This removes
   RenderedWebElement, and OperaWebElement.getvalue(). Use
   getAttribute("value") instead.

New features
------------

 * Add OperaDriver.setUseOperaIdle(boolean).

Enhancements
------------

 * Check Opera Actions case-insensitively.

Bug fixes
---------

 * Keep closer track of the Opera Idle timeout for cases where we don't
   receive the idle message.
 * Fix socket timeout.

Tests
-----

 * Remove consistently failing tests `testFlash` and `testReflow`

2011/05/19 OperaDriver 0.4.2
============================

New features
------------

 * Add ability to get source of XML page
 * Read value of the OPERA_ARGS environment variable.  This is
   concatenated with any arguments set in
   OperaDriverSettings.setOperaBinaryArguments()

Enhancements
------------

 * Reduce page load timeout to 30 seconds from 60s
 * Remove check for pressing already pressed key.

Bug fixes
---------

 * Distinguish between upper- and lower-case attributes in XHTML documents
 * Selenium test suite fixes:
   * Fix isDisplayed on map elements
   * Only switch to frames underneath the current frame
   * Return value property instead of attribute in getAttribute
   * Don't press and release shift to type captials if it's already down
   * Hold down shift and control keys in sendKeys

Tests
-----

 * Remove repeating keys tests.


2011/05/06 OperaDriver 0.4.2 (pre 7)
====================================

Enhancements
------------

 * Do not use idle detection unless the core version is >= 1.1
 * Call quit if init fails in OperaDriver
 * Show the output from launcher when using fine logging
 * Set socket timeout also for new sockets

Bug fixes
---------

 * OperaWebElement.isDisplayed() returns true for <option> elements whose
   <select> parent is displayed.
 * OperaWebElement.getText() collapses two spaces in two adjacent inline
   elements.
 * Don't switch to first frame on a page with a frameset
 * Fix for communication with Opera being faster than Opera

2011/04/29 OperaDriver 0.4.2 (pre 6)
====================================

API changes
-----------

 * Un-deprecate OperaDriverSettings.getNoQuit/setNoQuit and UmsEventHandler.
 * Deprecate OperaDriver.shutdown()
 * OperaDriver.quit() replaces OperaDriver.shutdown()
 * Deprecate OperaDriver.closeAll(), .executeActions(), .stop()
 * Deprecate RenderedWebElement methods on OperaWebElement
 * Deprecate OperaWebElement.hover(),
 * Add OperaWebElement.mouseOver() (replace .hover())
 * Update Selenium JAR to code from SVN revision 12071

New features
------------

 * Add ScopeServices.captureOperaIdle(). Enables the tracking of any Idle
   events received between now and when you call waitForOperaIdle().
 * Add OperaWebElement.saveScreenshot() to match behaviour of
   OperaDriver.saveScreenshot() method.
 * Add timeout for Opera to connect to Driver. If exceeded an exception is thrown.
 * Throw exception if Opera quits immediately after launch, for example if it
   doesn't recognise arguments.

Enhancements
------------

 * Regenerated desktopwm protos from updated service with new griditem and
   gridview types (Desktop).
 * Make specifying or constructing buttons by position possible (Desktop).
 * Add type checking to retrieval algorithm for widgets (Desktop).

Bug fixes
---------

 * Setting operaBinaryArguments to null no longer causes a crash.
 * Correctly shutdown when Opera crashes (Desktop).
 * Updated launcher-linux-x86_64 to work with an older libc library.
 * Updated OperaWebElement to pass more Selenium tests
   * OperaWebElement implements WrapsDriver
   * OperaWebElement uses InvalidElementStateExceptions
   * Add more checks for disabled or hidden elements
   * OperaWebElement.sendKeys now sends 'special' keys when part of a string

Tests
-----

 * Move test fixture setup into own function to allow use in tests that do
   not start driver.
 * Improved Idle tests.

Other
-----

 * Add the Scope .proto files to the repository


2011/03/14 OperaDriver 0.4.2 (pre 5)
====================================

API changes
-----------

 * Removed preference paths from OperaDriverSettings.

New features
------------

 * New settings added to OperaDriverSettings:
   * Disable Opera Idle
   * Autostart setting to replace passing `null` into OperaDriver().
 * Always pass `-autotestmode` in to Opera. This will cause versions of Opera
   which do not support this flag to fail to start. Use a manual start instead.

Enhancements
------------

 * Increase page load timeout from 5 seconds to 60 seconds.
 * Merged desktop changes.
  * Disallow deletion of default main user profile.
  * Do not use pageload timeout for window events
 * Reduce level of Opera Idle logging output.
 * Correctly re-extract launchers when they have changed inside the JAR.
 * Buildfile updated to make testing and releasing easier.
   * Version number is obtained from `git describe`.
   * `test` target generates reports.
   * Individual tests can be run using -Dclass=com.opera.core.systems.<Class>
     where <Class> is one of the classes under test/.
   * Add launchers to jar in `jar` task, rather than executing `zip`.
     Compatibility with Windows.


Bug fixes
---------

 * Update launchers to die after losing their connection to OperaDriver. Fix
   for launchers on Windows lingering.
 * Fixes for some "No response in a timely fashion" exceptions.
   * Prevent buffer under-runs on slow connections

Tests
-----

 * Added and updated unit tests for:
   * Frames
   * OperaDriver core
   * OperaLauncherRunner
   * Opera path guessing
   * Opera Idle
