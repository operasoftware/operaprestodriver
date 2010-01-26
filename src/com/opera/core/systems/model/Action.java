/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.model;

import java.util.Arrays;
import java.util.List;

public class Action {

	private List<UserInteraction> actions;

	public List<UserInteraction> getActions() {
		return actions;
	}

	public Action(UserInteraction... userInteractions) {
		this.actions = Arrays.asList(userInteractions);
	}
}
