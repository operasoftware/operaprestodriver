/*
Copyright 2008-2011 Opera Software ASA

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.opera.core.systems.interaction;

import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.WebDriver;

import com.opera.core.systems.OperaWebElement;

/**
 * Mouse user interactions, part of OperaUserInteraction
 * to support clicking and moving the mouse
 *
 * @author Deniz Turkoglu <denizt@opera.com>
 *
 */
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
