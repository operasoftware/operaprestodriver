package com.opera.core.systems.scope.services;

import java.awt.Point;

import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo.MouseButton;

public interface SystemInput {
		void click(Point location, MouseButton button, int num_clicks, ModifierPressed modifier);
		void keyPress(String key, ModifierPressed modifier);
		void mouseMove(Point location, MouseButton button, ModifierPressed modifier);
		void mouseUp(Point location, MouseButton button, ModifierPressed modifier);
		void mouseDown(Point location, MouseButton button, ModifierPressed modifier);
}

