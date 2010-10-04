package com.opera.core.systems;

import java.util.List;

import com.opera.core.systems.model.FilterRule;

public interface IConsoleListener {
	/**
	 * Map of possible filters, the callback will
	 * only be called if the message description matches
	 * that rule
	 */
	List<FilterRule> getFilters();
	
	/**
	 * Called back when a message is is received
	 * @param message the message that has been received
	 */
	void onMessage(String message);
}
