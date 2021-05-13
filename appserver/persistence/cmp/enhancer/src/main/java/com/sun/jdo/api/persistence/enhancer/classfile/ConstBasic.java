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
 * Abstract base class of the types which represent entries in
 * the class constant pool.
 */
abstract public class ConstBasic implements VMConstants {
    /* The index of the constant entry in the constant pool */
    protected int index = 0;

    /* public accessors */

    /* Get the index of this constant entry */
    public int getIndex() { return index; }

    /* Return the type of the constant entry - see VMConstants */
    public abstract int tag ();

    /* package local methods */

    /**
     * Sets the index of this constant with its containing constant pool
     */
    void setIndex(int ind) { index = ind; }

    /**
     * Write this Constant pool entry to the output stream
     */
    abstract void formatData (DataOutputStream b) throws IOException;

    /**
     * Resolve integer index references to the actual constant pool
     * entries that they represent.  This is used during class file
     * reading because a constant pool entry could have a forward
     * reference to a higher numbered constant.
     */
    abstract void resolve (ConstantPool p);

    /**
     * Return the index of this constant in the constant pool as
     * a decimal formatted String.
     */
    String indexAsString() { return Integer.toString(index); }

    /**
     * The constructor for subtypes
     */
    ConstBasic () {}
}

