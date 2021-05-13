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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Represents the LocalVariableTable attribute within a
 * method in a class file.
 */
public class LocalVariableTableAttribute extends ClassAttribute {
    /* The expected attribute name */
    public static final String expectedAttrName = "LocalVariableTable";//NOI18N

    /* The list of local variables */
    private Vector localTable;

    /* public accessors */

    /**
     * Returns an enumeration of the local variables in the table
     * Each element is a LocalVariable
     */
    Enumeration variables() {
        return localTable.elements();
    }

    /**
     * Constructor for a local variable table
     */
    public LocalVariableTableAttribute(
        ConstUtf8 nameAttr, Vector lvarTable) {
        super(nameAttr);
        localTable = lvarTable;
    }

    /* package local methods */

    static LocalVariableTableAttribute read(
        ConstUtf8 attrName, DataInputStream data, CodeEnv env)
            throws IOException {
        int nVars = data.readUnsignedShort();
        Vector lvarTable = new Vector();
        while (nVars-- > 0) {
            lvarTable.addElement(LocalVariable.read(data, env));
        }

        return new LocalVariableTableAttribute(attrName, lvarTable);
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(attrName().getIndex());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream tmp_out = new DataOutputStream(baos);
        tmp_out.writeShort(localTable.size());
        for (int i=0; i<localTable.size(); i++)
            ((LocalVariable) localTable.elementAt(i)).write(tmp_out);

        tmp_out.flush();
        byte tmp_bytes[] = baos.toByteArray();
        out.writeInt(tmp_bytes.length);
        out.write(tmp_bytes, 0, tmp_bytes.length);
    }

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println("LocalVariables: ");//NOI18N
        for (int i=0; i<localTable.size(); i++) {
            ((LocalVariable) localTable.elementAt(i)).print(out, indent+2);
        }
    }
}

