package com.opera.core.systems.scope.services;

import java.awt.Point;

public interface SystemInput {
		void click(Point location, int button, int num_clicks, int modifier);
		void keyPress(String key, int modifier);
		void mouseMove(Point location, int button, int modifier);
		void mouseUp(Point location, int button, int modifier);
		void mouseDown(Point location, int button, int modifier);
}

