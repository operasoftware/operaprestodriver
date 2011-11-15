CLASSPATH="../../lib/*:."

all: example google

example:
	javac -classpath ${CLASSPATH} com/opera/core/systems/example/Example.java

google:
	javac -classpath ${CLASSPATH} com/opera/core/systems/example/GoogleSuggest.java

run-example: example
	java -classpath ${CLASSPATH} com.opera.core.systems.example.Example

run-google: google
	java -classpath ${CLASSPATH} com.opera.core.systems.example.GoogleSuggest