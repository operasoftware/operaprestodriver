package com.opera.core.systems.profile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.opera.core.systems.settings.OperaDriverSettings;


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
		deleteFolder(largePrefsFolder);
		deleteFolder(cachePrefsFolder);
	}
	
	public void copyProfile(String newPrefs) {
		// For now, copy all to smallPrefsFolder
		copyFolder(newPrefs, smallPrefsFolder);
	}
	
	public boolean deleteFolder(String folderPath) {
		File dir = new File(folderPath);
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteFolder(folderPath + children[i]);
				if (!success) {
					//return false;
				}
	        }
		}
        // The directory/file is now empty so delete it
		boolean res = dir.delete();
        return res;
	}
	
	public void copyFolder(String from, String to) {
		try {
			copyFolder(new File(from), new File(to));
		} catch (IOException ex) {
			
		}
	}
	
	public void copyFolder(File srcFile, File dstFile) throws IOException {
		if (srcFile.isDirectory()){
			// Create destination folder if needed
			if (!dstFile.exists()){
				dstFile.mkdir();
			}
			
			String files[] = srcFile.list();
			for(int i = 0; i < files.length; i++){
				copyFolder(new File(srcFile, files[i]), new File(dstFile, files[i]));
			}
		}
		else {
			/*if(!srcFile.exists()){
				System.out.println("File or directory " + srcFile.getAbsolutePath() + " does not exist.");
			}
			else*/
			{
				InputStream in = new FileInputStream(srcFile);
				OutputStream out = new FileOutputStream(dstFile); 

				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				
				in.close();
				out.close();
			}
		}
	}
}
