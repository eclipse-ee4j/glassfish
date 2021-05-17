/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.contextpropagation.bootstrap;


import org.glassfish.contextpropagation.bootstrap.LoggerAdapter.Level;
import org.glassfish.contextpropagation.bootstrap.LoggerAdapter.MessageID;
import org.glassfish.contextpropagation.internal.DependencyProviderImpl;
import org.glassfish.contextpropagation.wireadapters.WireAdapter;

/**
 * The classes in this package were designed to facilitate the integration of
 * the context propagation feature in a server. Through the use of inversion of
 * control, we were able to abstract all the dependencies needed by context
 * propagation thus making this feature easy to port to another server. In this
 * way, we hope to encourage the use of context propagation on other servers.
 */
public class ContextBootstrap {
  private static LoggerAdapter loggerAdapter;
  private static ThreadLocalAccessor threadLocalAccessor;
  private static ContextAccessController contextAccessController;
  private static boolean isConfigured;
  private static WireAdapter wireAdapter;
  private static String guid;
  public static boolean IS_DEBUG;

  private static DependencyProvider dependencyProvider;
  static {
    if (dependencyProvider == null) {
      dependencyProvider = new DependencyProviderImpl(); // The service should have been injected, But we are not taking any chances.
    }
    if (dependencyProvider != null) {
      configure(dependencyProvider.getLoggerAdapter(),
          dependencyProvider.getDefaultWireAdapter(),
          dependencyProvider.getThreadLocalAccessor(),
          dependencyProvider.getContextAccessController(),
          dependencyProvider.getGuid());
    }
  }

  /**
   * This function must be called by the server prior to using context propagation.
   * @param loggerAdapter An adaptor to the logger that is appropriate for
   * context propagation messages.
   * @param tla An adaptor to the thread management system that allows safe
   * storage of the ContextMap on the current thread.
   * @param contextAccessController An adaptor to the security manager that
   * is used to determine access to particular work contexts by the user
   * associated to the current thread.
   * @param aGuid a unique identifier for this process that is suitable for
   * transmission over the wire.
   */
  public static void configure(LoggerAdapter aLoggerAdapter,
      WireAdapter aWireAdapter, ThreadLocalAccessor aThreadLocalAccessor,
      ContextAccessController aContextAccessController, String aGuid) {
    if (isConfigured) {
      throw new IllegalStateException("WorkArea is already configured");
    }
    if (aLoggerAdapter == null || aWireAdapter == null ||
        aThreadLocalAccessor == null || aContextAccessController == null ) {
      throw new IllegalArgumentException(
          "logger and wire adapters, threadLocalAccessor and " +
          "contextAccessController must be specified.");
    }
    loggerAdapter = aLoggerAdapter;
    wireAdapter = aWireAdapter;
    threadLocalAccessor = aThreadLocalAccessor;
    contextAccessController = aContextAccessController;
    guid = aGuid;
    IS_DEBUG = loggerAdapter.isLoggable(Level.DEBUG);

    isConfigured = true;
}

  /**
   * @return The bootstrapped WireAdapter
   */
  public static WireAdapter getWireAdapter() {
    checkIfConfigured();
    return wireAdapter;
  }

  private static void checkIfConfigured() {
    if (!isConfigured) {
      throw new IllegalStateException("Context propagation is not yet configured.");
    }
  }

  /**
   * @return The bootstrapped LoggerAdapter
   */
  public static LoggerAdapter getLoggerAdapter() {
    checkIfConfigured();
    return loggerAdapter;
  }

  /**
   * @param messageID a MessageID
   * @param args The objects to in the message
   */
  public static void debug(MessageID messageID, Object... args) {
    if (loggerAdapter.isLoggable(Level.DEBUG)) {
      loggerAdapter.log(Level.DEBUG, messageID, args);
    }
  }

  /**
   * @param t a Throwable to include in the debug message
   * @param messageID a MessageID
   * @param args The objects to in the message
   */
  public static void debug(Throwable t, MessageID messageID, Object... args) {
    if (loggerAdapter.isLoggable(Level.DEBUG)) {
      loggerAdapter.log(Level.DEBUG, t, messageID, args);
    }
  }

  /**
   * @return The adaptor to access the ContextMap stored on the curren thread
   */
  public static ThreadLocalAccessor getThreadLocalAccessor() {
    checkIfConfigured();
      return threadLocalAccessor;
  }

  /**
   * @return The adapter that checks acccess permissions.
   */
  public static ContextAccessController getContextAccessController() {
    checkIfConfigured();
    return contextAccessController;
  }

  /**
   * @return a String that uniquely identifies this process
   */
  public static String getGuid() {
    checkIfConfigured();
    return guid;
  }


}
