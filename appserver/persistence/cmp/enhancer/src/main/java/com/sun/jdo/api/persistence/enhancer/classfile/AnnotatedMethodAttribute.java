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
 * AnnotatedMethodAttribute represents a class level attribute
 * class file which identifies the level of annotation of the class.
 */
public class AnnotatedMethodAttribute extends ClassAttribute {

    /* The expected attribute name */
    public final static String expectedAttrName = "filter.annotatedMethod";//NOI18N

    /* The expected attribute version */
    public final static short expectedAttrVersion = 1;

    /* Bit mask indicating that the class was filter generated */
    public final static short generatedFlag = 0x1;

    /* Bit mask indicating that the class was filter annotated */
    public final static short annotatedFlag = 0x2;

    /* Bit mask indicating that the class was "repackaged" *///NOI18N
    public final static short modifiedFlag = 0x4;

    /* The version of the attribute */
    private short attrVersion;

    /* Flags associated with the annotation */
    private short annotationFlags;

    /* list of targets in the code sequence delimiting inserted instruction
     * sequences.  Even index targets are a range start (inclusive) and odd
     * targets represent a range end (exclusive) */
    private InsnTarget annotationRanges[];

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

    public InsnTarget[] getAnnotationRanges() {
        return annotationRanges;
    }

    public void setAnnotationRanges(InsnTarget[] ranges) {
        annotationRanges = ranges;
    }

    /**
     * Constructor
     */
    public AnnotatedMethodAttribute(
        ConstUtf8 nameAttr, short version, short annFlags,
        InsnTarget[] annRanges) {
        super(nameAttr);
        attrVersion = version;
        annotationFlags = annFlags;
        annotationRanges = annRanges;
    }

    /* package local methods */

    static AnnotatedMethodAttribute read(
        ConstUtf8 attrName, DataInputStream data, CodeEnv env)
            throws IOException {
        short version = data.readShort();
        short annFlags = data.readShort();

        short nRanges = data.readShort();

        InsnTarget ranges[] = new InsnTarget[nRanges*2];
        for (int i=0; i<nRanges; i++) {
            ranges[i*2] = env.getTarget(data.readShort());
            ranges[i*2+1] = env.getTarget(data.readShort());
        }
        return  new AnnotatedMethodAttribute(attrName, version, annFlags, ranges);
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(attrName().getIndex());
        if (annotationRanges == null) {
            out.writeShort(2);
        } else {
            out.writeShort(4 + 2 * annotationRanges.length);
        }
        out.writeShort(attrVersion);
        out.writeShort(annotationFlags);
        if (annotationRanges == null)
            out.writeShort(0);
        else {
            out.writeShort(annotationRanges.length / 2);
            for (int i=0; i<annotationRanges.length; i++)
                out.writeShort(annotationRanges[i].offset());
        }
    }

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println("version: " + attrVersion);//NOI18N
        out.println(" flags: " + annotationFlags);//NOI18N
        if (annotationRanges != null) {
            out.println("Annotations: ");//NOI18N
            for (int i=0; i<annotationRanges.length/2; i++) {
                ClassPrint.spaces(out, indent+2);
                out.println(annotationRanges[i*2] + " to " +//NOI18N
                    annotationRanges[i*2+1]);
            }
        }
    }
}
