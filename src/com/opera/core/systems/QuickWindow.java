package com.opera.core.systems;

import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;

public class QuickWindow {
	
	private final DesktopWindowInfo info; 
	//private final OperaDriver parent;
	//private final IOperaExec execService;
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.IOperaWidget#getObjectId()
	 */
	public int getObjectId() {
		return getWindowID();
	}
	
	public int getWindowID() {
		return info.getWindowID();
	}
	
	public QuickWindow(DesktopWindowInfo info) {
        //this.parent = parent;
        this.info = info;
        //execService = parent.getExecService();
    }
	
	public String getTitle() {
		return info.getTitle();
	}

	/*public boolean isVisible(){
		return info.getVisible() != 0 ? true : false; 
	}
	private QuickWindowRect getRect() {
		return info.getRect();
	}
	public Point getLocation() {
		QuickWidgetRect rect = getRect();
		return new Point(rect.getX(), rect.getY());
	}
	public Dimension getSize() {
		QuickWidgetRect rect = getRect();
		return new Dimension(rect.getWidth(), rect.getHeight());
	}*/
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof QuickWindow) {
			QuickWindow ref = (QuickWindow) obj;
			return (((QuickWindow)ref).getObjectId() == this.getObjectId());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int result = 42;
		result = 31 * result + getObjectId();
		//result = 31 * result + runtimeId;
		return result;
	}
	
	@Override
	public String toString() {
		return "quickwindow" + hashCode();
	}

}
