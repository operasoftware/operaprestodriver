/*
Copyright 2011 Opera Software ASA

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

package com.opera.core.systems.arguments;

import com.opera.core.systems.OperaArguments;

/**
 * Handles a list of strings/arguments for the Opera core binary.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class OperaCoreArguments extends OperaArguments implements
                                                       com.opera.core.systems.arguments.interfaces.OperaArguments {

  /**
   * The -autotestmode argument is always added to the arguments list passed to the browser.  This
   * enabled Proxy Auto Connect and (in Desktop) prevents loading of Speed Dial (which is not
   * script-injectable), the license dialogue, the crash dialogue amongst other things.
   *
   * We also read in environmental variable OPERA_ARGS before everything else.  These will typically
   * later be combined with the arguments set in {@link org.openqa.selenium.remote.DesiredCapabilities}
   * and passed on to the browser.
   */
  public OperaCoreArguments() {
    // TODO(andreastt): gogi doesn't support -pd, and for some reason this must be sent last
    //OperaArgument argument = new OperaArgument("autotestmode");
    //super.arguments.add(argument);
    merge(parse(System.getenv("OPERA_ARGS")));
  }

  public static OperaArguments parse(String string) {
    return OperaArguments.parse(string);
  }

}