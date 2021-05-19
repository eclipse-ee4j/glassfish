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

package org.glassfish.contextpropagation.internal;

// TODO LATER(After initial checkin) this class will need minor adjustments in most of its accessors once we merge with Glassfish

import org.glassfish.contextpropagation.bootstrap.ContextAccessController;
import org.glassfish.contextpropagation.bootstrap.DependencyProvider;
import org.glassfish.contextpropagation.bootstrap.LoggerAdapter;
import org.glassfish.contextpropagation.bootstrap.ThreadLocalAccessor;
import org.glassfish.contextpropagation.internal.AccessControlledMap.ContextAccessLevel;
import org.glassfish.contextpropagation.wireadapters.WireAdapter;
import org.glassfish.contextpropagation.wireadapters.glassfish.DefaultWireAdapter;
import org.glassfish.contextpropagation.wireadapters.wls.WLSWireAdapter;
import org.jvnet.hk2.annotations.Service;

//@Singleton
//@Named("myService")
//@Default from jakarta.enterprise.inject may be appropriate
/**
 * Provides the context-propagation dependencies in Glassfish. Other products
 * should consider replacing this implementation with their own.
 */
@Service
public class DependencyProviderImpl implements DependencyProvider {

  private boolean isClosedSource;

  public DependencyProviderImpl() {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    try {
      isClosedSource = cl.loadClass("weblogic.workarea.WorkContextMap") != null;
    } catch (ClassNotFoundException e) {
      isClosedSource = false;
    }
  }

  @Override
  public LoggerAdapter getLoggerAdapter() {
    return new LoggerAdapter() {
      @Override
      public boolean isLoggable(Level level) {
        return true;
      }

      @Override
      public void log(Level level, MessageID messageID, Object... args) {
        System.out.println(format(messageID.defaultMessage, args));

      }

      private String format(String defaultMessage, Object... args) {
        String formatString = defaultMessage.replaceAll("%([0-9]*)", "%$1\\$s"); // $1 refers to the group %1 is equivalent to %1$s
        return String.format(formatString, args);
      }

      @Override
      public void log(Level level, Throwable t, MessageID messageID, Object... args) {
        log(level, messageID, args);
        t.printStackTrace();
      }
    };
  }

  @Override
  public ThreadLocalAccessor getThreadLocalAccessor() {
    return new ThreadLocalAccessor() {
      private ThreadLocal<AccessControlledMap> mapThreadLocal = new ThreadLocal<AccessControlledMap>();

      @Override
      public void set(AccessControlledMap contextMap) {
        mapThreadLocal.set(contextMap);
      }

      @Override
      public AccessControlledMap get() {
        return mapThreadLocal.get();
      }
    };
  }

  @Override
  public ContextAccessController getContextAccessController() {
    return new ContextAccessController() {
      @Override
      public boolean isAccessAllowed(String key, AccessControlledMap.ContextAccessLevel type) {
        if (type == ContextAccessLevel.READ && isEveryoneAllowedToRead(key)) {
          return true; // First do a quick check for read access
        }
        return true;
      }

      @Override
      public boolean isEveryoneAllowedToRead(String key) {
        return false;
      }
    };
  }

  @Override
  public WireAdapter getDefaultWireAdapter() {
    return isClosedSource ? new WLSWireAdapter() : new DefaultWireAdapter();
  }

  @Override
  public String getGuid() {
    return "guid";
  }
}
