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
 * An instruction which requires a single constant from the constant
 * pool as an immediate operand
 */
public class InsnConstOp extends Insn {
    /* The constant from the constant pool */
    private ConstBasic constValue;

    /* public accessors */

    public int nStackArgs() {
        int n = VMOp.ops[opcode()].nStackArgs();
        if (n >= 0)
            return n;
        switch (opcode()) {
            case opc_putstatic:
            case opc_putfield:
            {
                ConstFieldRef fld = (ConstFieldRef) constValue;
                String sig = fld.nameAndType().signature().asString();
                if (sig.equals("J") || sig.equals("D"))//NOI18N
                    return (opcode() == opc_putfield) ? 3 : 2;
                return (opcode() == opc_putfield) ? 2 : 1;
            }
            case opc_invokevirtual:
            case opc_invokespecial:
            case opc_invokestatic:
                /* handle interface invoke too */
            case opc_invokeinterface:
            {
                ConstBasicMemberRef meth = (ConstBasicMemberRef) constValue;
                String sig = meth.nameAndType().signature().asString();
                int nMethodArgWords = Descriptor.countMethodArgWords(sig);
                return nMethodArgWords +
                    ((opcode() == opc_invokestatic) ? 0 : 1);
            }
            default:
                throw new InsnError("unexpected variable opcode");//NOI18N
        }
    }

    public int nStackResults() {
        int n = VMOp.ops[opcode()].nStackResults();
        if (n >= 0)
            return n;
        switch (opcode()) {
            case opc_getstatic:
            case opc_getfield:
            {
                ConstFieldRef fld = (ConstFieldRef) constValue;
                String sig = fld.nameAndType().signature().asString();
                if (sig.equals("J") || sig.equals("D"))//NOI18N
                    return 2;
                return 1;
            }
            case opc_invokevirtual:
            case opc_invokespecial:
            case opc_invokestatic:
                /* handle interface invoke too */
            case opc_invokeinterface:
            {
                ConstBasicMemberRef meth = (ConstBasicMemberRef) constValue;
                return Descriptor.countMethodReturnWords(
                    meth.nameAndType().signature().asString());
            }
            default:
                throw new InsnError("unexpected variable opcode");//NOI18N
        }
    }

    public String argTypes() {
        switch (opcode()) {
            case opc_putstatic:
            case opc_putfield:
            {
                ConstFieldRef fld = (ConstFieldRef) constValue;
                String sig = fld.nameAndType().signature().asString();
                if (opcode() == opc_putstatic)
                    return sig;
                else
                    return descriptorTypeOfObject(fld) + sig;
            }
            case opc_invokevirtual:
            case opc_invokespecial:
            case opc_invokestatic:
                /* handle interface invoke too */
            case opc_invokeinterface:
            {
                ConstBasicMemberRef meth = (ConstBasicMemberRef) constValue;
                String argSig =
                    Descriptor.extractArgSig(meth.nameAndType().signature().asString());
                if (opcode() == opc_invokestatic)
                    return argSig;
                else
                    return descriptorTypeOfObject(meth) + argSig;
            }
            default:
                return VMOp.ops[opcode()].argTypes();
        }
    }

    public String resultTypes() {
        switch (opcode()) {
            case opc_invokevirtual:
            case opc_invokespecial:
            case opc_invokestatic:
                /* handle interface invoke too */
            case opc_invokeinterface:
            {
                ConstBasicMemberRef meth = (ConstBasicMemberRef) constValue;
                String resultSig = Descriptor.extractResultSig(
                    meth.nameAndType().signature().asString());
                if (resultSig.equals("V"))//NOI18N
                    return "";//NOI18N
                return resultSig;
            }
            case opc_getstatic:
            case opc_getfield:
            {
                ConstFieldRef fld = (ConstFieldRef) constValue;
                return fld.nameAndType().signature().asString();
            }
            case opc_ldc:
            case opc_ldc_w:
            case opc_ldc2_w:
            {
                ConstValue constVal = (ConstValue) constValue;
                return constVal.descriptor();
            }
            default:
                return VMOp.ops[opcode()].resultTypes();
        }
    }

    public boolean branches() {
        /* invokes don't count as a branch */
        return false;
    }

    /**
     * Return the constant pool entry which is the immediate operand
     */
    public ConstBasic value() {
        return constValue;
    }

    /**
     * Modify the referenced constant
     */
    public void setValue(ConstBasic newValue) {
        checkConstant(newValue);
        constValue = newValue;
    }

    /* package local methods */

    void print (PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println(offset() + "  " + opName(opcode()) + "  pool(" + //NOI18N
            constValue.getIndex() + ")");//NOI18N
    }

    int store(byte[] buf, int index) {
        if (opcode() == opc_ldc && !isNarrowldc())
            buf[index++] = (byte) opc_ldc_w;
        else
            buf[index++] = (byte) opcode();
        int constIndex = constValue.getIndex();
        if (size() == 3)
            buf[index++] = (byte) (constIndex >> 8);
        buf[index++] = (byte)(constIndex & 0xff);
        return index;
    }

    int size() {
        return isNarrowldc() ? 2 : 3;
    }

    private boolean isNarrowldc() {
        return (opcode() == opc_ldc && constValue.getIndex() < 256);
    }


    InsnConstOp (int theOpcode, ConstBasic theOperand) {
        this(theOpcode, theOperand, NO_OFFSET);
    }

    InsnConstOp (int theOpcode, ConstBasic theOperand, int pc) {
        super(theOpcode, pc);
        constValue = theOperand;
        checkConstant(theOperand);
        if (theOpcode == opc_invokeinterface)
            throw new InsnError("attempt to create an " + opName(theOpcode) +//NOI18N
                " as an InsnConstOp instead of InsnInterfaceInvoke");//NOI18N
    }

    /* used only by InsnInterfaceInvoke, to make sure that opc_invokeinterface cannot
     * come through the wrong path and miss its extra nArgsOp */
    InsnConstOp (int theOpcode, ConstInterfaceMethodRef theOperand, int pc) {
        super(theOpcode, pc);
        constValue = theOperand;
        checkConstant(theOperand);
    }

    private void checkConstant (ConstBasic operand) {
        switch(opcode()) {
            case opc_ldc:
            case opc_ldc_w:
            case opc_ldc2_w:
                /* ConstValue */
                if (operand == null ||
                (! (operand instanceof ConstValue)))
                    throw new InsnError ("attempt to create an " + opName(opcode()) +//NOI18N
                        " without a ConstValue operand");//NOI18N
                break;

            case opc_getstatic:
            case opc_putstatic:
            case opc_getfield:
            case opc_putfield:
                /* ConstFieldRef */
                if (operand == null ||
                (! (operand instanceof ConstFieldRef)))
                    throw new InsnError ("attempt to create an " + opName(opcode()) +//NOI18N
                        " without a ConstFieldRef operand");//NOI18N
                break;

            case opc_invokevirtual:
            case opc_invokespecial:
            case opc_invokestatic:
                /* ConstMethodRef */
                if (operand == null ||
                (! (operand instanceof ConstMethodRef)))
                    throw new InsnError ("attempt to create an " + opName(opcode()) +//NOI18N
                        " without a ConstMethodRef operand");//NOI18N
                break;

            case opc_invokeinterface:
                /* ConstInterfaceMethodRef */
                if (operand == null ||
                (! (operand instanceof ConstInterfaceMethodRef)))
                    throw new InsnError("Attempt to create an " + opName(opcode()) +//NOI18N
                        " without a ConstInterfaceMethodRef operand");//NOI18N
                break;

            case opc_new:
            case opc_anewarray:
            case opc_checkcast:
            case opc_instanceof:
                /* ConstClass */
                if (operand == null ||
                (! (operand instanceof ConstClass)))
                    throw new InsnError ("attempt to create an " + opName(opcode()) +//NOI18N
                        " without a ConstClass operand");//NOI18N
                break;

            default:
                throw new InsnError ("attempt to create an " + opName(opcode()) +//NOI18N
                    " with a constant operand");//NOI18N
        }
    }

    private final String descriptorTypeOfObject(ConstBasicMemberRef memRef) {
        String cname = memRef.className().className().asString();
        return "L" + cname + ";";//NOI18N
    }

}
