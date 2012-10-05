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

import com.opera.core.systems.model.ScreenCaptureReply;

import java.util.List;

/**
 * Indicates that an OperaDriver can capture the whole screen area.
 *
 * @see com.opera.core.systems.model.ScreenCaptureReply
 */
public interface CapturesScreen {

  /**
   * Takes a screen capture of the whole screen, including the areas outside of the viewport and the
   * browser window.
   *
   * @return a screen capture reply
   */
  ScreenCaptureReply captureScreen();

  /**
   * Takes a screen capture of the whole screen, including the areas outside of the viewport and the
   * browser window.
   *
   * @param timeout the number of milliseconds to wait before taking the screenshot
   * @return a screen capture reply
   */
  ScreenCaptureReply captureScreen(long timeout);

  /**
   * Takes a screen capture of the whole screen, including the areas outside of the viewport and the
   * browser window.
   *
   * @param timeout   the number of milliseconds to wait before taking the screenshot
   * @param knownMD5s a list of previous screenshots' MD5 hashes; if they match the hash of this
   *                  screenshot then no image data is returned
   * @return a screen capture reply
   */
  ScreenCaptureReply captureScreen(long timeout, List<String> knownMD5s);

}