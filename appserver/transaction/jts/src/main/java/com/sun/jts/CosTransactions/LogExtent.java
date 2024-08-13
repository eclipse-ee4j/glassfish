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
// Module:      LogExtent.java
//
// Description: Log extent file interface.
//
// Product:     com.sun.jts.CosTransactions
//
// Author:      Simon Holdsworth
//
// Date:        March, 1997
//----------------------------------------------------------------------------

package com.sun.jts.CosTransactions;

// Import required definitions.
import java.io.File;

//------------------------------------------------------------------------------
// LogExtent class
//------------------------------------------------------------------------------
/**A structure containing information for an open log file extent.
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

class LogExtent extends Object {

    // The type of access last made to an extent file is stored in the extent
    // descriptor block. This is used to save doing uneccessary fseeks.

    /**Type of last access is unknown (forces fseek to required cursor pos'n)
     */
    final static int ACCESSTYPE_UNKNOWN = 0;

    /**Last access was for reading
     */
    final static int ACCESSTYPE_READ = 1;

    /**Last access was for writing
     */
    final static int ACCESSTYPE_WRITE = 2;

    /**The radix used to convert extent numbers to strings.
     */
    final static int EXTENT_RADIX      = 36;

    /**The maximum number of extent files that can be allocated to a single
     * log at any one time. Extent names are made up of <logfilename>.nnn
     * Hence this value is restricted by the .nnn extension (3 characters
     * only, to support the FAT file system.
     */
    final static int MAX_NO_OF_EXTENTS = EXTENT_RADIX*EXTENT_RADIX*EXTENT_RADIX;

    /**This value is used to validate the LogExtent object.
     */
    LogExtent blockValid = null;

    /**The extent number.
     */
    int extentNumber = -1;

    /**The file handle for the log extent file.
     */
    LogFileHandle  fileHandle = null;

    /**The file for the log extent file.
     */
    File file = null;

    /**Indicates whether any information has been written since the last force.
     */
    boolean writtenSinceLastForce = false;

    /**The cursor position in the log extent.
     */
    int cursorPosition = 0;

    /**The last type of access to the extent.
     */
    int lastAccess = ACCESSTYPE_UNKNOWN;

    /**LogExtent constructor
     *
     * @param extent   The number of the extent.
     * @param extentFH The handle of the extent file.
     *
     * @return
     *
     * @see
     */
    LogExtent( int           extent,
               LogFileHandle extentFH,
               File          extentFile ) {
        extentNumber = extent;
        fileHandle = extentFH;
        file = extentFile;
    }

    /**Default LogExtent destructor.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    public void doFinalize() {
        try {
            fileHandle.destroy();
        } catch( Throwable e ) {};

        blockValid = null;
        file = null;
    }

    /**Modulates the extent number using the maximum extent number.
     *
     * @param ext  The extent number
     *
     * @return  The modulated extent number.
     *
     * @see
     */
    final static int modExtent( int ext ) {
        return (ext % MAX_NO_OF_EXTENTS);
    }
}
