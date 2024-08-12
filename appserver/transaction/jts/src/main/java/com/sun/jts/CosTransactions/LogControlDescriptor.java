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
// Module:      LogControlDescriptor.java
//
// Description: Log control record.
//
// Product:     com.sun.jts.CosTransactions
//
// Author:      Simon Holdsworth
//
// Date:        March, 1997
//----------------------------------------------------------------------------

package com.sun.jts.CosTransactions;

import java.io.Serializable;

/**A class containing control information relating to an open log file.
 *
 * @version 0.01
 *
 * @author Simon Holdsworth, IBM Corporation
 *
 * @see LogHandle
*/
//----------------------------------------------------------------------------
// CHANGE HISTORY
//
// Version By     Change Description
//   0.01  SAJH   Initial implementation.
//------------------------------------------------------------------------------

class LogControlDescriptor implements Serializable {
    /**This constant holds the size of the LogControlDescriptor object.
     */
    final static int SIZEOF = 3*LogLSN.SIZEOF;

    LogLSN headLSN = new LogLSN();
    LogLSN tailLSN = new LogLSN();
    LogLSN nextLSN = new LogLSN();

    /**Default LogControlDescriptor constructor.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    LogControlDescriptor() {
        headLSN = new LogLSN();
        tailLSN = new LogLSN();
        nextLSN = new LogLSN();
    }

    /**Constructs a LogControlDescriptor from the given byte array.
     *
     * @param bytes The array of bytes from which the object is to be constructed.
     * @param index The index in the array where copy is to start.
     *
     * @return
     *
     * @see
     */
    LogControlDescriptor( byte[] bytes,
                          int  index ) {
        headLSN = new LogLSN(bytes,index);  index += LogLSN.SIZEOF;
        tailLSN = new LogLSN(bytes,index);  index += LogLSN.SIZEOF;
        nextLSN = new LogLSN(bytes,index);  index += LogLSN.SIZEOF;
    }

    /**Makes a byte representation of the LogControlDescriptor.
     *
     * @param bytes The array of bytes into which the object is to be copied.
     * @param index The index in the array where copy is to start.
     *
     * @return  Number of bytes copied.
     *
     * @see
     */
    final int toBytes( byte[] bytes,
                       int  index ) {
        index += headLSN.toBytes(bytes,index);
        index += tailLSN.toBytes(bytes,index);
        index += nextLSN.toBytes(bytes,index);

        return SIZEOF;
    }

    /**This method is called to direct the object to format its state into a String.
     *
     * @param
     *
     * @return  The formatted representation of the object.
     *
     * @see
     */
    public final String toString() {
        return "LCD(head="/*#Frozen*/+headLSN+",tail="/*#Frozen*/+tailLSN+",next="/*#Frozen*/+nextLSN+")"/*#Frozen*/;
    }
}
