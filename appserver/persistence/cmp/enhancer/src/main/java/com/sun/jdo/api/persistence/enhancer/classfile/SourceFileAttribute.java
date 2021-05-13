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
 * Represents the source file attribute in a class file
 */
public class SourceFileAttribute extends ClassAttribute {
    /* The expected attribute name */
    public static final String expectedAttrName = "SourceFile";//NOI18N

    /* The source file name */
    private ConstUtf8 sourceFileName;

    /* public accessors */

    /**
     * Returns the source file name
     * The file name should not include directories
     */
    public ConstUtf8 fileName() {
        return sourceFileName;
    }

    /**
     * Sets the source file name
     */
    public void setFileName(ConstUtf8 name) {
        sourceFileName = name;
    }

    /**
     * Constructor for a source file attribute
     */
    public SourceFileAttribute(ConstUtf8 attrName, ConstUtf8 sourceName) {
        super(attrName);
        sourceFileName = sourceName;
    }

    /* package local methods */
    static SourceFileAttribute read(ConstUtf8 attrName, DataInputStream data, ConstantPool pool) throws IOException {
        int index = 0;
        index = data.readUnsignedShort();

        return new SourceFileAttribute(attrName, (ConstUtf8) pool.constantAt(index));
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(attrName().getIndex());
        out.writeInt(2);
        out.writeShort(sourceFileName.getIndex());
    }

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println("SourceFile: " + sourceFileName.asString());//NOI18N
    }
}

