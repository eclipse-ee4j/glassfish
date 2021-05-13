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
 * Class representing a reference to a method of some class in the
 * constant pool of a class file
 */
public class ConstMethodRef extends ConstBasicMemberRef {
    /* The tag value associated with ConstMethodRef */
    public static final int MyTag = CONSTANTMethodRef;

    /* public accessors */

    /**
     * The tag of this constant entry
     */
    public int tag () { return MyTag; }

    /**
     * A printable representation
     */
    public String toString () {
        return "CONSTANTMethodRef(" + indexAsString() + "): " + //NOI18N
            super.toString();
    }

    /* package local methods */

    ConstMethodRef (ConstClass cname, ConstNameAndType NT) {
        super(cname, NT);
    }

    ConstMethodRef (int cnameIndex, int NT_index) {
        super(cnameIndex, NT_index);
    }

    static ConstMethodRef read (DataInputStream input) throws IOException {
        int cname = input.readUnsignedShort();
        int NT = input.readUnsignedShort();
        return new ConstMethodRef (cname, NT);
    }
}
