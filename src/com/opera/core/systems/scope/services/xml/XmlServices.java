package com.opera.core.systems.scope.services.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.beans.ObjectFactory;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IOperaExec;
import com.opera.core.systems.scope.services.IWindowManager;

public class XmlServices {

	protected final IEcmaScriptDebugger debugger;
	protected final IWindowManager windowManager;
	protected final IOperaExec exec;
	
	
	public static JAXBContext context = initContext();
	
	
    private static JAXBContext initContext() {
        try {
			return JAXBContext.newInstance(ObjectFactory.class);
		} catch (JAXBException e) {
			throw new WebDriverException("Error initializing the JAXB context");
		}
    }

	public IEcmaScriptDebugger getDebugger() {
		return debugger;
	}

	public IWindowManager getWindowManager() {
		return windowManager;
	}

	public IOperaExec getExec() {
		return exec;
	}
	
	public XmlServices(ScopeServices services) {
		windowManager = new WindowManager(services, "window-manager");
		debugger = new EcmaScriptDebugger(services, "ecmascript-debugger");
		exec = new OperaExec(services, "exec");
	}
}
