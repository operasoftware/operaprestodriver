/*
Copyright 2008-2011 Opera Software ASA

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

import java.io.File;
import java.util.logging.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.io.FileHandler;
import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.settings.OperaDriverSettings;

/**
 * Class to manage browser profile
 *
 */
public class ProfileUtils {
	private String largePrefsFolder;
	private String smallPrefsFolder;
	private String cachePrefsFolder;
	private Capabilities capabilities;

	protected final Logger logger = Logger.getLogger(this.getClass().getName());

	@Deprecated
	public ProfileUtils(String largePrefsFolder, String smallPrefsFolder, String cachePrefsFolder, OperaDriverSettings settings) {
		this(largePrefsFolder, smallPrefsFolder, cachePrefsFolder, settings.getCapabilities());
	}

	public ProfileUtils(String largePrefsFolder, String smallPrefsFolder, String cachePrefsFolder, Capabilities capabilities) {
		this.capabilities = capabilities;
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
					absolutePrefsPath.startsWith(prefs.getAbsolutePath()))
				return true;

		} else if (isWindows()) {

			// On XP and Vista/7:
			String appData = System.getenv("APPDATA");
			File appFile = new File(appData + "\\Opera");
			if (absolutePrefsPath.startsWith(appFile.getAbsolutePath()))
				return true;

			// On XP:
			String homeDrive = System.getenv("HOMEDRIVE");
			String homePath = System.getenv("HOMEPATH");
			File homeOpera = new File(homeDrive + homePath + "\\Local Settings\\Application Data\\Opera");
			if (absolutePrefsPath.startsWith(homeOpera.getAbsolutePath()))
				return true;

			// In Vista/7:
			String localAppData = System.getenv("LOCALAPPDATA");
			File localAppDataFile = new File(localAppData + "\\Opera");
			if (absolutePrefsPath.startsWith(localAppDataFile.getAbsolutePath()))
				return true;

			// On all Windows systems, <Installation Path>\profile:
			File exeFile = new File((String) capabilities.getCapability(OperaDriver.BINARY));
			String parentPath = exeFile.getParent();
			File profileFolder = new File(parentPath + "\\profile");

			//a/b/c/exe
			//a/b/c/profile
			if (prefsFile.equals(profileFolder))
				return true;

		} else {

			/* *nix */
			File dotOpera = new File(path + "/.opera");
			if (/*platform nix && */ prefsFile.equals(dotOpera))
				return true;

		}

		return false;
	}

	/**
	 * Deletes prefs folders for
	 * Does nothing if prefs folders are default main user profile
	 */
	public boolean deleteProfile() {
    String[] profile_dirs = {smallPrefsFolder, largePrefsFolder, cachePrefsFolder};

    // Assuming if any of those are main profile, skip the whole delete
    for (int i = 0; i < profile_dirs.length; i++)
    {
      if (isMainProfile(profile_dirs[i]))
      {
        logger.finer("Skipping profile deletion since '" + profile_dirs[i] + "' is the main profile.");
        return false;
      }
    }

    for (int i = 0; i < profile_dirs.length; i++)
    {
      String current_dir = profile_dirs[i];

      File current_dir_handle = new File(current_dir);

      if (!current_dir_handle.exists())
      {
        logger.finer("Skipping profile deletion for '" + current_dir + "' since it doesn't exist.");
        continue;
      }

      boolean deleted = deleteFolder(current_dir);
      if (!deleted)
      {
        int retry_interval_ms = 500;
        int retry_max_count = 10;
        int retry_count = 0;
        boolean ok = false;

        logger.warning("Profile could not be deleted, retrying...");

        do
        {
          try {
            Thread.sleep(retry_interval_ms);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

          ok = deleteFolder(current_dir);
          retry_count++;
          if (retry_count > retry_max_count)
            break;

        } while (!ok);

        if (!ok)
        {
          logger.severe("Could not delete profile in '" + current_dir + "'. Skipping further deletion.");
          return false;
        }
        else
          logger.warning("Deleted profile, retry count = " + retry_count);
      }
      else
      {
        logger.finer("Deleted profile in '" + current_dir + "'");
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
		if (new File(newPrefs).exists() == false) {
			return false;
		}

		logger.info("Copying profile from " + newPrefs);
		return WatirUtils.CopyDirAndFiles(newPrefs, smallPrefsFolder);
	}

	/**
	 *
	 * @param folderPath
	 * @return true if folder was deleted, else false
	 */
	private boolean deleteFolder(String folderPath) {
		//true if the file or directory was deleted, otherwise false
    return FileHandler.delete(new File(folderPath));
	}
}
