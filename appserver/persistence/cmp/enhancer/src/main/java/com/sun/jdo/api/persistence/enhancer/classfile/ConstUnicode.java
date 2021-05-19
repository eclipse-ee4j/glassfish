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
 * Class representing a unicode string value in the constant pool
 */

/*
 * Note: evidence suggests that this is no longer part of the java VM
 * spec.
 */

public class ConstUnicode extends ConstBasic {
    /* The tag associated with ConstClass entries */
    public static final int MyTag = CONSTANTUnicode;

    /* The unicode string of interest */
    private String stringValue;

    /* public accessors */

    /**
     * The tag of this constant entry
     */
    public int tag () { return MyTag; }

    /**
     * return the value associated with the entry
     */
    public String asString() {
        return stringValue;
    }

    /**
     * A printable representation
     */
    public String toString () {
        return "CONSTANTUnicode(" + indexAsString() + "): " + stringValue;//NOI18N
    }

    /* package local methods */

    ConstUnicode (String s) {
        stringValue = s;
    }

    void formatData (DataOutputStream b) throws IOException {
        b.writeBytes(stringValue);
    }

    static ConstUnicode read (DataInputStream input) throws IOException {
        int count = input.readShort(); // Is this chars or bytes?
        StringBuffer b = new StringBuffer();
        for (int i=0; i < count; i++) {
            b.append(input.readChar());
        }
        return new ConstUnicode (b.toString());
    }

    void resolve (ConstantPool p) {
    }
}
