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
// Module:      LogRestartDescriptor.java
//
// Description: Log restart record.
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
 * A class containing restart information.
 *
 * @version 0.01
 *
 * @author Simon Holdsworth, IBM Corporation
 *
 * @see LogHandle
 */
class LogRestartDescriptor implements Serializable {
    /**
     *This constant holds the size of the LogRestartDecriptor object.
     */
    final static int SIZEOF = 12;

    int restartValid      = 0;
    int restartDataLength = 0;
    int timeStamp         = 0;

    /**
     * Default LogRestartDescriptor constructor.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    LogRestartDescriptor() {}

    /**
     * Constructs a LogRestartDescriptor from the given byte array.
     *
     * @param bytes The array of bytes from which the object is
     *              to be constructed.
     * @param index The index in the array where copy is to start.
     *
     * @return
     *
     * @see
     */
    LogRestartDescriptor(byte[] bytes, int  index) {
        restartValid =  (bytes[index++]&255) +
                        ((bytes[index++]&255) << 8) +
                        ((bytes[index++]&255) << 16) +
                        ((bytes[index++]&255) << 24);

        restartDataLength = (bytes[index++]&255) +
                            ((bytes[index++]&255) << 8) +
                            ((bytes[index++]&255) << 16) +
                            ((bytes[index++]&255) << 24);

        timeStamp = (bytes[index++]&255) +
                    ((bytes[index++]&255) << 8) +
                    ((bytes[index++]&255) << 16) +
                    ((bytes[index++]&255) << 24);
    }

    /**
     * Makes a byte representation of the LogRestartDescriptor.
     *
     * @param bytes The array of bytes into which the object is to be copied.
     * @param index The index in the array where copy is to start.
     *
     * @return  Number of bytes copied.
     *
     * @see
     */
    final int toBytes(byte[] bytes, int index) {
        bytes[index++] = (byte) restartValid;
        bytes[index++] = (byte)(restartValid >> 8);
        bytes[index++] = (byte)(restartValid >> 16);
        bytes[index++] = (byte)(restartValid >> 24);
        bytes[index++] = (byte) restartDataLength;
        bytes[index++] = (byte)(restartDataLength >> 8);
        bytes[index++] = (byte)(restartDataLength >> 16);
        bytes[index++] = (byte)(restartDataLength >> 24);
        bytes[index++] = (byte) timeStamp;
        bytes[index++] = (byte)(timeStamp >> 8);
        bytes[index++] = (byte)(timeStamp >> 16);
        bytes[index++] = (byte)(timeStamp >> 24);

        return SIZEOF;
    }

    /**
     * Determines whether the target object is equal to the parameter.
     *
     * @param other  The other LogRestartDescriptor to be compared.
     *
     * @return  Indicates whether the objects are equal.
     *
     * @see
     */
    final boolean equals(LogRestartDescriptor other) {
        return (restartValid      == other.restartValid &&
                restartDataLength == other.restartDataLength &&
                timeStamp         == other.timeStamp);
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
        return "LRD(valid="/*#Frozen*/ + restartValid +
               ",len="/*#Frozen*/ + restartDataLength +
               ",time="/*#Frozen*/ + timeStamp + ")"/*#Frozen*/;
    }
}
