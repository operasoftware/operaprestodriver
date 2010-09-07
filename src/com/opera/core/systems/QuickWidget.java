package com.opera.core.systems;

import java.awt.Dimension;
import java.awt.Point;

import org.openqa.selenium.By;
import com.opera.core.systems.scope.internal.OperaColors;
import com.opera.core.systems.scope.internal.OperaFlags;
import com.opera.core.systems.scope.internal.OperaMouseKeys;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetRect;
import com.opera.core.systems.scope.services.IOperaExec;

public class QuickWidget {
		private final QuickWidgetInfo info; // or just int objectId
		//private final OperaDriver parent;
		//private final IOperaExec execService;
		
		public int getObjectId() {
			return getWidgetID();
		}
		
		public int getWidgetID() {
			return info.getWidgetID();
		}
		
		public QuickWidget(QuickWidgetInfo info) {
	        //this.parent = parent;
	        this.info = info;
	        //execService = parent.getExecService();
	    }
		
		public String getName() {
			return getText();
		}

		public String getText() {
			return info.getText();
		}

		
		/* (non-Javadoc)
		 * @see com.opera.core.systems.IOperaWidget#isEnabled()
		 */
		public boolean isEnabled() {
			return info.getEnabled();
		}
		
		/* (non-Javadoc)
		 * @see com.opera.core.systems.IOperaWidget#isSelected()
		 */
		public boolean isSelected() {
			// TODO FIXME
			return false;
		}
		
		/* (non-Javadoc)
		 * @see com.opera.core.systems.IOperaWidget#isVisible()
		 */
		public boolean isVisible(){
			return info.getVisible() != 0 ? true : false; 
		}
		
		/* (non-Javadoc)
		 * @see com.opera.core.systems.IOperaWidget#toggle()
		 */
		public boolean toggle() {
			// TODO FIXME
			return false; 
		}
		
		private QuickWidgetRect getRect() {
			return info.getRect();
		}
		
		
		public Point getLocation() {
			QuickWidgetRect rect = getRect();
			return new Point(rect.getX(), rect.getY());
		}

		/* (non-Javadoc)
		 * @see com.opera.core.systems.IOperaWidget#getSize()
		 */
		public Dimension getSize() {
			QuickWidgetRect rect = getRect();
			return new Dimension(rect.getWidth(), rect.getHeight());
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof QuickWidget) {
				QuickWidget ref = (QuickWidget) obj;
				return (((QuickWidget)ref).getObjectId() == this.getObjectId());
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			int result = 42;
			result = 31 * result + getObjectId();
			return result;
		}
		
		@Override
		public String toString() {
			return "quickwidget" + hashCode();
		}

}



