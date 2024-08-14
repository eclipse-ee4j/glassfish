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

/*
 * ErrorMsg.java
 *
 * Created on November 12, 2001
 */

package com.sun.jdo.spi.persistence.support.ejb.ejbqlc;

import com.sun.jdo.spi.persistence.utility.logging.Logger;

import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;

/**
 * This is a helper class to report error messages from the EJBQL compiler.
 * @author  Michael Bouschen
 * @author  Shing Wai Chan
 */
public class ErrorMsg
{
    /** I18N support. */
    private final static ResourceBundle msgs = I18NHelper.loadBundle(
        ErrorMsg.class);

    /** The logger */
    private static Logger logger = LogHelperQueryCompilerEJB.getLogger();

    /**
     * This method throws an EJBQLException indicating an user error.
     * @param line line number
     * @param col column number
     * @param text error message
     * @exception EJBQLException describes the user error.
     */
    public static void error(int line, int col, String text)
        throws EJBQLException
    {
        EJBQLException ex = null;
        if (line > 1) {
            // include line and column info
            Object args[] = {new Integer(line), new Integer(col), text};
            ex = new EJBQLException(I18NHelper.getMessage(
                msgs, "EXC_PositionInfoMsgLineColumn", args)); //NOI18N
        }
        else if (col > 0) {
            // include column info
            Object args[] = {new Integer(col), text};
            ex = new EJBQLException(I18NHelper.getMessage(
                msgs, "EXC_PositionInfoMsgColumn", args)); //NOI18N
        }
        else {
            ex = new EJBQLException(I18NHelper.getMessage(
                msgs, "EXC_PositionInfoMsg", text)); //NOI18N
        }
        throw ex;
    }

    /**
     * This method throws an EJBQLException indicating an user error.
     * @param text error message
     * @param cause the cause of the error
     * @exception EJBQLException describes the user error.
     */
    public static void error(String text, Throwable cause)
        throws EJBQLException
    {
        throw new EJBQLException(text, cause);
    }

    /**
     * This method throws an EJBQLException indicating an user error.
     * @param text error message
     * @exception EJBQLException describes the user error.
     */
    public static void error(String text)
        throws EJBQLException
    {
        throw new EJBQLException(text);
    }

    /**
     * This method throws an UnsupportedOperationException indicating an
     * unsupported feature.
     * @param line line number
     * @param col column number
     * @param text message
     * @exception UnsupportedOperationException describes the unsupported
     * feature.
     */
    public static void unsupported(int line, int col, String text)
        throws UnsupportedOperationException
    {
        UnsupportedOperationException ex;
        if (line > 1)
        {
            // include line and column info
            Object args[] = {new Integer(line), new Integer(col), text};
            ex = new UnsupportedOperationException(I18NHelper.getMessage(
                msgs, "EXC_PositionInfoMsgLineColumn", args)); //NOI18N
        }
        else if (col > 0) {
            // include column info
            Object args[] = {new Integer(col), text};
            ex = new UnsupportedOperationException(I18NHelper.getMessage(
                msgs, "EXC_PositionInfoMsgColumn", args)); //NOI18N
        }
        else {
            Object args[] = {text};
            ex = new UnsupportedOperationException(I18NHelper.getMessage(
                msgs, "EXC_PositionInfoMsg", args)); //NOI18N
        }
        throw ex;
    }

    /**
     * This method is called in the case of an fatal internal error.
     * @param text error message
     * @exception EJBQLException describes the fatal internal error.
     */
    public static void fatal(String text)
        throws EJBQLException
    {
        throw new EJBQLException(I18NHelper.getMessage(
            msgs, "ERR_FatalInternalError", text)); //NOI18N
    }

    /**
     * This method is called in the case of an fatal internal error.
     * @param text error message
     * @param nested the cause of the error
     * @exception EJBQLException describes the fatal internal error.
     */
    public static void fatal(String text, Throwable nested)
        throws EJBQLException
    {
        throw new EJBQLException(I18NHelper.getMessage(
            msgs, "ERR_FatalInternalError", text), nested); //NOI18N
    }

    /**
     * This method is called when we want to log an exception in a given level.
     * Note that all other methods in this class do not log a stack trace.
     * @param level log level
     * @param text error message
     * @param nested the cause of the error
     * @exception EJBQLException describes the fatal internal error.
     */
    public static void log(int level, String text, Throwable nested)
        throws EJBQLException
    {
        logger.log(level, text, nested);
        throw new EJBQLException(text, nested);
    }
}
