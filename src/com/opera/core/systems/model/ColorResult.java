/* Copyright (C) 2009-2011 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.model;

/**
 * Represents a result from screenwathcer that identifies
 * if a the color we were looking for matched, and number of
 * matches if any
 *
 */
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
