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
 * ClassField models the static and non-static fields of a class within
 * a class file.
 */
final public class ClassField extends ClassMember {

    /* access flag bit mask - see VMConstants */
    private int accessFlags;

    /* The name of the field */
    private ConstUtf8 fieldName;

    /* The type signature of the field */
    private ConstUtf8 fieldSignature;

    /* The attributes associated with the field */
    private AttributeVector fieldAttributes;

    /* public accessors */

    /**
     * Is the field transient?
     */
    public boolean isTransient() {
        return (accessFlags & ACCTransient) != 0;
    }


    /**
     * Return the access flags for the field - see VMConstants
     */
    @Override
    public int access() {
        return accessFlags;
    }

    /**
     * Update the access flags for the field - see VMConstants
     */
    @Override
    public void setAccess(int newFlags) {
        accessFlags = newFlags;
    }

    /**
     * Return the name of the field
     */
    @Override
    public ConstUtf8 name() {
        return fieldName;
    }

    /**
     * Change the name of the field
     */
    public void changeName(ConstUtf8 name) {
        fieldName = name;
    }

    /**
     * Return the type signature of the field
     */
    @Override
    public ConstUtf8 signature() {
        return fieldSignature;
    }

    /**
     * Change the type signature of the field
     */
    public void changeSignature(ConstUtf8 newSig) {
        fieldSignature = newSig;
    }

    /**
     * Return the attributes associated with the field
     */
    @Override
    public AttributeVector attributes() {
        return fieldAttributes;
    }

    /**
     * Construct a class field object
     */
    public ClassField(int accFlags, ConstUtf8 name, ConstUtf8 sig,
        AttributeVector field_attrs) {
        accessFlags = accFlags;
        fieldName = name;
        fieldSignature = sig;
        fieldAttributes = field_attrs;
    }

    /* package local methods */

    static ClassField read(DataInputStream data, ConstantPool pool)
        throws IOException {
        ClassField f = null;
        int accessFlags = data.readUnsignedShort();
        int name_index = data.readUnsignedShort();
        int sig_index = data.readUnsignedShort();
        AttributeVector fieldAttribs = AttributeVector.readAttributes(data, pool);
        f = new ClassField(accessFlags,
            (ConstUtf8) pool.constantAt(name_index),
            (ConstUtf8) pool.constantAt(sig_index),
            fieldAttribs);
        return f;
    }

    void write (DataOutputStream data) throws IOException {
        data.writeShort(accessFlags);
        data.writeShort(fieldName.getIndex());
        data.writeShort(fieldSignature.getIndex());
        fieldAttributes.write(data);
    }

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.print("'" + fieldName.asString() + "'");//NOI18N
        out.print(" sig = " + fieldSignature.asString());//NOI18N
        out.print(" access_flags = " + Integer.toString(accessFlags));//NOI18N
        out.println(" attributes:");//NOI18N
        fieldAttributes.print(out, indent+2);
    }
}

