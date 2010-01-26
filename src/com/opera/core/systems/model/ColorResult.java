package com.opera.core.systems.model;

public class ColorResult {
	protected int id;
	protected int count;

	public ColorResult(int count, int id) {
		this.count = count;
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
