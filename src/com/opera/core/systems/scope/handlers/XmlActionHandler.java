/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.scope.handlers;

import java.io.File;

import org.openqa.selenium.RenderedWebElement;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.model.ScopeActions;
import com.opera.core.systems.scope.beans.Action;

/**
 * This library handles actions for Core 2.3
 * Core 2.3 lacks enchanments to exec service
 * @author Deniz Turkoglu
 *
 */
public class XmlActionHandler extends ScopeActions {

	public XmlActionHandler(ScopeServices services) {
		super(services);
	}


	@Override
	public void click(RenderedWebElement element, String locator) {
		scriptDebugger.executeJavascript("if ("
								+ locator
								+ "[\"click\"]){\r"
								+ locator
								+ ".click();\r"
								+ " } else {\r"
								+ "var event = document.createEvent(\"MouseEvents\");\r"
								+ "event.initMouseEvent(\"click\", true, true, null, 1, 0, 0, 0, 0, false, false, false, false, 0, null);"
								+ locator + ".dispatchEvent(event); }\r", false);
	}


	@Override
	public void click(RenderedWebElement element, int x, int y) {
		throw new UnsupportedOperationException("native mouse clicks");
	}

	@Override
	public void get(String url) {
		scriptDebugger.executeJavascript("window.location.href='" + url + "'", true);
	}

	@Override
	public void saveScreenshot(File pngFile) {
		throw new UnsupportedOperationException("saveScreenshot");
	}


	@Override
	public Action createAction(String using) {
		Action action = new Action();
		action.getContent().add(using);
		return action;
	}

	@Override
	public Action createAction(String using, String param) {
		throw new UnsupportedOperationException("actions with parameters");
	}


	@Override
	public void rightClick(RenderedWebElement element) {
		throw new UnsupportedOperationException("native mouse clicks");
		
	}


}
