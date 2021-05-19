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

package org.glassfish.contextpropagation.wireadapters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.contextpropagation.SerializableContextFactory;
import org.glassfish.contextpropagation.bootstrap.LoggerAdapter.MessageID;
import org.glassfish.contextpropagation.internal.Entry;
import org.glassfish.contextpropagation.internal.Utils;

/**
 * Concrete WireAdapter instances define the methods for transforming
 * Entry's into serialized wire data and vice versa.
 *
 * WireAdapters should thrive to:
 *  - encode data efficiently and compactly
 *  - be resilient to errors -- a problem decoding an entry should not cause the
 *    decoding of subsequent entries to fail.
 */
public interface WireAdapter {
  static interface WireAdapterHelper {
    SerializableContextFactory findContextFactory(String contextName, String wireClassName);
    public void registerContextFactoryForContextNamed(String contextName,
        String wireClassName, SerializableContextFactory factory);
    public void registerContextFactoryForClass(Class<?> contextClass,
        String wireClassName, SerializableContextFactory factory);
  }
  public void prepareToWriteTo(OutputStream out) throws IOException;
  public <T> void write(String key, Entry entry) throws IOException;
  public void flush() throws IOException;
  public void prepareToReadFrom(InputStream is) throws IOException;
  public String readKey() throws IOException;
  public Entry readEntry() throws IOException, ClassNotFoundException;

  public static final WireAdapterHelper HELPER = new WireAdapterHelper() {
    Map<String, SerializableContextFactory> contextFactoriesByContextName = new HashMap<String, SerializableContextFactory>();
    Map<String, String> wireClassNameByContextName = new HashMap<String, String>();
    public void registerContextFactoryForContextNamed(String contextName,
        String wireClassName, SerializableContextFactory factory) {
      Utils.validateFactoryRegistrationArgs("contextName",
          MessageID.WARN_FACTORY_ALREADY_REGISTERED_FOR_NAME, contextName,
          factory, contextFactoriesByContextName);
      contextFactoriesByContextName.put(contextName, factory);
      wireClassNameByContextName.put(contextName, wireClassName);
    }

    Map<String, SerializableContextFactory> contextFactoriesByClassName = new HashMap<String, SerializableContextFactory>();
    Map<String, String> wireClassNameByClassName = new HashMap<String, String>();
    public void registerContextFactoryForClass(Class<?> contextClass,
        String wireClassName, SerializableContextFactory factory) {
      Utils.validateFactoryRegistrationArgs("Context class name",
          MessageID.WARN_FACTORY_ALREADY_REGISTERED_FOR_CLASS,
          contextClass.getName(), factory, contextFactoriesByClassName);
      contextFactoriesByClassName.put(wireClassName, factory);
      wireClassNameByClassName.put(contextClass.getName(), wireClassName);
    }

    @Override
    public SerializableContextFactory findContextFactory(String contextName,
        String wireClassName) {
      SerializableContextFactory factory = contextFactoriesByClassName.get(wireClassName);
      if (factory == null) {
        factory = contextFactoriesByContextName.get(contextName);
      }
      return factory;
    }
  };
}
