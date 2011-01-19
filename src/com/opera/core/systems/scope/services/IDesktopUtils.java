package com.opera.core.systems.scope.services;


public interface IDesktopUtils {
	void init();

	/*
	 * @return Full path to the Opera binary 
	 */
	String getOperaPath();

	/*
	 * @return Full path to large preferences folder
	 */
	String getLargePreferencesPath();

	/*
	 * @return Full path to small preferences folder
	 */
	String getSmallPreferencesPath();

	/*
	 * @return Full path to cache preferences folder
	 */
	String getCachePreferencesPath();

	/*
	 * Get the string specified by the given string id
	 *
	 * @param enum_text
	 * @return
	 */
	String getString(String enum_text);


	/*
	 * Used to stop problems comparing strings that have 
	 * 		just \n with those with \r\n
	 *
	 * @param text, to remove the \r's from
	 * @return string without \r's
	 */
	String removeCR(String text);

	/*
	 * Get Pid of connected Opera instance
	 * @return
	 */
	int getOperaPid();

}
