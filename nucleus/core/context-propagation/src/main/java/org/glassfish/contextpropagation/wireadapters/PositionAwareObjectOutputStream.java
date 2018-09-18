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
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

/**
 * A stream that keeps track of the position
 */
public class PositionAwareObjectOutputStream extends ObjectOutputStream {
  short pos; // initialized by the super constructor
  ObjectOutputStream underlying;

  public PositionAwareObjectOutputStream(OutputStream os) throws IOException {
    super();
    underlying = new ObjectOutputStream(os);
  }

  public short position() throws IOException {
    return pos;
  }

  @Override
  protected void writeObjectOverride(Object obj) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(obj);
    oos.flush();
    byte[] bytes = baos.toByteArray();
    pos += bytes.length - 1;
    underlying.writeObject(obj);
  }

  @Override
  public void writeUnshared(Object obj) throws IOException {
    throw new UnsupportedOperationException("We do not need this for the WireAdapter");
  }

  @Override
  public void writeFields() throws IOException {
    throw new UnsupportedOperationException("We do not need this for the WireAdapter");
  }

  @Override
  protected void writeStreamHeader() throws IOException {
    pos += 4;
    throw new UnsupportedOperationException("We do not need this for the WireAdapter");
  }

  @Override
  public void useProtocolVersion(int version) throws IOException {
    underlying.useProtocolVersion(version);
  }

  @Override
  public void defaultWriteObject() throws IOException {
    underlying.defaultWriteObject();
  }

  @Override
  public PutField putFields() throws IOException {
    return underlying.putFields();
  }

  @Override
  public void reset() throws IOException {
    underlying.reset();
  }

  @Override
  public void flush() throws IOException {
    underlying.flush();
  }

  @Override
  public void close() throws IOException {
    underlying.close();
  }

  @Override
  protected void writeClassDescriptor(ObjectStreamClass desc)
      throws IOException {
    throw new UnsupportedOperationException("We should never use this directly");
  }

  @Override
  public void write(int val) throws IOException {
    pos += Byte.SIZE / Byte.SIZE;
    underlying.write(val);
  }

  @Override
  public void write(byte[] buf) throws IOException {
    pos += buf.length;
    underlying.write(buf);
  }

  @Override
  public void write(byte[] buf, int off, int len) throws IOException {
    pos += len - off;
    underlying.write(buf, off, len);
  }

  @Override
  public void writeBoolean(boolean val) throws IOException {
    pos += Byte.SIZE / Byte.SIZE;
    underlying.writeBoolean(val);
  }

  @Override
  public void writeByte(int val) throws IOException {
    pos += Byte.SIZE / Byte.SIZE;
    underlying.writeByte(val);
  }

  @Override
  public void writeShort(int val) throws IOException {
    pos += Short.SIZE / Byte.SIZE;
    underlying.writeShort(val);
  }

  @Override
  public void writeChar(int val) throws IOException {
    pos += Character.SIZE / Byte.SIZE;
    underlying.writeChar(val);
  }

  @Override
  public void writeInt(int val) throws IOException {
    pos += Integer.SIZE / Byte.SIZE;
    underlying.writeInt(val);
  }

  @Override
  public void writeLong(long val) throws IOException {
    pos += Long.SIZE / Byte.SIZE;
    underlying.writeLong(val);
  }

  @Override
  public void writeFloat(float val) throws IOException {
    pos += Float.SIZE / Byte.SIZE;
    underlying.writeFloat(val);
  }

  @Override
  public void writeDouble(double val) throws IOException {
    pos += Double.SIZE / Byte.SIZE;
    underlying.writeDouble(val);
  }

  @Override
  public void writeBytes(String str) throws IOException {
    pos += str.length(); // may not be correct
    underlying.writeBytes(str);
  }

  @Override
  public void writeChars(String str) throws IOException {
    pos += str.length() * Character.SIZE / Byte.SIZE;
    underlying.writeChars(str);
  }

  @Override
  public void writeUTF(String str) throws IOException {
    pos += Short.SIZE / Byte.SIZE + getUTFLength(str);
    underlying.writeUTF(str);
  }

  /* From java.io.ObjectOutputStream */
  private static final int CHAR_BUF_SIZE = 2048;
  private char[] cbuf = new char[CHAR_BUF_SIZE];
  synchronized long getUTFLength(String s) {
    int len = s.length();
    long utflen = 0;
    for (int off = 0; off < len; ) {
      int csize = Math.min(len - off, CHAR_BUF_SIZE);
      s.getChars(off, off + csize, cbuf, 0);
      for (int cpos = 0; cpos < csize; cpos++) {
        char c = cbuf[cpos];
        if (c >= 0x0001 && c <= 0x007F) {
          utflen++;
        } else if (c > 0x07FF) {
          utflen += 3;
        } else {
          utflen += 2;
        }
      }
      off += csize;
    }
    return utflen;
  }

}
