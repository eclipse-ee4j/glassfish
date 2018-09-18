/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.webservices.monitoring;


public interface MessageTrace {

    /**
     * Return the SOAPMessage as a string including the SOAPHeaders or not
     * @param includeHeaders the soap headers.
     * @return the soap message
     */
    public String getMessage(boolean includeHeaders);

    /**
     * Returns the endpoint this message originated from
     */
    public Endpoint getEndpoint();

    /**
     * Returns the transport dependent information
     */
    public TransportInfo getTransportInfo();

}
