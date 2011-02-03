package com.opera.core.systems.util;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import com.opera.core.systems.settings.OperaDriverSettings;

/**
 * Class to manage browser profile 
 *
 */
public class ProfileUtils {
	private String largePrefsFolder;
	private String smallPrefsFolder;
	private String cachePrefsFolder;
	
	public ProfileUtils(OperaDriverSettings settings){
		this.largePrefsFolder = settings.getLargePrefsFolder();
		this.smallPrefsFolder = settings.getSmallPrefsFolder();
		this.cachePrefsFolder = settings.getCachePrefsFolder();
	}
	
	public void deleteProfile() {
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
	
	public boolean deleteFolder(String folderPath) {
		return FileUtils.deleteQuietly(new File(folderPath));
	}
}
