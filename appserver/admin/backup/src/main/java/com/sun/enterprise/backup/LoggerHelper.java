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

package com.sun.enterprise.backup;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

// Resource Bundle:
// com/sun/logging/enterprise/system/tools/deployment/backend/LogStrings.properties

class LoggerHelper
{
    private LoggerHelper()
    {
    }

    ///////////////////////////////////////////////////////////////////////////

    final static Logger get()
    {
        // the final should cause this to be inlined...
        return logger;
    }

    ///////////////////////////////////////////////////////////////////////////

    final static void setLevel(BackupRequest req)
    {
        // the final should cause this to be inlined...
        if(req.terse) {
            logger.setLevel(Level.WARNING);
        } else {
            logger.setLevel(Level.INFO);
        }

        /* test logging messages
                 String me = System.getProperty("user.name");
                if(me != null && me.equals("bnevins"))
                {
                        logger.finest("finest");
                        logger.finer("finer");
                        logger.fine("fine");
                        logger.info("info");
                        logger.warning("warning");
                        logger.severe("severe");
                }
         **/
    }

    ///////////////////////////////////////////////////////////////////////////
    ////////         Convenience methods        ///////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    final static void finest(String s) { logger.finest(s); }
    final static void finest(String s, Object o) { logger.log(Level.FINEST, s, new Object[] { o }); }
    final static void finer(String s) { logger.finer(s); }
    final static void finer(String s, Object o) { logger.log(Level.FINER, s, new Object[] { o }); }
    final static void fine(String s) { logger.fine(s); }
    final static void fine(String s, Object o) { logger.log(Level.FINE, s, new Object[] { o }); }
    final static void info(String s) { logger.info(s); }
    final static void info(String s, Object o) { logger.log(Level.INFO, s, new Object[] { o }); }
    final static void warning(String s) { logger.warning(s); }
    final static void warning(String s, Object o) { logger.log(Level.WARNING, s, new Object[] { o }); }
    final static void severe(String s) { logger.severe(s); }
    final static void severe(String s, Object o) { logger.log(Level.SEVERE, s, new Object[] { o }); }

    ///////////////////////////////////////////////////////////////////////////

    private static Logger        logger = null;

    static
    {
        try
        {
            //System.setProperty("java.util.logging.ConsoleHandler.level", Constants.logLevel);
            logger = Logger.getLogger("backup", Constants.loggingResourceBundle);

            // attach a handler that will at least be capable of spitting out FINEST messages
            // the Level of the Logger itself will determine what the handler actually gets...
            Handler h = new ConsoleHandler();
            h.setLevel(Level.FINEST);
            logger.addHandler(h);
        }
        catch(Throwable t)
        {
            try
            {
                logger = Logger.getLogger("backup");
                logger.warning("Couldn't create Backup Logger with a resource bundle.  Created a Logger without a Resource Bundle.");
            }
            catch(Throwable t2)
            {
                // now what?
            }
        }
    }
}

