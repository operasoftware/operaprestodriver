OperaDriver
===========

A WebDriver implementation for the Opera browser

Installation
============

 * Download OperaDriver from https://github.com/operasoftware/operadriver
 * Extract the operadriver-{VERSION}/ directory to a location of your choice.
 * IMPORTANT: In the Opera browser visit `opera:config` and enable the "Proxy
   Auto Connect" preference.

To see some examples look at the examples/ directory. For your own projects
include the operadriver-{VERSION}/lib/* directory on your classpath, for example
`javac -classpath "operadriver-{VERSION}/lib/*:." Example.java`

In Eclipse this can be done under Project > Properties > Java Build Path and
then "Add JARs..." or "Add External JARs..." depending on the layout of your
project.
