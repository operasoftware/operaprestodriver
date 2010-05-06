package com.opera.core.systems.model;

public class FilterRule {
	
	public enum FilterType {
		STARTS_WITH,
		ENDS_WITH,
		CONTAINS,
		EQUALS;
	}
	
	private String value;
	private FilterType type;

	public FilterType getType() {
		return type;
	}

	public void setType(FilterType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
