package com.opera.core.systems.model;

public class FilterRule {
	
	public enum FilterType {
		STARTS_WITH,
		ENDS_WITH,
		CONTAINS,
		EQUALS;
	}
	
	private String field;
	private String value;
	private FilterType type;

	public FilterRule(){
		
	}
	public FilterRule(String field, FilterType type, String value){
		this.field = field;
		this.type = type;
		this.value = value;
	}
	
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
	
	public String getField() {
		return field;
	}

	public void setField(String value) {
		this.field = value;
	}
}
