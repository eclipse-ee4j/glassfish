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
 * InsnTarget is a pseudo-instruction which represents a branch target
 * in an instruction stream.
 */
public class InsnTarget extends Insn {

    private boolean branchTarget = false;

    public int nStackArgs() {
        return 0;
    }

    public int nStackResults() {
        return 0;
    }

    public String argTypes() {
        return "";//NOI18N
    }

    public String resultTypes() {
        return "";//NOI18N
    }

    public boolean branches() {
        return false;
    }

    public void setBranchTarget() {
        branchTarget = true;
    }

    /* not valid unless method instructions processed specially */
    public boolean isBranchTarget() {
        return branchTarget;
    }

    /**
     * Constructor
     */
    public InsnTarget() {
        super(opc_target, NO_OFFSET);
    }

    /* package local methods */

    void print (PrintStream out, int indent) {
        ClassPrint.spaces(out, indent);
        out.println(offset() + ":");//NOI18N
    }

    int store(byte buf[], int index) {
        return index;
    }

    int size() {
        return 0;
    }

    InsnTarget(int offset) {
        super(opc_target, offset);
    }

}

