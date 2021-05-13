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

//@olsen: fix 4467428, added class for synthetic attribute to be added
// to generated jdo fields and methods

/**
 * SyntheticAttribute represents a constant value attribute
 * in a class file.  These attributes are used as initialization
 * values for static fields.
 */
public class SyntheticAttribute extends ClassAttribute {
    /* The expected name of this attribute */
    public static final String expectedAttrName = "Synthetic";//NOI18N

    /**
     * Construct a constant value attribute
     */
    public SyntheticAttribute(ConstUtf8 attrName) {
        super(attrName);
        //System.out.println("new SyntheticAttribute()");
    }

    /* package local methods */

    static SyntheticAttribute read (ConstUtf8 attrName,
        DataInputStream data,
        ConstantPool pool)
            throws IOException {
        return new SyntheticAttribute(attrName);
    }

    @Override
    void write(DataOutputStream out) throws IOException {
        out.writeShort(attrName().getIndex());
        final int attributeBytesLength = 0;
        out.writeInt(attributeBytesLength);
    }

    @Override
    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println(expectedAttrName);
    }
}
