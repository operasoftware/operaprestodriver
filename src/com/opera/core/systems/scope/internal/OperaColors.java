/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.scope.internal;

import com.opera.core.systems.model.OperaColor;

/**
 * Enumerator for contains color API (RGB values for standard web colors)
 * @author Deniz Turkoglu
 *
 */
public enum OperaColors {
		AQUA(0,255,255),
		BLACK(0,0,0),
		BLUE(0,0,255),
		FUSCHIA(255,0,255),
		GRAY(128,128,128),
		GREEN(0,128,0),
		LIME(0,255,0),
		MAROON(128,0,0),
		NAVY(0,0,128),
		OLIVE(128,128,0),
		PURPLE(128,0,128),
		RED(255,0,0),
		SILVER(192,192,192),
		TEAL(0,128,128),
		WHITE(0,0,0),
		YELLOW(255,255,0);
		
		private OperaColors(int r, int g, int b) {
			this.operaColor = new OperaColor();
			operaColor.setLowRed(r);
			operaColor.setHighRed(r);
			operaColor.setLowGreen(g);
			operaColor.setHighGreen(g);
			operaColor.setLowBlue(b);
			operaColor.setHighBlue(b);
		}
		
		private OperaColor operaColor;

		public OperaColor getColour() {
			return operaColor;
		}
	
}
