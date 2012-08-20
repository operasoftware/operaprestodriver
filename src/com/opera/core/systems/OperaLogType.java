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

package com.opera.core.systems;

/**
 * Supported log types in Opera.  Note that this list is normative, and the log types available in
 * any given version or product of Opera may differ.
 *
 * Logs are not cleared on loading a new document, but they are cleared on every {@link
 * org.openqa.selenium.logging.Logs#get(String)} request.
 */
public class OperaLogType {

  /**
   * This log type pertains the logs from the HTML (quirk) parsing module.
   */
  public static final String HTML = "html";

  /**
   * This log type pertains the logs from the XML (strict) parsing module.
   */
  public static final String XML = "xml";

  /**
   * This log type pertains the logs from the CSS (Cascading Stylesheets) module.
   */
  public static final String CSS = "css";

  /**
   * This log type pertains the logs from ECMAscript in the document.
   */
  public static final String ECMASCRIPT = "ecmascript";

  /**
   * This log type pertains the logs from the SVG (Scalable Vector Graphics) module.
   */
  public static final String SVG = "svg";

  /**
   * This log type pertains the logs from the web storage (or DOM storage).
   */
  public static final String PERSISTENT_STORAGE = "persistent_storage";

  /**
   * This log type pertains the logs from web socket operations in Opera.
   */
  public static final String WEBSOCKETS = "websockets";

  /**
   * This log type pertains the logs from the geolocation API.
   */
  public static final String GEOLOCATION = "geolocation";

  /**
   * This log type pertains the logs from the plugins module in Opera.
   */
  public static final String PLUGINS = "plugins";

  /**
   * This log type pertains the logs from the Opera Mail client, M2.
   */
  public static final String MAIL = "mail";

  /**
   * This log type pertains the logs from the network module.
   */
  public static final String NETWORK = "network";

  /**
   * This log type pertains the logs from the XSLT (Extensible Stylesheet Language Transformations)
   * module.
   */
  public static final String XSLT = "xslt";

  /**
   * This log type pertains the logs from the selftest module.
   */
  public static final String SELFTEST = "selftest";

}