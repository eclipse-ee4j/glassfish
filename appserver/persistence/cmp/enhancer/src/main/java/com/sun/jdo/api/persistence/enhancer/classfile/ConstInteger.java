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

/**
 * Class representing an integer constant in the constant pool of a class file
 */
public class ConstInteger extends ConstValue {
    /* The tag value associated with ConstInteger */
    public static final int MyTag = CONSTANTInteger;

    /* The value */
    private int intValue;

    /* public accessors */

    /**
     * The tag of this constant entry
     */
    public int tag () { return MyTag; }

    /**
     * return the value associated with the entry
     */
    public int value() {
        return intValue;
    }

    /**
     * Return the descriptor string for the constant type.
     */
    public String descriptor() {
        return "I";//NOI18N
    }

    /**
     * A printable representation
     */
    public String toString () {
        return "CONSTANTInteger(" + indexAsString() + "): " + //NOI18N
            "intValue(" + Integer.toString(intValue) + ")";//NOI18N
    }

    /* package local methods */

    ConstInteger (int i) {
        intValue = i;
    }

    void formatData (DataOutputStream b) throws IOException {
        b.writeInt(intValue);
    }

    static ConstInteger read (DataInputStream input) throws IOException {
        return new ConstInteger (input.readInt());
    }

    void resolve (ConstantPool p) { }

}

