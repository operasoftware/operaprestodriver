package com.opera.core.systems.scope.services;

import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;

public interface IDesktopUtils {
	void init();

	/*
	 * @return Full path to the Opera binary 
	 */
	String getOperaPath();

	/*
	 * @return List of full paths to each of the folders
	 * 			that contains Opera preferences
	 */
	List<String> getPreferencesPaths();
	
	/*
	 * Get the string specified by the given string id
	 *  
	 * @param enum_text
	 * @return
	 */
	String getString(String enum_text);

	// 
	/*
	 * Used to stop problems comparing strings that have 
	 * 		just \n with those with \r\n
	 *  
	 * @param text, to remove the \r's from
	 * @return string without \r's
	 */
	String removeCR(String text);
	
}
