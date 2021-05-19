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
 * An instruction which requires a single branch offset
 * as an immediate operand .
 */
public class InsnTargetOp extends Insn {
    /* The branch target */
    InsnTarget targetOp;

    /* public accessors */

    public int nStackArgs() {
        return VMOp.ops[opcode()].nStackArgs();
    }

    public int nStackResults() {
        return VMOp.ops[opcode()].nStackResults();
    }

    public String argTypes() {
        return VMOp.ops[opcode()].argTypes();
    }

    public String resultTypes() {
        return VMOp.ops[opcode()].resultTypes();
    }

    public boolean branches() {
        return true;
    }

    /**
     * Mark possible branch targets
     */
    public void markTargets() {
        targetOp.setBranchTarget();
    }

    /**
     * Return the branch target which is the immediate operand
     */
    public InsnTarget target() {
        return targetOp;
    }

    /* package local methods */

    void print (PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        /* print offset in non-relative form for readability */
        out.println(offset() + "  " + opName(opcode()) + "  " + //NOI18N
            targetOp.offset());
    }

    int store(byte[] buf, int index) {
        buf[index++] = (byte) opcode();
        int off = targetOp.offset() - offset();
        if (opcode() == opc_goto_w || opcode() == opc_jsr_w)
            return storeInt(buf, index, off);
        else
            return storeShort(buf, index, (short)off);
    }

    int size() {
        if (opcode() == opc_goto_w || opcode() == opc_jsr_w)
            return 5;
        return 3;
    }

    InsnTargetOp (int theOpcode, InsnTarget theOperand, int pc) {
        super(theOpcode, pc);
        targetOp = theOperand;
    }

    InsnTargetOp (int theOpcode, InsnTarget theOperand) {
        super(theOpcode, NO_OFFSET);

        targetOp = theOperand;

        switch(theOpcode) {
            case opc_ifeq:
            case opc_ifne:
            case opc_iflt:
            case opc_ifge:
            case opc_ifgt:
            case opc_ifle:
            case opc_if_icmpeq:
            case opc_if_icmpne:
            case opc_if_icmplt:
            case opc_if_icmpge:
            case opc_if_icmpgt:
            case opc_if_icmple:
            case opc_if_acmpeq:
            case opc_if_acmpne:
            case opc_goto:
            case opc_jsr:
            case opc_ifnull:
            case opc_ifnonnull:
            case opc_goto_w:
            case opc_jsr_w:
                /* Target */
                if (theOperand == null)
                    throw new InsnError ("attempt to create an " + opName(theOpcode) +//NOI18N
                        " with a null Target operand");//NOI18N
                break;

            default:
                throw new InsnError ("attempt to create an " + opName(theOpcode) +//NOI18N
                    " with an InsnTarget operand");//NOI18N
        }
    }
}
