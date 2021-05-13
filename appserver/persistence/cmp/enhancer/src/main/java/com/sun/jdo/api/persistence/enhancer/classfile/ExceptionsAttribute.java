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
import java.util.Vector;

/**
 * ExceptionsAttribute represents a method attribute in a class file
 * listing the checked exceptions for the method.
 */
public class ExceptionsAttribute extends ClassAttribute {
    public final static String expectedAttrName = "Exceptions";//NOI18N

    /* The list of checked exceptions */
    private Vector exceptionTable;

    /* public accessors */

    /**
     *  Return an enumeration of the checked exceptions
     */
    public Enumeration exceptions() {
        return exceptionTable.elements();
    }

    /**
     * Constructor
     */
    public ExceptionsAttribute(ConstUtf8 attrName, Vector excTable) {
        super(attrName);
        exceptionTable = excTable;
    }

    /**
     * Convenience Constructor - for single exception
     */
    public ExceptionsAttribute(ConstUtf8 attrName, ConstClass exc) {
        super(attrName);
        exceptionTable = new Vector(1);
        exceptionTable.addElement(exc);
    }

    /* package local methods */

    static ExceptionsAttribute read(ConstUtf8 attrName,
        DataInputStream data, ConstantPool pool)
            throws IOException {
        int nExcepts = data.readUnsignedShort();
        Vector excTable = new Vector();
        while (nExcepts-- > 0) {
            int excIndex = data.readUnsignedShort();
            ConstClass exc_class = null;
            if (excIndex != 0)
                exc_class = (ConstClass) pool.constantAt(excIndex);
            excTable.addElement(exc_class);
        }

        return new ExceptionsAttribute(attrName, excTable);
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(attrName().getIndex());
        out.writeInt(2+2*exceptionTable.size());
        out.writeShort(exceptionTable.size());
        for (int i=0; i<exceptionTable.size(); i++)
            out.writeShort(((ConstClass) exceptionTable.elementAt(i)).getIndex());
    }

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.print("Exceptions:");//NOI18N
        for (int i=0; i<exceptionTable.size(); i++)
            out.print(" " + ((ConstClass) exceptionTable.elementAt(i)).asString());//NOI18N
        out.println();
    }

}
