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

package com.opera.core.systems.scope.services.ums;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.DesktopUtilsCommand;
import com.opera.core.systems.scope.protos.DesktopUtilsProtos.DesktopPath;
import com.opera.core.systems.scope.protos.DesktopUtilsProtos.DesktopPid;
import com.opera.core.systems.scope.protos.DesktopUtilsProtos.DesktopStringID;
import com.opera.core.systems.scope.protos.DesktopUtilsProtos.DesktopStringText;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.IDesktopUtils;
import com.opera.core.systems.internal.WatirUtils;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DesktopUtils extends AbstractService implements IDesktopUtils {

  private static final String SERVICE_NAME = "desktop-utils";
  // Matches possibly all printf formatters, i.e. %s, %d, %1.3f
  private static final String SUBSTITUTED_STRING_REGEXP =
      "(%[\\+\\#\\-\\ 0]?(?:\\d+)?(?:\\.\\d)?[hlL]?[cdieEfgGosuxXpn%])";

  private final Logger logger = Logger.getLogger(getClass().getName());

  public DesktopUtils(ScopeServices services, String version) {
    super(services, version);

    if (!isVersionInRange(version, "3.0", SERVICE_NAME)) {
      throw new UnsupportedOperationException(
          String.format("%s version %s is not supported", SERVICE_NAME, version));
    }

    services.setDesktopUtils(this);
  }

  public void init() {
  }

  public String getString(String enumText, boolean stripAmpersand) {
    String string = "";

    DesktopStringID.Builder stringBuilder = DesktopStringID.newBuilder();
    stringBuilder.setEnumText(enumText);
    Response response = executeCommand(DesktopUtilsCommand.GET_STRING, stringBuilder);

    if (response == null) {
      logger.warning(String.format("Could not fetch string for ID: %s", enumText));
    } else {
      DesktopStringText.Builder stringTextBuilder = DesktopStringText.newBuilder();
      buildPayload(response, stringTextBuilder);
      DesktopStringText stringText = stringTextBuilder.build();

      // Remember to remove all CRLF
      string = removeCR(stringText.getText());

      if (stripAmpersand && string.contains("(&")) {
        return string.replaceAll("\\(&.\\)", "");
      }
      if (stripAmpersand && string.contains("&")) {
        return string.replace("&", "");
      }
    }

    return string;
  }

  /**
   * Fetches a translated string fetched basing on a string id and substitutes the printf formatters
   * in it. Each substitution argument needs to be a string.  The printf formatter types are
   * ignored, and the given string argument is substituted.
   *
   * The method distinguishes between ordered substitution (i.e. %1, %2) and a standard substitution
   * (i.e. %s, %d).
   *
   * In case the given argument list is too long or too short for the number of arguments in the
   * fetched translated string, this method triggers a warning. In case the given argument list is
   * too short, the method substitutes as many formatters as possible and leaves the rest of them
   * intact. "%%" is substituted to "%".
   *
   * If a given argument is empty, i.e. "", it will be substituted to ANY_MATCHER, allowing later
   * searching for the text without specific formatter values, i.e. matching "Show _ANY_ more..." to
   * any string like "Show 1 more...", "Show 2 more...".
   *
   * @param args           an array containing the printf substitution arguments, all of string
   *                       type, and the string id as the first element
   * @param stripAmpersand whether to strip the "&" character from the fetched string
   * @return the final substituted string
   */
  public String getSubstitutedString(String[] args, boolean stripAmpersand) {
    String stringId = args[0];
    String rawString = getString(stringId, stripAmpersand);

    logger.finer(String.format("String \"%s\" fetched as \"%s\"", stringId, rawString));

    StringBuffer buf = new StringBuffer();
    int curArg = 0;
    int formattersCount = 0;
    boolean orderedMode;
    // Matches %1, %2, ...
    String orderedFormatterRegexp = "%(\\d+)";

    Pattern pattern = Pattern.compile(SUBSTITUTED_STRING_REGEXP);
    Matcher matcher = pattern.matcher(rawString);

    // Check if there is at least one "standard" formatter
    if (matcher.find()) {
      // There is, assume there are no ordered formatters in the string
      logger.finer("Parsing with standard formatters");
      // Reset the search since we'll also need to substitute the first occurrence later on.
      matcher.reset();
      orderedMode = false;
    } else {
      // There is none, assume there are only ordered formatters in the string
      logger.finer("Parsing with ordered formatters");
      pattern = Pattern.compile(orderedFormatterRegexp);
      matcher = pattern.matcher(rawString);
      orderedMode = true;
    }

    // For each formatter occurrence found in string...
    while (matcher.find()) {
      formattersCount++;
      String replaceStr = matcher.group();
      String substitution = "";

      if (replaceStr.equals("%%")) {
        substitution = "%";
      } else {
        if (orderedMode) {
          // Choose the argument basing on the ordered argument number, i.e. %3 will fetch the third
          // element from args
          curArg = Integer.parseInt(replaceStr.substring(1));
        } else {
          // Just go one by one
          curArg++;
        }

        if (curArg < args.length) {
          // If args is long enough to contain the argument we want, substitute it
          substitution = args[curArg];

          if (substitution.isEmpty()) {
            substitution = WatirUtils.ANY_MATCHER;
          }
        } else {
          // If args is too short, leave the formatter intact
          substitution = replaceStr;
        }
      }

      matcher.appendReplacement(buf, substitution);
    }
    matcher.appendTail(buf);

    if (formattersCount != (args.length - 1)) {
      logger.warning(String.format("Argument count incorrect for %s, got %d, expected %d",
                                   stringId, (args.length - 1), formattersCount));
    }

    String result = buf.toString();
    logger.finer("Final string: '" + result + "'");
    return result;
  }

  public String removeCR(String text) {
    // Hack to remove all the \r's as we sometimes get just \n and sometimes \r\n then the string
    // comparison doesn't work
    return text.replaceAll("(\\r)", "");
  }

  public String getOperaPath() {
    Response response = executeCommand(DesktopUtilsCommand.GET_OPERA_PATH, null);

    DesktopPath.Builder pathBuilder = DesktopPath.newBuilder();
    buildPayload(response, pathBuilder);
    DesktopPath string_path = pathBuilder.build();

    return string_path.getPath();
  }

  public String getLargePreferencesPath() {
    Response response = executeCommand(DesktopUtilsCommand.GET_LARGE_PREFERENCES_PATH, null);

    DesktopPath.Builder pathBuilder = DesktopPath.newBuilder();
    buildPayload(response, pathBuilder);
    DesktopPath stringPath = pathBuilder.build();

    return stringPath.getPath();
  }

  public String getSmallPreferencesPath() {
    Response response = executeCommand(DesktopUtilsCommand.GET_SMALL_PREFERENCES_PATH, null);

    DesktopPath.Builder pathBuilder = DesktopPath.newBuilder();
    buildPayload(response, pathBuilder);
    DesktopPath stringPath = pathBuilder.build();

    return stringPath.getPath();
  }

  public String getCachePreferencesPath() {
    Response response = executeCommand(DesktopUtilsCommand.GET_CACHE_PREFERENCES_PATH, null);

    DesktopPath.Builder pathBuilder = DesktopPath.newBuilder();
    buildPayload(response, pathBuilder);
    DesktopPath stringPath = pathBuilder.build();

    return stringPath.getPath();
  }

  public int getOperaPid() {
    Response response = executeCommand(DesktopUtilsCommand.GET_OPERA_PID, null);

    DesktopPid.Builder pathBuilder = DesktopPid.newBuilder();
    buildPayload(response, pathBuilder);
    DesktopPid desktopPid = pathBuilder.build();

    return desktopPid.getPid();
  }

}