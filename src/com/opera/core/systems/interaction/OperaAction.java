/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.interaction;

import java.util.Arrays;
import java.util.List;


public class OperaAction {

	private List<UserInteraction> actions;

	public List<UserInteraction> getActions() {
		return actions;
	}

	public OperaAction(UserInteraction... userInteractions) {
		this.actions = Arrays.asList(userInteractions);
	}
}
