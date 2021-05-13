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
 * Special instruction form for the opc_multianewarray instruction
 */
public class InsnMultiDimArrayNew extends Insn {
    /* The array class for creation */
    private ConstClass classOp;

    /* The number of dimensions present on the stack */
    private int nDimsOp;

    /* public accessors */

    public boolean isSimpleLoad() {
        return false;
    }

    public int nStackArgs() {
        return nDimsOp;
    }

    public int nStackResults() {
        return 1;
    }

    /**
     * What are the types of the stack operands ?
     */
    public String argTypes() {
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<nDimsOp; i++) {
            buf.append("I");//NOI18N
        }
        return buf.toString();
    }

    /**
     * What are the types of the stack results?
     */
    public String resultTypes() {
        return "A";//NOI18N
    }

    public boolean branches() {
        return false;
    }

    /**
     * Return the array class being created
     */
    public ConstClass arrayClass() {
        return classOp;
    }

    /**
     * Sets the array class being created
     */
    public void setArrayClass(ConstClass classOp) {
        this.classOp = classOp;
    }

    /**
     * Return the number of dimensions of the array class being created
     */
    public int nDims() {
        return nDimsOp;
    }

    /**
     * Constructor for opc_multianewarray.
     * classOp must be an array class
     * nDimsOp must be > 0 and <= number of array dimensions for classOp
     */
    public InsnMultiDimArrayNew (ConstClass classOp, int nDimsOp) {
        this(classOp, nDimsOp, NO_OFFSET);
    }

    /* package local methods */

    InsnMultiDimArrayNew (ConstClass classOp, int nDimsOp, int offset) {
        super(opc_multianewarray, offset);

        this.classOp = classOp;
        this.nDimsOp = nDimsOp;

        if (classOp == null || nDimsOp < 1)
            throw new InsnError ("attempt to create an opc_multianewarray" +//NOI18N
                " with invalid operands");//NOI18N
    }



    void print (PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println(offset() + "  opc_multianewarray  pool(" +//NOI18N
            classOp.getIndex() + ")," + nDimsOp);//NOI18N
    }

    int store(byte[] buf, int index) {
        buf[index++] = (byte) opcode();
        index = storeShort(buf, index, (short) classOp.getIndex());
        buf[index++] = (byte) nDimsOp;
        return index;
    }

    int size() {
        return 4;
    }

    static InsnMultiDimArrayNew read (InsnReadEnv insnEnv, int myPC) {
        ConstClass classOp = (ConstClass)
            insnEnv.pool().constantAt(insnEnv.getUShort());
        int nDims = insnEnv.getUByte();
        return new InsnMultiDimArrayNew(classOp, nDims, myPC);
    }

}
