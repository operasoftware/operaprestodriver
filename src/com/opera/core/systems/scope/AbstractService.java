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
import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.model.ICommand;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.internal.OperaIntervals;

/**
 * All scope services derive from this base class for generic operations
 * @author Deniz Turkoglu
 *
 */
public abstract class AbstractService {

	protected ScopeServices services;
	
	private static final Logger logger = Logger.getLogger(AbstractService.class.getName());
		
	private final String version;
	
	public String getVersion() {
		return version;
	}

	public AbstractService(ScopeServices services, String version) {
		this.services = services;
		this.version = version;
	}

	public static void sleep(long timeInMillis) {
		try {
			Thread.sleep(timeInMillis);
		} catch (InterruptedException ignored) {
			//ignore
		}
	}
	
	public Response executeCommand(ICommand command, Builder<?> builder) {
		return executeCommand(command, builder, OperaIntervals.DEFAULT_RESPONSE_TIMEOUT.getValue());
	}
	
	public Response executeCommand(ICommand command, Builder<?> builder, long timeout) {
		return services.executeCommand(command, builder, timeout);
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
	 * FIXME: This does not belong here!
	 * 
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
	 * FIXME: This does not belong here!
	 * 
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
