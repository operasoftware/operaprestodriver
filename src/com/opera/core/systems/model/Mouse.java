/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.model;

import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.WebDriver;

import com.opera.core.systems.OperaWebElement;

public abstract class Mouse implements UserInteraction {

	public static Mouse moveOn(final RenderedWebElement element) {
		return new Mouse() {

			public void execute(WebDriver driver) {
				element.hover();
			}

		};
	}

	public static Mouse clickOn(final RenderedWebElement element) {
		return new Mouse() {

			public void execute(WebDriver driver) {
				element.click();
			}
		};
	}

	public static Mouse buttonDownOn(final RenderedWebElement element) {
		return new Mouse() {

			public void execute(WebDriver driver) {
				((OperaWebElement) element).mouseDown();
			}
		};
	}

	public static Mouse buttonUpFrom(final RenderedWebElement element) {
		return new Mouse() {

			public void execute(WebDriver driver) {
				((OperaWebElement) element).mouseUp();
			}
		};
	}

}
