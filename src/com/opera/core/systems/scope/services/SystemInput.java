package com.opera.core.systems.scope.services;

import java.awt.Point;
import java.util.List;

import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo.MouseButton;

public interface SystemInput {
	
	/*
	 * click MouseButton button num_clicks times with the given modifiers
	 */
	void click(Point location, MouseButton button, int num_clicks, List<ModifierPressed> modifiers);
	
	/*
	 * 
	 */
	void keyPress(String key, List<ModifierPressed> modifiers);
}

