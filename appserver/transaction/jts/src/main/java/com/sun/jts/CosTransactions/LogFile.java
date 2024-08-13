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
// Module:      LogFile.java
//
// Description: Log File interface.
//
// Product:     com.sun.jts.CosTransactions
//
// Author:      Simon Holdsworth
//
// Date:        March, 1997
//----------------------------------------------------------------------------

package com.sun.jts.CosTransactions;

import com.sun.jts.utils.LogFormatter;
import com.sun.logging.LogDomains;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**The LogFile interface provides operations that control the
 * individual log entries that make up the physical log. It allows writing to
 * the log and reading from the log, along with the capability to close a
 * portion of the log. Different physical logs can be placed on the system
 * with only minor changes to the methods contained in this class.
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

class LogFile {

    /**Constants for write types.
     */
    final static int UNFORCED = 0;
    final static int FORCED   = 1;

    /**Constants for log record types.
     */
    final static int NORMAL         = 0;
    final static int KEYPOINT_START = 1;
    final static int KEYPOINT_END   = 2;
    final static int REWRITE        = 3;

    /*
        Logger to log transaction messages
    */

    static Logger _logger = LogDomains.getLogger(LogFile.class, LogDomains.TRANSACTION_LOGGER);

    /**The handle of the log file.
     */
    LogHandle handle = null;

    /**LogFile constructor.
     *
     * @param LogHandle
     *
     * @return
     *
     * @see
     */
    LogFile( LogHandle handle ) {

        // Set up the instance variables to those values passed in.

        this.handle = handle;

    }

    /**Writes a log record to the physical log.
     * <p>
     * Supports either a force or unforced option with force requiring an immediate
     * write to the log and unforced keeping the data until a force is done somewhere
     * else in the log service.
     * <p>
     * The LSN of the written log record is an output parameter.
     * <p>
     * Returns true if the write completed successfully and false if the write
     * did not complete.
     *
     * @param writeType   Forced/non-forced write indicator.
     * @param record      Log record data.
     * @param recordType  Log record type.
     * @param recordLSN   LSN of the written record.
     *
     * @return
     *
     * @see
     */
    synchronized boolean write( int    writeType,
                                byte[] record,
                                int    recordType,
                                LogLSN recordLSN ) {

        boolean result = true;

        // Write the record.
        // Set the result based on return code from log write.

        try {
            LogLSN resultLSN = handle.writeRecord(record,recordType,
                                                  (writeType==LogFile.FORCED ? LogHandle.FORCE : LogHandle.BUFFER));
            if( recordLSN != null )
                recordLSN.copy(resultLSN);
        } catch( LogException le ) {
            _logger.log(Level.SEVERE,"jts.log_error",le.toString());
             String msg = LogFormatter.getLocalizedMessage(_logger,"jts.log_error",
                                        new java.lang.Object[] {le.toString()});
             throw (org.omg.CORBA.INTERNAL) (new org.omg.CORBA.INTERNAL(msg)).initCause(le);
            //if( recordLSN != null )
            //recordLSN.copy(LogLSN.NULL_LSN);
            //result = false;
        }

        return result;
    }

    /**Informs the log that all log records older than the one with the given LSN
     * are no longer required.
     * <p>
     * The checkpoint marks the point where log processing will begin in the event
     * of recovery processing. This will generally correspond to the last record
     * before a successful keypoint.
     *
     * @param firstLSN
     *
     * @return
     *
     * @see
     */
    synchronized boolean checkpoint( LogLSN firstLSN ) {

        boolean result = true;
        LogLSN checkLSN;

        // If the LSN passed in is NULL, assume it means the head.

        if( firstLSN.isNULL() )
            checkLSN = new LogLSN(LogLSN.HEAD_LSN);
        else
            checkLSN = new LogLSN(firstLSN);

        // Checkpoint the log.

        try {
            handle.checkLSN(checkLSN);
            handle.truncate(checkLSN,LogHandle.TAIL_NOT_INCLUSIVE);
        } catch( LogException le ) {
            result = false;
        }

        return result;
    }

    /**Writes the given information in the restart record for the log.
     * <p>
     *
     * @param record  The information to be written.
     *
     * @return  Indicates success of the operation.
     *
     * @see
     */
    synchronized boolean writeRestart( byte[] record ) {

        boolean result = false;

        // Write the restart information.

        try {
            handle.writeRestart(record);
            result = true;
        } catch( LogException le ) {
            result = false;
        }

        return result;
    }

    /**Reads the restart record from the log.
     * <p>
     *
     * @param
     *
     * @return  The restart record.
     *
     * @see
     */
    synchronized byte[] readRestart() {
        byte[] result = null;

        // Write the restart information.

        try {
            result = handle.readRestart();
        } catch( LogException le ) {
        }

        return result;
    }

    /**Closes the portion of the log defined by the LogFile object reference.
     * <p>
     * Deletes the associated logfile if requested.
     *
     * @param deleteFile
     *
     * @return
     *
     * @see
     */
    synchronized boolean close( boolean deleteFile ) {

        boolean result = true;

        // Call to close the physical log.

        try {
            handle.closeFile(deleteFile);
        } catch( LogException le ) {
            result = false;
        }

        return result;
    }

    /**Returns all of the log records written to the log since the last checkpoint.
     * <p>
     * The caller is responsible for freeing the sequence storage.
     * <p>
     * If the log is empty, an empty sequence is returned.
     * <p>
     * The result is returned in a Vector as we do not know ahead of time how
     * many log records there are.
     *
     * @param
     *
     * @return  The log records.
     *
     * @see
     */
    synchronized Vector getLogRecords() {
        Vector logRecords = new Vector();
        boolean keypointEndFound = false;
        LogCursor logCursor;

        // Open a cursor for use with the log.

        try {
            logCursor = handle.openCursor(LogLSN.HEAD_LSN,LogLSN.TAIL_LSN);
        } catch( LogException le ) {

            return new Vector();
        }

        // Read each log record from the physical log and place in temporary queue.

        try {
            LogLSN lsn = new LogLSN();
            int[] recordType = new int[1];

            for(;;) {
                byte[] logRecord = logCursor.readCursor(recordType,lsn);

                // Process the log record depending on its type.

                switch( recordType[0] ) {

                    // If the record is a keypoint start, and we have found the end of the
                    // keypoint, then we can stop processing the log.  If the end has not been
                    // found then a failure must have occurred during the keypoint operation,
                    // so we must continue to process the log.
                    // We do not do anything with the contents of the keypoint start record.

                case LogFile.KEYPOINT_START :
                    if( keypointEndFound )
                        throw new LogException(null,LogException.LOG_END_OF_CURSOR,2);
                    break;

                    // If the record is a keypoint end, remember this so that we can stop when
                    // we find the start of the keypoint.
                    // We do not do anything with the contents of the keypoint end record.

                case LogFile.KEYPOINT_END :
                    keypointEndFound = true;
                    break;

                    // For a normal log record, add the records to the list.
                    // For a rewritten record, only add the record to the list if the
                    // keypoint end record has been found.

                case LogFile.NORMAL :
                case LogFile.REWRITE :
                    if( (recordType[0] == LogFile.NORMAL) || keypointEndFound )
                        logRecords.addElement(logRecord);
                    break;

                    // Any other type of log record is ignored.

                default :
                    break;
                }
            }
        } catch( LogException le ) {

            // If any exception other that END_OF_CURSOR was thrown, then return an empty
            // list.

            if( le.errorCode != LogException.LOG_END_OF_CURSOR ) {
                return new Vector();
            }
        }

        // Close the cursor.

        try {
            handle.closeCursor(logCursor);
        } catch( LogException le ) {
        }

        return logRecords;
    }

    /**Dumps the state of the object.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    void dump() {
        //! somtrDump_OBJECT_HEADER;

        // Dump all of the instance variables in the LogFile object, without going
        // any further down object references.

    }
}
