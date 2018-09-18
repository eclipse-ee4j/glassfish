/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.logging;

import java.util.logging.Level;

public class LogLevel extends Level {

    /**
     * Serial version UID constant
     */
    private static final long serialVersionUID = 204716607972038217L;

    /**
     * Level constant for Alert.
     */
    public static final int ALERT_INT = 1050;

    /**
     * Level constant for Emergency
     */
    public static final int EMERGENCY_INT = 1100;

    /**
     * Log level to indicate a system-wide failure condition.
     */
    public static final Level EMERGENCY = new LogLevel("EMERGENCY",
            EMERGENCY_INT);

    /**
     * Log level to indicate a failure condition in one of the subsystems.
     */
    public static final Level ALERT = new LogLevel("ALERT", ALERT_INT);

    /**
     * Constructor to initialize a custom Level instance
     * 
     * @param name
     * @param value
     */
    protected LogLevel(String name, int value) {
        super(name, value);
    }

}
