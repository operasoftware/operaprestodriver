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
package com.opera.core.systems.model;

import java.io.File;

import org.openqa.selenium.RenderedWebElement;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IOperaExec;

/**
 *
 * ScopeActions handles actions that differ from Core 2.3 and 2.4+, it calls the
 * appropriate action depending on the spec as the spec varies
 *
 * @author Deniz Turkoglu <denizt@opera.com>
 *
 */

// TODO Merge as we dropped support < 2.5
public abstract class ScopeActions {

	protected final ScopeServices services;
	protected final IEcmaScriptDebugger scriptDebugger;
	protected final IOperaExec execService;

	public ScopeActions(ScopeServices services) {
		this.services = services;
		this.scriptDebugger = services.getDebugger();
		this.execService = services.getExec();
	}

	/**
	 * Creates a native left mouse click (2.4+) or an ecmascript MouseEvent(2.3)
	 *
	 * @param element The element to be clicked on top left
	 * @param locator The index of the element in elements array
	 */
	public abstract void click(RenderedWebElement element, String locator);

	/**
   * Creates a native left mouse click (2.4+)
   *
   * @throws UnsupportedOperationException on 2.3
   * @param element The element to be clicked with x and y coordinates added to top left
	 * @param x
	 * @param y
	 */
	public abstract void click(RenderedWebElement element, int x, int y);

	/**
	 * Creates a native right mouse click (2.4+)
	 *
	 * @throws UnsupportedOperationException on 2.3
	 * @param element The element to be clicked on top left
	 */
	public abstract void rightClick(RenderedWebElement element);

	/**
	 * Loads the specified url via Opera action 'Go' (2.4+) or script injection
	 * (2.3) 2.4+ interacts directly with address bar allowing script injection
	 * and urls without protocol
	 *
	 * @param url The page to load
	 */
	public abstract void get(String url);

	/**
	 * Saves the complete page as png file to pngFile (2.4+)
	 *
	 * @throws UnsupportedOperationException on 2.3
	 * @param pngFile
	 */
	public abstract void saveScreenshot(File pngFile);

}
