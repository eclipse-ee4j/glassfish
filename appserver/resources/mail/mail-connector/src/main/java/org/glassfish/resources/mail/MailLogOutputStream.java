/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resources.mail;

import java.util.logging.Level;

import org.glassfish.logging.annotation.LoggerInfo;

/**
 * Capture Jakarta Mail debug output.
 */
public class MailLogOutputStream extends LogOutputStream {
    @LoggerInfo(subsystem = "MAIL", description = "Jakarta Mail Logger", publish=true)
    private static final String MAIL_DOMAIN = "jakarta.mail";

    public MailLogOutputStream() {
        super(MAIL_DOMAIN, Level.FINE);
    }

    /**
     * All output except detained protocol traces starts with "DEBUG".
     * Log protocol traces at lower level.
     * <p/>
     * NOTE: protocol trace output can include lines that start with
     * "DEBUG" so this isn't perfect.
     */
    protected void log(String msg) {
        if (msg.startsWith("DEBUG"))
            logger.log(Level.FINE, msg);
        else
            logger.log(Level.FINER, msg);
    }
}
