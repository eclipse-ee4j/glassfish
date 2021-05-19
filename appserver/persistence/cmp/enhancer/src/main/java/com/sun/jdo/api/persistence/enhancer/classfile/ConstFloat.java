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
 * Class representing a float constant in the constant pool of a class file
 */
public class ConstFloat extends ConstValue {
    /* The tag value associated with ConstFloat */
    public static final int MyTag = CONSTANTFloat;

    /* The value */
    private float floatValue;

    /* public accessors */

    /**
     * The tag of this constant entry
     */
    public int tag () { return MyTag; }

    /**
     * return the value associated with the entry
     */
    public float value() {
        return floatValue;
    }

    /**
     * Return the descriptor string for the constant type.
     */
    public String descriptor() {
        return "F";//NOI18N
    }

    /**
     * A printable representation
     */
    public String toString () {
        return "CONSTANTFloat(" + indexAsString() + "): " + //NOI18N
            "floatValue(" + Float.toString(floatValue) + ")";//NOI18N
    }

    /* package local methods */

    ConstFloat (float f) {
        floatValue = f;
    }

    void formatData (DataOutputStream b) throws IOException {
        b.writeFloat(floatValue);
    }

    static ConstFloat read (DataInputStream input) throws IOException {
        return new ConstFloat (input.readFloat());
    }

    void resolve (ConstantPool p) { }

}
