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
// Module:      LogRecordHeader.java
//
// Description: Log record header.
//
// Product:     com.sun.jts.CosTransactions
//
// Author:      Simon Holdsworth
//
// Date:        March, 1997
//----------------------------------------------------------------------------

package com.sun.jts.CosTransactions;

import java.io.Serializable;

/**
 * A class containing header information for a log record.
 *
 * @version 0.01
 *
 * @author Simon Holdsworth, IBM Corporation
 *
 * @see LogHandle
 */
class LogRecordHeader implements Serializable {

    /**
     * This constant holds the size of the LogRecordHeader object.
     */
    final static int SIZEOF = 3 * LogLSN.SIZEOF + 8;

    int    recordType   = 0;
    LogLSN currentLSN   = null;
    LogLSN previousLSN  = null;
    LogLSN nextLSN      = null;
    int    recordLength = 0;

    LogRecordHeader() {
        currentLSN = new LogLSN();
        previousLSN = new LogLSN();
        nextLSN = new LogLSN();
    }

    /**
     * Constructs a LogRecordHeader from the given byte array.
     *
     * @param bytes The array of bytes from which the object is to
     *              be constructed.
     * @param index The index in the array where copy is to start.
     *
     * @return
     *
     * @see
     */
    LogRecordHeader(byte[] bytes, int index) {
        recordType = (bytes[index++]&255) +
                     ((bytes[index++]&255) << 8) +
                     ((bytes[index++]&255) << 16) +
                     ((bytes[index++]&255) << 24);

        currentLSN  = new LogLSN(bytes,index);  index += LogLSN.SIZEOF;
        previousLSN = new LogLSN(bytes,index);  index += LogLSN.SIZEOF;
        nextLSN     = new LogLSN(bytes,index);  index += LogLSN.SIZEOF;

        recordLength = (bytes[index++]&255) +
                       ((bytes[index++]&255) << 8) +
                       ((bytes[index++]&255) << 16) +
                       ((bytes[index++]&255) << 24);
    }

    /**
     * Makes the target object a copy of the parameter.
     *
     * @param other  The object to be copied.
     *
     * @return
     *
     * @see
     */
    void copy( LogRecordHeader other) {
        recordType  = other.recordType;
        currentLSN.copy(other.currentLSN);
        previousLSN.copy(other.previousLSN);
        nextLSN.copy(other.nextLSN);
        recordLength = other.recordLength;
    }

    /**
     * Makes a byte representation of the LogRecordHeader.
     *
     * @param bytes The array of bytes into which the object is to be copied.
     * @param index The index in the array where copy is to start.
     *
     * @return  Number of bytes copied.
     *
     * @see
     */
    final int toBytes(byte[] bytes, int  index) {
        bytes[index++] = (byte) recordType;
        bytes[index++] = (byte)(recordType >> 8);
        bytes[index++] = (byte)(recordType >> 16);
        bytes[index++] = (byte)(recordType >> 24);
        index += currentLSN.toBytes(bytes,index);
        index += previousLSN.toBytes(bytes,index);
        index += nextLSN.toBytes(bytes,index);
        bytes[index++] = (byte) recordLength;
        bytes[index++] = (byte)(recordLength >> 8);
        bytes[index++] = (byte)(recordLength >> 16);
        bytes[index++] = (byte)(recordLength >> 24);

        return SIZEOF;
    }

    /**
     * This method is called to direct the object to format its state
     * to a String.
     *
     * @param
     *
     * @return  The formatted representation of the object.
     *
     * @see
     */
    public final String toString() {
        return "LRH(type="/*#Frozen*/ + recordType + ",curr="/*#Frozen*/ + currentLSN +
               ",prev="/*#Frozen*/ + previousLSN + ",next="/*#Frozen*/ + nextLSN +
               ",len="/*#Frozen*/ + recordLength + ")"/*#Frozen*/;
    }
}
