/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.connectors.config.validators;

import com.sun.enterprise.util.i18n.StringManager;

/**
 * Enum containing the different custom validation failures for a connection
 * pool. The message strings for the different validation scenarios are
 * used for interpolation and display when there is a failure in
 * custom validation.
 *
 * @author Shalini M
 */
public enum ConnectionPoolErrorMessages {

    MAX_STEADY_INVALID ("connpool.maxpoolsize.invalid"),

    STMT_WRAPPING_DISABLED ("connpool.stmtwrapping.disabled"),

    RES_TYPE_MANDATORY ("connpool.restype.mandatory"),

    TABLE_NAME_MANDATORY ("connpool.validation.table.name.mandatory"),

    CUSTOM_VALIDATION_CLASS_NAME_MANDATORY("connpool.custom.validation.classname.mandatory");

    private final StringManager localStrings = StringManager.getManager(getClass());

    private ConnectionPoolErrorMessages(String messagekey) {
        this.message = localStrings.getString(messagekey);
    }
    private final String message;

    public String toString() {
        return message;
    }
}
