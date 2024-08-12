/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jdo.api.persistence.enhancer.classfile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * AnnotatedClassAttribute represents a class level attribute
 * class file which identifies the level of annotation of the class.
 */

public class AnnotatedClassAttribute extends ClassAttribute {

  /* The expected attribute name */
    public final static String expectedAttrName = "filter.annotatedClass";//NOI18N

  /* The expected attribute version */
  public final static short expectedAttrVersion = 1;

  /* Bit mask indicating that the class was filter generated */
  public final static short generatedFlag = 0x1;

  /* Bit mask indicating that the class was filter annotated */
  public final static short annotatedFlag = 0x2;

  /* Bit mask indicating that the class was "repackaged" or similarly
   * modified */
  public final static short modifiedFlag = 0x4;

  /* The version of the attribute */
  private short attrVersion;

  /* Flags associated with the annotation */
  private short annotationFlags;

  /* The modification date of the class file at the time of modification */
  private long classModTime;

  /* The date of the annotation */
  private long classAnnotationTime;

  /* public accessors */

  public short getVersion() {
    return attrVersion;
  }

  public void setVersion(short version) {
    attrVersion = version;
  }

  public short getFlags() {
    return annotationFlags;
  }

  public void setFlags(short flags) {
    annotationFlags = flags;
  }

  public long getModTime() {
    return classModTime;
  }

  public void setModTime(long time) {
    classModTime = time;
  }

  public long getAnnotationTime() {
    return classAnnotationTime;
  }

  public void setAnnotationTime(long time) {
    classAnnotationTime = time;
  }

  /**
   * Constructor
   */
  public AnnotatedClassAttribute(
    ConstUtf8 nameAttr, short version, short annFlags,
    long modTime, long annTime) {
    super(nameAttr);
    attrVersion = version;
    annotationFlags = annFlags;
    classModTime = modTime;
    classAnnotationTime = annTime;
  }

  /* package local methods */

  static AnnotatedClassAttribute read(
    ConstUtf8 attrName, DataInputStream data, ConstantPool pool)
    throws IOException {
    short version = data.readShort();
    short annFlags = data.readShort();
    long modTime = data.readLong();
    long annTime = data.readLong();
    return  new AnnotatedClassAttribute(attrName, version, annFlags,
                    modTime, annTime);
  }

  void write(DataOutputStream out) throws IOException {
    out.writeShort(attrName().getIndex());
    out.writeShort(20);
    out.writeShort(attrVersion);
    out.writeShort(annotationFlags);
    out.writeLong(classModTime);
    out.writeLong(classAnnotationTime);
  }

  void print(PrintStream out, int indent) {
    ClassPrint.spaces(out, indent);
    out.println("version: " + attrVersion);//NOI18N
    out.println(" flags: " + annotationFlags);//NOI18N
    out.println(" modTime: " + classModTime);//NOI18N
    out.println(" annTime: " + classAnnotationTime);//NOI18N
  }
}
