package com.opera.core.systems.settings;

public class OperaDriverSettings {

	private boolean runOperaLauncherFromOperaDriver = true;
	private String operaLauncherBinary = null;
	private int    operaLauncherListeningPort = 9999; 
	
	private String operaBinaryLocation;
	private String operaBinaryArguments;
	
	private Integer operaLauncherXvfbDisplay; //could be null
	
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
}
