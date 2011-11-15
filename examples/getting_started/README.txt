The 5 Minute Getting Started Guide
==================================

These examples are adapted from the Selenium "The 5 Minute Getting Started
Guide", available at http://code.google.com/p/selenium/wiki/GettingStarted

Running and compiling
---------------------

To compile on a Linux and Mac systems:

		make all

the examples can then be run with:

		make run-example
		make run-google

On Windows the following commands can be used:

		javac -classpath ${CLASSPATH} com/opera/core/systems/example/Example.java
		java -classpath ${CLASSPATH} com.opera.core.systems.example.Example

		javac -classpath ${CLASSPATH} com/opera/core/systems/example/GoogleSuggest.java
		java -classpath ${CLASSPATH} com.opera.core.systems.example.GoogleSuggest

Eclipse
-------

You can import this directory into Eclipse, and then add all the JARs under
the `lib/` directory to your libraries.