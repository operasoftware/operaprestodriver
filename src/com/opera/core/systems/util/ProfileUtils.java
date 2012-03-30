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

package com.opera.core.systems.util;

import com.google.common.io.Files;

import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.OperaSettings;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.io.FileHandler;

import java.io.File;
import java.io.IOException;

/**
 * Class to manage browser profile.
 */
public class ProfileUtils {

  private String largePrefsFolder;
  private String smallPrefsFolder;
  private String cachePrefsFolder;
  private OperaSettings settings;

  public ProfileUtils(String largePrefsFolder, String smallPrefsFolder, String cachePrefsFolder,
                      OperaSettings settings) {
    this.settings = settings;
    this.largePrefsFolder = largePrefsFolder;
    this.smallPrefsFolder = smallPrefsFolder;
    this.cachePrefsFolder = cachePrefsFolder;
  }

  public boolean isMac() {
    return System.getProperty("os.name").startsWith("Mac");
  }

  public boolean isWindows() {
    return System.getProperty("os.name").startsWith("Windows");
  }

  public boolean isMainProfile(String prefsPath) {
    File prefsFile = new File(prefsPath);
    String absolutePrefsPath = prefsFile.getAbsolutePath();

    // Get user home
    String path = System.getProperty("user.home");

    if (isMac()) {
      /* Mac
                         * ~/Library/Application Support/Opera
                         * ~/Library/Caches/Opera
                         * ~/Library/Preferences/Opera Preferences
                         */
      File appSupport = new File(path + "/Library/Application Support/Opera");
      File cache = new File(path + "/Library/Caches/Opera");
      File prefs = new File(path + "/Library/Preferences/Opera Preference");

      // Check if profiles start with this path
      if (absolutePrefsPath.startsWith(appSupport.getAbsolutePath()) ||
          absolutePrefsPath.startsWith(cache.getAbsolutePath()) ||
          absolutePrefsPath.startsWith(prefs.getAbsolutePath())) {
        return true;
      }

    } else if (isWindows()) {

      // On XP and Vista/7:
      String appData = System.getenv("APPDATA");
      File appFile = new File(appData + "\\Opera");
      if (absolutePrefsPath.startsWith(appFile.getAbsolutePath())) {
        return true;
      }

      // On XP:
      String homeDrive = System.getenv("HOMEDRIVE");
      String homePath = System.getenv("HOMEPATH");
      File homeOpera = new File(homeDrive + homePath + "\\Local Settings\\Application Data\\Opera");
      if (absolutePrefsPath.startsWith(homeOpera.getAbsolutePath())) {
        return true;
      }

      // In Vista/7:
      String localAppData = System.getenv("LOCALAPPDATA");
      File localAppDataFile = new File(localAppData + "\\Opera");
      if (absolutePrefsPath.startsWith(localAppDataFile.getAbsolutePath())) {
        return true;
      }

      // On all Windows systems, <Installation Path>\profile:
      File profileFolder = new File(settings.getBinary().getParent() + "\\profile");

      //a/b/c/exe
      //a/b/c/profile
      if (prefsFile.equals(profileFolder)) {
        return true;
      }

    } else {

      /* *nix */
      File dotOpera = new File(path + "/.opera");
      if (/*platform nix && */ prefsFile.equals(dotOpera)) {
        return true;
      }

    }

    return false;
  }

  /**
   * Deletes prefs folders for Does nothing if prefs folders are default main user profile
   */
  public boolean deleteProfile() {
    // Assuming if any of those are main profile, skip the whole delete
    if (isMainProfile(smallPrefsFolder) ||
        isMainProfile(largePrefsFolder) ||
        isMainProfile(cachePrefsFolder)) {
      return false;
    }

    boolean deleted = deleteFolder(smallPrefsFolder);
    if (deleted && !smallPrefsFolder.equals(largePrefsFolder)) {
      deleted = deleteFolder(largePrefsFolder);
    }
    if (deleted && !smallPrefsFolder.equals(cachePrefsFolder) && !largePrefsFolder
        .equals(cachePrefsFolder)) {
      deleted = deleteFolder(cachePrefsFolder);
    }
    return deleted;
    // TODO: logger.warning("Could not delete profile");
  }

  /**
   * @return true if profile was copied, else false
   */
  public boolean copyProfile(String newPrefs) {
    if (new File(newPrefs).exists() == false) {
      return false;
    }

    try {
      Files.copy(new File(newPrefs), new File(smallPrefsFolder));
    } catch (IOException e) {
      // Ignore
      // e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * @return true if folder was deleted, else false
   */
  private boolean deleteFolder(String folderPath) {
    //true if the file or directory was deleted, otherwise false
    return FileHandler.delete(new File(folderPath));
  }

}