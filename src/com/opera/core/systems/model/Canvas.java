package com.opera.core.systems.model;

public class Canvas {
	protected int x;
	protected int y;
	protected int w;
	protected int h;
	
	public Canvas(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	
	public Canvas() {
	}
	
	protected boolean viewPortRelative;


	public boolean isViewPortRelative() {
		return viewPortRelative;
	}

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

	public int getW() {
		return w;
	}

	public void setW(int w) {
		this.w = w;
	}

	public int getH() {
		return h;
	}

	public void setH(int h) {
		this.h = h;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Canvas))
			return false;
		
		Canvas canvas = (Canvas) obj;
		return (getX() == canvas.getX() && getY() == canvas.getY() &&
				getH() == canvas.getH() && getW() == canvas.getW());
	}
	
	@Override
	public int hashCode() {
		// TODO implement
		return super.hashCode();
	}

	@Override
	public String toString() {
		return "Canvas [x=" + x + ", y=" + y + ", w=" + w + ", h=" + h
				+ ", viewPortRelative=" + viewPortRelative + "]";
	}
}
