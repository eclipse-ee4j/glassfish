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
 * Special instruction form for the opc_lookupswitch instruction
 */
public class InsnLookupSwitch extends Insn {
    /* The target for the default case */
    private InsnTarget defaultOp;

    /* The int constants against which to perform the lookup */
    private int[] matchesOp;

    /* The branch targets for the cases corresponding to the entries in
     * the matchesOp array */
    private InsnTarget[] targetsOp;

    /* public accessors */

    public int nStackArgs() {
        return 1;
    }

    public int nStackResults() {
        return 0;
    }

    /**
     * What are the types of the stack operands ?
     */
    public String argTypes() {
        return "I";//NOI18N
    }

    /**
     * What are the types of the stack results?
     */
    public String resultTypes() {
        return "";//NOI18N
    }

    public boolean branches() {
        return true;
    }

    /**
     * Mark possible branch targets
     */
    public void markTargets() {
        defaultOp.setBranchTarget();
        for (int i=0; i<targetsOp.length; i++)
            targetsOp[i].setBranchTarget();
    }


    /**
     * Return the defaultTarget for the switch
     */
    public InsnTarget defaultTarget() {
        return defaultOp;
    }

    /**
     * Return the case values of the switch.
     */
    public int[] switchCases() {
        return matchesOp;
    }

    /**
     * Return the targets for the cases of the switch.
     */
    public InsnTarget[] switchTargets() {
        return targetsOp;
    }

    /**
     * Constructor for opc_lookupswitch
     */
    public InsnLookupSwitch (InsnTarget defaultOp, int[] matchesOp,
        InsnTarget[] targetsOp) {
        this(defaultOp, matchesOp, targetsOp, NO_OFFSET);
    }


    /* package local methods */

    InsnLookupSwitch (InsnTarget defaultOp, int[] matchesOp,
        InsnTarget[] targetsOp, int offset) {
        super(opc_lookupswitch, offset);

        this.defaultOp = defaultOp;
        this.matchesOp = matchesOp;
        this.targetsOp = targetsOp;

        if (defaultOp == null || targetsOp == null || matchesOp == null ||
            targetsOp.length != matchesOp.length)
            throw new InsnError ("attempt to create an opc_lookupswitch" +//NOI18N
                " with invalid operands");//NOI18N
    }

    void print (PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println(offset() + "  opc_lookupswitch  ");//NOI18N
        for (int i=0; i<matchesOp.length; i++) {
            ClassPrint.spaces(out, indent+2);
            out.println(matchesOp[i] + " -> " + targetsOp[i].offset());//NOI18N
        }
        ClassPrint.spaces(out, indent+2);
        out.println("default -> " + defaultOp.offset());//NOI18N
    }

    int store(byte[] buf, int index) {
        buf[index++] = (byte) opcode();
        index = (index + 3) & ~3;
        index = storeInt(buf, index, defaultOp.offset() - offset());
        index = storeInt(buf, index, targetsOp.length);
        for (int i=0; i<targetsOp.length; i++) {
            index = storeInt(buf, index, matchesOp[i]);
            index = storeInt(buf, index, targetsOp[i].offset() - offset());
        }
        return index;
    }

    int size() {
        /* account for the instruction, 0-3 bytes of pad, 2 ints */
        int basic = ((offset() + 4) & ~3) - offset() + 8;
        /* Add 8*number of offsets */
        return basic + targetsOp.length*8;
    }

    static InsnLookupSwitch read (InsnReadEnv insnEnv, int myPC) {
        /* eat up any padding */
        int thisPC = myPC +1;
        for (int pads = ((thisPC + 3) & ~3) - thisPC; pads > 0; pads--)
            insnEnv.getByte();
        InsnTarget defaultTarget = insnEnv.getTarget(insnEnv.getInt() + myPC);
        int npairs = insnEnv.getInt();
        int matches[] = new int[npairs];
        InsnTarget[] offsets = new InsnTarget[npairs];
        for (int i=0; i<npairs; i++) {
            matches[i] = insnEnv.getInt();
            offsets[i] = insnEnv.getTarget(insnEnv.getInt() + myPC);
        }
        return new InsnLookupSwitch(defaultTarget, matches, offsets, myPC);
    }
}
