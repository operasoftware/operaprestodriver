package com.opera.core.systems.scope.services;


public interface IDesktopUtils {
	void init();

	/**
	 * @return Full path to the Opera binary 
	 */
	String getOperaPath();

	/**
	 * @return Full path to large preferences folder
	 */
	String getLargePreferencesPath();

	/**
	 * @return Full path to small preferences folder
	 */
	String getSmallPreferencesPath();

	/**
	 * @return Full path to cache preferences folder
	 */
	String getCachePreferencesPath();

	/**
	 * Gets the string specified by the given string id.
	 *
	 * @param enum_text
	 * @return string corresponding to the enumText
	 */
	String getString(String enumText);


	/**
	 * Used to stop problems comparing strings that have 
	 * 		just \n with those with \r\n.
	 *
	 * @param text, to remove the \r's from
	 * @return string without \r's
	 */
	String removeCR(String text);

	/**
	 * Gets process id of connected Opera instance.
	 * 
	 * @return pid
	 */
	int getOperaPid();

}
