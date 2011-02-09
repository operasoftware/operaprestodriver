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
package com.opera.core.systems.settings;

/**
 * Stores settings for OperaDriver.
 * 
 * @author Deniz Turkoglu
 *
 */
public class OperaDriverSettings {

	private boolean runOperaLauncherFromOperaDriver = true;
	private String operaLauncherBinary = null;
	private int    operaLauncherListeningPort = 9999;

	private String operaBinaryLocation;
	private String operaBinaryArguments = "";

	private Integer operaLauncherXvfbDisplay; //could be null

	private String largePrefsFolder;
	private String smallPrefsFolder;
	private String cachePrefsFolder;

	private boolean noRestart = false;
	private boolean noQuit = false;

	//Generated getters and setters:
	public boolean doRunOperaLauncherFromOperaDriver() {
		return runOperaLauncherFromOperaDriver;
	}
	/**
	 * Set whether OperaDriver should start Opera launcher. Defaults to
	 * <code>true</code>. Set to <code>false</code> if you will start Opera
	 * launcher manually.
	 * @param runOperaLauncherFromOperaDriver <code>true</code> to start Opera
	 * launcher, <code>false</code> to not.
	 */
	public void setRunOperaLauncherFromOperaDriver(
			boolean runOperaLauncherFromOperaDriver) {
		this.runOperaLauncherFromOperaDriver = runOperaLauncherFromOperaDriver;
	}
	public int getOperaLauncherListeningPort() {
		return operaLauncherListeningPort;
	}
	/**
	 * Set the port for Opera launcher to listen on. Defaults to port 9999.
	 * @param operaLauncherPort Port number to listen on
	 */
	public void setOperaLauncherListeningPort(int operaLauncherPort) {
		this.operaLauncherListeningPort = operaLauncherPort;
	}
	public String getOperaLauncherBinary() {
		return operaLauncherBinary;
	}
	/**
	 * Set the location of the Opera launcher binary.
	 * @param operaLauncherBinary The absolute path to the Opera launcher. 
	 */
	public void setOperaLauncherBinary(String operaLauncherBinary) {
		this.operaLauncherBinary = operaLauncherBinary;
	}
	public String getOperaBinaryLocation() {
		return operaBinaryLocation;
	}
	/**
	 * Set the path to the of Opera.
	 * @param operaBinaryLocation The absolute path to Opera.
	 */
	public void setOperaBinaryLocation(String operaBinaryLocation) {
		this.operaBinaryLocation = operaBinaryLocation;
	}
	public String getOperaBinaryArguments() {
		return operaBinaryArguments;
	}
	/**
	 * Set the arguments that will be passed to Opera.
	 * 
	 * For example:
	 * <code>opera -nosession opera:debug</code> to start without restoring a
	 * session (on *nix systems), and load the <code>opera:debug</code> page in
	 * a tab.
	 * @param operaBinaryArguments The arguments to pass, separated by spaces.
	 */
	public void setOperaBinaryArguments(String operaBinaryArguments) {
		this.operaBinaryArguments = operaBinaryArguments;
	}

	public Integer getOperaLauncherXvfbDisplay() {
		return operaLauncherXvfbDisplay;
	}
	/**
	 * Set the *nix display to start Opera on. This will be concatenated with
	 * ":" e.g. passing <code>1</code> will result in the command line option
	 * <code>-display :1</code> being passed.
	 * @param operaLauncherXvfbDisplay The X screen number to start Opera on.
	 */
	public void setOperaLauncherXvfbDisplay(Integer operaLauncherXvfbDisplay) {
		this.operaLauncherXvfbDisplay = operaLauncherXvfbDisplay;
	}

	/**
	 * @deprecated
	 * @param noQuit
	 */
	@Deprecated
	public void setNoQuit(boolean noQuit) {
		this.noQuit = noQuit;
	}
	/**
	 * @deprecated
	 * @return
	 */
	@Deprecated
	public boolean getNoQuit() {
		return noQuit;
	}

	public void setLargePrefsFolder(String largePrefsFolder) {
		this.largePrefsFolder = largePrefsFolder;
	}

	public String getLargePrefsFolder() {
		return largePrefsFolder;
	}

	public void setSmallPrefsFolder(String smallPrefsFolder) {
		this.smallPrefsFolder = smallPrefsFolder;
	}

	public String getSmallPrefsFolder() {
		return smallPrefsFolder;
	}

	public void setCachePrefsFolder(String cachePrefsFolder) {
		this.cachePrefsFolder = cachePrefsFolder;
	}

	public String getCachePrefsFolder() {
		return cachePrefsFolder;
	}
	
	/**
	 * @deprecated
	 * @param noRestart
	 */
	@Deprecated
	public void setNoRestart(boolean noRestart) {
		this.noRestart = noRestart;
	}
	/**
	 * @deprecated
	 * @return
	 */
	@Deprecated
	public boolean getNoRestart() {
		return noRestart;
	}

}
