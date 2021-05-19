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
 * ExceptionRange represents a range an exception handler within
 * a method in class file.
 */
public class ExceptionRange {
    /* The start of the exception hander (inclusive) */
    private InsnTarget excStartPC;

    /* The end of the exception hander (exclusive) */
    private InsnTarget excEndPC;

    /* The exception handler code */
    private InsnTarget excHandlerPC;

    /* The exception specification */
    private ConstClass excCatchType;

    /* public accessors */

    /**
     * return the start of the exception hander (inclusive)
     */
    public InsnTarget startPC() {
        return excStartPC;
    }

    /**
     * return the end of the exception hander (exclusive)
     */
    public InsnTarget endPC() {
        return excEndPC;
    }

    /**
     * return the exception handler code
     */
    public InsnTarget handlerPC() {
        return excHandlerPC;
    }

    /**
     * return the exception specification
     * a null return value means a catch of any (try/finally)
     */
    public ConstClass catchType() {
        return excCatchType;
    }

    /**
     * constructor
     */

    public ExceptionRange(InsnTarget startPC, InsnTarget endPC,
        InsnTarget handlerPC, ConstClass catchType) {
        excStartPC = startPC;
        excEndPC = endPC;
        excHandlerPC = handlerPC;
        excCatchType = catchType;
    }

    /* package local methods */

    static ExceptionRange read(DataInputStream data, CodeEnv env)
        throws IOException {
        InsnTarget startPC = env.getTarget(data.readUnsignedShort());
        InsnTarget endPC = env.getTarget(data.readUnsignedShort());
        InsnTarget handlerPC = env.getTarget(data.readUnsignedShort());
        ConstClass catchType =
            (ConstClass) env.pool().constantAt(data.readUnsignedShort());
        return new ExceptionRange(startPC, endPC, handlerPC, catchType);
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(excStartPC.offset());
        out.writeShort(excEndPC.offset());
        out.writeShort(excHandlerPC.offset());
        out.writeShort(excCatchType == null ? 0 : excCatchType.getIndex());
    }

    void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.print("Exc Range:");//NOI18N
        if (excCatchType == null)
            out.print("any");//NOI18N
        else
            out.print("'" + excCatchType.asString() + "'");//NOI18N
        out.print(" start = " + Integer.toString(excStartPC.offset()));//NOI18N
        out.print(" end = " + Integer.toString(excEndPC.offset()));//NOI18N
        out.println(" handle = " + Integer.toString(excHandlerPC.offset()));//NOI18N
    }
}
