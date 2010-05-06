/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.scope.handlers;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.model.Canvas;
import com.opera.core.systems.model.ScopeActions;
import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.scope.beans.Action;
import com.opera.core.systems.scope.beans.ObjectFactory;
import com.opera.core.systems.scope.internal.OperaMouseKeys;

/**
 * This library handles actions for Core 2.4+
 * Core 2.4 supports native mouse mouse / keys,
 * screenshots and all Opera actions
 * @author Deniz Turkoglu
 *
 */
public class UmsActionHandler extends ScopeActions {
	
	protected ObjectFactory factory = new ObjectFactory();
	
	private List<String> excludedActions = Arrays.asList(new String[] { /*"Close page",*/ "Select all", "Delete"});

	public UmsActionHandler(ScopeServices services) {
		super(services);
	}
	

	@Override
	public void click(RenderedWebElement element, String locator) {
		Point point = element.getLocation();
		services.getExec().mouseAction(point.x, point.y, OperaMouseKeys.LEFT);
	}

	@Override
	public void click(RenderedWebElement element, int x, int y) {
		Point point = element.getLocation();
		services.getExec().mouseAction(point.x + x, point.y + y, OperaMouseKeys.LEFT);
	}
	
	
	@Override
	public void get(String url) {
		execService.action("Go", url);
	}

	
	@Override
	public void saveScreenshot(File pngFile) {
		if (pngFile == null) {
            throw new IllegalArgumentException("Method parameter pngFile must not be null");
        }
        
        File dir = pngFile.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new WebDriverException("Could not create directory " + dir.getAbsolutePath());
        }
        Canvas canvas = new Canvas();
        canvas.setX(0);
        canvas.setY(0);

		String[] dimensions = scriptDebugger.executeJavascript("return (window.innerWidth + \",\" + window.innerHeight);").split(",");
		canvas.setH(Integer.valueOf(dimensions[1]));
		canvas.setW(Integer.valueOf(dimensions[0]));
		canvas.setViewPortRelative(true);
		
        ScreenShotReply reply = execService.screenWatcher(canvas, 1l, true);
		FileOutputStream stream;
		try {
			stream = new FileOutputStream(pngFile.getAbsolutePath());
			stream.write(reply.getPng());
			stream.close();
		} catch (FileNotFoundException e) {
			//ignore
		} catch (IOException e) {
			//TODO log
		}

	}

	@Override
	public Action createAction(String using) {
		Action action = new Action();
		action.getContent().add(factory.createName(using));
		if(!excludedActions.contains(using)) {
			services.getWindowManager().updateActiveWindow();
			action.getContent().add(factory.createWindowId(services.getWindowManager().getActiveWindowId()));
		}
		return action;
	}

	@Override
	public Action createAction(String using, String param) {
		Action action = new Action();
		action.getContent().add(factory.createName(using));
		action.getContent().add(factory.createParam(param));
		action.getContent().add(factory.createWindowId(services.getWindowManager().getActiveWindowId()));
		return action;
	}


	@Override
	public void rightClick(RenderedWebElement element) {
		Point point = element.getLocation();
		services.getExec().mouseAction(point.x, point.y, OperaMouseKeys.RIGHT);
		
	}


}
