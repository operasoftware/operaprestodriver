package com.opera.core.systems;

import java.util.Map;

import com.opera.core.systems.model.FilterRule;

public interface IConsoleListener {
	
	void setFilters(Map<String, FilterRule> filters);
	Map<String, FilterRule> getFilters();
	
	//field and filter criteria
	String onMessage(String message);
}
