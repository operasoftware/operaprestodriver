OperaDriver
===========

OperaDriver is a vendor-supported
[WebDriver](http://dvcs.w3.org/hg/webdriver/raw-file/tip/webdriver-spec.html)
implementation developed by [Opera](http://opera.com/) and volunteers
that enable programmatic automation of different Presto-based Opera
products.  It is a part of the
[Selenium](http://code.google.com/p/selenium) project.

__Note that OperaDriver is only compatible with Presto-based Operas up
until 12.16.  Blink-based Operas (15 and onwards) are not supported.__

WebDriver is a general purpose library for automating web browsers.
It can drive the browser running various tests on your web pages, just
as if a real user was navigating through them.  It can emulate actions
like clicking links, enter text and submitting forms, and reporting
results back to you so you know that your website works as intended.

OperaDriver's end-user emulation ensures that your entire stack (HTML,
scripts, styling, embedded resources and backend setup) is functioning
correctly without tedious manual testing routines.

OperaDriver is usable out of the box from the [official Selenium
packages](https://code.google.com/p/selenium/downloads/list), and can
be used no extra setup on any modern version of Opera.


Documentation
-------------

* [API documentation](http://operasoftware.github.com/operadriver/docs/)
* [Selenium WebDriver documentation](http://seleniumhq.org/docs/03_webdriver.html)
* [Python bindings API](http://selenium.googlecode.com/svn/trunk/docs/api/py/index.html)
* [Ruby bindings API](http://selenium.googlecode.com/svn/trunk/docs/api/rb/index.html)
* [Complete list of capabilities](https://code.google.com/p/selenium/wiki/DesiredCapabilities)


Getting started
---------------

To get set up, first
[download](http://code.google.com/p/selenium/downloads/list) either
the *selenium-server-standalone* or *selenium-server* package and make
sure you have a fairly recent version of Opera installed in a default
location.  Finally, all you need to do is create a new
[`OperaDriver`](http://operasoftware.github.com/operadriver/docs/com/opera/core/systems/OperaDriver.html)
instance:

    WebDriver driver = new OperaDriver();
    driver.get("http://opera.com/");

If you prefer using a package management system, OperaDriver is also
available through Maven; either as a part of the Selenium package, or
as a separate package.  The group ID for Selenium is
[`org.openqa.selenium.*`](http://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.openqa.*),
and `com.opera` and artifact ID `operadriver` for OperaDriver
specifically.


### Other languages

Since WebDriver provides bindings for several programming languages,
you can follow the same approach as above in both
[Python](http://selenium.googlecode.com/svn/trunk/docs/api/py/index.html)
(using the [selenium](http://pypi.python.org/pypi/selenium) package)
and in
[Ruby](http://selenium.googlecode.com/svn/trunk/docs/api/rb/index.html)
(using the
[selenium-webdriver](http://rubygems.org/gems/selenium-webdriver)
gem).

The Python equivalent of the above example would be:

    from selenium import webdriver
    driver = webdriver.Opera()
    driver.get('http://opera.com/')

The same in Ruby:

    require 'selenium-webdriver'
    driver = Selenium::WebDriver.for :opera
    driver.get('http://opera.com/')

To execute the tests, please ensure that the environmental variable
*SELENIUM_SERVER_JAR* contains the path to the
*selenium-server-standalone* JAR you downloaded earlier.

To execute the Python test:

    SELENIUM_SERVER_JAR=/path/to/selenium-server-standalone.jar python test.py

And for Ruby:

    SELENIUM_SERVER_JAR=/path/to/selenium-server-standalone.jar ruby test.rb


### Running the server as a standalone process

OperaDriver is fully compatible with the
[RemoteWebDriver](http://code.google.com/p/selenium/wiki/RemoteWebDriver)
client.  This allows you to run tests on a remote computer which is
typically very useful in a distributed environment.  First, ensure
that your Selenium server is running, then create a remote client as
usual:

    WebDriver driver = new RemoteWebDriver("http://localhost:4444", DesiredCapabilities.opera());
    driver.get("http://opera.com/");


Capabilities
------------

To customize Opera and WebDriver in various ways you may request a
certain configuration from the Selenium server.  Since not all server
implementations supports every WebDriver feature, the client and
server should use JSON objects with the properties when describing
which features are desirable.

In additon to the [general WebDriver
capabilities](https://code.google.com/p/selenium/wiki/DesiredCapabilities)
available, OperaDriver has a number of custom capabilities that may be
requested.  To request a specific driver configuration you

You can use the
[DesiredCapabilities](http://selenium.googlecode.com/svn/trunk/docs/api/java/org/openqa/selenium/remote/DesiredCapabilities.html)
class to request a specific driver configuration.  The Opera-specific
capabilities supported are:

| __Capability__              | __Type__ | __Default__ | __Description__ |
|-----------------------------|----------|-------------|-----------------|
| __opera.logging.level__     | [Level](http://docs.oracle.com/javase/1.4.2/docs/api/java/util/logging/Level.html)/String/Integer | Level.INFO | (String/Level/Integer) How verbose the logging should be.  Available levels are: SEVERE (highest value), WARNING, INFO, CONFIG, FINE, FINER, FINEST (lowest value), ALL, OFF.<br /><br >The argument may consist of either a level name as a string, an integer value, a [Level reference](http://docs.oracle.com/javase/1.4.2/docs/api/java/util/logging/Level.html), or null.  If the value is neither of a known name nor an integer, an IllegalArgumentException will be thrown.
| __opera.logging.file__      | [File](http://docs.oracle.com/javase/1.4.2/docs/api/java/io/File.html)/String | null     | Where to send the output of the logging.  Default is to not write to file.
| __opera.product__           | [OperaProduct](http://operasoftware.github.com/operadriver/docs/com/opera/core/systems/OperaProduct.html)/String   | Desktop | The product to request, for example `OperaProduct#DESKTOP` or `OperaProduct#MOBILE`.  It will attempt to locate the product binary based on the operating system's default installation paths if _opera.binary_ is not set.
| __opera.binary__            | String   | Default location of Opera on system | Path to the Opera binary to use.  If not specified, OperaDriver will guess the path to your Opera installation (typically */usr/bin/opera*, *C:\Program Files\Opera\opera.exe*, or similar).
| __opera.arguments__         | String   | null        | Arguments to pass to Opera, separated by spaces.  See `opera -help` for available command-line switches.
| __opera.emulationProfile__  | [EmulationProfile](http://operasoftware.github.com/operadriver/docs/com/opera/core/systems/EmulationProfile.html)/JSON object | null | Allows you to specify an emulation profile to use with Opera Mobile.
| __opera.host__              | String   | Non-loopback IP if available, loopback otherwise | The host Opera should connect to.  Since OperaDriver works in a client-server relationship to Opera (where Opera is the client, driver the server) you can also run remote instances of Opera on other devices; that be a phone, a TV or another computer.
| __opera.port__              | Integer  | Random port | The port to Opera should connect to.  Setting this capability to 0 will probe for a free, random port, setting it to -1 will ensure compatibility mode using port the default port 7001 for Operas version 11.52 or older.
| __opera.profile__           | [OperaProfile](http://operasoftware.github.com/operadriver/docs/com/opera/core/systems/operaprofile.html)/string | temporary profile | directory of the profile to use, or an [OperaProfile](http://operasoftware.github.com/operadriver/docs/com/opera/core/systems/operaprofile.html) instance object representing a profile.  if null is given, a random temporary directory is used.  if "", an empty string, then the default *~/.autotest* profile directory will be used (for backwards compatibility with opera < 11.60).
| __opera.autostart__         | Boolean  | true        | Whether to auto-start the Opera binary.  If false, OperaDriver will wait for a connection from the browser.  Go to *"opera:debug"*, enter the correct host/port information, and hit _Connect_ to connect manually.
| __opera.detach__            | Boolean  | false       | Whether to detach the Opera browser when the driver shuts down.  This will leave Opera running.
| __opera.display__           | Integer  | null        | The X display to use.  If set, Opera will be started on the specified display.  (Only works on GNU/Linux.)
| __opera.idle__              | Boolean  | false       | Whether to use Opera's alternative implicit wait implementation.  It will use an in-browser heuristic to guess when a page has finished loading, allowing us with great accuracy tell whether there are any planned events in the document.  This functionality is useful for very simple test cases, but not designed for real-world testing.  It is disabled by default.
| __opera.runner__            | String | OperaLauncherRunner | Allows you to specify which runner to use to control the Opera browser binary process.
| __opera.launcher__          | String   | null        | Path to the launcher binary to use.  The launcher is an external wrapper around the browser, and is used for controlling the binary and taking external screenshots.  If left blank, OperaDriver will use a launcher supplied with the package.

OperaDriver also supports some of the
[common desired capabilities](http://code.google.com/p/selenium/wiki/DesiredCapabilities)
too:

  * [__proxy__](http://code.google.com/p/selenium/wiki/DesiredCapabilities#Proxy_JSON_Object)

### Custom profile

For instance the OperaDriver can be made to start the browser with
specific command-line arguments using the `opera.arguments` key.  This
key should define a list of a command-line arguments that should be
passed to the browser on startup.  For example, to start Opera with a
custom profile:

    DesiredCapabilities capabilities = DesiredCapabilities.opera();

    OperaProfile profile = new OperaProfile("/path/to/profile");  // prepared profile
    capabilities.setCapability("opera.profile", profile);

    WebDriver driver = new OperaDriver(capabilities);


### Opera Mobile Emulator

Or to tell the Opera Mobile Emulator to use the tablet UI and a
specific screen resolution:

    DesiredCapabilities capabilities = DesiredCapabilities.opera();

    capabilities.setCapability("opera.product", OperaProduct.MOBILE);
    capabilities.setCapability("opera.arguments", "-tabletui -displaysize 860x600");

    WebDriver driver = new OperaDriver(capabilities);


#### Emulation profile

An
[emulation profile](http://operasoftware.github.com/operadriver/docs/com/opera/core/systems/EmulationProfile.html)
may be specified when interacting with Opera Mobile to instruct the
emulator to use a certain configuration.  You may either give it an
instance of
[OperaMobileEmulation](http://operasoftware.github.com/operadriver/docs/com/opera/core/systems/mobile/OperaEmulationProfile.html)
which transparently deserializes into a JSON object, or a JSON object
directly:

    {
      profileName: "foo",
      width: 800,
      height: 600,
      ppi: 230,
      ime: "tablet",
      userAgent: "Android"
    }


### Logging and X display

Similarly, to increase the logging verbosity and, for GNU/Linux, ask
Opera to start on a different X display:

    DesiredCapabilities capabilities = DesiredCapabilities.opera();

    capabilities.setCapability("opera.logging.level", Level.CONFIG);
    capabilities.setCapability("opera.logging.file", "/var/log/operadriver.log");
    capabilities.setCapability("opera.display", 8);

    WebDriver driver = new OperaDriver(capabilities);


### Proxy

It is also possible to configure a proxy for use with Opera.  The
proxy configuration is set through the capabilities.  You can use the
[Proxy](http://selenium.googlecode.com/svn/trunk/docs/api/java/org/openqa/selenium/Proxy.html)
helper in Selenium to manage it:

    DesiredCapabilities capabilities = DesiredCapabilities.opera();

    Proxy proxy = new Proxy();
    proxy.setHttpProxy("127.0.0.1:1234");
    capabilities.setCapability("proxy", proxy);

    WebDriver driver = new OperaDriver(capabilities);


### Environment variables

To specify a custom location of the Opera binary and the command-line
arguments to use, you may also use environmental variables.  These are
the available variables:

| __Name__          | __Description__                                                                                                                                                                                                              |
|-------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| __OPERA_PATH__    | The absolute path to the Opera binary you want to use.  If not set OperaDriver will try to locate an Opera (desktop or mobile) on your system.                                                                               |
| __OPERA_ARGS__    | A space-delimited list of arguments to pass on to Opera, e.g. `-nowindow`, `-dimensions 1600x1200`, &c.  See `opera -help` or `operamobile -help` to see available arguments.                                                |

To set environment variables:

  * __GNU/Linux and Mac__: `export OPERA_PATH=...`, and add this line to `~/.bashrc` (or your shell's configuration file) to use in all future sessions.
  * __Windows__: Please follow this guide: http://support.microsoft.com/kb/310519


Supported Opera versions
------------------------

### Desktop

This is a list of the official Opera Desktop versions supported by OperaDriver.
Please note that this Java version of OperaDriver will not support future
Chromium-based Operas.

| __Version__ | __Workaround/tweaks needed__                                                                                                |
|-------------|-----------------------------------------------------------------------------------------------------------------------------|
| 12.16       |                                                                                                                             |
| 12.15       |                                                                                                                             |
| 12.14       |                                                                                                                             |
| 12.13       |                                                                                                                             |
| 12.12       |                                                                                                                             |
| 12.10       |                                                                                                                             |
| 12.01       |                                                                                                                             |
| 12.00       |                                                                                                                             |
| 11.64       |                                                                                                                             |
| 11.62       |                                                                                                                             |
| 11.61       |                                                                                                                             |
| 11.60       |                                                                                                                             |
| 11.52       | Set `opera.port` to `-1` and `opera.profile` to "" (empty string) to disable `-debugproxy` and `-pd` command-line arguments |
| 11.51       |                                                                                                                             |
| 11.50       |                                                                                                                             |
| 11.11       |                                                                                                                             |
| 11.10       |                                                                                                                             |
| 11.01       | `-autotestmode` command-line argument is not supported, use a wrapper script                                                |
| 11.00       |                                                                                                                             |


### Mobile

Please use one of the
[prepared builds](http://dev.opera.com/articles/view/introducing-mobile-browser-automation/)
to use OperaDriver with Opera Mobile.

OperaDriver does not support the Chromium-based Opera Mobile builds.


#### Wrapper script

Some Opera versions don't support the `-autotestmode`, `-debugproxy`
or `-pd` arguments sent by OperaDriver by default.  You can bypass
this problem by creating a wrapper script like this and pointing the
capability `opera.binary` to its absolute path:

    #!/bin/sh
    # Wrapper to prevent the -autotestmode argument reaching this version of Opera
    # which doesn't support it.
    `dirname $0`/opera


Development
-----------

While OperaDriver is officially maintained by Opera, it is [free
software](http://fsfe.org/about/basics/freesoftware.en.html) and would
only be possible thanks to [many volunteer
contributors](https://github.com/operasoftware/operadriver/blob/master/AUTHORS).
If you come across a reproducible bug, please [open an
issue](http://code.google.com/p/selenium/issues/list) to submit a bug
report.

Even better, you can send a pull request!  Any changes you make must
follow the [Google Coding
Standards](http://code.google.com/p/java-coding-standards/wiki/Introduction)
and have test cases attached to them if you introduce a new feature.

Much of OperaDriver's code is shared with the other WebDriver
implementations, and for working on this code base you should also
familiarize yourself with the [Selenium code
base](http://code.google.com/p/selenium/source/checkout).  There are
also a few tips available for [working on
Selenium](http://code.google.com/p/selenium/wiki/DeveloperTips).


Support
-------

If you have problems or questions regarding OperaDriver or Selenium,
there are many channels in which you can seek help:

* __IRC__: The *#selenium* channel on the *irc.freenode.org* network
* __Mailing lists__: The [webdriver](http://groups.google.com/group/webdriver) or [selenium-users](http://groups.google.com/group/selenium-users) mailing list
