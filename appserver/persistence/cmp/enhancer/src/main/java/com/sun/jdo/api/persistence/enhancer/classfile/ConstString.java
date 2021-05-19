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
 * Class representing a class specification in the constant pool
 */

/*
   ConstString strictly speaking is not a ConstantValue in the
   Java VM sense.  However, the compiler generates ConstantValue attributes
   which refer to ConstString entries.  This occurs for initialized static
   final String fields.  I've changed ConstString to be a ConstValue for
   now as a simplification.
*/

public class ConstString extends ConstValue {
    /* The tag associated with ConstClass entries */
    public static final int MyTag = CONSTANTString;

    /* The name of the class being referred to */
    private ConstUtf8 stringValue;

    /* The index of name of the class being referred to
     *  - used while reading from a class file */
    private int stringValueIndex;

    /* public accessors */

    /**
     * Return the tag for this constant
     */
    public int tag () { return MyTag; }

    /**
     * Return the utf8 string calue
     */
    public ConstUtf8 value() {
        return stringValue;
    }

    /**
     * Return the descriptor string for the constant type.
     */
    public String descriptor() {
        return "Ljava/lang/String;";//NOI18N
    }

    /**
     * A printable representation
     */
    public String toString () {
        return "CONSTANTString(" + indexAsString() + "): " + //NOI18N
            "string(" + stringValue.asString() + ")";//NOI18N
    }

    /* package local methods */

    ConstString (ConstUtf8 s) {
        stringValue = s;
    }

    ConstString (int sIndex) {
        stringValueIndex = sIndex;
    }

    void formatData (DataOutputStream b) throws IOException {
        b.writeShort (stringValue.getIndex());
    }
    static ConstString read (DataInputStream input) throws IOException {
        return new ConstString (input.readUnsignedShort());
    }
    void resolve (ConstantPool p) {
        stringValue = (ConstUtf8) p.constantAt(stringValueIndex);
    }
}
