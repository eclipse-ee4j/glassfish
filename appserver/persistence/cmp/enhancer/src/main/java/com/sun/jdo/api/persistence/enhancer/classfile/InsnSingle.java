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
 * A java VM instruction which takes no immediate operands.
 */
public class InsnSingle extends Insn {

    public int nStackArgs() {
        return VMOp.ops[opcode()].nStackArgs();
    }

    public int nStackResults() {
        return VMOp.ops[opcode()].nStackResults();
    }

    /**
     * What are the types of the stack operands ?
     */
    public String argTypes() {
        return VMOp.ops[opcode()].argTypes();
    }

    /**
     * What are the types of the stack results?
     */
    public String resultTypes() {
        return VMOp.ops[opcode()].resultTypes();
    }

    public boolean branches() {
        switch (opcode()) {
            case opc_ireturn:
            case opc_lreturn:
            case opc_freturn:
            case opc_dreturn:
            case opc_areturn:
            case opc_return:
            case opc_athrow:
                return true;
            default:
                return false;
        }
    }


    /* package local methods */

    void print (PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println(offset() + "  " + opName(opcode()));//NOI18N
    }

    int store(byte[] buf, int index) {
        buf[index] = (byte) opcode();
        return index+1;
    }

    int size() {
        return 1;
    }

    /* Construct an instruction.  The opcode must be one which requires
     no operands */

    InsnSingle(int theOpcode) {
        this(theOpcode, NO_OFFSET);
    }

    /* The no-check constructor */

    InsnSingle(int theOpcode, int theOffset) {
        super(theOpcode, theOffset);

        switch (theOpcode) {
            case opc_nop:
            case opc_aconst_null:
            case opc_iconst_m1:
            case opc_iconst_0:
            case opc_iconst_1:
            case opc_iconst_2:
            case opc_iconst_3:
            case opc_iconst_4:
            case opc_iconst_5:
            case opc_lconst_0:
            case opc_lconst_1:
            case opc_fconst_0:
            case opc_fconst_1:
            case opc_fconst_2:
            case opc_dconst_0:
            case opc_dconst_1:
            case opc_iload_0:
            case opc_iload_1:
            case opc_iload_2:
            case opc_iload_3:
            case opc_lload_0:
            case opc_lload_1:
            case opc_lload_2:
            case opc_lload_3:
            case opc_fload_0:
            case opc_fload_1:
            case opc_fload_2:
            case opc_fload_3:
            case opc_dload_0:
            case opc_dload_1:
            case opc_dload_2:
            case opc_dload_3:
            case opc_aload_0:
            case opc_aload_1:
            case opc_aload_2:
            case opc_aload_3:
            case opc_iaload:
            case opc_laload:
            case opc_faload:
            case opc_daload:
            case opc_aaload:
            case opc_baload:
            case opc_caload:
            case opc_saload:
            case opc_istore_0:
            case opc_istore_1:
            case opc_istore_2:
            case opc_istore_3:
            case opc_lstore_0:
            case opc_lstore_1:
            case opc_lstore_2:
            case opc_lstore_3:
            case opc_fstore_0:
            case opc_fstore_1:
            case opc_fstore_2:
            case opc_fstore_3:
            case opc_dstore_0:
            case opc_dstore_1:
            case opc_dstore_2:
            case opc_dstore_3:
            case opc_astore_0:
            case opc_astore_1:
            case opc_astore_2:
            case opc_astore_3:
            case opc_iastore:
            case opc_lastore:
            case opc_fastore:
            case opc_dastore:
            case opc_aastore:
            case opc_bastore:
            case opc_castore:
            case opc_sastore:
            case opc_pop:
            case opc_pop2:
            case opc_dup:
            case opc_dup_x1:
            case opc_dup_x2:
            case opc_dup2:
            case opc_dup2_x1:
            case opc_dup2_x2:
            case opc_swap:
            case opc_iadd:
            case opc_ladd:
            case opc_fadd:
            case opc_dadd:
            case opc_isub:
            case opc_lsub:
            case opc_fsub:
            case opc_dsub:
            case opc_imul:
            case opc_lmul:
            case opc_fmul:
            case opc_dmul:
            case opc_idiv:
            case opc_ldiv:
            case opc_fdiv:
            case opc_ddiv:
            case opc_irem:
            case opc_lrem:
            case opc_frem:
            case opc_drem:
            case opc_ineg:
            case opc_lneg:
            case opc_fneg:
            case opc_dneg:
            case opc_ishl:
            case opc_lshl:
            case opc_ishr:
            case opc_lshr:
            case opc_iushr:
            case opc_lushr:
            case opc_iand:
            case opc_land:
            case opc_ior:
            case opc_lor:
            case opc_ixor:
            case opc_lxor:
            case opc_i2l:
            case opc_i2f:
            case opc_i2d:
            case opc_l2i:
            case opc_l2f:
            case opc_l2d:
            case opc_f2i:
            case opc_f2l:
            case opc_f2d:
            case opc_d2i:
            case opc_d2l:
            case opc_d2f:
            case opc_i2b:
            case opc_i2c:
            case opc_i2s:
            case opc_lcmp:
            case opc_fcmpl:
            case opc_fcmpg:
            case opc_dcmpl:
            case opc_dcmpg:
            case opc_ireturn:
            case opc_lreturn:
            case opc_freturn:
            case opc_dreturn:
            case opc_areturn:
            case opc_return:
            case opc_xxxunusedxxx:
            case opc_arraylength:
            case opc_athrow:
            case opc_monitorenter:
            case opc_monitorexit:
                break;

            default:
                throw new InsnError ("attempt to create an " + opName(opcode()) +//NOI18N
                    " without specifying the required operands");//NOI18N
        }
    }
}
