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


import java.io.PrintStream;

/**
 * Special instruction form for the opc_iinc instruction
 */
public class InsnIInc extends Insn {

    /* The local variable slot to be incremented */
    private int localVarIndex;

    /* The amount by which the slot is to be incremented */
    private int value;

    /* public accessors */

    public int nStackArgs() {
        return 0;
    }

    public int nStackResults() {
        return 0;
    }

    /**
     * What are the types of the stack operands ?
     */
    public String argTypes() {
        return "";//NOI18N
    }

    /**
     * What are the types of the stack results?
     */
    public String resultTypes() {
        return "";//NOI18N
    }

    public boolean branches() {
        return false;
    }

    /**
     * The local variable slot to be incremented
     */
    public int varIndex() {
        return localVarIndex;
    }

    /**
     * The amount by which the slot is to be incremented
     */
    public int incrValue() {
        return value;
    }

    /**
     * Constructor for opc_iinc instruction
     */
    public InsnIInc (int localVarIndex, int value) {
        this(localVarIndex, value, NO_OFFSET);
    }

    /* package local methods */

    InsnIInc (int localVarIndex, int value, int pc) {
        super(opc_iinc, pc);

        this.localVarIndex = localVarIndex;
        this.value =value;
    }

    void print (PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println(offset() + "  opc_iinc  " + //NOI18N
            localVarIndex + "," + value);//NOI18N
    }

    int store(byte[] buf, int index) {
        if (isWide())
            buf[index++] = (byte) opc_wide;
        buf[index++] = (byte) opcode();
        if (isWide()) {
            index = storeShort(buf, index, (short) localVarIndex);
            index = storeShort(buf, index, (short) value);
        } else {
            buf[index++] = (byte)localVarIndex;
            buf[index++] = (byte)value;
        }
        return index;
    }

    int size() {
        return isWide() ? 6 : 3;
    }

    private boolean isWide() {
        return (value > 127 || value < -128 || localVarIndex > 255);
    }

}
