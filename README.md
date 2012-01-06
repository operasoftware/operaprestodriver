OperaDriver
===========

OperaDriver is a vendor-supported WebDriver implementation developed
by [Opera Software](http://opera.com/) and volunteers that implements
[WebDriver](http://code.google.com/p/selenium/) for Opera.

OperaDriver can drive the browser running various tests on your web
pages, just as if a real user was navigating through them.  It can
emulate actions like clicking links, entering text and submitting
forms, and reports results back to you so you know that your website
works as intended.

OperaDriver's end-user emulation ensures that your entire stack (HTML,
scripts, styling, embedded resources and backend setup) is functioning
correctly, and this without tedious manual testing routines.


Requirements
------------

You will need a Java Runtime Environment 1.5 or newer (Oracle or
OpenJDK) to use OperaDriver.  It uses the [Scope
interface](http://dragonfly.opera.com/app/scope-interface/) (same as
for [Dragonfly](http://www.opera.com/dragonfly/)) to communicate
directly with Opera from Java.  Consequently, it is only compatible
out-ofthe-box with Opera version 11.6 or newer, although it works for
older versions with some tweaks.

The OperaDriver server expects you to have Opera or Opera Next
installed in the default location for each system:

| __OS__    | __Expected Location of Opera__                                                                      |
|-----------|-----------------------------------------------------------------------------------------------------|
| GNU/Linux | /usr/bin/opera<br />/usr/bin/opera-next                                                             |
| Mac       | /Applications/Opera.app/Contents/MacOS/Opera<br />/Applications/Opera Next.app/Contents/MacOS/Opera |
| Windows   | %PROGRAMFILES%\Opera\opera.exe<br />%PROGRAMFILES%\Opera Next\opera.exe                             |

However, you can override this and specify a custom location to Opera
by setting the capability `opera.path` or environmnetal variable
`OPERA_PATH`.  You can read more about configuring WebDriver under
Advanced Usage.

Other libraries required by OperaDriver:

  * Apache Commons Exec 1.1
  * Apache Commons JXPath 1.3
  * Google Guava 10.0.1
  * Google protobuf 2.4.1
  * ini4j 0.5.2
  * json-20080701.jar
  * JUnit 4.10
  * Selenium 2.16.0

### OperaDriver on 11.52 or older

To use OperaDriver on Opera < 11.52, make sure you set the
capabilities `opera.port` to `-1` and `opera.profile` to `""` (empty
string) to disable `-debugproxy` and `-pd` command-line arguments
which older versions of Opera do not support.  For even older versions
(11.01 or older) you might need a wrapper script (see below).


Getting started
---------------

### Remotely

To get set up [please
download](http://code.google.com/p/selenium/downloads/list) either
*selenium-server-standalone* or *selenium-server* and make sure you
have a fairly recent version of Opera installed.  All you do is to
create a new
`[WebDriver](http://selenium.googlecode.com/svn/trunk/docs/api/java/org/openqa/selenium/WebDriver.html)`
instance:

    WebDriver driver = new OperaDriver();
    driver.navigate().to("http://opera.com/");

Since WebDriver provides APIs (called *bindings*) for several
languages, you can do the same in
[Ruby](http://selenium.googlecode.com/svn/trunk/docs/api/rb/index.html)
(using the
[selenium-webdriver](http://rubygems.org/gems/selenium-webdriver) gem)
by setting the environmental variable `SELENIUM_SERVER_JAR` to the
full path of the *selenium-server-standalone* JAR you just downloaded:

    export SELENIUM_SERVER_JAR=/path/to/selenium-server-standalone.jar

And then do the following in Ruby:

    driver = Selenium::WebDriver.for :opera
    driver.navigate.to('http://opera.com/');


### Running the server as a standalone process

Since OperaDriver implements the wire protocol, it is fully compatibly
with any
[RemoteWebDriver](http://code.google.com/p/selenium/wiki/RemoteWebDriver)
client.  Simply start up your server, create a remote client, and away
you go:

    WebDriver driver = new RemoteWebDriver("http://localhost:9515", DesiredCapabilities.opera());
    driver.navigate().to("http://opera.com/");


### Separate OperaDriver

You can also use OperaDriver as a standalone dependency in your
project.  Download the package from the [Github project's download
section](https://github.com/operasoftware/operadriver/downloads) and
extract it to a location of your choice.  To see some examples look at
the
[examples/](https://github.com/operasoftware/operadriver/tree/master/examples)
directory.  For your own projects include the `lib/*` directory on
your classpath, for example:

    javac -classpath "lib/*:." Example.java

In Eclipse this can be done under _Project_ → _Properties_ → _Java
Build Path_ and then *"Add JARs..."* or *"Add External JARs..."*
depending on the layout of your project.


Advanced Usage
--------------

### Capabilities

You can use the
[DesiredCapabilities](http://selenium.googlecode.com/svn/trunk/docs/api/java/org/openqa/selenium/remote/DesiredCapabilities.html)
class to specify settings for OperaDriver.  The capabilities we
support are:

| __Capability__              | __Type__ | __Default__ | __Description__             |
|-----------------------------|----------|-------------|-----------------------------|
| __opera.logging.level__     | String/[Level](http://docs.oracle.com/javase/1.4.2/docs/api/java/util/logging/Level.html) | "INFO" | How verbose the logging should be.  Available levels are: SEVERE (highest value), WARNING, INFO, CONFIG, FINE, FINER, FINEST (lowest value), ALL.  Default is INFO.
| __opera.logging.file__      | String   | null        | Where to send the output of the logging.  Default is to not write to file.
| __opera.binary__            | String   | null        | Path to the Opera binary to use.  If not specified, OperaDriver will guess the path to your Opera installation.
| __opera.arguments__         | String   | null        | Arguments to pass on to Opera, separated by spaces.  See `opera -help` for available command-line arguments.
| __opera.host__              | String   | "127.0.0.1" | The host Opera should connect to.  Unless you're starting Opera manually you won't need this.
| __opera.port__              | Integer  | 7001        | The port Opera should connect to.  0 = Random port, -1 = Opera default (port 7001) (for use with Opera < 12).
| __opera.launcher__          | String   | null        | Absolute path to the launcher binary to use.  The launcher is a gateway between OperaDriver and the Opera browser, and is being used for controlling and monitoring the binary, and taking external screenshots.  If left blank, OperaDriver will use the launcher supplied with the package.
| __opera.profile__           | String/OperaProfile | `new OperaProfile()` | Directory string for the Opera profile to use.  If left empty, a new random profile will be used.  If "", an empty string, then the default autotest profile directory is used.
| __opera.idle__              | Boolean  | false       | Whether to use Opera's alternative implicit wait implementation.  It will use an in-browser heuristic to guess when a page has finished loading,allowing us to determine with great accuracy whether there are any planned events in the document.  This functionality is useful for very simple test cases, but not designed for real-world testing.  It is disabled by default.
| __opera.display__           | Integer  | null        | The X display to use.  (Only works on UNIX-like OSes.)
| __opera.autostart__         | Boolean  | true        | Whether to auto-start the Opera binary.  If false, OperaDriver will wait for a connection from the browser.  Go to "opera:debug", enter the correct port number and hit "Connect" to connect manually.
| __opera.no_restart__        | Boolean  | false       | Whether to restart.
| __opera.no_quit__           | Boolean  | false       | Whether to quit Opera when OperaDriver is shut down.  If enabled, it will keep the browser running after the driver is shut down.
| __opera.guess_binary_path__ | Boolean  | true        | Whether to guess the path to Opera if it isn't set in `opera.binary`.
| __opera.product__           | String   | null        | The product we are using, for example "desktop" or "core-desktop", "sdk".  See [OperaProduct](https://github.com/operasoftware/operadriver/blob/master/src/com/opera/core/systems/OperaProduct.java) for all available values.

To use capabilities:

    DesiredCapabilities capabilities = DesiredCapabilities.opera();
    capabilities.setCapability("opera.profile", new OperaProfile("/path/to/existing/profile"));
    capabilities.setCapability("opera.logging.level", Level.CONFIG);
    capabilities.setCapability("opera.logging.file", "/var/log/operadriver.log");
    capabilities.setCapability("opera.display", 8);
    capabilities.setCapability("opera.profile", "/home/andreastt/my-own-opera-profile");

    // Now use it
    WebDriver driver = new OperaDriver(capabilities);
    driver.navigate().to("http://opera.com/");

See also the information available on the
[RemoteWebDriver](http://code.google.com/p/selenium/wiki/RemoteWebDriver)
at the Selenium wiki.

You can also provide an `OperaProfile` object to use a new, fresh
random profile (default) or specifying an existing profile on your
system.  You can manipulate the profile before Opera is started to
i.e. set preferences you wish to use:

    DesiredCapabiltiies capabilities = DesiredCapabilities.opera();
    OperaProfile profile = new OperaProfile();  // fresh, random profile
    profile.preferences().set("User Prefs", "Ignore Unrequested Popups", false);
    capabilities.setCapability("opera.profile", profile);

    WebDriver driver = new OperaDriver(capabilities);


### Environment variables

To specify such things as a custom location of the Opera binary and the
command-line arguments to use, you may use environmental variables also.  This
is a list of the environmental variables which can be set on any operating
system:

| __Name__          | __Description__                                                                                                                                                                                                              |
|-------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| __OPERA_PATH__    | The absolute path to the Opera binary you want to use.  If not set OperaDriver will try to locate Opera on your system.                                                                                                      |
| __OPERA_ARGS__    | A space-delimited list of arguments to pass on to Opera, e.g. `-nowindow`, `-dimensions 1600x1200`, &c.  See `opera --help` to view available arguments.                                                                     |
| __OPERA_PRODUCT__ | To override the product check when running the OperaDriver tests.  Set this to any value specified in [OperaProduct](https://github.com/operasoftware/operadriver/blob/master/src/com/opera/core/systems/OperaProduct.java). |

To set environment variables:

  * __Linux and Mac__: `export OPERA_PATH=...`, and add this line to `~/.bashrc` (or your shell's configuration file) to use in all future sessions.
  * __Windows__: Please follow this guide: http://support.microsoft.com/kb/310519


Supported Opera versions
------------------------

### Desktop

This is a list of the official Opera Desktop versions supported by OperaDriver:

| __Version__ | __Workaround/tweaks needed__                                                                                                |
|-------------|-----------------------------------------------------------------------------------------------------------------------------|
| 12.00       | *(Not released yet)*                                                                                                        |
| 11.60       |                                                                                                                             |
| 11.52       | Set `opera.port` to `-1` and `opera.profile` to "" (empty string) to disable `-debugproxy` and `-pd` command-line arguments |
| 11.51       |                                                                                                                             |
| 11.50       |                                                                                                                             |
| 11.11       |                                                                                                                             |
| 11.10       |                                                                                                                             |
| 11.01       | `-autotestmode` command-line argument is not supported, use a wrapper script                                                |
| 11.00       |                                                                                                                             |


#### Wrapper script

Some Opera versions don't support the `-autotestmode`, `-debugproxy` or `-pd`
arguments sent by OperaDriver by default.  You can bypass this problem by
creating a wrapper script like this and pointing the capability `opera.binary`
to its absolute path:

    #!/bin/sh
    # Wrapper to prevent the -autotestmode argument reaching this version of Opera
    # which doesn't support it.
    `dirname $0`/opera

Known issues 
------------

* Modifier keys' states are not preserved across multiple `.sendKeys()` calls in `Actions`
* Problems with Operas with IME feature enabled (Opera Mobile, Android)
* No support for Opera Mini
* No support for JavaScript alert/popup dialogues
* getScreenshotAs() only returns image data of the current viewport, other parts of the image will be black.
* Requires Administrator privileges on Windows Vista and 7 when Opera 11.5x is installed in the default location (Program Files).
* Not possible to move into an image enclosed in a link
* Failing the test for clicking a button that closes an open window does not cause the browser to hang
* Failing test for getting window handles after closing a window
* Problems with coordinates on moving mouse back and forth past view port
* Closing the final browser window causes an exception
* Tests for typing multibyte characters will fail on Windows unless you have the correct charset