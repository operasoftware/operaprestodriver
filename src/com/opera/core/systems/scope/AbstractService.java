/*
Copyright 2008-2012 Opera Software ASA

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

import com.opera.core.systems.internal.VersionUtil;
import com.opera.core.systems.scope.exceptions.ScopeException;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.protos.UmsProtos.Response;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathNotFoundException;
import org.apache.commons.jxpath.Pointer;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * All Scope services derive from this abstract base class for generic operations.
 */
public abstract class AbstractService implements Service {

  protected final ScopeServices services;

  private final Logger logger = Logger.getLogger(getClass().getName());
  private final ScopeService service;
  private final String version;

  public AbstractService(ScopeServices services, String name) {
    this(services, name, null);
  }

  public AbstractService(ScopeServices services, String name, String requiredVersion) {
    this.services = services;
    this.service = ScopeService.get(name);
    this.version = services.getAvailableServices().get(ScopeService.get(name));

    checkRequiredVersion(requiredVersion);
  }

  private void checkRequiredVersion(String requiredVersion) {
    if (requiredVersion == null) {
      return;
    }

    if (!isVersionInRange(requiredVersion)) {
      throw new ScopeException(String.format(
          "Unmet version dependency for Scope service %s: Requested version %s, but only has " +
          "version %s", getServiceName(), requiredVersion, getServiceVersion()));
    }
  }

  public boolean isVersionInRange(String requiredVersion) {
    checkNotNull(requiredVersion);
    return VersionUtil.compare(getServiceVersion(), requiredVersion) >= 0;
  }

  public String getServiceName() {
    return service.toOpera();
  }

  public String getServiceVersion() {
    return version;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  public Response executeMessage(Message message) {
    return executeMessage(message, null);
  }

  public Response executeMessage(Message message, Builder<?> builder) {
    if (services.getConnection() == null) {
      return Response.getDefaultInstance();
    }
    return executeMessage(message, builder, OperaIntervals.DEFAULT_RESPONSE_TIMEOUT.getMs());
  }

  public Response executeMessage(Message message, Builder<?> builder, long timeout) {
    if (services.getConnection() == null) {
      return Response.getDefaultInstance();
    }
    return services.executeMessage(message, builder, timeout);
  }

  /**
   * Query a collection JXPath and return a pointer.
   *
   * @return pointer to node
   */
  // TODO: This does not belong here!
  public Pointer xpathPointer(Collection<?> collection, String query) {
    JXPathContext pathContext = JXPathContext.newContext(collection);
    Pointer result = null;

    try {
      result = pathContext.getPointer(query);
    } catch (JXPathNotFoundException e) {
      logger.warning(String.format("JXPath exception: %s", e.getMessage()));
    }

    return result;
  }

  /**
   * Query a collection JXPath and return a pointer.
   *
   * @return pointer to node
   */
  // TODO: This does not belong here!
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

  public static GeneratedMessage.Builder<?> buildPayload(
      Response response, GeneratedMessage.Builder<?> builder) {
    return buildMessage(builder, response.getPayload().toByteArray());
  }

  private static GeneratedMessage.Builder<?> buildMessage(
      GeneratedMessage.Builder<?> builder, byte[] message) {
    try {
      return builder.mergeFrom(message);
    } catch (InvalidProtocolBufferException e) {
      throw new ScopeException(String.format("Could not build %s: %s",
                                             builder.getDescriptorForType().getFullName(),
                                             e.getMessage()));
    }
  }

  public static void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException ignored) {
      Thread.currentThread().interrupt();
    }
  }

}