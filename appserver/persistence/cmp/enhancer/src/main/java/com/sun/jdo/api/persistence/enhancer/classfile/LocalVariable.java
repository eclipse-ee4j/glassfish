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
 * Represents a local variable within a LocalVariableTable within
 * a CodeAttribute in a class file.
 */
public class LocalVariable {
    /* The pc at which the variable becomes effecive */
    private InsnTarget varStartPC; /* inclusive */

    /* The pc at which the variable becomes in-effecive */
    private InsnTarget varEndPC;   /* exclusive */

    /* The name of the variable */
    private ConstUtf8 varName;

    /* The type signature of the variable */
    private ConstUtf8 varSig;

    /* The slot to which the variable is assigned */
    private int varSlot;

    /* public accessors */

    /**
     * Constructor for a local variable
     */
    public LocalVariable(InsnTarget startPC, InsnTarget endPC,
        ConstUtf8 name, ConstUtf8 sig, int slot) {
        varStartPC = startPC;
        varEndPC = endPC;
        varName = name;
        varSig = sig;
        varSlot = slot;
    }

    /* package local methods */

    static LocalVariable read(DataInputStream data, CodeEnv env)
        throws IOException {
        int startPC = data.readUnsignedShort();
        InsnTarget startPCTarget = env.getTarget(startPC);
        int length = data.readUnsignedShort();
        InsnTarget endPCTarget = env.getTarget(startPC+length);
        ConstUtf8 name = (ConstUtf8) env.pool().constantAt(data.readUnsignedShort());
        ConstUtf8 sig = (ConstUtf8) env.pool().constantAt(data.readUnsignedShort());
        int slot = data.readUnsignedShort();
        return new LocalVariable(startPCTarget, endPCTarget, name, sig, slot);
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(varStartPC.offset());
        out.writeShort(varEndPC.offset() - varStartPC.offset());
        out.writeShort((varName == null) ? 0 : varName.getIndex());
        out.writeShort((varSig == null) ? 0 : varSig.getIndex());
        out.writeShort(varSlot);
    }

    public void print(PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.print("'" + ((varName == null) ? "(null)" : varName.asString()) + "'");//NOI18N
        out.print(" sig = " + ((varSig == null) ? "(null)" : varSig.asString()));//NOI18N
        out.print(" start_pc = " + Integer.toString(varStartPC.offset()));//NOI18N
        out.print(" length = " +//NOI18N
            Integer.toString(varEndPC.offset() - varStartPC.offset()));
        out.println(" slot = " + Integer.toString(varSlot));//NOI18N
    }

}

