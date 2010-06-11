package com.opera.core.systems;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.opera.core.systems.model.FilterRule;
import com.opera.core.systems.scope.protos.ConsoleLoggerProtos.ConsoleMessage;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo;
import com.opera.core.systems.scope.protos.WmProtos.WindowInfo;


public class UmsEventHandler extends EventHandler {

	public UmsEventHandler(ScopeServices services) {
		super(services);
	}
	
	@Override
	public void onRuntimeStarted(RuntimeInfo info) {
		if(info.getHtmlFramePath().equals("_top") && info.getWindowID() == services.getWindowManager().getActiveWindowId()) {
			//check if we already have such a runtime and clean them up if needed
			services.getDebugger().cleanUpRuntimes();
			services.getDebugger().setRuntime(info);
		}
		services.getDebugger().addRuntime(info);
	}
	
	@Override
	public void onUpdatedWindow(WindowInfo info) {
		//logger.log(Level.INFO, window.toString());
		if(info.getOpenerID() == 0)
			services.getWindowManager().addWindow(info);
	}
	
	@Override
	public void onMessage(ConsoleMessage message) {
		List<IConsoleListener> listeners = services.getConsoleListeners();
		for(IConsoleListener listener : listeners) {
			if(listener.getFilters() != null) { //apply filters
				if(isFilteredMessage(listener.getFilters(), message))
					listener.onMessage(message.getDescription());
			} else
				listener.onMessage(message.getDescription());
		}
	}
	

	/**
	 * Checks if a given message matches any of the filters
	 * supplied in the filters map
	 * @param filters Map of filter rules based on fields (description, context, severity)
	 * @param message The console message to parse
	 * @return true if any of the filters match the given rule
	 */
	private boolean isFilteredMessage(Map<String, FilterRule> filters, ConsoleMessage message) {
		Iterator<Entry<String, FilterRule>> itr = filters.entrySet().iterator();
		while(itr.hasNext()) {
			Entry<String, FilterRule> entry = itr.next();
			String key = entry.getKey();
			if(message.hasField(ConsoleMessage.getDescriptor().findFieldByName(key))) {
				FilterRule rule = filters.get(key);
				String value = message.getField(ConsoleMessage.getDescriptor().findFieldByName(key)).toString();
				String match = entry.getValue().getValue();
				switch(rule.getType()) {
				case STARTS_WITH:
					if(value.startsWith(match))
						return true;
				case EQUALS:
					if(value.equals(match))
						return true;
				case CONTAINS:
					if(value.contains(match))
						return true;
				case ENDS_WITH:
					if(value.endsWith(match))
						return true;
				}
			}
		}
		return false;
	}

}
