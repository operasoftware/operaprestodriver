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

public class OperaDriverSettings {

	private boolean runOperaLauncherFromOperaDriver = true;
	private String operaLauncherBinary = null;
	private int    operaLauncherListeningPort = 9999;

	private String operaBinaryLocation;
	private String operaBinaryArguments;

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
	public void setRunOperaLauncherFromOperaDriver(
			boolean runOperaLauncherFromOperaDriver) {
		this.runOperaLauncherFromOperaDriver = runOperaLauncherFromOperaDriver;
	}
	public int getOperaLauncherListeningPort() {
		return operaLauncherListeningPort;
	}
	public void setOperaLauncherListeningPort(int operaLauncherPort) {
		this.operaLauncherListeningPort = operaLauncherPort;
	}
	public String getOperaLauncherBinary() {
		return operaLauncherBinary;
	}
	public void setOperaLauncherBinary(String operaLauncherBinary) {
		this.operaLauncherBinary = operaLauncherBinary;
	}
	public String getOperaBinaryLocation() {
		return operaBinaryLocation;
	}
	public void setOperaBinaryLocation(String operaBinaryLocation) {
		this.operaBinaryLocation = operaBinaryLocation;
	}
	public String getOperaBinaryArguments() {
		return operaBinaryArguments;
	}
	public void setOperaBinaryArguments(String operaBinaryArguments) {
		this.operaBinaryArguments = operaBinaryArguments;
	}

	public Integer getOperaLauncherXvfbDisplay() {
		return operaLauncherXvfbDisplay;
	}
	public void setOperaLauncherXvfbDisplay(Integer operaLauncherXvfbDisplay) {
		this.operaLauncherXvfbDisplay = operaLauncherXvfbDisplay;
	}

	public void setNoQuit(boolean noQuit) {
		this.noQuit = noQuit;
	}
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

	public void setNoRestart(boolean noRestart) {
		this.noRestart = noRestart;
	}

	public boolean getNoRestart() {
		return noRestart;
	}

}
