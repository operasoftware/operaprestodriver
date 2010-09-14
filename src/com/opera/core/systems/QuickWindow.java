package com.opera.core.systems;

import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;

public class QuickWindow {
	
	private final DesktopWindowInfo info; 
	
	/* 
	 */
	public int getObjectId() {
		return getWindowID();
	}
	
	/**
	 * 
	 * @return
	 */
	public int getWindowID() {
		return info.getWindowID();
	}
	
	public QuickWindow(DesktopWindowInfo info) {
        this.info = info;
    }
	
	public String getTitle() {
		return info.getTitle();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof QuickWindow) {
			QuickWindow ref = (QuickWindow) obj;
			return (((QuickWindow)ref).getObjectId() == this.getObjectId());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int result = 42;
		result = 31 * result + getObjectId();
		return result;
	}
	
	@Override
	public String toString() {
		return "quickwindow" + hashCode();
	}

}
