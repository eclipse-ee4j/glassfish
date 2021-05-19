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
 * BackupException.java
 *
 * Created on January 21, 2004, 3:51 PM
 */


package com.sun.enterprise.backup;


/**
 * Backup-Restore <strong>guarantees</strong> that this will be the one and only one kind of
 * Exception that will ever be thrown.  All fatal errors will
 * result in this Exception being thrown.  This is a checked Exception so callers
 * will be forced to deal with it.  <p>
 * the class features built-in i18n.  I.e. any String passed to a BackupException
 * constructor will first be used as a key into the i18n Strings.  If it is not
 * found, the String itself will be used as the messsage.
 *
 * @author bnevins
 */

public class BackupException extends Exception
{
    /**
     * Constructs a BackupException with a possibly i18n'd detail message.
     * @param s the detail message which is first checked for as a key for an i18n string.
     * If not found it will be used as the message itself.
     */
    public BackupException(String s)
    {
        super(StringHelper.get(s));
    }

    /**
     * @param s the detail message which is first checked for as a key for an i18n string.
     * If not found it will be used as the message itself.
     * @param o the parameter for the recovered i18n string. I.e. "{0}" will be
     * replaced with o.toString().  If there is no i18n string located
     * o will be ignored.
     */
    public BackupException(String s, Object o)
    {
        super(StringHelper.get(s, o));
    }

    /**
     * @param s the detail message which is first checked for as a key for an i18n string.
     * If not found it will be used as the message itself.
     * @param t the cause.
     */
    public BackupException(String s, Throwable t)
    {
        super(StringHelper.get(s), t);
    }

    /**
     * @param s the detail message which is first checked for as a key for an i18n string.
     * If not found it will be used as the message itself.
     * @param t the cause.
     * @param o the parameter for the recovered i18n string. I.e. "{0}" will be
     * replaced with o.toString().  If there is no i18n string located
     * o will be ignored.
     */
    public BackupException(String s, Throwable t, Object o)
    {
        super(StringHelper.get(s, o), t);
    }
}
