package com.opera.core.systems.scope.handlers;

public interface IConnectionHandler {
	void onDisconnect();
	
	void onException(Exception ex);

	void onResponseReceived();
}
