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
 * Created on April 3, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore.query.jqlc;

import com.sun.jdo.api.persistence.support.JDOFatalInternalException;
import com.sun.jdo.api.persistence.support.JDOQueryException;
import com.sun.jdo.api.persistence.support.JDOUnsupportedOptionException;
import com.sun.jdo.spi.persistence.utility.logging.Logger;

import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;

/**
 *
 * @author  Michael Bouschen
 * @version 0.1
 */
public class ErrorMsg
{
    /**
     *
     */
    protected String context = null;

    /**
     * I18N support
     */
    protected final static ResourceBundle messages =
      I18NHelper.loadBundle(ErrorMsg.class);

    /** The logger */
    private static Logger logger = LogHelperQueryCompilerJDO.getLogger();

    /**
     *
     */
    public String getContext()
    {
        return context;
    }

    /**
     *
     */
    public void setContext(String name)
    {
        context = name;
    }

    /**
     * Indicates an error situation.
     * @param line line number
     * @param col column number
     * @param msg error message
     */
    public void error(int line, int col, String msg)
        throws JDOQueryException
    {
        JDOQueryException ex;
        if (line > 1)
        {
            // include line and column info
            Object args[] = {context, new Integer(line), new Integer(col), msg};
            ex = new JDOQueryException(I18NHelper.getMessage(
                messages, "jqlc.errormsg.generic.msglinecolumn", args)); //NOI18N
        }
        else if (col > 0)
        {
            // include column info
            Object args[] = {context, new Integer(col), msg};
            ex = new JDOQueryException(I18NHelper.getMessage(
                messages, "jqlc.errormsg.generic.msgcolumn", args)); //NOI18N
        }
        else
        {
            Object args[] = {context, msg};
            ex = new JDOQueryException(I18NHelper.getMessage(
                messages, "jqlc.errormsg.generic.msg", args)); //NOI18N
        }
        logger.throwing("jqlc.ErrorMsg", "error", ex);
        throw ex;
    }

    /**
     * Indicates that a feature is not supported by the current release.
     * @param line line number
     * @param col column number
     * @param msg message
     */
    public void unsupported(int line, int col, String msg)
        throws JDOUnsupportedOptionException
    {
        JDOUnsupportedOptionException ex;
        if (line > 1)
        {
            // include line and column info
            Object args[] = {context, new Integer(line), new Integer(col), msg};
            ex = new JDOUnsupportedOptionException(I18NHelper.getMessage(
                messages, "jqlc.errormsg.generic.msglinecolumn", args)); //NOI18N
        }
        else if (col > 0)
        {
            // include column info
            Object args[] = {context, new Integer(col), msg};
            ex = new JDOUnsupportedOptionException(I18NHelper.getMessage(
                messages, "jqlc.errormsg.generic.msgcolumn", args)); //NOI18N

        }
        else
        {
            Object args[] = {context, msg};
            ex = new JDOUnsupportedOptionException(I18NHelper.getMessage(
                messages, "jqlc.errormsg.generic.msg", args)); //NOI18N
        }
        logger.throwing("jqlc.ErrorMsg", "unsupported", ex);
        throw ex;
    }

    /**
     * Indicates a fatal situation (implementation error).
     * @param msg error message
     */
    public void fatal(String msg)
        throws JDOFatalInternalException
    {
        JDOFatalInternalException ex = new JDOFatalInternalException(msg);
        logger.throwing("jqlc.ErrorMsg", "fatal", ex);
        throw ex;
    }

    /**
     * Indicates a fatal situation (implementation error).
     * @param msg error message
     */
    public void fatal(String msg, Exception nested)
        throws JDOFatalInternalException
    {
        JDOFatalInternalException ex = new JDOFatalInternalException(msg, nested);
        logger.throwing("jqlc.ErrorMsg", "fatal", ex);
        throw ex;
    }
}


