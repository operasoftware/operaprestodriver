package com.opera.core.systems.scope.services;

import java.awt.Point;
import java.util.List;

import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo.MouseButton;

public interface SystemInput {

	/**
	 * Click MouseButton button num_clicks times with the given modifiers.
	 *
	 * @param location - Point to click
	 * @param button - MouseButton to click
	 * @param numClicks - number of clicks
	 * @param modifiers - modifiers to hold down during click
	 *
	 */
	void click(Point location, MouseButton button, int numClicks, List<ModifierPressed> modifiers);

	/**
	 * Press key with modifiers given.
	 *
	 * @param key - key to press
	 * @param modifiers - modifiers to hold
	 *
	 */
	void keyPress(String key, List<ModifierPressed> modifiers);
}

