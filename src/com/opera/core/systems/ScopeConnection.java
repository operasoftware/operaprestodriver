package com.opera.core.systems;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.openqa.selenium.WebDriverException;

import com.google.protobuf.ByteString;
import com.google.protobuf.AbstractMessage.Builder;
import com.opera.core.systems.model.ICommand;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.protos.UmsProtos.Command;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.stp.StpConnection;

public class ScopeConnection {
	
	private List<String> listedServices;
	private List<String> enabledServices;
	private StpConnection connection = null;
        private WaitState waitState;
        private ScopeServices services;
       
	private int tagCounter;
	
	protected CountDownLatch responseLatch = new CountDownLatch(1);
	
        @Deprecated
	public CountDownLatch getResponseReceivedLatch() {
		return this.responseLatch;
	}

	public List<String> getEnabledServices() {
		return enabledServices;
	}
	
	public List<String> getListedServices() {
		return listedServices;
	}
	
	public boolean isConnected() {
		return connection.isConnected();
	}

	public ScopeConnection(StpConnection connection, ScopeServices services, WaitState waitState) {
                this.connection = connection;
                this.services = services;
                this.waitState = waitState;
	}
	
	private Response waitForResponse(int tag, long timeout)
        {
            try {
                waitState.waitFor(tag, timeout);
            } catch (WebDriverException e) {
                services.shutdown();
                throw e;
            }
            Response response = connection.getStpResponse();
            return response;
	}


	/**
	 * Parse the services message
	 * @param serviceMessage
	 */
	public void setListedServices(java.util.List<String> services) {
		listedServices = services;
	}

	/**
	 * Send 'quit' command to scope
	 */
	public void quit() throws WebDriverException {
               connection.sendQuit();
	}
	
	/**
	 * Close the connection and cleanup the channel
	 */
	public void close() {
		connection.close();
	}
	
	/**
	 * Enable requested services if they were announced
	 * @param services
	 * @return
	 */
	public List<String> enableServices(List<String> services) throws WebDriverException {
            for (String requestedService : services) {
                System.out.println("Enable service " + requestedService);
                if(listedServices.contains(requestedService)){
                    connection.send("*enable " + requestedService);
                    enabledServices.add(requestedService);
                } else {
                    throw new WebDriverException("Error while enabling : " + requestedService + ", service does not exist in announced services");
                }
            }
            return enabledServices;
	}

	/**
	 * Send a message directly
	 * @param message
	 */
        @Deprecated
	public void send(String message) {
            responseLatch = new CountDownLatch(1);
            connection.send(message);
	}

	/**
	 * Send a message prepending xml tag
	 * @param serviceName
	 * @param message
	 */
        @Deprecated
        public void sendXmlMessage(String serviceName, String message) {
            responseLatch = new CountDownLatch(1);
            String xml = (serviceName + " <?xml version=\"1.0\"?>" + message);
            connection.send(xml);
	}
	
	
	private Command.Builder buildCommand(int commandId, String service, ByteString payload){
		Command.Builder command  = Command.newBuilder();
		command.setCommandID(commandId); //connect
		command.setFormat(0); //protobuf
		command.setService(service);
		command.setTag(++tagCounter);
		
		command.setPayload(payload);
		return command;
	}
	
	/**
	 * Sends a command
	 * @param serviceName
	 * @param message
	 */
	//FIXME (ask) move the tag control logic from here to another layer?
	//FIXME protocol is async, is it OK to use sync this way?
	public Response executeCommand(ICommand command, String service, Builder<?> builder) {
		return executeCommand(command, service, builder,OperaIntervals.RESPONSE_TIMEOUT.getValue());
	}
	
	public Response executeCommand(ICommand command, String service, Builder<?> builder, long timeout) {
            ByteString payload = (builder != null) ? builder.build().toByteString() : ByteString.EMPTY; 
            Command.Builder commandBuilder = buildCommand(command.getCommandID(), service, payload);
            int tag = commandBuilder.getTag();
            connection.send(commandBuilder.build());
            return waitForResponse(tag, timeout);
	}

	/**
	 * Disable requested services --only if they are enabled before
	 * @param services
	 * @return
	 */
	public List<String> disableServices(List<String> services) {
            for (String requestedService : services) {
                if(enabledServices.contains(requestedService)){
                    connection.send("*disable " + requestedService);
                    enabledServices.remove(requestedService);
                }
            }
            return enabledServices;
        }
}
