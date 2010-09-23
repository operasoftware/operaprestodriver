package com.opera.core.systems.scope.services;

import java.awt.Point;
import java.util.List;

import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo.MouseButton;

public interface SystemInput {
		void click(Point location, MouseButton button, int num_clicks, List<ModifierPressed> modifiers);
		void keyPress(String key, List<ModifierPressed> modifiers);
		void mouseMove(Point location, MouseButton button, List<ModifierPressed> modifiers);
		void mouseUp(Point location, MouseButton button, List<ModifierPressed> modifiers);
		void mouseDown(Point location, MouseButton button, List<ModifierPressed> modifiers);
}

