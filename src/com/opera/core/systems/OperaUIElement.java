package com.opera.core.systems;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowRect;
import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo.MouseButton;
import com.opera.core.systems.scope.services.IDesktopUtils;
import com.opera.core.systems.scope.services.ums.SystemInputManager;

public abstract class OperaUIElement {
	private SystemInputManager systemInputManager;
	private IDesktopUtils desktopUtils;

	public OperaUIElement(SystemInputManager systemInputManager, IDesktopUtils desktopUtils) {
		super();
		this.systemInputManager = systemInputManager;
		this.desktopUtils = desktopUtils;
	}
	
	protected SystemInputManager getSystemInputManager() {
		return systemInputManager;
	}
	
	protected IDesktopUtils getDesktopUtils() {
		return desktopUtils;
	}
	
	public abstract DesktopWindowRect getRect();

	/**
	 * @return Point describing location of this element
	 */
	public Point getLocation() {
		DesktopWindowRect rect = getRect();
		return new Point(rect.getX(), rect.getY());
	}
	
	/**
	 * @return true if widget is visible, else false
	 */
	public boolean isVisible(){
		return true; // default
	}

	/**
	 * @return size of Element
	 */
	public Dimension getSize() {
		DesktopWindowRect rect = getRect();
		return new Dimension(rect.getWidth(), rect.getHeight());
	}
	
	public abstract String toFullString();

	public void click(MouseButton button, int numClicks, List<ModifierPressed> modifiers) {
		systemInputManager.click(getCenterLocation(), button, numClicks, modifiers);
	}

	protected Point getCenterLocation() {
		DesktopWindowRect rect = getRect();
		Point topLeft = getLocation();
		//Point center = new Point(topLeft.x + rect.getWidth() / 2, topLeft.y + rect.getHeight() / 2);
		return new Point(topLeft.x + rect.getWidth() / 2, topLeft.y + rect.getHeight() / 2);
	}

	/**
	 * Hovers Element
	 */
	public void hover() {
		List<ModifierPressed> alist = new ArrayList<ModifierPressed>();
		alist.add(ModifierPressed.NONE);
		systemInputManager.mouseMove(getCenterLocation(), MouseButton.LEFT, alist);
	}

	
	/**
     * Checks if widget text equals the text specified by the given string id
     *
     * @return true if text specified by stringId equals widget text
     */
	public boolean verifyText(String stringId) {
		String text = desktopUtils.getString(stringId, true /* skipAmpersand */);
		
		return getText().equals(text);
	}
	
	/**
     * Checks if widget text contains the text specified by the given string id
     *
     * @param stringId String id of string 
     * @return true if text specified by stringId is contained in widget text
     */
	public boolean verifyContainsText(String stringId) {
		String text = desktopUtils.getString(stringId, true);
		return getText().indexOf(text) >= 0;
	}

	public abstract String getText();
}