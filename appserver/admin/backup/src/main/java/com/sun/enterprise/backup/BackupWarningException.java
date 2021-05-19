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
 * BackupWarningException.java
 *
 * Created on April 1, 2004, 6:23 PM
 */

package com.sun.enterprise.backup;

/**
 *
 * @author  bnevins
 * Problem -- some "errors" should not be handled by CLI as "errors", but as warnings.
 * However, the Exception throwing mechanism is the non-kludgiest way to get the
 * message back to the command handler in CLI.
 * Thus this class.
 */
public class BackupWarningException extends BackupException
{
    /**
     * Constructs a BackupWarningException with a possibly i18n'd detail message.
     * @param s the detail message which is first checked for as a key for an i18n string.
     * If not found it will be used as the message itself.
     */
    public BackupWarningException(String s) {
        super(s);
    }

    /**
     * @param s the detail message which is first checked for as a key for an i18n string.
     * If not found it will be used as the message itself.
     * @param o the parameter for the recovered i18n string. I.e. "{0}" will be
     * replaced with o.toString().  If there is no i18n string located
     * o will be ignored.
     */
    public BackupWarningException(String s, Object o) {
        super(s, o);
    }
}
