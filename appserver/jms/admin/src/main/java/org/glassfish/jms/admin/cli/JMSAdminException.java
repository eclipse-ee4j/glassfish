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

package org.glassfish.jms.admin.cli;


public class JMSAdminException extends Exception {

    /**
     * Exception reference
     **/
    private volatile Exception linkedException;
    private String _message = null;

    /**
     * Constructs an JMSAdminException object
     */
    public JMSAdminException() {
        super();
        linkedException = null;
    }


    /**
     * Constructs an JMSAdminException object
     *
     * @param message Exception message
     */
    public JMSAdminException(String message) {
        super(message);
        _message = message;
        linkedException = null;
    }


    /**
     * Gets the exception linked to this one
     *
     * @return the linked Exception, null if none
     **/
    public Exception getLinkedException() {
        return (linkedException);
    }


    /**
     * Adds a linked Exception
     *
     * @param ex the linked Exception
     **/
    public void setLinkedException(Exception ex) {
        linkedException = ex;
    }


    /**
     * Returns the message along with the message from any linked exception.
     **/
    @Override
    public String getMessage() {
        String retString = null;

        // Return the message of this exception.
        if (_message != null) {
            retString = _message;
        }

        // Append any message from the linked exception.
        Exception localLinkedException = linkedException;
        if (localLinkedException != null && localLinkedException.getMessage() != null) {
            if (retString != null) {
                retString += retString + "\n" + localLinkedException.getMessage();
            } else {
                retString = localLinkedException.getMessage();
            }
        }
        return retString;
    }

}
