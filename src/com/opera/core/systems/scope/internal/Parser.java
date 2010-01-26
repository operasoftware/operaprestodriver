/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.scope.internal;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.scope.services.xml.XmlServices;

public class Parser {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	// JAXB marshaller and unmarshaller are not thread safe,
	// create for each thread
	private final Unmarshaller unmarshaller = initUnmarshaller();
	private final Marshaller marshaller = initMarshaller();

	private Unmarshaller initUnmarshaller() {
		try {
			return XmlServices.context.createUnmarshaller();
		} catch (JAXBException e) {
			throw new WebDriverException("Could not create the unmarshaller");
		}
	}

	private Marshaller initMarshaller() {
		try {
			return XmlServices.context.createMarshaller();
		} catch (JAXBException e) {
			throw new WebDriverException("Could not create the marshaller");
		}
	}

	/**
	 * Parses a result from string and creates a JAXB object uses the
	 * unmarshaller from cache if available
	 * 
	 * @param response
	 * @param type
	 *            The object to parse into
	 * @return
	 */
	public Object parseResponse(String response) {
		Object result = null;
		try {
			String parseable = response.substring(response.indexOf(" ") + 1);
			result = unmarshaller.unmarshal(new ByteArrayInputStream(parseable.getBytes("UTF-16BE")));
		} catch (JAXBException e) {
			logger.log(Level.INFO, response);
			logger.log(Level.WARNING, "Could not parse the message: {0}", e.getMessage());
		} catch (UnsupportedEncodingException ex) {
			logger.log(Level.WARNING, "Encoding not supported: {0}", ex.getMessage());
		}
		return result;
	}

	/**
	 * Creates an XML document via JAXB
	 * 
	 * @param request
	 * @param type
	 * @return a string presentation of XML document
	 */
	public String createRequest(Object request, Class<?> type) {
		String result = null;
		try {
			StringWriter writer = new StringWriter();
			marshaller.marshal(request, writer);
			result = writer.toString();
		} catch (JAXBException e) {
			logger.log(Level.WARNING, "Could not create the message: {0}", e.getMessage());
		}
		return result;
	}
}
