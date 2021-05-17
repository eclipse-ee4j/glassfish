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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.EnumSet;

import org.glassfish.contextpropagation.PropagationMode;
import org.glassfish.contextpropagation.bootstrap.ContextBootstrap;
import org.glassfish.contextpropagation.bootstrap.LoggerAdapter.Level;
import org.glassfish.contextpropagation.bootstrap.LoggerAdapter.MessageID;
import org.glassfish.contextpropagation.internal.Entry;
import org.glassfish.contextpropagation.internal.Entry.ContextType;

/**
 * This class provides a framework for collecting catalog information, writing
 * the catalog ahead of all other contexts and for skipping over the deserialization
 * of contexts that cannot be deserialized.
 */
public abstract class AbstractWireAdapter implements WireAdapter {
  protected static final int READ_LIMIT = 32000;
  protected String key;
  private OutputStream outputStream;
  private ByteArrayOutputStream bufferedStream;
  PositionAwareObjectOutputStream oos;
  protected ObjectInputStream ois;
  Catalog catalog = new Catalog();
  private boolean needsToReadCatalog = true;

  @Override
  public final void prepareToWriteTo(OutputStream out) throws IOException {
    outputStream = out;
    bufferedStream = new ByteArrayOutputStream();
    oos = new PositionAwareObjectOutputStream(bufferedStream);
    writeHeader(oos);
    catalog.add(oos.position());
  }

  protected abstract void writeHeader(ObjectOutputStream os)  throws IOException;

  @Override
  public final <T> void write(String key, Entry entry) throws IOException {
    write(oos, key, entry.getValue(), entry.getContextType(), entry.getPropagationModes(),
        entry.getContextType() == ContextType.OPAQUE ? entry.getClassName() : null);
    catalog.add(oos.position());
    ContextBootstrap.debug(MessageID.WRITE_ENTRY, catalog.positions.size(), key, entry);
  }

  protected abstract void write(ObjectOutputStream oos, String key, Object value, ContextType contextType,
      EnumSet<PropagationMode> propagationModes, String className) throws IOException;

  @Override
  public final void prepareToReadFrom(InputStream is) throws IOException {
    catalog.prepareToRead();
    ois = new ResettableObjectInputStream(is);
    readHeader(ois, catalog);
  }

  protected abstract void readHeader(ObjectInputStream ois, Catalog catalog) throws IOException;

  @Override
  public final void flush() throws IOException {
    write(oos, catalog);
    writeFooter(oos);
    oos.flush();
    byte[] contents = bufferedStream.toByteArray();
    catalog.updateCatalogMetadata(contents);
    outputStream.write(contents);
  }

  protected abstract void write(ObjectOutputStream objectOutputStream, Catalog catalog) throws IOException;

  protected abstract void writeFooter(ObjectOutputStream objectOutputStream) throws IOException;

  @Override
  public final String readKey() throws IOException {
    try {
      catalog.upItemNumber(1);
      key = nextKey();
      if (key == null) {
        read(false, ois, catalog);
      }
    } catch (IOException ioe) {
      ContextBootstrap.getLoggerAdapter().log(Level.ERROR, ioe,
          MessageID.ERROR_IOEXCEPTION_WHILE_READING_KEY, key);
      if (catalog.skipToNextItem(ois)) {
        key = readKey();
      } else {
        return null;
      }
    }
    ContextBootstrap.debug(MessageID.READ_KEY, key);
    return key;
  }

  protected abstract void read(boolean mandatory, ObjectInputStream ois, Catalog catalog) throws IOException;

  protected abstract String nextKey() throws IOException;

  @Override
  public final Entry readEntry() throws IOException, ClassNotFoundException {
    try {
      return nextEntry();
    } catch (ClassNotFoundException cnfe) {
      ContextBootstrap.getLoggerAdapter().log(Level.ERROR, cnfe,
          MessageID.ERROR_CLASSNOTFOUND, key);
      recover(ois, catalog);
      return null;
    } catch (IOException ioe) {
      ContextBootstrap.getLoggerAdapter().log(Level.ERROR, ioe,
          MessageID.ERROR_IOEXCEPTION_WHILE_READING_ENTRY, key);
      recover(ois, catalog);
      return null;
    }
  }

  private void recover(ObjectInputStream ois, Catalog catalog) throws IOException, ClassNotFoundException {
    if (needsToReadCatalog ) {
      read(true, ois, catalog);
      needsToReadCatalog = false;
    }
    catalog.skipToNextItem(ois);
  }

  protected abstract Entry nextEntry() throws ClassNotFoundException, IOException;
}
