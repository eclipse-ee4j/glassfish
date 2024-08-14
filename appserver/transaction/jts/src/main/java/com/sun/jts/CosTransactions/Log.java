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
// Module:      Log.java
//
// Description: Transaction state logger.
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

import java.util.logging.Level;
import java.util.logging.Logger;


/**The Log class provides operations that control the physical log
 * as an entity versus the individual LogFiles that form the log. It supports
 * the initialisation, opening and termination of the log. Different physical
 * logs can be placed on the system with only minor changes to the methods
 * contained in this class.
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

class Log {

    /**A reference to the LogControl object.
     */
    private LogControl logControl = null;

    /**The log path.
     */
    // private static String logPath = null;
    private String logPath = null;
    /*
        Logger to log transaction messages
    */
    static Logger _logger = LogDomains.getLogger(Log.class, LogDomains.TRANSACTION_LOGGER);

    /**Default Log constructor.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    Log() {
        // We need to ensure that messaging is initialised as this may be called
        // prior to SOMTR_Init.

        // Initialise the instance variables.
        logPath = LogControl.getLogPath();

    }


    Log(String logPath) {
        this.logPath = logPath;
    }

    /**Initialises the log.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    boolean initialise() {
        boolean result = true;

        // Call the initialize operation for the log

        logControl = new LogControl();
        logControl.initLog(false,false,logPath);

        return result;
    }


    /**Opens the log for the given server.
     * <p>
     * The given LogSOS object, if any, will be called in the event of the log
     * going short-on-storage. A LogFile object reference is returned that is used
     * for operations on the specific portion of the log.
     *
     * @param serverName  The name of the server whose log file is being opened.
     * @param upcall      The object which will handle upcalls from the log.
     *
     * @return  The object representing the physical log file.
     *
     * @see
     */
    LogFile open( String          serverName,
                  LogUpcallTarget upcall ) {

        LogFile logFile = null;

        boolean[] newLog = new boolean[1];  newLog[0] = true;

        // Open the log using the server name.

        try {
            LogHandle handle = logControl.openFile(serverName,upcall,null,newLog);

            // Create a new LogFile object with the handle to represent the open log.

            logFile = new LogFile(handle);
        }

        // If the log open failed, report the error.

        catch( LogException le ) {
            _logger.log(Level.SEVERE,"jts.log_error",le);
             String msg = LogFormatter.getLocalizedMessage(_logger,"jts.log_error",
                         new java.lang.Object[] {le.toString()});
             throw  (org.omg.CORBA.INTERNAL) (new org.omg.CORBA.INTERNAL(msg)).initCause(le);
        }

        return logFile;
    }

    /**Terminates the log.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    boolean terminate() {

        boolean result = true;

        // No special action needed after the close for the logger

        return result;
    }

    /**Determines whether a log file exists for the given server.
     * <p>
     * This method may be used without initialising the Log object to determine
     * whether recovery should be performed, without initialising the log or the OTS.
     *
     * @param String
     *
     * @return
     *
     * @see
     */
    static boolean checkFileExists( String serverName ) {
        // Check whether the file exists.
        boolean exists = false;

        if( serverName != null ) {
            String logPath = LogControl.getLogPath();
            exists = LogControl.checkFileExists(serverName,logPath);
        }

        return exists;
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
        //! somtrDUMP_OBJECT_HEADER;

        // Dump all of the instance variables in the LogFile object, without going
        // any further down object references.

        logControl.dump();
    }
}
