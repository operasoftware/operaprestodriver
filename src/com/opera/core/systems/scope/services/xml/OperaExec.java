/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */

package com.opera.core.systems.scope.services.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.model.Canvas;
import com.opera.core.systems.model.ColorResult;
import com.opera.core.systems.model.OperaColor;
import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.model.AbstractService;
import com.opera.core.systems.scope.beans.ActionInfoList;
import com.opera.core.systems.scope.beans.Area;
import com.opera.core.systems.scope.beans.ColorMatch;
import com.opera.core.systems.scope.beans.ColorSpec;
import com.opera.core.systems.scope.beans.Exec;
import com.opera.core.systems.scope.beans.IncludeImage;
import com.opera.core.systems.scope.beans.MouseAction;
import com.opera.core.systems.scope.beans.No;
import com.opera.core.systems.scope.beans.ScreenWatcher;
import com.opera.core.systems.scope.beans.ScreenWatcherReply;
import com.opera.core.systems.scope.beans.Type;
import com.opera.core.systems.scope.beans.ViewportRelative;
import com.opera.core.systems.scope.internal.OperaColors;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.internal.OperaMouseKeys;
import com.opera.core.systems.scope.internal.Parser;
import com.opera.core.systems.scope.services.model.IOperaExec;

/**
 * The exec service actions
 * @author Deniz Turkoglu
 *
 */
public class OperaExec extends AbstractService implements IOperaExec {

	private List<String> keys;
	
	private List<String> actions;

	private ScopeServices services;
	
	private Parser parser;

	public OperaExec(ScopeServices services, String serviceName) {
		super(services, serviceName);
		services.setExec(this);
		this.parser = services.getParser();
		this.services = services;
		keys = new CopyOnWriteArrayList<String>();
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IOperaExec#init()
	 */
	public void init() {
		actions = getActionList();
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IOperaExec#type(java.lang.String)
	 */
	public void type(String using) {
		Exec exec = new Exec();
		Type type = new Type();
		type.setSpace("preserve");
		type.setContent(using);
		exec.setType(type);
		this.postRequest(parser.createRequest(exec, Exec.class));
		sleep(OperaIntervals.EXEC_SLEEP.getValue());
	}


	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IOperaExec#mouseAction(int, int, com.opera.core.systems.scope.internal.OperaMouseKeys)
	 */
	public void mouseAction(int x, int y, OperaMouseKeys... keys) {
		int key  = 0;
		
		for (OperaMouseKeys operaMouseKeys : keys) {
			key |= operaMouseKeys.getValue();
		}
		this.mouseAction(x, y, key);
	}
	
	/**
	 * Added as a quick workaround for watir API
	 * @param x an unsigned int
	 * @param y an unsigned int
	 * @param value
	 */
	public void mouseAction(int x, int y, int value) {
		Exec exec = new Exec();
		MouseAction mouseAction = new MouseAction();
		mouseAction.setWindowId(services.getWindowManager().getActiveWindowId());
		mouseAction.setX(x);
		mouseAction.setY(y);
		mouseAction.setButtons(value);
		exec.setMouseAction(mouseAction);
		this.postRequest(parser.createRequest(exec, Exec.class));
		sleep(OperaIntervals.EXEC_SLEEP.getValue());
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IOperaExec#getActionList()
	 */
	public List<String> getActionList(){
		this.post("<get-action-info-list />");
		String response = waitForResponse("<action-info-list>");
		ActionInfoList infoList = (ActionInfoList) parser.parseResponse(response);
		return infoList.getName();
	}
	
	public void action(String using, String... params) {
		if(!actions.contains(using))
			throw new WebDriverException("The requested action is not supported : " + using);
		Exec exec = new Exec();
		if(params != null && params.length > 0)
			exec.setAction(services.getActionHandler().createAction(using, params[0]));
		else
			exec.setAction(services.getActionHandler().createAction(using));
		this.postRequest(parser.createRequest(exec, Exec.class));
		sleep(OperaIntervals.EXEC_SLEEP.getValue());
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IOperaExec#key(java.lang.String)
	 */
	public void key(String key) {
		key(key, false);
		key(key, true);
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IOperaExec#key(java.lang.String, boolean)
	 */
	public void key(String key, boolean up) {
		Exec exec = new Exec();
		if (up) {
			if (!keys.contains(key)) {
				throw new WebDriverException("Impossible to release key " + key + " while it's not down");
			}
			exec.setKeyup(key);
			this.postRequest(parser.createRequest(exec, Exec.class));
			keys.remove(key);
		} else {
			if (keys.contains(key)) {
				throw new WebDriverException("Impossible to push " + key + " while it's down");
			}
			exec.setKeydown(key);
			this.postRequest(parser.createRequest(exec, Exec.class));
			keys.add(key);
		}
		// This one should be pretty fast
		sleep(OperaIntervals.EXEC_SLEEP.getValue() / 5);
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IOperaExec#releaseKeys()
	 */
	public void releaseKeys() {
		for (String key : keys) {
			this.key(key, true);
		}
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IOperaExec#containsColor(com.opera.core.systems.model.Canvas, long, com.opera.core.systems.scope.internal.OperaColors)
	 */
	public ScreenShotReply containsColor(Canvas canvas, long timeout, OperaColors... colors){
		int i = 0;
		Exec exec = new Exec();
		ScreenWatcher watcher = new ScreenWatcher();
		Area area = new Area();
		area.setX(canvas.getX());
		area.setY(canvas.getY());
		area.setH(canvas.getH());
		area.setW(canvas.getW());
		
		watcher.setArea(area);
		
		for (OperaColors color : colors) {
			ColorSpec colorSpec = convertColor(color.getColour());
			colorSpec.setId(++i);
			watcher.getColorSpec().add(colorSpec);
		}
		
		exec.setScreenWatcher(watcher);
		watcher.setTimeout(timeout);
		int windowId = services.getWindowManager().getActiveWindowId();
		watcher.setWindowId(windowId);
		this.postRequest(parser.createRequest(exec, Exec.class));
		
		String response = waitForResponse("<screen-watcher-reply>", timeout + OperaIntervals.RESPONSE_TIMEOUT.getValue());

		ScreenWatcherReply reply = (ScreenWatcherReply) parser.parseResponse(response);
		List<ColorResult> matches = new ArrayList<ColorResult>();
		
		for (ColorMatch match : reply.getColorMatch()) {
			matches.add(new ColorResult(match.getCount(), match.getId()));
		}
		
		return new ScreenShotReply(reply.getMd5(), matches);

	}
	
	private ColorSpec convertColor(OperaColor color) {
		ColorSpec spec = new ColorSpec();
		spec.setHighBlue(color.getHighBlue());
		spec.setHighGreen(color.getHighGreen());
		spec.setHighRed(color.getHighRed());
		spec.setLowBlue(color.getLowBlue());
		spec.setLowGreen(color.getLowGreen());
		spec.setLowRed(color.getLowRed());
		return spec;
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IOperaExec#screenWatcher(java.util.List, com.opera.core.systems.model.Canvas, long, boolean)
	 */
	public ScreenShotReply screenWatcher(Canvas canvas, long timeout, boolean includeImage, String... hashes) {
		
		Exec exec = new Exec();
		ScreenWatcher watcher = new ScreenWatcher();
		
		int windowId = services.getWindowManager().getActiveWindowId();

		if (hashes.length > 0) {
			watcher.getMd5().addAll(Arrays.asList(hashes));
		}

		Area area = new Area();
		if(canvas.isViewPortRelative())
			area.setViewportRelative(new ViewportRelative());
		
		area.setX(canvas.getX());
		area.setY(canvas.getY());
		area.setH(canvas.getH());
		area.setW(canvas.getW());
		
		watcher.setArea(area);
		watcher.setWindowId(windowId);
		watcher.setTimeout(timeout);
		
		if(!includeImage) {
			IncludeImage include = new IncludeImage();
			include.setNo(new No());
			watcher.setIncludeImage(include);
		}
		
		exec.setScreenWatcher(watcher);

		this.postRequest(parser.createRequest(exec, Exec.class));
		
		String response = waitForResponse("<screen-watcher-reply>", timeout + OperaIntervals.RESPONSE_TIMEOUT.getValue());

		ScreenWatcherReply reply = (ScreenWatcherReply) parser.parseResponse(response);
		
		return new ScreenShotReply(reply.getMd5(), reply.getPng());
	}

}
