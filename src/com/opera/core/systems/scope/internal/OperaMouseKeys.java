/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.scope.internal;

/**
 * Enumerator for native mouse key events, 
 * @author Deniz Turkoglu
 *
 */
public enum OperaMouseKeys {
		LEFT_DOWN(1),
		LEFT_UP(2),
		LEFT(LEFT_DOWN.value | LEFT_UP.value),
		RIGHT_DOWN(4),
		RIGHT_UP(8),
		RIGHT(RIGHT_DOWN.value | RIGHT_UP.value),
		MIDDLE_DOWN(16),
		MIDDLE_UP(32),
		MIDDLE(MIDDLE_DOWN.value | MIDDLE_UP.value);

		private OperaMouseKeys(int value) {
			this.value = value;
		}
		
		private Integer value;

		public Integer getValue() {
			return value;
		}
	
}
