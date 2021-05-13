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
import java.util.Enumeration;

/**
 * ClassMethod models the static and non-static methods of a class within
 * a class file.  This includes constructors and initializer code.
 */
public class ClassMethod extends ClassMember {
    /* The name of the constructor code */
    public final static String intializerName = "<init>";//NOI18N

    /* The name of the static initializer code */
    public final static String staticIntializerName = "<clinit>";//NOI18N

    /* access flag bit mask - see VMConstants */
    private int accessFlags;

    /* The name of the method */
    private ConstUtf8 methodName;

    /* The type signature of the method */
    private ConstUtf8 methodSignature;

    /* The attributes associated with the field */
    private AttributeVector methodAttributes;


    /* public accessors */

    /**
     * Return the access flags for the method - see VMConstants
     */
    public int access() {
        return accessFlags;
    }

    /**
     * Update the access flags for the field - see VMConstants
     */
    public void setAccess(int newFlags) {
        accessFlags = newFlags;
    }

    /**
     * Is the method abstract?
     */
    public boolean isAbstract() {
        return (accessFlags & ACCAbstract) != 0;
    }

    /**
     * Is the method native?
     */
    public boolean isNative() {
        return (accessFlags & ACCNative) != 0;
    }

    /**
     * Return the name of the method
     */
    public ConstUtf8 name() {
        return methodName;
    }

    /**
     * Change the name of the method
     */
    public void changeName(ConstUtf8 name) {
        methodName = name;
    }

    /**
     * Return the type signature of the method
     */
    public ConstUtf8 signature() {
        return methodSignature;
    }

    /**
     * Change the type signature of the method
     */
    public void changeSignature(ConstUtf8 newSig) {
        methodSignature = newSig;
    }

    /**
     * Return the attributes associated with the method
     */
    public AttributeVector attributes() {
        return methodAttributes;
    }

    /**
     * Construct a class method object
     */

    public ClassMethod(int accFlags, ConstUtf8 name, ConstUtf8 sig,
        AttributeVector methodAttrs) {
        accessFlags = accFlags;
        methodName = name;
        methodSignature = sig;
        methodAttributes = methodAttrs;
    }

    /**
     * Returns the size of the method byteCode (if any)
     */
    int codeSize() {
        CodeAttribute codeAttr = codeAttribute();
        return (codeAttr == null) ? 0  : codeAttr.codeSize();
    }

    /**
     * Returns the CodeAttribute associated with this method (if any)
     */
    public CodeAttribute codeAttribute() {
        Enumeration e = methodAttributes.elements();
        while (e.hasMoreElements()) {
            ClassAttribute attr = (ClassAttribute) e.nextElement();
            if (attr instanceof CodeAttribute)
                return (CodeAttribute) attr;
        }
        return null;
    }

    /* package local methods */


    static ClassMethod read(DataInputStream data, ConstantPool pool)
        throws IOException {
        int accessFlags = data.readUnsignedShort();
        int nameIndex = data.readUnsignedShort();
        int sigIndex = data.readUnsignedShort();
        ClassMethod f =
            new ClassMethod(accessFlags,
                (ConstUtf8) pool.constantAt(nameIndex),
                (ConstUtf8) pool.constantAt(sigIndex),
                null);

        f.methodAttributes = AttributeVector.readAttributes(data, pool);
        return f;
    }

    void write(DataOutputStream data) throws IOException {
        CodeAttribute codeAttr = codeAttribute();
        data.writeShort(accessFlags);
        data.writeShort(methodName.getIndex());
        data.writeShort(methodSignature.getIndex());
        methodAttributes.write(data);
    }

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.print("'" + methodName.asString() + "'");//NOI18N
        out.print(" sig = " + methodSignature.asString());//NOI18N
        out.print(" accessFlags = " + Integer.toString(accessFlags));//NOI18N
        out.println(" attributes:");//NOI18N
        methodAttributes.print(out, indent+2);
    }
}
