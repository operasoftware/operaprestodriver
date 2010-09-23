package com.opera.core.systems.scope.services.ums;

import java.awt.Point;
import java.util.List;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.SystemInputCommand;
import com.opera.core.systems.scope.protos.SystemInputProtos.KeyPressInfo;
import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo.MouseButton;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo;
import com.opera.core.systems.scope.services.SystemInput;
import com.opera.core.systems.scope.internal.OperaKeys;

public class SystemInputManager extends AbstractService implements SystemInput {

	 SystemInputManager(ScopeServices services, String version) {
			super(services, version);
			
			String serviceName = "system-input";
			
			if(!isVersionInRange(version, "5.0", serviceName))
				throw new UnsupportedOperationException(serviceName + " version " + version + " is not supported");
			
			services.setSystemInputManager(this);
		}
	
	public void click(Point location, MouseButton button, int num_clicks, List<ModifierPressed> modifiers) {
		MouseInfo.Builder actionBuilder = MouseInfo.newBuilder();
		actionBuilder.setX(location.x);
		actionBuilder.setY(location.y);
		actionBuilder.setButton(button); 
		actionBuilder.setNumClicks(num_clicks);
		int modifier = ModifierPressed.NONE.getNumber();
		for (ModifierPressed mod : modifiers) {
			modifier |= mod.getNumber();
		}
		actionBuilder.setModifier(modifier); 
		executeCommand(SystemInputCommand.CLICK, actionBuilder.clone());
	}
	
	public void mouseMove(Point location, MouseButton button, List<ModifierPressed> modifiers) {
		MouseInfo.Builder actionBuilder = MouseInfo.newBuilder();
		actionBuilder.setX(location.x);
		actionBuilder.setButton(button);  
		actionBuilder.setNumClicks(1); 
		int modifier = ModifierPressed.NONE.getNumber();
		for (ModifierPressed mod : modifiers) {
			modifier |= mod.getNumber();
		}
		actionBuilder.setModifier(modifier); // TODO FIXME
		executeCommand(SystemInputCommand.MOUSEMOVE, actionBuilder.clone());
	}
	
	public void mouseUp(Point location, MouseButton button, List<ModifierPressed> modifiers) {
		MouseInfo.Builder actionBuilder = MouseInfo.newBuilder();
		actionBuilder.setX(location.x);
		actionBuilder.setButton(button); 
		actionBuilder.setNumClicks(1); 
		int modifier = ModifierPressed.NONE.getNumber();
		for (ModifierPressed mod : modifiers) {
			modifier |= mod.getNumber();
		}
		actionBuilder.setModifier(modifier); 
		executeCommand(SystemInputCommand.MOUSEUP, actionBuilder.clone());
	}

	public void mouseDown(Point location, MouseButton button, List<ModifierPressed> modifiers) {
		MouseInfo.Builder actionBuilder = MouseInfo.newBuilder();
		actionBuilder.setX(location.x);
		actionBuilder.setButton(button);  
		actionBuilder.setNumClicks(1); 
		int modifier = ModifierPressed.NONE.getNumber();
		for (ModifierPressed mod : modifiers) {
			modifier |= mod.getNumber();
		}
		actionBuilder.setModifier(modifier); 
		executeCommand(SystemInputCommand.MOUSEDOWN, actionBuilder.clone());
	}
	
	public void keyPress(String key, List<ModifierPressed> modifiers) {
		String myKey = OperaKeys.get(key);
		System.out.println("myKey = " + myKey);
		KeyPressInfo.Builder actionBuilder = KeyPressInfo.newBuilder();
		actionBuilder.setKey(key);
		int modifier = ModifierPressed.NONE.getNumber();
		for (ModifierPressed mod : modifiers) {
			modifier |= mod.getNumber();
		}
		actionBuilder.setModifier(modifier);
		executeCommand(SystemInputCommand.KEYPRESS, actionBuilder.clone());
	}
	
}
