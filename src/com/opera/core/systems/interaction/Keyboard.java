/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.interaction;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.scope.internal.OperaKeys;

public abstract class Keyboard implements UserInteraction {

	public static Keyboard keyDown(final Keys keys) {
		return new Keyboard() {

			public void execute(WebDriver driver) {
				String key = OperaKeys.get(keys.name());
				keyUp(key);
			}

		};
	}

	public static Keyboard keyUp(final Keys keys) {
		return new Keyboard() {

			public void execute(WebDriver driver) {
				String key = OperaKeys.get(keys.name());
				keyDown(key);
			}

		};
	}
	
	public static Keyboard keyDown(final String key) {
		return new Keyboard() {

			public void execute(WebDriver driver) {
				((OperaDriver) driver).keyDown(key);
			}

		};
	}

	public static Keyboard keyUp(final String key) {
		return new Keyboard() {

			public void execute(WebDriver driver) {
				((OperaDriver) driver).keyUp(key);
			}

		};
	}

	public static Keyboard key(final Keys keys) {
		return new Keyboard() {

			public void execute(WebDriver driver) {
				String key = OperaKeys.get(keys.name());
				key(key);
			}

		};
	}
	
	public static Keyboard key(final String key) {
		return new Keyboard() {

			public void execute(WebDriver driver) {
				((OperaDriver) driver).key(key);
			}

		};
	}
	
	public static Keyboard type(final String using) {
		return new Keyboard() {
			public void execute(WebDriver driver) {
				((OperaDriver) driver).type(using);
			}

		};
	}

	public static Keyboard releaseKeys() {
		return new Keyboard() {
			public void execute(WebDriver driver) {
				((OperaDriver) driver).releaseKeys();
			}

		};
	}

}
