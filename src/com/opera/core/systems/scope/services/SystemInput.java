package com.opera.core.systems.scope.services;

import java.awt.Point;
import java.util.List;

import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo.MouseButton;

public interface SystemInput {

	/**
	 * Clicks MouseButton a specified number of times with zero or more
	 * modifiers held down.
	 *
	 * @param location - Point to click
	 * @param button - MouseButton to click
	 * @param numClicks - number of clicks
	 * @param modifiers - modifiers to hold down during click
	 *
	 */
	void click(Point location, MouseButton button, int numClicks, List<ModifierPressed> modifiers);

	/**
	 * Presses key, optionally with one or more modifiers held down.
	 *
	 * @param key - key to press
	 * @param modifiers - modifiers to hold
	 */
	void keyPress(String key, List<ModifierPressed> modifiers);
}

