/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.model;

import java.io.File;

import org.openqa.selenium.RenderedWebElement;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.beans.Action;
import com.opera.core.systems.scope.services.model.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.model.IOperaExec;

/**
 * 
 * ScopeActions handles actions that differ from Core 2.3 and 2.4+, it calls the
 * appropriate action depending on the spec as the spec varies
 * 
 * @author Deniz Turkoglu
 * 
 */
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
	 * @param locator The index of the element in elements array
	 */
	public abstract void click(RenderedWebElement element, int x, int y);

	/**
	 * Creates a native right mouse click (2.4+)
	 * 
	 * @throws UnsupportedOperationException on 2.3
	 * @param element The element to be clicked on top left
	 * @param locator The index of the element in elements array
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

	/**
	 * Creates an Opera Action based on specs for each engine version
	 * 
	 * @param using Opera Action to execute
	 * @return {@link Action} to be sent
	 */
	public abstract Action createAction(String using);

	/**
	 * Creates an Opera Action with parameters (2.4+)
	 * 
	 * @throws UnsupportedOperationException on 2.3
	 * @param using Opera Action to execute
	 * @return {@link Action} to be sent
	 */
	public abstract Action createAction(String using, String param);

}
