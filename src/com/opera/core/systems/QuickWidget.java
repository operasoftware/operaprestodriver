package com.opera.core.systems;

import java.awt.Dimension;
import java.awt.Point;

import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowRect;

public class QuickWidget {
		private final QuickWidgetInfo info; // or just int objectId
		//private final OperaDriver parent;
		//private final IOperaExec execService;
		
		public QuickWidget(QuickWidgetInfo info) {
	        //this.parent = parent;
	        this.info = info;
	        //execService = parent.getExecService();
	    }
		
		public String getName() {
			return info.getName();
		}

		public String getText() {
			return info.getText();
		}
		
		/* (non-Javadoc)
		 * @see com.opera.core.systems.IOperaWidget#isEnabled()
		 */
		public boolean isDefault() {
			return info.getDefaultLook();
		}
		
		public boolean hasFocusedLook() {
			return info.getFocusedLook();
		}

		public boolean isEnabled() {
			return info.getEnabled();
		}
		
		/* (non-Javadoc)
		 * @see com.opera.core.systems.IOperaWidget#isSelected()
		 */
		public boolean isSelected() {
			return info.getState() == 1;
		}
		
		/* (non-Javadoc)
		 * @see com.opera.core.systems.IOperaWidget#isVisible()
		 * TODO, FIXME: This needs to handle also if the dialog is not expanded, is off_screen etc.
		 */
		public boolean isVisible(){
			return info.getVisible() != 0 ? true : false; 
		}
		
		public boolean isDefault() {
			return info.getDefaultLook() &&  info.getFocusedLook();
		}
		
		
		/* (non-Javadoc)
		 * @see com.opera.core.systems.IOperaWidget#toggle()
		 */
		public boolean toggle() {
			// TODO FIXME
			return false; 
		}
		
		private DesktopWindowRect getRect() {
			return info.getRect();
		}
		
		
		public Point getLocation() {
			DesktopWindowRect rect = getRect();
			return new Point(rect.getX(), rect.getY());
		}

		/* (non-Javadoc)
		 * @see com.opera.core.systems.IOperaWidget#getSize()
		 */
		public Dimension getSize() {
			DesktopWindowRect rect = getRect();
			return new Dimension(rect.getWidth(), rect.getHeight());
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof QuickWidget) {
				QuickWidget ref = (QuickWidget) obj;
				return (ref.getName().equals(this.getName()));
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			int result = 42;
			result = 31 * result + getName().hashCode();
			return result;
		}
		
		@Override
		public String toString() {
			return "quickwidget" + hashCode();
		}
}



