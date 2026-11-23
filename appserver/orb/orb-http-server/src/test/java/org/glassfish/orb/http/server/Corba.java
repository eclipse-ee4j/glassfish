/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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


package org.glassfish.orb.http.server;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

/**
 * The CORBA system exceptions the container's IIOP mapping produces, built the
 * way it builds them: with a minor code and a completion status, and the
 * original exception attached as the cause afterwards.
 */
final class Corba {

    private Corba() {
    }

    static NO_PERMISSION noPermission(String message, Throwable cause) {
        return caused(new NO_PERMISSION(message, 0, CompletionStatus.COMPLETED_MAYBE), cause);
    }

    static TRANSACTION_ROLLEDBACK rolledBack(String message, Throwable cause) {
        return caused(new TRANSACTION_ROLLEDBACK(message, 0, CompletionStatus.COMPLETED_MAYBE), cause);
    }

    static TRANSACTION_REQUIRED transactionRequired(String message, Throwable cause) {
        return caused(new TRANSACTION_REQUIRED(message, 0, CompletionStatus.COMPLETED_MAYBE), cause);
    }

    static OBJECT_NOT_EXIST objectNotExist(String message, Throwable cause) {
        return caused(new OBJECT_NOT_EXIST(message, 0, CompletionStatus.COMPLETED_MAYBE), cause);
    }

    private static <T extends Throwable> T caused(T exception, Throwable cause) {
        if (cause != null) {
            exception.initCause(cause);
        }
        return exception;
    }
}
