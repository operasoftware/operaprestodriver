package com.opera.core.systems;

import java.awt.Dimension;
import java.awt.Point;

import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo.DesktopWindowType;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowRect;

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
	
	/**
	 * @return true if widget is visible
	 */
	public boolean isOnScreen(){
		return info.getOnScreen(); 
	}
	
	/**
	 * @return string which is the type of the widget
	 */
	public DesktopWindowType getType(){
		return info.getWindowType(); 
	}
	
	/**
	 * @return DesktopWindowRect of the widget
	 */
	private DesktopWindowRect getRect() {
		return info.getRect();
	}
	
	/**
	 * @return Point describing location of widget
	 */
	public Point getLocation() {
		DesktopWindowRect rect = getRect();
		return new Point(rect.getX(), rect.getY());
	}

	/**
	 * @return size of widget
	 */
	public Dimension getSize() {
		DesktopWindowRect rect = getRect();
		return new Dimension(rect.getWidth(), rect.getHeight());
	}
	
	@Override
	// TODO: FIXME
	public boolean equals(Object obj) {
		if (obj == this) return true;
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
