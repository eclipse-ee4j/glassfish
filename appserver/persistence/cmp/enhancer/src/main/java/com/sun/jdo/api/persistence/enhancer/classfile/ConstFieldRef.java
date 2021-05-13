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
import java.io.IOException;

/**
 * ConstFieldRef represents a reference to a field of some class
 * in the constant pool of a class file.
 */
public class ConstFieldRef extends ConstBasicMemberRef {
    /* The tag associated with ConstFieldRef */
    public static final int MyTag = CONSTANTFieldRef;

    /* public accessors */
    @Override
    public int tag () { return MyTag; }

    @Override
    public String toString () {
        return "CONSTANTFieldRef(" + indexAsString() + "): " + //NOI18N
            super.toString();
    }

    /* package local methods */

    ConstFieldRef (ConstClass cname, ConstNameAndType NT) {
        super(cname, NT);
    }

    ConstFieldRef (int cnameIndex, int NT_index) {
        super(cnameIndex, NT_index);
    }

    static ConstFieldRef read (DataInputStream input) throws IOException {
        int cname = input.readUnsignedShort();
        int NT = input.readUnsignedShort();
        return new ConstFieldRef (cname, NT);
    }

}

