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
 * LineNumberTableAttribute represents a line number table attribute
 * within a CodeAttribute within a class file
 */
public class LineNumberTableAttribute extends ClassAttribute {
    /* The expected attribute name */
    public final static String expectedAttrName = "LineNumberTable";//NOI18N

    /* The line numbers */
    private short lineNumbers[];

    /* The corresponding instructions */
    private InsnTarget targets[];

    /* public accessors */

    /**
     * Constructor
     */
    public LineNumberTableAttribute(
        ConstUtf8 nameAttr, short lineNums[], InsnTarget targets[]) {
        super(nameAttr);
        lineNumbers = lineNums;
        this.targets = targets;
    }

    /* package local methods */

    static LineNumberTableAttribute read(
        ConstUtf8 attrName, DataInputStream data, CodeEnv env)
            throws IOException {
        int nLnums = data.readUnsignedShort();
        short lineNums[] = new short[nLnums];
        InsnTarget targs[] = new InsnTarget[nLnums];
        for (int i=0; i<nLnums; i++) {
            targs[i] = env.getTarget(data.readShort());
            lineNums[i] = data.readShort();
        }
        return  new LineNumberTableAttribute(attrName, lineNums, targs);
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(attrName().getIndex());
        int nlines = lineNumbers.length;
        out.writeInt(2+4*nlines);
        out.writeShort(nlines);
        for (int i=0; i<nlines; i++) {
            out.writeShort(targets[i].offset());
            out.writeShort(lineNumbers[i]);
        }
    }

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println("Line Numbers: ");//NOI18N
        for (int i=0; i<lineNumbers.length; i++) {
            ClassPrint.spaces(out, indent+2);
            out.println(Integer.toString(lineNumbers[i]) + " @ " +//NOI18N
                Integer.toString(targets[i].offset()));
        }
    }
}

