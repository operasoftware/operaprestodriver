/* Copyright (C) 2009-2011 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.model;

/**
 * A canvas object to use in taking screenshots of Opera's viewport.
 * 
 */
public class Canvas {
	protected int x;
	protected int y;
	protected int width;
	protected int height;

	// Should the screenshot be relative to top of the page or viewport
	protected boolean viewPortRelative;

	public Canvas(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public Canvas() {
	}

	public boolean isViewPortRelative() {
		return viewPortRelative;
	}

	/**
	 * Sets the screenshots to be relative to screen or to window, default is
	 * window.
	 * 
	 * @param viewPortRelative true if relative to window
	 */
	public void setViewPortRelative(boolean viewPortRelative) {
		this.viewPortRelative = viewPortRelative;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Canvas))
			return false;

		Canvas canvas = (Canvas) obj;
		return (getX() == canvas.getX() && getY() == canvas.getY()
				&& getHeight() == canvas.getHeight()
				&& getWidth() == canvas.getWidth());
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		return "Canvas [x=" + x + ", y=" + y + ", width=" + width + ", height="
				+ height + ", viewPortRelative=" + viewPortRelative + "]";
	}
}
