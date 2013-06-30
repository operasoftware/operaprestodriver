/*
Copyright 2013 Software Freedom Conservancy

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

import org.openqa.selenium.WebDriverException;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * OperaExtensions is a class which takes a profile directory path, and manages the installation
 * and custom extensions. It is fully responsible for the custom extensions, from installation to
 * uninstallation and clean-up. Its API is exposed through the {@link OperaProfile} class.
 *
 * @see OperaProfile
 */
class OperaExtensions {

  private File directory;
  private File widgetsDat;
  private File backupWidgetDat;

  private static final String WUID_PREFIX = "wuid-operadriver-";
  private static final String OEX_PATH_PREFIX = "operadriver-";

  private static final String WIDGET_DAT_CONTENT =
      "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
    + "<preferences>\n"
    + "</preferences>\n";

  private static final String WIDGET_DAT_ITEM =
      "  <section id=\"%s\">\n"                                                   // %s = widgetID
    + "    <value id=\"path to widget data\" xml:space=\"preserve\">%s</value>\n" // %s = oex path
    + "    <value id=\"download_URL\" null=\"yes\"/>\n"
    + "    <value id=\"content-type\" xml:space=\"preserve\">3</value>\n"
    + "    <value id=\"class state\" xml:space=\"preserve\">enabled</value>\n"
    + "  </section>\n";

  private static final String WIDGET_PREFS_DAT_CONTENT =
      "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
    + "<preferences>\n"
    + "  <section id=\"ui\">\n"
    + "    <value id=\"default-prefs-applied\" xml:space=\"preserve\">1</value>\n"
    + "  </section>\n"
    + "  <section id=\"%s\">\n"                                                   // %s = widgetID
    + "    <value id=\"network_access\" xml:space=\"preserve\">24</value>\n"
    + "  </section>\n"
    + "  <section id=\"user\">\n"
    + "    <value id=\"GadgetRunOnSecureConn\" xml:space=\"preserve\">yes</value>\n"
    + "    <value id=\"GadgetEnabledOnStartup\" xml:space=\"preserve\">yes</value>\n"
    + "  </section>\n"
    + "</preferences>\n";

  /**
   * Create a new manager for custom Opera extensions.
   *
   * @param profileDirectory An Opera profile directory. This directory must exist.
   */
  OperaExtensions(File profileDirectory) {
    // Extensions will be installed here.
    directory = new File(profileDirectory, "widgets");
    widgetsDat = new File(directory, "widgets.dat");
    if (widgetsDat.exists()) {
      try {
        backupWidgetDat = File.createTempFile("operadriver-", "-widgets.dat");
        Files.copy(widgetsDat, backupWidgetDat);
      } catch (IOException e) {
        throw new WebDriverException("Unable to create a back-up of " + widgetsDat.getPath());
      }
    }
  }

  /**
   * Adds an extension to the profile. The file is expected to be an Opera 12 extension file (OEX).
   * @throws IOException If the extension files cannot be written to the profile.
   */
  public void addExtension(File extensionToInstall) throws IOException {
    verifyPathIsOEX(extensionToInstall);
    createInitialDirectoryIfNecessary();

    String wuid = WUID_PREFIX + UUID.randomUUID().toString();
    // the name is guaranteed to end with .oex, enforced earlier by the verifyPathIsOEX method.
    File oexfile = File.createTempFile(OEX_PATH_PREFIX, extensionToInstall.getName(), directory);

    Files.copy(extensionToInstall, oexfile);
    writeOEXtoWidgetsDat(wuid, oexfile);
    createOEXDirectory(wuid);
  }

  /**
   * Remove all custom extensions
   */
  public void cleanUp() {
    // Removing the entries from widgets.dat suffices. Opera will remove the other files.
    // These other files are:
    // widgets/xxx.oex
    // widgets/OEX_PATH_PREFIX/
    if (backupWidgetDat != null) {
      try {
        Files.move(backupWidgetDat, widgetsDat);
      } catch (IOException e) {
        throw new WebDriverException("Unable to restore widgets.dat");
      }
    } else {
      widgetsDat.delete();
    }
  }

  /**
   * Verify that the given path exists and is an OEX file.
   */
  private void verifyPathIsOEX(File path) {
    checkNotNull(path);
    checkArgument(path.exists(), "%s does not exist", path.getAbsolutePath());
    checkArgument(!path.isDirectory(), "%s is a directory", path.getAbsolutePath());
    checkArgument(path.getName().endsWith(".oex"), "%s does not end with .oex", path.getName());
  }

  /**
   * Create initial widgets directory and extension configuration file (widgets.dat) if needed.
   * @throws IOException If 
   */
  private void createInitialDirectoryIfNecessary() throws IOException {
    if (!directory.exists()) {
      if (!directory.mkdirs()) {
        throw new WebDriverException("Unable to create directory path: " + directory.getPath());
      }
    }
    if (!widgetsDat.exists()) {
      Files.write(WIDGET_DAT_CONTENT, widgetsDat, Charsets.UTF_8);
    }
  }

  /**
   * Install the extension by writing to widgets.dat
   * @param wuid Unique identifier of the widget.
   * @param oexpath Location of .oex file within profile
   */
  private void writeOEXtoWidgetsDat(String wuid, File oexpath) throws IOException {
    // Define a path relative to the profile directory. Don't use an absolute path,
    // because that will break when used with the Remote web driver.
    String oexpathRelative = "{LargePreferences}widgets/" + oexpath.getName();

    String widgetString = Files.toString(widgetsDat, Charsets.UTF_8);
    int beforePreferencesEndTag = widgetString.lastIndexOf("</preferences>");
    if (beforePreferencesEndTag == -1) {
      throw new WebDriverException("</preferences> not found in " + widgetsDat.getPath());
    }
    String widgetDatSection = String.format(WIDGET_DAT_ITEM, wuid, oexpathRelative);
    // Insert the new <section ..> ... </section> before </preferences>
    widgetString = widgetString.substring(0, beforePreferencesEndTag)
                 + widgetDatSection
                 + widgetString.substring(beforePreferencesEndTag);
    Files.write(widgetString, widgetsDat, Charsets.UTF_8);
  }

  /**
   * Create the minimal directory structure and prefs.dat for an Opera extension.
   * @param wuid ID of extension as known in widgets.dat
   */
  private void createOEXDirectory(String wuid) throws IOException {
    File oexDirectory = new File(directory, wuid);
    if (!oexDirectory.exists() && !oexDirectory.mkdirs()) {
        throw new WebDriverException("Unable to create directory path: " + directory.getPath());
    }
    File prefsDatPath = new File(oexDirectory, "prefs.dat");
    String prefContent = String.format(WIDGET_PREFS_DAT_CONTENT, wuid);
    Files.write(prefContent, prefsDatPath, Charsets.UTF_8);
  }
}
