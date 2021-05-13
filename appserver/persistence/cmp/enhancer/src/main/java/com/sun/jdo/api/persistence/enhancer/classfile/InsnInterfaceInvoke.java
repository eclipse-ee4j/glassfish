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
 * Special instruction form for the opc_invokeinterface instruction
 */
public class InsnInterfaceInvoke extends InsnConstOp {
    /* The number of arguments to the interface method */
    private int nArgsOp;

    /* public accessors */

    public int nStackArgs() {
        return super.nStackArgs();
    }

    public int nStackResults() {
        return super.nStackResults();
    }

    /**
     * What are the types of the stack operands ?
     */
    public String argTypes() {
        return super.argTypes();
    }

    /**
     * What are the types of the stack results?
     */
    public String resultTypes() {
        return super.resultTypes();
    }

    public boolean branches() {
        return false;
    }

    /**
     * Return the interface to be invoked
     */
    public ConstInterfaceMethodRef method() {
        return (ConstInterfaceMethodRef) value();
    }

    /**
     * Return the number of arguments to the interface
     */
    public int nArgs() {
        return nArgsOp;
    }

    /**
     * constructor for opc_invokeinterface
     */
    public InsnInterfaceInvoke (ConstInterfaceMethodRef methodRefOp,
        int nArgsOp) {
        this(methodRefOp, nArgsOp, NO_OFFSET);
    }

    /* package local methods */

    InsnInterfaceInvoke (ConstInterfaceMethodRef methodRefOp, int nArgsOp,
        int offset) {
        super(opc_invokeinterface, methodRefOp, offset);

        this.nArgsOp = nArgsOp;

        if (methodRefOp == null || nArgsOp < 0)
            throw new InsnError ("attempt to create an opc_invokeinterface" +//NOI18N
                " with invalid operands");//NOI18N
    }

    void print (PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println(offset() + "  opc_invokeinterface  " + //NOI18N
            "pool(" + method().getIndex() + ")," + nArgsOp);//NOI18N
    }

    int store(byte[] buf, int index) {
        buf[index++] = (byte) opcode();
        index = storeShort(buf, index, (short)method().getIndex());
        buf[index++] = (byte) nArgsOp;
        buf[index++] = (byte) 0;
        return index;
    }

    int size() {
        return 5;
    }

    static InsnInterfaceInvoke read(InsnReadEnv insnEnv, int myPC) {
        ConstInterfaceMethodRef iface = (ConstInterfaceMethodRef)
            insnEnv.pool().constantAt(insnEnv.getUShort());
        int nArgs = insnEnv.getUByte();
        insnEnv.getByte(); // eat reserved arg
        return new InsnInterfaceInvoke(iface, nArgs, myPC);
    }
}
