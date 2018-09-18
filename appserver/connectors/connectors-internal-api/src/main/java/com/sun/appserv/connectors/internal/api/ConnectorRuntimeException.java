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

package com.sun.appserv.connectors.internal.api;

/**
 * This is an Exception class. Connector module uses this class to
 * thow exceptions both for internal and external(outside connector module )
 * Contains an message which indicates the error message.
 *
 * @author Binod P.G
 */
public class ConnectorRuntimeException extends Exception {

    /**
     * Constructor
     *
     * @param msg Error message
     */
    public ConnectorRuntimeException(String msg) {
        super(msg);
    }

    public ConnectorRuntimeException (String msg, Throwable cause){
        super(msg, cause);
    }

    public ConnectorRuntimeException(Throwable t){
        super(t);
    }
}
