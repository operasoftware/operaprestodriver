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

package com.opera.core.systems.testing;

import com.opera.core.systems.environment.webserver.AppServer;

public class Pages {

  public String alert;
  public String basicAuth;
  public String console;
  public String windows;
  public String download;
  public String ecmascriptEvent;
  public String ecmascriptLoop;
  public String ecmascriptTimeout;
  public String ecmascriptTimeoutLoop;
  public String flash;
  public String frames;
  public String html5form;
  public String iframes;
  public String javascript;
  public String keys;
  public String metaRefresh;
  public String mouse;
  public String none;
  public String result;
  public String select;
  public String tall;
  public String test;
  public String timer;
  public String twoInputFields;
  public String zeroHeightQuirks;
  public String zeroHeightStandards;

  public Pages(AppServer server) {
    alert = server.whereIs("alert.html");
    basicAuth = server.whereIs("basic_auth.html");
    console = server.whereIs("console.html");
    windows = server.whereIs("windows.html");
    download = server.whereIs("download.html");
    ecmascriptEvent = server.whereIs("idle/ecmascript-event.html");
    ecmascriptLoop = server.whereIs("idle/ecmascript-loop.html");
    ecmascriptTimeout = server.whereIs("idle/ecmascript-timeout.html");
    ecmascriptTimeoutLoop = server.whereIs("idle/ecmascript-timeout-loop.html");
    flash = server.whereIs("flash.html");
    frames = server.whereIs("frames.html");
    html5form = server.whereIs("html5form.html");
    iframes = server.whereIs("iframes.html");
    javascript = server.whereIs("javascript.html");
    keys = server.whereIs("keys.html");
    metaRefresh = server.whereIs("idle/metarefresh.html");
    mouse = server.whereIs("mouse.html");
    none = server.whereIs("none.html");
    result = server.whereIs("result_page.html");
    select = server.whereIs("select.html");
    tall = server.whereIs("tall.html");
    test = server.whereIs("test.html");
    timer = server.whereIs("timer.html");
    twoInputFields = server.whereIs("two_input_fields.html");
    zeroHeightQuirks = server.whereIs("zero-height-quirks.html");
    zeroHeightStandards = server.whereIs("zero-height-standards.html");
  }

}