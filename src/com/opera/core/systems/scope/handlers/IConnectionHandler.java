package com.opera.core.systems.scope.handlers;

import com.opera.core.systems.scope.stp.StpConnection;

public interface IConnectionHandler {
    
        /**
         * @param connection The connection that has just been connected or accepted.
         * @return false if the connection should be closed immediately, or true to continue.
         */
        boolean onConnected(StpConnection connection);
    
	void onDisconnect();
	
	void onException(Exception ex);

	void onResponseReceived();
}
