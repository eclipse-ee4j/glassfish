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
// Module:      LogCursor.java
//
// Description: Log file browsing operations.
//
// Product:     com.sun.jts.CosTransactions
//
// Author:      Simon Holdsworth
//
// Date:        March, 1997
//----------------------------------------------------------------------------

package com.sun.jts.CosTransactions;


import com.sun.enterprise.util.i18n.StringManager;

/**Contains information for an open cursor.
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

class LogCursor {
    private static final StringManager sm = StringManager.getManager(LogCursor.class);

    /**Constants used to identify browse direction.
     */
    final static int ASCENDING  = 0;
    final static int DESCENDING = 1;

    /**Internal instance members.
     */
    LogCursor  blockValid;
    LogControl logControl;
    LogHandle  logHandle;
    LogLSN     startLSN;
    LogLSN     endLSN;
    LogLSN     currentLSN;
    int        direction;
    boolean    symbolicsChecked;

    /**Creates a cursor for a browse in the given range.
     *
     * @param control   The LogControl object for the log instance.
     * @param handle    the handle for the log file.
     * @param startLSN  The LSN for the start of the browse.
     * @param endLSN    The LSN for the end of the browse.
     *
     * @return
     *
     * @see
     */
    LogCursor( LogControl control,
               LogHandle  handle,
               LogLSN     startLSN,
               LogLSN     endLSN ) {

        logHandle = handle;
        logControl = control;

        // Initialise the Log_CursorDescriptor block
        // - set start of range field to startLsn
        // - set end of range field to endLsn
        // - set current position to startLsn
        // - IF startLsn < endLsn
        //     set direction field to ASCENDING
        //   ELSE
        //     set direction field to DESCENDING
        // - set the file descriptor to the logHandle parameter
        // - set the BlockValid field to the address of the
        //   Log_FileDescriptor block

        this.startLSN = new LogLSN(startLSN);
        currentLSN = new LogLSN(startLSN);
        this.endLSN = new LogLSN(endLSN);
        if( startLSN.lessThan(endLSN) )
            direction = ASCENDING;
        else
            direction = DESCENDING;

        blockValid = this;
        symbolicsChecked = false;

    }

    /**Reads a record from the cursor.
     *
     * @param type     An array which will contain the type for record read.
     * @param LSNread  The LSN of the record read.
     *
     * @return  The bytes read.
     *
     * @exception LogException  The operation failed.
     *
     * @see
     */
    synchronized byte[] readCursor( int[/*1*/] type,
                                    LogLSN     LSNread )
        throws LogException {

        // Check BlockValid field in Log_CursorDescriptor block pointed to
        // by cursorId parameter, and ensure it is valid
        // IF not valid Log_CursorDescriptor
        //   Return LOG_INVALID_CURSOR

        if( blockValid != this )
            throw new LogException(null,LogException.LOG_INVALID_CURSOR,1);

        // Check BlockValid field in Log_FileDescriptor block pointed to
        // by field in Log_CursorDescriptor block and ensure it is valid

        if( logHandle == null || logHandle.blockValid != logHandle )
            throw new LogException(null,LogException.LOG_INVALID_CURSOR,2);

        // IF not LogInitialised Return LOG_NOT_INITIALISED

        if( !logControl.logInitialised )
            throw new LogException(null,LogException.LOG_NOT_INITIALISED,3);

        // IF the head LSN in the Log_FileDescriptor block = LOG_NULL_LSN
        //   Unlock the Log_FileDescriptor latch
        //   Return LOG_END_OF_CURSOR


        if( logHandle.logControlDescriptor.headLSN.isNULL() )
            throw new LogException(null,LogException.LOG_END_OF_CURSOR,5);

        // Check if the current position in Log_CursorDescriptor block
        // contains symbolic constants
        // IF equal to LOG_HEAD_LSN
        //   Replace it with head LSN value from Log_FileDescriptor block
        // ELSE
        //   IF equal to LOG_TAIL_LSN
        //     Replace it with tail LSN value from Log_FileDescriptor block

        if( !symbolicsChecked ) {
            if( currentLSN.equals(LogLSN.HEAD_LSN) )
                currentLSN.copy(logHandle.logControlDescriptor.headLSN);
            else if( currentLSN.equals(LogLSN.TAIL_LSN) )
                currentLSN.copy(logHandle.logControlDescriptor.tailLSN);

            symbolicsChecked = true;
        }

        // Check that the current position lies within the log record range
        // IF current position > head LSN (in Log_FileDescriptor block)
        //   Indicate 'end of cursor'
        // ELSE
        //   IF current position < tail LSN (indicating log truncate)
        //     Indicate 'end of cursor'
        // IF 'end of cursor' has been set
        //   Unlock the Log_FileDescriptor latch
        //   Return LOG_END_OF_CURSOR

        if( currentLSN.greaterThan(logHandle.logControlDescriptor.headLSN) ||
            currentLSN.lessThan(logHandle.logControlDescriptor.tailLSN) )
            throw new LogException(null,LogException.LOG_END_OF_CURSOR,6);

        // Now retrieve the record identified by the CurrentLogLSN value.
        // This is done in a loop, since we may have to do this more than once
        // if the 'current position' LSN points to a link record.

        boolean recordRetrieved = false;
        LogExtent logEDP = null;
        LogRecordHeader logRH = null;
        LogRecordEnding logRE = null;

        while( !recordRetrieved ) {

            // Determine the extent file containing the 'current position'
            // log record
            // Position the file pointer at the 'current position' using LSEEK
            // IF not successful allow the error to pass to the caller.
            //   Unlock the Log_FileDescriptor latch
            //   Return error from Log_PositionFilePointer function

            logEDP = logHandle.positionFilePointer(currentLSN,0,LogExtent.ACCESSTYPE_READ);

            // Issue READ to get the log record header
            // IF not successful
            //   set last access as unknown as we cannot be sure of the
            //   new cursor position.
            //   Unlock the Log_FileDescriptor latch
            //   return LOG_READ_FAILURE

            byte[] headerBytes = new byte[LogRecordHeader.SIZEOF];
            int bytesRead = 0;
            try {
                bytesRead = logEDP.fileHandle.fileRead(headerBytes);
            } catch( LogException le ) {
                logEDP.lastAccess = LogExtent.ACCESSTYPE_UNKNOWN;
                throw new LogException(LogException.LOG_READ_FAILURE, 8,
                        sm.getString("jts.log_read_header_failed"), le);
            }

            logRH = new LogRecordHeader(headerBytes,0);
            logEDP.cursorPosition += bytesRead;

            // IF the lsn value in the record header is not same as the
            // 'current cursor position' lsn
            //   Unlock the Log_FileDescriptor latch
            //   Return LOG_CORRUPTED

            if( !logRH.currentLSN.equals(currentLSN) )
                throw new LogException(null,LogException.LOG_CORRUPTED,9);

            // Check the record header in case it's a link record thats been read in
            // IF it is a link record
            //   IF cursor is ASCENDING
            //     Set 'current position' to next LSN in link record
            //   ELSE
            //     Set 'current position' to previous LSN in link record
            //   Iterate LOOP

            if( logRH.recordType == LogHandle.LINK )
                if( direction == ASCENDING )
                    currentLSN.copy(logRH.nextLSN);
                else
                    currentLSN.copy(logRH.previousLSN);
            else
                recordRetrieved = true;
        }

        // Set up a 2-element iovec array so that the record data
        // and record ending are separated during the READV

        byte[][] readVect = new byte[2][];
        readVect[0] = new byte[logRH.recordLength];
        readVect[1] = new byte[LogRecordEnding.SIZEOF];

        // Issue READV to get the log record
        // IF not successful
        //   Unlock the Log_FileDescriptor latch
        //   Return LOG_READ_FAILURE

        int bytesRead = 0;
        try {
            bytesRead = logEDP.fileHandle.readVector(readVect);
        } catch( LogException le ) {
            logEDP.lastAccess = LogExtent.ACCESSTYPE_UNKNOWN;
            throw new LogException(le.errorCode, 11, sm.getString("jts.log_readvector_failed"), le);
        }
        logEDP.cursorPosition += bytesRead;

        logRE = new LogRecordEnding(readVect[1],0);

        // IF the lsn contained in the record ending is not the same as the
        // 'current cursor position' lsn
        //   Unlock the Log_FileDescriptor latch
        //   Return LOG_CORRUPTED

        if( !logRE.currentLSN.equals(currentLSN) )
            throw new LogException(null,LogException.LOG_CORRUPTED,12);

        // Update the Log_CursorDescriptor 'current position'
        // IF cursor is ASCENDING
        //   Set 'current position' to next LSN in log record
        // ELSE
        //   Set 'current position' to previous LSN in log record

        if( direction == ASCENDING )
            currentLSN.copy(logRH.nextLSN);
        else
            currentLSN.copy(logRH.previousLSN);

        // Copy the record type and lsn values from the
        // Log_RecordHeader to callers recordTypeP and lsnP
        // parameters respectively

        type[0] = logRH.recordType;
        LSNread.copy(logRH.currentLSN);

        // Return LOG_SUCCESS

        return readVect[0];
    }
}
