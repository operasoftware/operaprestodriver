/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */

package com.opera.core.systems.scope;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathNotFoundException;
import org.apache.commons.jxpath.Pointer;
import org.openqa.selenium.WebDriverException;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.AbstractMessage.Builder;
import com.opera.core.systems.ScopeConnection;
import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.model.ICommand;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.protos.UmsProtos.Response;

/**
 * All scope services derive from this base class for generic operations
 * @author Deniz Turkoglu
 *
 */
public abstract class AbstractService {

	protected ScopeServices services;
	
	private static final Logger logger = Logger.getLogger(AbstractService.class.getName());
		
	private String serviceName;
	private final ScopeConnection connection;
	private final String version;
	
	protected int messageOffset;
	
	public String getVersion() {
		return version;
	}

	public AbstractService(ScopeServices services, String serviceName, String version) {
		this.services = services;
		this.version = version;
		this.serviceName = serviceName;
		this.connection = services.getConnection();
		this.messageOffset = serviceName.length() + 22;
	}

        @Deprecated
	public static void sleep(long timeInMillis) {
		try {
			Thread.sleep(timeInMillis);
		} catch (InterruptedException e) {
			//ignore
		}
	}
	
	public Response executeCommand(ICommand command, Builder<?> builder) {
		return connection.executeCommand(command, serviceName, builder);
	}
	
	public Response executeCommand(ICommand command, Builder<?> builder, long timeout) {
		return connection.executeCommand(command, serviceName, builder, timeout);
	}

	/**
	 * Send an xml message to scope, xml tag is prepended
	 * @param using message to send
	 */
	public void post(String using) {
            
            connection.sendXmlMessage(serviceName, using);
            waitFor("using");
	}
	
	/**
	 * Send a message to scope, xml must be intact
	 * @param using
	 */
	public void postRequest(String using) {
		connection.send(serviceName + " " + using);
	}

	/**
	 * Query a collection with JXPath and return value of node
	 * @param collection
	 * @param query a valid XPath query
	 * @return result
	 */
	public Object xpathQuery(Collection<?> collection, String query) {
		JXPathContext pathContext = JXPathContext.newContext(collection);
		Object result = null;
		try {
			result = pathContext.getValue(query);
		} catch (JXPathNotFoundException e) {
			logger.log(Level.WARNING, "JXPath exception: {0}", e.getMessage());
		}
		return result;
	}
	
	/**
	 * Query a collection JXPath and return a pointer
	 * @param collection
	 * @param query
	 * @return Pointer to node
	 */
	public Pointer xpathPointer(Collection<?> collection, String query) {
		JXPathContext pathContext = JXPathContext.newContext(collection);
		Pointer result = null;
		try {
			result = pathContext.getPointer(query);
		} catch (JXPathNotFoundException e) {
			logger.log(Level.WARNING, "JXPath exception: {0}", e.getMessage());
		}
		return result;
	}
	
	
	/**
	 * Query a collection JXPath and return a pointer
	 * @param collection
	 * @param query
	 * @return Pointer to node
	 */
	public Iterator<?> xpathIterator(Collection<?> collection, String query) {
		JXPathContext pathContext = JXPathContext.newContext(collection);
		Iterator<?> result = null;
		try {
			result = pathContext.iteratePointers(query);
		} catch (JXPathNotFoundException e) {
			logger.log(Level.WARNING, "JXPath exception: {0}", e.getMessage());
		}
		return result;
	}
	
	//FIXME revise, and eventually remove
	
	/**
	 * Waits (timeout ms) for a response message that starts with
	 * given string if there is an internal error returns immediately
	 * @param startsWith
	 * @param timeout Wait timeout on queue(other responses might affect)
	 * @return
	 */
        @Deprecated
	public String waitForResponse(String startsWith, long timeout) {
		String response = ""; // services.getConnection().getResponse(timeout);
		/*
		if(!response.startsWith(startsWith, this.messageOffset))
			throw new WebDriverException("Unexpected response");
		*/
		if(response.isEmpty())
			throw new WebDriverException("Could not get the response in a timely manner");
		return response;
		/*
		String response;
		
		do {
			response = pollQueue(timeout);
			if(response.startsWith("<error>Internal error</error>", this.messageOffset)){
				return response;
			}
			discardedResponses.incrementAndGet();
		} while (!(response.startsWith(startsWith, this.messageOffset)));
	
		return response;
		*/
	}
	
	/**
	 * Wait for response with default timeout
	 * FIXME Make configurable
	 * @param startsWith
	 * @return
	 */
        @Deprecated
	public String waitForResponse(String startsWith) {
		return waitForResponse(startsWith, OperaIntervals.RESPONSE_TIMEOUT.getValue());
	}
	
	public static final GeneratedMessage.Builder<?> buildPayload(Response response, GeneratedMessage.Builder<?> builder) {
		return buildMessage(builder, response.getPayload().toByteArray());
	}
	
	private static final GeneratedMessage.Builder<?> buildMessage(GeneratedMessage.Builder<?> builder, byte[] message) {
		try {
			return builder.mergeFrom(message);
		} catch (InvalidProtocolBufferException ex) {
			throw new WebDriverException("Could not build " + builder.getDescriptorForType().getFullName() +  " : " + ex.getMessage());
		}
	}

}
