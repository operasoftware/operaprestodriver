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
	 * @return name of window
	 */
	public String getName() {
		return info.getName();
	}

	/**
	 * @return true if window is on screen
	 */
	public boolean isOnScreen() {
		return info.getOnScreen();
	}

	/**
	 * @return the type of the window
	 */
	public DesktopWindowType getType() {
		return info.getWindowType();
	}

	/**
	 * @return DesktopWindowRect of the window
	 */
	public DesktopWindowRect getRect() {
		return info.getRect();
	}

	/**
	 * @return Point describing location of window
	 */
	public Point getLocation() {
		DesktopWindowRect rect = getRect();
		return new Point(rect.getX(), rect.getY());
	}

	/**
	 * @return size of this window
	 */
	public Dimension getSize() {
		DesktopWindowRect rect = getRect();
		return new Dimension(rect.getWidth(), rect.getHeight());
	}

	@Override
	// TODO: FIXME
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof QuickWindow) {
			QuickWindow ref = (QuickWindow) obj;
			return (ref.getObjectId() == this.getObjectId());
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
