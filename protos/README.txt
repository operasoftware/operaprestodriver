OperaDriver proto files
=======================

These are the protobuf definition files used in OperaDriver. The core Scope
services are from https://bitbucket.org/scope/scope-services . The desktop
proto files (desktop_utils, desktop_window_manager and system_input.proto) are
new here.

Compiling
---------

These proto files use some Opera specific extensions to the Protobuf syntax.
To compile these proto files into Java use the `compile.py` file in this
directory, which performs the steps listed below.

    ./compile.py --java_out ../src *.proto

You must use a version of protoc >= 2.4.1

Steps to convert to regular proto files
---------------------------------------

The proto files require some changes so that they can be compiled into Java
source code.

 * Remove the `service { ... }` section from the file.
 * At the top of the file replace `syntax = scope;` with

        package scope;

        option java_package = "com.opera.core.systems.scope.protos";
        option java_outer_classname = "<SERVICE NAME>Protos";
        option optimize_for = SPEED;

 * (See `src/com/opera/core/systems/scope/protos` for the service names used)
 * Run `protoc --java_out=../src <proto file>`
