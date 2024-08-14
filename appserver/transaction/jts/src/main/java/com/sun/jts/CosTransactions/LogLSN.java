/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1995-1997 IBM Corp. All rights reserved.
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

//----------------------------------------------------------------------------
//
// Module:      LogLSN.java
//
// Description: Log sequence number.
//
// Product:     com.sun.jts.CosTransactions
//
// Author:      Simon Holdsworth
//
// Date:        March, 1997
//----------------------------------------------------------------------------

package com.sun.jts.CosTransactions;

import java.io.Serializable;

/**A structure containing 2 unsigned integers.
 * extent: the extent file number
 * offset: the offset within the extent file
 *
 * @version 0.01
 *
 * @author Simon Holdsworth, IBM Corporation
 *
 * @see
*/
//----------------------------------------------------------------------------
// CHANGE HISTORY
//
// Version By     Change Description
//   0.01  SAJH   Initial implementation.
//-----------------------------------------------------------------------------

class LogLSN implements Serializable {

    /**Constants for particular LSN values.
     */
    static final LogLSN HEAD_LSN  = new LogLSN(0xFFFFFFFF, 0xFFFFFFFF);
    static final LogLSN TAIL_LSN  = new LogLSN(0xFFFFFFFF, 0xFFFFFFFE);
    static final LogLSN NULL_LSN  = new LogLSN(0x00000000, 0x00000000);
    static final LogLSN FIRST_LSN = new LogLSN(0x00000001, 0x00000000);

    /**This constant holds the size of the LogRecordEnding object.
     */
    static final int SIZEOF = 8;

    /**Internal instance members.
     */
    int offset = 0;
    int extent = 0;

    /**Default LogLSN constructor
     *
     * @param
     *
     * @return
     *
     * @see
     */
    LogLSN() {
        offset = 0;
        extent = 0;
    }

    /**LogLSN constructor
     *
     * @param ext Extent for new LSN.
     * @param off Offset for new LSN.
     *
     * @return
     *
     * @see
     */
    LogLSN( int ext,
            int off ) {
        offset = off;
        extent = ext;
    }

    /**LogLSN constructor
     *
     * @param lsn Other LSN to be copied.
     *
     * @return
     *
     * @see
     */
    LogLSN( LogLSN lsn ) {
        offset = lsn.offset;
        extent = lsn.extent;
    }

    /**Constructs a LogLSN from the given byte array.
     *
     * @param bytes The array of bytes from which the LogLSN is to be constructed.
     * @param index The index in the array where copy is to start.
     *
     * @return
     *
     * @see
     */
    LogLSN( byte[] bytes,
            int  index ) {
        offset =  (bytes[index++]&255) +
            ((bytes[index++]&255) << 8) +
            ((bytes[index++]&255) << 16) +
            ((bytes[index++]&255) << 24);
        extent =  (bytes[index++]&255) +
            ((bytes[index++]&255) << 8) +
            ((bytes[index++]&255) << 16) +
            ((bytes[index++]&255) << 24);
    }

    /**Determines whether the target LSN is NULL.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    final boolean isNULL() {
        return offset == 0 && extent == 0;
    }

    /**Determines whether the given LSN is equal to the target.
     *
     * @param other The other LogLSN to be compared.
     *
     * @return
     *
     * @see
     */
    final boolean equals( LogLSN other ) {
        return offset == other.offset && extent == other.extent;
    }

    /**Determines whether the target LSN is less than the parameter.
     *
     * @param other The other LogLSN to be compared.
     *
     * @return
     *
     * @see
     */
    final boolean lessThan( LogLSN other ) {
        return ( (offset < other.offset && extent == other.extent) ||
                 extent < other.extent);
    }

    /**Determines whether the target LSN is greater than the parameter.
     *
     * @param other The other LogLSN to be compared.
     *
     * @return
     *
     * @see
     */
    final boolean greaterThan( LogLSN other ) {
        return ( (offset > other.offset && extent == other.extent) ||
                 extent > other.extent);
    }

    /**makes the target LSN a copy of the parameter.
     *
     * @param LogLSN  The LSN to be copied.
     *
     * @return
     *
     * @see
     */
    final void copy( LogLSN other ) {
        extent = other.extent;
        offset = other.offset;
    }

    /**Makes a byte representation of the LogLSN.
     *
     * @param bytes The array of bytes into which the LogLSN is to be copied.
     * @param index The index in the array where copy is to start.
     *
     * @return  Number of bytes copied.
     *
     * @see
     */
    final int toBytes( byte[] bytes,
                       int  index ) {
        bytes[index++] = (byte) offset;
        bytes[index++] = (byte)(offset >> 8);
        bytes[index++] = (byte)(offset >> 16);
        bytes[index++] = (byte)(offset >> 24);
        bytes[index++] = (byte) extent;
        bytes[index++] = (byte)(extent >> 8);
        bytes[index++] = (byte)(extent >> 16);
        bytes[index++] = (byte)(extent >> 24);

        return SIZEOF;
    }

    /**This method is called to direct the object to format its state to a String.
     *
     * @param
     *
     * @return  The formatted representation of the object.
     *
     * @see
     */
    public final String toString() {
        return "LSN(ext="/*#Frozen*/+extent+",off="/*#Frozen*/+offset+")"/*#Frozen*/;
    }
}
