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

import java.util.Hashtable;

/**
 * Environment in which to decode the attributes of a CodeAttribute.
 */
class CodeEnv {
    /* The constant pool */
    private ConstantPool constantPool;

    /* hash table mapping byte code offset to InsnTarget */
    private Hashtable targets = new Hashtable(7);

    CodeEnv(ConstantPool constantPool) {
        this.constantPool = constantPool;
    }

    final InsnTarget getTarget(int offset) {
        Integer off = new Integer(offset);
        InsnTarget targ = (InsnTarget)targets.get(off);
        if (targ == null) {
            targ = new InsnTarget(offset);
            targets.put(off, targ);
        }
        return targ;
    }

    final InsnTarget findTarget(int offset) {
        Integer off = new Integer(offset);
        return (InsnTarget)targets.get(off);
    }

    final ConstantPool pool() {
        return constantPool;
    }
}
