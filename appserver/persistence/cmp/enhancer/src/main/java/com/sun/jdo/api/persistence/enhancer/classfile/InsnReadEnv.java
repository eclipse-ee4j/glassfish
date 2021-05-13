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

/**
 * Environment for decoding byte codes into instructions
 */
class InsnReadEnv {

    /* The parent method environment */
    private CodeEnv codeEnv;

    /* The byte codes to be decoded */
    private byte[] byteCodes;

    /* The index into byteCodes for the next instruction to be decoded */
    private int currPc;

    /**
     * Constructor
     */
    InsnReadEnv(byte[] bytes, CodeEnv codeEnv) {
        this.byteCodes = bytes;
        this.currPc = 0;
        this.codeEnv = codeEnv;
    }

    /**
     * Return the index of the next instruction to decode
     */
    int currentPC() {
        return currPc;
    }

    /**
     * Are there more byte codes to decode?
     */
    boolean more() {
        return currPc < byteCodes.length;
    }

    /**
     * Get a single byte from the byte code stream
     */
    byte getByte() {
        if (!more())
            throw new InsnError("out of byte codes");//NOI18N

        return byteCodes[currPc++];
    }

    /**
     * Get a single unsigned byte from the byte code stream
     */
    int getUByte() {
        return getByte() & 0xff;
    }

    /**
     * Get a short from the byte code stream
     */
    int getShort() {
        byte byte1 = byteCodes[currPc++];
        byte byte2 = byteCodes[currPc++];
        return (byte1 << 8) | (byte2 & 0xff);
    }

    /**
     * Get an unsigned short from the byte code stream
     */
    int getUShort() {
        return getShort() & 0xffff;
    }

    /**
     * Get an int from the byte code stream
     */
    int getInt() {
        byte byte1 = byteCodes[currPc++];
        byte byte2 = byteCodes[currPc++];
        byte byte3 = byteCodes[currPc++];
        byte byte4 = byteCodes[currPc++];
        return (byte1 << 24) | ((byte2 & 0xff) << 16) |
            ((byte3  & 0xff) << 8) | (byte4 & 0xff);
    }

    /**
     * Get the constant pool which applies to the method being decoded
     */
    ConstantPool pool() {
        return codeEnv.pool();
    }

    /**
     * Get the canonical InsnTarget instance for the specified
     * pc within the method.
     */
    InsnTarget getTarget(int targ) {
        return codeEnv.getTarget(targ);
    }
}
