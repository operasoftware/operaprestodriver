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
		System.out.println("[ProfileUtils]: deleteFolder(" + folderPath + ")");
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
		System.out.println("File.exists? " + dir.exists());
		System.out.println("Can we write/change the file? " + dir.canWrite());
		boolean res = dir.delete();
		System.out.println("Dir.delete() " + folderPath + " => " + res);
        return res;
	}
	
	public void copyFolder(String from, String to) {
		System.out.println("[ProfileUtils]: copyFolder(from=" + from + "), to=" + to + ")");
		try {
			copyFolder(new File(from), new File(to));
		} catch (IOException ex) {
			
		}
	}
	
	public void copyFolder(File srcFile, File dstFile) throws IOException {
		System.out.println("[ProfileUtils]: copyFolder[2](" + srcFile.getAbsolutePath() + ", " + dstFile.getAbsolutePath() + ")");
		if (srcFile.isDirectory()){
			System.out.println(srcFile.getAbsolutePath() + " is a directory");
			// Create destination folder if needed
			if (!dstFile.exists()){
				System.out.println(dstFile.getAbsolutePath() + " exists? " + dstFile.exists());
				System.out.println("Create it");
				dstFile.mkdir();
				System.out.println(dstFile.getAbsolutePath() + " exists now? " + dstFile.exists());
			}
			
			String files[] = srcFile.list();
			for(int i = 0; i < files.length; i++){
				copyFolder(new File(srcFile, files[i]), new File(dstFile, files[i]));
			}
		}
		else {
			if(!srcFile.exists()){
				System.out.println("File or directory " + srcFile.getAbsolutePath() + " does not exist.");
			}
			else
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
