/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.scope.internal;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

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
	 * Implemented from https://jaxb.dev.java.net/tutorial/section_3_1-Unmarshalling-and-Using-the-Data.html#Unmarshalling and Using the Data
	 * Uses StreamSource instead of casting (type safe)
	 * @param <T>
	 * @param docClass
	 * @param response
	 * @return
	 */
	public <T>T parseResponse(Class<T> docClass, String response) {
		JAXBElement<T> result = null;
		try {
			String parseable = response.substring(response.indexOf(" ") + 1);
			StreamSource source = new StreamSource(new ByteArrayInputStream(parseable.getBytes("UTF-16BE")));
			result = unmarshaller.unmarshal(source, docClass);
		} catch (JAXBException e) {
			logger.log(Level.INFO, response);
			logger.log(Level.WARNING, "Could not parse the message: {0}", e.getMessage());
		} catch (UnsupportedEncodingException ex) {
			logger.log(Level.WARNING, "Encoding not supported: {0}", ex.getMessage());
		}
		
		return (result != null) ? result.getValue() : null;
	}

	/**
	 * Creates an XML document via JAXB
	 * 
	 * @param request
	 * @param type
	 * FIXME type is not needed 
	 * @return a string presentation of XML document
	 */
	public String createRequest(Object request, Class<?> type) {
		String result = null;
		try {
			StringWriter writer = new StringWriter();
			marshaller.marshal(request, writer);
			result = writer.toString();
		} catch (JAXBException e) {
			logger.log(Level.WARNING, "Could not create {0} message: {1}", new String[]{type.getName(), e.getMessage()});
		}
		return result;
	}
}
