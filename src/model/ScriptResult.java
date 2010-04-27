package com.opera.core.systems.model;

public class ScriptResult {

	protected int objectId;
	protected String className;

	public ScriptResult(int objectId, String className) {
		this.objectId = objectId;
		this.className = className;
	}

	public int getObjectId() {
		return objectId;
	}

	public void setObjectId(int objectId) {
		this.objectId = objectId;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
}
