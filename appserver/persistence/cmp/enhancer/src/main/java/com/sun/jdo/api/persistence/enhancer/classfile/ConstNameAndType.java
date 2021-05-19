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
 * Class representing a name and an associated type in the constant pool
 * of a class file
 */
public class ConstNameAndType extends ConstBasic {
    /* The tag value associated with ConstDouble */
    public static final int MyTag = CONSTANTNameAndType;

    /* The name of interest */
    private ConstUtf8 theName;

    /* The index of the name to be resolved
     *   - used during class file reading */
    private int theNameIndex;

    /* The type signature associated with the name */
    private ConstUtf8 typeSignature;

    /* The index of the signature to be resolved
     *   - used during class file reading */
    private int typeSignatureIndex;

    /* public accessors */

    /**
     * The tag of this constant entry
     */
    public int tag () { return MyTag; }

    /**
     * Return the name
     */
    public ConstUtf8 name() {
        return theName;
    }

    /**
     * Return the type signature associated with the name
     */
    public ConstUtf8 signature() {
        return typeSignature;
    }

    /**
     * Modify the signature
     */
    public void changeSignature(ConstUtf8 newSig) {
        typeSignature = newSig;
    }

    /**
     * A printable representation
     */
    public String toString () {
        return "CONSTANTNameAndType(" + indexAsString() + "): " + //NOI18N
            "name(" + theName.toString() + ") " +//NOI18N
            " type(" + typeSignature.toString() + ")";//NOI18N
    }

    /* package local methods */

    ConstNameAndType (ConstUtf8 n, ConstUtf8 sig) {
        theName = n; typeSignature = sig;
    }

    ConstNameAndType (int n, int sig) {
        theNameIndex = n; typeSignatureIndex = sig;
    }

    void formatData (DataOutputStream b) throws IOException {
        b.writeShort(theName.getIndex());
        b.writeShort(typeSignature.getIndex());
    }

    static ConstNameAndType read (DataInputStream input) throws IOException {
        int cname = input.readUnsignedShort();
        int sig = input.readUnsignedShort();

        return new ConstNameAndType (cname, sig);
    }

    void resolve (ConstantPool p) {
        theName = (ConstUtf8) p.constantAt(theNameIndex);
        typeSignature = (ConstUtf8) p.constantAt(typeSignatureIndex);
    }
}
