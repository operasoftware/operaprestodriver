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

package com.opera.core.systems.internal;

import com.opera.core.systems.OperaSettings;

import org.openqa.selenium.io.FileHandler;

import java.io.File;
import java.util.logging.Logger;

/**
 * Class to manage browser profile.
 */
public class ProfileUtils {
  private String largePrefsFolder;
  private String smallPrefsFolder;
  private String cachePrefsFolder;
  private OperaSettings settings;

  protected final Logger logger = Logger.getLogger(this.getClass().getName());

  public ProfileUtils(String largePrefsFolder, String smallPrefsFolder, String cachePrefsFolder, OperaSettings settings) {
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
      File exeFile = settings.getBinary();
      String parentPath = exeFile.getParent();
      File profileFolder = new File(parentPath + "\\profile");

      //a/b/c/exe
      //a/b/c/profile
      if (prefsFile.equals(profileFolder)) {
        return true;
      }
    }
    else {
      /* *nix */
      File dotOpera = new File(path + "/.opera");
      if (/*platform nix && */ prefsFile.equals(dotOpera)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Deletes prefs folders for
   * Does nothing if prefs folders are default main user profile
   */
  public boolean deleteProfile() {
    String[] profileDirs = {smallPrefsFolder, largePrefsFolder, cachePrefsFolder};

    // Assuming if any of those are main profile, skip the whole delete
    for (String profileDir: profileDirs) {
      if (isMainProfile(profileDir)) {
        logger.finer("Skipping profile deletion since '" + profileDir + "' is the main profile.");
        return false;
      }
    }

    for (String profileDir: profileDirs) {
      File currentDirHandle = new File(profileDir);

      if (!currentDirHandle.exists()) {
        logger.finer("Skipping profile deletion for '" + profileDir + "' since it doesn't exist.");
        continue;
      }

      boolean deleted = deleteFolder(profileDir);
      if (!deleted) {
        final int retryIntervalMs = 500;
        final int retryMaxCount = 10;
        int retryCount = 0;
        boolean ok = false;

        logger.warning("Profile could not be deleted, retrying...");

        do {
          try {
            Thread.sleep(retryIntervalMs);
          } catch (InterruptedException e) {
            // fall through
          }

          ok = deleteFolder(profileDir);
          retryCount++;
          if (retryCount > retryMaxCount) {
            break;
          }

        } while (!ok);

        if (!ok) {
          logger.severe("Could not delete profile in '" + profileDir + "'. Skipping further deletion.");
          return false;
        }
        else {
          logger.warning("Deleted profile, retry count = " + retryCount);
        }
      }
      else {
        logger.finer("Deleted profile in '" + profileDir + "'");
      }
    }
    return true;
  }

  /**
   *
   * @param newPrefs
   * @return true if profile was copied, else false
   */
  public boolean copyProfile(String newPrefs) {
    File newPrefsFile = new File(newPrefs);
    if (!newPrefsFile.exists()) {
      logger.warning("The directory '" + newPrefs + "' doesn't exist, failed to copy profile.");
      return false;
    }

    File smallPrefsFolderFile = new File(smallPrefsFolder);

    logger.finer("Copying profile from '" + newPrefs + "'");
    return WatirUtils.copyDirAndFiles(newPrefsFile, smallPrefsFolderFile);
  }

  /**
   *
   * @param folderPath
   * @return true if folder was deleted, else false
   */
  private boolean deleteFolder(String folderPath) {
    return FileHandler.delete(new File(folderPath));
  }
}
