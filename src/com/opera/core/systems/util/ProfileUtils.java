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
import java.io.IOException;
import org.apache.commons.io.FileUtils;

/**
 * Class to manage browser profile
 *
 */
public class ProfileUtils {
	private String largePrefsFolder;
	private String smallPrefsFolder;
	private String cachePrefsFolder;

	public ProfileUtils(String largePrefsFolder, String smallPrefsFolder, String cachePrefsFolder){
		this.largePrefsFolder = largePrefsFolder;
		this.smallPrefsFolder = smallPrefsFolder;
		this.cachePrefsFolder = cachePrefsFolder;
	}
	
	public boolean isMainProfile(String prefsPath) {
		// TODO: Get platform
		
		
		File prefsFile = new File(prefsPath);
		// Get user home
		String path = System.getProperty("user.home");
		
		//String os = System.getProperty("os.name");
		
		/* *nix */
		File dotOpera = new File(path + "/.opera");
		
		if (/*platform nix && */ prefsFile.equals(dotOpera))
			return true;
		
		/* Mac
		 * ~/Library/Application Support/Opera 
		 * ~/Library/Caches/Opera 
		 * ~/Library/Preferences/Opera Preferences
		 */
		File appSupport = new File(path + "/Library/Application Support/Opera");
		File cache = new File(path + "/Library/Caches/Opera");
		File prefs = new File(path + "/Library/Preferences/Opera Preference");
		
		if (/* platform mac && */ prefsFile.equals(appSupport) || 
				prefsFile.equals(cache) ||
				prefsFile.equals(prefs))
			return true;
		
		/* TODO: Windows */
		
		return false;
	}

	/**
	 * Deletes prefs folders for
	 * Does nothing if prefs folders are default main user profile
	 */
	public void deleteProfile() {
		// Assuming if any of those are main profile, skip the whole delete
		if (isMainProfile(smallPrefsFolder) ||
				isMainProfile(largePrefsFolder) ||
				isMainProfile(cachePrefsFolder))
		{
			return;
		}
		
		deleteFolder(smallPrefsFolder);
		if (!smallPrefsFolder.equals(largePrefsFolder))
			deleteFolder(largePrefsFolder);
		if (!smallPrefsFolder.equals(cachePrefsFolder) && !largePrefsFolder.equals(cachePrefsFolder))
			deleteFolder(cachePrefsFolder);
	}

	public void copyProfile(String newPrefs) {
		try {
			FileUtils.copyDirectory(new File(newPrefs), new File(smallPrefsFolder));
		} catch (IOException e) {
			// Ignore
			// e.printStackTrace();
		}

	}

	private boolean deleteFolder(String folderPath) {
		return FileUtils.deleteQuietly(new File(folderPath));
	}
}
