/*
Copyright 2008-2012 Opera Software ASA

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

package com.opera.core.systems.runner;

/**
 * Used whenever the runner implementation has problems.  We are _not_ supposed to fix launcher
 * problems.  Just pass them upwards.
 *
 * @author Anders Oredsson <anderso@opera.com>
 */
@SuppressWarnings("serial")
public class OperaRunnerException extends RuntimeException {

  public OperaRunnerException(String message) {
    super(message);
  }

  public OperaRunnerException(String message, Throwable cause) {
    super(message, cause);
  }

}