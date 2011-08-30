/*
Copyright 2008-2011 Opera Software ASA

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

package com.opera.core.systems.scope.internal;

public class OperaFlags {

  /**
   * Enable checks for accessing web elements that are disabled.  This allows clicking on disabled
   * elements, &c.
   */
  public static final boolean ENABLE_CHECKS = true;

  /**
   * Whether or not to append to log file instead of overwriting contents.  If true the driver will
   * append, otherwise it will overwrite.  By default it overwrites the file.
   */
  public static final boolean APPEND_TO_LOGFILE = false;

}