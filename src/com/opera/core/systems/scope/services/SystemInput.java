package com.opera.core.systems.scope.services;

import java.awt.Point;

public interface SystemInput {
		void click(Point location);
		void keyPress(String key, int modifier);
}

