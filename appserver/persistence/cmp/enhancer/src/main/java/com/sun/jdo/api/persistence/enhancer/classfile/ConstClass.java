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
 * Class representing a class reference in the constant pool
 */

public class ConstClass extends ConstBasic {
    /* The tag associated with ConstClass entries */
    public static final int MyTag = CONSTANTClass;

    /* The name of the class being referred to */
    private ConstUtf8 theClassName;

    /* The index of name of the class being referred to
     *  - used while reading from a class file */
    private int theClassNameIndex;

    /* public accessors */

    /**
     * Return the tag for this constant
     */
    public int tag () { return MyTag; }

    /**
     * Return the class name
     */
    public ConstUtf8 className() {
        return theClassName;
    }

    /**
     * Return the class name in simple string form
     */
    public String asString() {
        return theClassName.asString();
    }

    /**
     * A printable representation
     */
    public String toString () {
        return "CONSTANTClass(" + indexAsString() + "): " + //NOI18N
            "className(" + theClassName.toString() + ")";//NOI18N
    }

    /**
     * Change the class reference (not to be done lightly)
     */
    public void changeClass(ConstUtf8 newName) {
        theClassName = newName;
        theClassNameIndex = newName.getIndex();
    }

    /* package local methods */

    /**
     * Construct a ConstClass
     */
    public ConstClass (ConstUtf8 cname) {
        theClassName = cname;
    }

    ConstClass (int cname) {
        theClassNameIndex = cname;
    }

    void formatData (DataOutputStream b) throws IOException {
        b.writeShort(theClassName.getIndex());
    }

    static ConstClass read (DataInputStream input) throws IOException {
        return new ConstClass (input.readUnsignedShort());
    }

    void resolve (ConstantPool p) {
        theClassName = (ConstUtf8) p.constantAt(theClassNameIndex);
    }
}

