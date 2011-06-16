OperaDriver
===========

OperaDriver is Opera's implementation of WebDriver.

OperaDriver can drive the browser running various tests on your web pages, just
as if a real user was navigating through them. It can simulate actions like
clicking links, entering text and submitting forms, and reports results back to
you so you know that your website works as intended.

OperaDriver's end-user emulation ensures that your entire stack (HTML, scripts,
styling, embedded resources and backend setup) is functioning correctly, and
this without tedious manual testing routines.

**Test profile**

To prevent testing changing your usual Opera profile, an "autotest" profile is
created. You can browse with this profile by running Opera with the
`-autotestmode` flag

    Linux:   opera -autotestmode
    Windows: "C:\Program Files\Opera\opera.exe" -autotestmode
    Mac:     /Applications/Opera.app/Contents/MacOS/Opera -autotestmode

The profile data is stored in the following locations:

    Linux:   ~/.opera-autotest
    Windows: C:\Program Files\Opera\profile-autotest
            (or under the Opera install directory)
    Mac:     ~/.operaprofile-autotest


Remote web driver
-----------------

See examples/RemoteWebDriver/ for an example on how to use OperaDriver as a
RemoteWebDriver. OperaDriver is included in the standalone jar so you do not
need to download it separately.

Standalone
----------

 * Download OperaDriver from https://github.com/operasoftware/operadriver
 * Extract the operadriver directory to a location of your choice.

To see some examples look at the examples/ directory. For your own projects
include the lib/* directory on your classpath, for example:

    javac -classpath "lib/*:." Example.java

In Eclipse this can be done under Project > Properties > Java Build Path and
then "Add JARs..." or "Add External JARs..." depending on the layout of your
project.
