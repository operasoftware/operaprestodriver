package com.opera.core.systems;

import java.awt.Dimension;
import java.awt.Point;

import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo.DesktopWindowType;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowRect;

/**
 * A window in the Desktop UI
 *
 * @author Adam Minchinton, Karianne Ekern 
 *
 */
public class QuickWindow {

	private final DesktopWindowInfo info;

	/**
	 * Constructor.
	 *
	 * @param info DesktopWindowInfo describing the window
	 */
	public QuickWindow(DesktopWindowInfo info) {
        this.info = info;
    }

	/**
	 * @return window id of this window
	 */
	public int getWindowID() {
		return info.getWindowID();
	}

	/**
	 * @return title of this window
	 */
	public String getTitle() {
		return info.getTitle();
	}


	/**
	 * @return name of this window
	 */
	public String getName() {
		return info.getName();
	}

	/**
	 * @return true if the window is on screen, otherwise false
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
	public String toString() {
		return "QuickWindow " + getWindowID() + ", " + getName() + ", " + getTitle();
	}

}
