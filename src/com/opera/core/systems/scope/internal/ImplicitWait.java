/*
Copyright 2012 Opera Software ASA

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

import org.openqa.selenium.support.ui.Duration;

/**
 * Generalizes HtmlUnitDriver's wait code, inspired by {@link org.openqa.selenium.support.ui.WebDriverWait},
 * but does not aim to provide a public support class.
 */
public class ImplicitWait extends CallbackWait {

  // TODO(andreastt): OPDRV-195
  public static final Duration DEFAULT_INTERVAL = OperaIntervals.POLL_INTERVAL.getValue();

  public ImplicitWait(Duration timeout) {
    this(timeout, DEFAULT_INTERVAL);
  }

  public ImplicitWait(Duration timeout, Duration interval) {
    super();
    withTimeout(timeout);
    pollingEvery(interval);
  }

}