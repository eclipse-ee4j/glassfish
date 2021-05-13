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
 * ConstantValueAttribute represents a constant value attribute
 * in a class file.  These attributes are used as initialization
 * values for static fields.
 */
public class ConstantValueAttribute extends ClassAttribute {
    /* The expected name of this attribute */
    public static final String expectedAttrName = "ConstantValue";//NOI18N

    /* The value */
    private ConstValue constantValue;

    /* public accessors */

    public ConstValue value() {
        return constantValue;
    }

    /**
     * Construct a constant value attribute
     */
    public ConstantValueAttribute(ConstUtf8 attrName, ConstValue value) {
        super(attrName);
        constantValue = value;
    }

    /* package local methods */

    static ConstantValueAttribute read (ConstUtf8 attrName,
        DataInputStream data, ConstantPool pool)
            throws IOException {
        int index = 0;
        index = data.readUnsignedShort();

        return new ConstantValueAttribute(attrName,
            (ConstValue) pool.constantAt(index));
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(attrName().getIndex());
        out.writeInt(2);
        out.writeShort(constantValue.getIndex());
    }

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println("ConstantValue: " + constantValue.toString());//NOI18N
    }
}

