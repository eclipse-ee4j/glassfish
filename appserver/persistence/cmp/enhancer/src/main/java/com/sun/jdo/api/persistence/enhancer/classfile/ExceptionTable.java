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
 * ExceptionTable represents the exception handlers within the code
 * of a method.
 */
public class ExceptionTable {
    /* A variable length list of ExceptionRange objects */
    private Vector theVector = new Vector();

    /* public accessors */

    /**
     * Return an enumeration of the exception handlers
     * Each element in the enumeration is an ExceptionRange
     */
    public Enumeration handlers() {
        return theVector.elements();
    }

    /**
     * Add an exception handler to the list
     */
    public void addElement(ExceptionRange range) {
        theVector.addElement(range);
    }

    public ExceptionTable() { }

    /* package local methods */

    static ExceptionTable read(DataInputStream data, CodeEnv env)
        throws IOException {
        ExceptionTable excTable = new ExceptionTable();
        int nExcepts = data.readUnsignedShort();
        while (nExcepts-- > 0) {
            excTable.addElement(ExceptionRange.read(data, env));
        }
        return excTable;
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(theVector.size());
        for (int i=0; i<theVector.size(); i++)
            ((ExceptionRange) theVector.elementAt(i)).write(out);
    }

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println("Exception Table: ");//NOI18N
        for (int i=0; i<theVector.size(); i++)
            ((ExceptionRange) theVector.elementAt(i)).print(out, indent+2);
    }
}
