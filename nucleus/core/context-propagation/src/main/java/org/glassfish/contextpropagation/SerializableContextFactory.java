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

package org.glassfish.contextpropagation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *  The purpose of this factory is to support legacy custom work contexts
 *  from WebLogic Server (WLS). This is for the case where a custom work context
 *  is NOT wrapped into a weblogic.workarea.SerializableWorkContext.
 *  It is also used when Glassfish uses a different class than WLS. In that
 *  case a factory can be registered to replace a WLS class with a Glassfish class.
 *  This will work as
 *  long as the Glassfish and WLS classes have the same serialization profile.
 */
 /*   - TODO QUESTION(For PM) Do we want to support legacy custom work contexts in open source glassfish.
 *     If not, we can move this factory interface to closed source.
 */
public interface SerializableContextFactory {
  public WLSContext createInstance();

  public interface WLSContext {
    /**
     * Writes the implementation of <code>Context</code> to the
     * {@link ContextOutput} data stream.
     */
    public void writeContext(ObjectOutput out) throws IOException;

    /**
     * Reads the implementation of <code>Context</code> from the
     * {@link ContextInput} data stream.
     */
    public void readContext(ObjectInput in) throws IOException;

    public interface WLSContextHelper {
      public byte[] toBytes(WLSContext ctx) throws IOException;
      public byte[] toBytes(Serializable object) throws IOException;
      public WLSContext readFromBytes(WLSContext ctx, byte[] bytes) throws IOException;
      public Serializable readFromBytes(byte[] bytes) throws IOException, ClassNotFoundException;
    }

    /**
     * HELPER is used internally to facilitate work with WLSContexts
     */
    public static WLSContextHelper HELPER = new WLSContextHelper() {
      @Override
      public byte[] toBytes(WLSContext ctx) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        ctx.writeContext(oos);
        oos.flush();
        return baos.toByteArray();
      }
      @Override
      public WLSContext readFromBytes(WLSContext ctx, byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        ctx.readContext(ois);
        return ctx;
      }
      @Override
      public byte[] toBytes(Serializable object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.flush();
        return baos.toByteArray();
     }
      @Override
      public Serializable readFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (Serializable) ois.readObject();
      }
    };

  }

}
