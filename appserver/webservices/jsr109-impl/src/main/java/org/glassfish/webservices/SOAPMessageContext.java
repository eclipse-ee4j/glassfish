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

package org.glassfish.webservices;

/**
 * This is a wrapper class over JAXWS's Packet interface - this is being added to minimize the changes
 * required for the AS's WS security and monitoring mechamisms to work properly.
 */
public interface SOAPMessageContext
    extends jakarta.xml.ws.handler.soap.SOAPMessageContext {

    /**
     * If there is a SOAPMessage already, use getSOAPMessage(). Ignore all other methods
     * @return
     */
    public boolean isAlreadySoap();
}
