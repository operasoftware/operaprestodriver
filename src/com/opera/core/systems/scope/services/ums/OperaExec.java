/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */

package com.opera.core.systems.scope.services.ums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openqa.selenium.WebDriverException;
import com.opera.core.systems.scope.exceptions.WindowNotFoundException;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.model.Canvas;
import com.opera.core.systems.model.ColorResult;
import com.opera.core.systems.model.OperaColor;
import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.ExecCommand;
import com.opera.core.systems.scope.internal.OperaColors;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.internal.OperaMouseKeys;
import com.opera.core.systems.scope.protos.ExecProtos.ActionInfoList;
import com.opera.core.systems.scope.protos.ExecProtos.ActionList;
import com.opera.core.systems.scope.protos.ExecProtos.Area;
import com.opera.core.systems.scope.protos.ExecProtos.MouseAction;
import com.opera.core.systems.scope.protos.ExecProtos.ScreenWatcher;
import com.opera.core.systems.scope.protos.ExecProtos.ScreenWatcherResult;
import com.opera.core.systems.scope.protos.ExecProtos.ActionInfoList.ActionInfo;
import com.opera.core.systems.scope.protos.ExecProtos.ActionList.Action;
import com.opera.core.systems.scope.protos.ExecProtos.ScreenWatcher.ColorSpec;
import com.opera.core.systems.scope.protos.ExecProtos.ScreenWatcherResult.ColorMatch;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.IOperaExec;
import com.opera.core.systems.util.VersionUtil;

/**
 * The exec service actions
 * @author Deniz Turkoglu
 *
 */
public class OperaExec extends AbstractService implements IOperaExec {

	private List<String> keys;
	
	private Set<String> actions;

	private ScopeServices services;
	
	//FIXME remove me, VERY UGLY HACK FOR window-id bug
	private List<String> excludedActions;
	/*
	command Exec(ActionList) returns (Default) = 1;
    command GetActionInfoList(Default) returns (ActionInfoList) = 2;
    command SetupScreenWatcher(ScreenWatcher) returns (ScreenWatcherResult) = 3 
    command SendMouseAction(MouseAction) returns (Default) = 5;
	 */

	public OperaExec(ScopeServices services, String serviceName, String version) {
		super(services, serviceName,version);
		excludedActions = new ArrayList<String>();
		excludedActions.add("Select all");
		excludedActions.add("Delete");
		
		if(VersionUtil.compare(version, "3.0") >= 0)
			throw new UnsupportedOperationException(serviceName + " version " + version + " is not supported");
		//Another ugly hack for patch version
		if(VersionUtil.compare(version, "2.0.1") == - 1)
			excludedActions.add("Close page");
		
		services.setExec(this);
		this.services = services;
		keys = new CopyOnWriteArrayList<String>();
	}

	public void init() {
		actions = initActionList();
	}

	private Set<String> initActionList() {
		Response response = executeCommand(ExecCommand.GET_ACTION_LIST, null);
		ActionInfoList.Builder builder = ActionInfoList.newBuilder();
		buildPayload(response, builder);
		ActionInfoList infoList = builder.build();
		
		Set<String> actions = new HashSet<String>();
		
		for (ActionInfo info : infoList.getActionInfoListList()) {
			actions.add(info.getName());
		}
		
		return actions;
	}

	public void type(String using) {
		action("_type", using);
	}

	public void mouseAction(int x, int y, OperaMouseKeys... keys) {
		int key  = 0;
		
		for (OperaMouseKeys operaMouseKeys : keys) {
			key |= operaMouseKeys.getValue();
		}
		this.mouseAction(x, y, key, 1);
	}
	
	/**
	 * Added as a quick workaround for watir API
	 * @param x an unsigned int
	 * @param y an unsigned int
	 * @param value
	 */
	public void mouseAction(int x, int y, int value, int count) {
		MouseAction.Builder actionBuilder = MouseAction.newBuilder();
		actionBuilder.setWindowID(services.getWindowManager().getActiveWindowId());
		actionBuilder.setX(x);
		actionBuilder.setY(y);
		//actionBuilder.setCount(count);
		actionBuilder.setButtonAction(value);
		
		for(int i = 0; i < count; i++)
			executeCommand(ExecCommand.SEND_MOUSE_ACTION, actionBuilder);
	}
	
	public Set<String> getActionList() {
		return actions;
	}
	

	//FIXME sending params, we have commas, space, what?
	public void action(String using, String... params) {
		if(!actions.contains(using))
			throw new WebDriverException("The requested action is not supported : " + using);
		ActionList.Builder builder = ActionList.newBuilder();
		Action.Builder actionBuilder = Action.newBuilder();
		actionBuilder.setName(using);
		if(params != null && params.length > 0) {
			actionBuilder.setValue(params[0]);
		}
		
		if(!excludedActions.contains(using)) {
                    try {
			actionBuilder.setWindowID(services.getWindowManager().getActiveWindowId());
                    } catch (WindowNotFoundException e) {
                    }
		}
		
		//type.setSpace("preserve");
		builder.addActionList(actionBuilder);
		if(executeCommand(ExecCommand.EXEC, builder) == null)
			throw new WebDriverException("Unexpected error while calling action : " + using);
	}

	public void key(String key) {
		key(key, false);
		key(key, true);
	}


	public void key(String key, boolean up) {
		if (up) {
			if (!keys.contains(key)) {
				throw new WebDriverException("Impossible to release key " + key + " while it's not down");
			}
			action("_keyup", key);
			keys.remove(key);
		} else {
			if (keys.contains(key)) {
				throw new WebDriverException("Impossible to push " + key + " while it's down");
			}
			action("_keydown", key);
			keys.add(key);
		}
	}

	public void releaseKeys() {
		for (String key : keys) {
			this.key(key, true);
		}
	}

	public ScreenShotReply containsColor(Canvas canvas, long timeout, OperaColors... colors){
		//command SetupScreenWatcher(ScreenWatcher) returns (ScreenWatcherResult) = 3 
		int i = 0;
		
		ScreenWatcher.Builder builder = ScreenWatcher.newBuilder();
		Area.Builder areaBuilder = Area.newBuilder();
		
		areaBuilder.setX(canvas.getX());
		areaBuilder.setY(canvas.getY());
		areaBuilder.setH(canvas.getH());
		areaBuilder.setW(canvas.getW());
		
		builder.setArea(areaBuilder);
		
		for (OperaColors color : colors) {
			ColorSpec.Builder colorSpec = convertColor(color.getColour());
			colorSpec.setId(++i);
			builder.addColorSpecList(colorSpec);
		}
		
		builder.setTimeOut((int)timeout);
		
		int windowId = services.getWindowManager().getActiveWindowId();
		builder.setWindowID(windowId);
		
		Response response = executeCommand(ExecCommand.SETUP_SCREEN_WATCHER, builder);
		
		ScreenWatcherResult.Builder watcherBuilder = ScreenWatcherResult.newBuilder();
		buildPayload(response, watcherBuilder);
		ScreenWatcherResult result = watcherBuilder.build();		
				
		List<ColorResult> matches = new ArrayList<ColorResult>();
		
		for (ColorMatch match : result.getColorMatchListList()) {
			matches.add(new ColorResult(match.getCount(), match.getId()));
		}
		
		return new ScreenShotReply(result.getMd5(), matches);

	}
	
	private ColorSpec.Builder convertColor(OperaColor color) {
		ColorSpec.Builder builder = ColorSpec.newBuilder(); 
		builder.setBlueHigh(color.getHighBlue());
		builder.setGreenHigh(color.getHighGreen());
		builder.setRedHigh(color.getHighRed());
		builder.setBlueLow(color.getLowBlue());
		builder.setGreenLow(color.getLowGreen());
		builder.setRedLow(color.getLowRed());
		return builder;
	}

	public ScreenShotReply screenWatcher(Canvas canvas, long timeout, boolean includeImage, String... hashes) {
		
		ScreenWatcher.Builder builder = ScreenWatcher.newBuilder();
		Area.Builder areaBuilder = Area.newBuilder();
		//FIXME viewport relative
		areaBuilder.setX(canvas.getX());
		areaBuilder.setY(canvas.getY());
		areaBuilder.setH(canvas.getH());
		areaBuilder.setW(canvas.getW());
		
		builder.setArea(areaBuilder);
		
		int windowId = services.getWindowManager().getActiveWindowId();
		
		if (hashes.length > 0) {
			builder.addAllMd5List(Arrays.asList(hashes));
		}

		builder.setWindowID(windowId);
		builder.setTimeOut((int) timeout);
		
		if(!includeImage) {
			builder.setIncludeImage(false);
		}
		
		Response response = executeCommand(ExecCommand.SETUP_SCREEN_WATCHER, builder, OperaIntervals.RESPONSE_TIMEOUT.getValue() + timeout);
		
		ScreenWatcherResult.Builder watcherBuilder = ScreenWatcherResult.newBuilder();
		buildPayload(response, watcherBuilder);
		ScreenWatcherResult result = watcherBuilder.build();
		
		return new ScreenShotReply(result.getMd5(), result.getPng().toByteArray());
	}

}
