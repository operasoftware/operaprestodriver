/*
Copyright 2008-2011 Opera Software ASA

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.opera.core.systems.scope;

import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.model.ICommand;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.util.VersionUtil;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathNotFoundException;
import org.apache.commons.jxpath.Pointer;
import org.openqa.selenium.WebDriverException;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * All scope services derive from this base class for generic operations
 *
 * @author Deniz Turkoglu
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
      // ignore
    }
  }

  /**
   * Returns true if the given version is less than the maximum given version and greater than the
   * minimum version set in the ScopeServices constructor by OperaDriver.
   *
   * @param version     Service version
   * @param maxVersion  Maximum version for the service
   * @param serviceName Name of the service (used for the error message)
   */
  public boolean isVersionInRange(String version, String maxVersion,
                                  String serviceName) {

    logger.warning(String.valueOf(VersionUtil.compare(version, maxVersion)));
    logger.warning(String.valueOf(VersionUtil.compare(version, services.getMinVersionFor(serviceName))));

    if (VersionUtil.compare(version, maxVersion) > 0
        || VersionUtil.compare(version, services.getMinVersionFor(serviceName)) < 0) {
      return false;
    }
    return true;
  }

  public Response executeCommand(ICommand command, Builder<?> builder) {
    if (services.getConnection() == null) {
      return Response.getDefaultInstance();
    }
    return executeCommand(command, builder,
                          OperaIntervals.DEFAULT_RESPONSE_TIMEOUT.getValue());
  }

  public Response executeCommand(ICommand command, Builder<?> builder,
                                 long timeout) {
    if (services.getConnection() == null) {
      return Response.getDefaultInstance();
    }
    return services.executeCommand(command, builder, timeout);
  }

  /**
   * Query a collection with JXPath and return value of node
   *
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
   * Query a collection JXPath and return a pointer FIXME: This does not belong here!
   *
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
   * Query a collection JXPath and return a pointer FIXME: This does not belong here!
   *
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

  public static final GeneratedMessage.Builder<?> buildPayload(
      Response response, GeneratedMessage.Builder<?> builder) {
    return buildMessage(builder, response.getPayload().toByteArray());
  }

  private static final GeneratedMessage.Builder<?> buildMessage(
      GeneratedMessage.Builder<?> builder, byte[] message) {
    try {
      return builder.mergeFrom(message);
    } catch (InvalidProtocolBufferException ex) {
      throw new WebDriverException("Could not build "
                                   + builder.getDescriptorForType().getFullName() + " : "
                                   + ex.getMessage());
    }
  }

}
