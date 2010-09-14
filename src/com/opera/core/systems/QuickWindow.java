package com.opera.core.systems;

import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;

public class QuickWindow {
	
	private final DesktopWindowInfo info;
	
	public QuickWindow(DesktopWindowInfo info) {
        this.info = info;
    }
	
	public int getObjectId() {
		return getWindowID();
	}
	
	/**
	 * @return window id of this window
	 */
	public int getWindowID() {
		return info.getWindowID();
	}
	
	/**
	 * @return title of window
	 */
	public String getTitle() {
		return info.getTitle();
	}

	@Override
	// TODO: FIXME
	public boolean equals(Object obj) {
		if(obj instanceof QuickWindow) {
			QuickWindow ref = (QuickWindow) obj;
			return (((QuickWindow)ref).getObjectId() == this.getObjectId());
		}
		return false;
	}
	
	@Override
	// TODO: FIXME
	public int hashCode() {
		int result = 42;
		result = 31 * result + getObjectId();
		return result;
	}
	
	@Override
	public String toString() {
		return "QuickWindow " + getWindowID() + ", " + getTitle();
	}

}
