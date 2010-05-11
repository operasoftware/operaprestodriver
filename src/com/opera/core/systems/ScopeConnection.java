package com.opera.core.systems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriverException;

import com.google.protobuf.ByteString;
import com.google.protobuf.AbstractMessage.Builder;
import com.opera.core.systems.model.ICommand;
import com.opera.core.systems.scope.exceptions.ResponseNotReceivedException;
import com.opera.core.systems.scope.handlers.AbstractEventHandler;
import com.opera.core.systems.scope.handlers.IConnectionHandler;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.protos.UmsProtos.Command;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.stp.StpConnection;

public class ScopeConnection {
	
	private List<String> listedServices;
	private List<String> enabledServices;
	private StpConnection connection;

	private int tag;
	
	protected CountDownLatch responseLatch = new CountDownLatch(1);
	
	public CountDownLatch getResponseReceivedLatch() {
		return this.responseLatch;
	}

	public List<String> getEnabledServices() {
		return enabledServices;
	}
	
	public List<String> getListedServices() {
		return listedServices;
	}
	
	public void setEventHandler(AbstractEventHandler eventHandler){
		connection.setEventHandler(eventHandler);
	}
	
	public boolean isConnected() {
		return connection.isConnected();
	}

	public ScopeConnection(IConnectionHandler handler) {
		connection = new StpConnection((int)OperaIntervals.SERVER_PORT.getValue(), handler);
	}
	
	/**
	 * Initialize the connection, scope connection
	 * is run at a separate thread on async network IO
	 * After handshake we no longer need to listen as we
	 * currently only support 1-1
	 */
	public void init(){
		Thread worker = new Thread(null, connection, "stp-connection");
		//wait for connection
		worker.start();
	}
	
	public void waitForHandShake() {
		//wait for handshake, this is a blocking call, so we have to call Opera just
		//before this one
		String serviceMessage = getServiceMessage();
		
		//after the handshake we no longer need the server channel to listen
		connection.closeListener();
		initializeServices(serviceMessage);
		
		if(listedServices == null)
			throw new WebDriverException("Services response error");
		
		enabledServices = new ArrayList<String>();
	}
	
	public boolean isStp1() {
		return listedServices.contains("stp-1");
	}
	
	public void switchToStp0(){
		connection.switchToStp0();
	}
	
	public void switchToStp1(){
		connection.switchToStp1();
		enableStp1();
	}
	
	private void enableStp1(){
		send("*enable stp-1");
		waitForResponse(OperaIntervals.RESPONSE_TIMEOUT.getValue());
	}
	
	private void waitForResponse(long timeout) {
		boolean responseReceived;
		try {
			responseReceived = responseLatch.await(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new WebDriverException("Exception while waiting for a response : " + e.getMessage());
		}
		if(!responseReceived) {
			throw new ResponseNotReceivedException("Could not get a response in a timely manner");
		}
		
		
	}

	/**
	 * Get the service message, this is a blocking call
	 * @return
	 */
	private String getServiceMessage() {
		String serviceMessage = getResponse(OperaIntervals.HANDSHAKE_TIMEOUT.getValue());
		if(serviceMessage.isEmpty()) {
			connection.closeListener();
			connection.close();
			throw new WebDriverException("No handshake recevied in " + (OperaIntervals.HANDSHAKE_TIMEOUT.getValue() / 1000) + " seconds");
		}
		return serviceMessage;
	}

	/**
	 * Parse the services message
	 * @param serviceMessage
	 */
	public void initializeServices(String serviceMessage){
		int cutPoint = serviceMessage.indexOf(" ");
		
		if(cutPoint < 0) {
			connection.close();
			throw new WebDriverException("No handshake received or message is incomplete, message received : " + serviceMessage);
		}
		
		cutPoint++;
		String serviceList = serviceMessage.substring(cutPoint);
		listedServices = Arrays.asList(serviceList.split(","));
	}

	/**
	 * Send 'quit' command to scope
	 */
	public void quit() throws WebDriverException {
               connection.send("*quit");
	}
	
	/**
	 * Close the connection and cleanup the channel
	 */
	public void close() {
		connection.close();
	}
	
	/**
	 * Get the response (in specified timeout), if
	 * response is null (on timeout) throw exception
	 * @param timeout
	 * @return
	 */
	public String getResponse(long timeout) {
            try {
                responseLatch.await(timeout, TimeUnit.MILLISECONDS);
                return connection.getAndEmptyResponse();
            } catch (InterruptedException e) {
                throw new WebDriverException("Failed to get response in a timely fashion");
            }
	}
	
	/**
	 * Enable requested services if they were announced
	 * @param services
	 * @return
	 */
	public List<String> enableServices(List<String> services) throws WebDriverException {
            for (String requestedService : services) {
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
	public void send(String message) {
            responseLatch = new CountDownLatch(1);
            connection.send(message);
	}

	/**
	 * Send a message prepending xml tag
	 * @param serviceName
	 * @param message
	 */
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
		command.setTag(++tag);
		
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
            responseLatch = new CountDownLatch(1);

            connection.send(commandBuilder.build());
		
            waitForResponse(timeout);
            Response response = connection.getStpResponse();
            if(response == null)
                return null;

            if(response.getTag() != tag)
                throw new WebDriverException("Protocol error, tag mismatch, expected " + tag + ", got " + response.getTag() + "\n " + command.toString());
            return response;
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
