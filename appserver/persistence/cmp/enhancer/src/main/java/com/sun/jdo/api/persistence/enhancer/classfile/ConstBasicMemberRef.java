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

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The abstract base class used to represent the various type of
 * references to members (fields/methods) within the constant pool.
 */
public abstract class ConstBasicMemberRef extends ConstBasic {
    /* The name of the class on which the member is defined */
    protected ConstClass theClassName;

    /* The index of the class on which the member is defined
     *   - used temporarily while reading from a class file */
    protected int theClassNameIndex;

    /* The name and type of the member */
    protected ConstNameAndType theNameAndType;

    /* The index of the name and type of the member
     *   - used temporarily while reading from a class file */
    protected int theNameAndTypeIndex;

    /* public accessors */

    /**
     * Return the name of the class defining the member
     */
    public ConstClass className() {
        return theClassName;
    }

    /**
     * Return the name and type of the member
     */
    public ConstNameAndType nameAndType() {
        return theNameAndType;
    }

    public String toString () {
        return "className(" + theClassName.toString() + ")" +//NOI18N
            " nameAndType(" + theNameAndType.toString() + ")";//NOI18N
    }

    /* package local methods */

    /**
     * Constructor for "from scratch" creation
     */
    ConstBasicMemberRef (ConstClass cname, ConstNameAndType NT) {
        theClassName = cname;
        theNameAndType = NT;
    }

    /**
     * Constructor for reading from a class file
     */
    ConstBasicMemberRef (int cnameIndex, int NT_index) {
        theClassNameIndex = cnameIndex;
        theNameAndTypeIndex = NT_index;
    }

    void formatData (DataOutputStream b) throws IOException {
        b.writeShort(theClassName.getIndex());
        b.writeShort(theNameAndType.getIndex());
    }
    void resolve (ConstantPool p) {
        theClassName = (ConstClass) p.constantAt(theClassNameIndex);
        theNameAndType = (ConstNameAndType) p.constantAt(theNameAndTypeIndex);
    }
}

