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

package org.glassfish.internal.embedded;

import org.jvnet.hk2.annotations.Contract;

/**
 *
 * Port abstraction, used to bind several containers to the same port.
 *
 * @author Jerome Dochez
 */
@Contract
public interface Port {

    // default set of protocol we support
    public final static String HTTP_PROTOCOL = "http";
    public final static String HTTPS_PROTOCOL = "https";
    public final static String IIOP_PROTOCOL = "iiop";

    /**
     * Returns the port number occupied by this instance.
     *
     * @return port number
     */
    public int getPortNumber();

    /**
     * Unbinds (close) this port instance, releasing network resources at portNumber
     */
    public void close();

    // todo : return a list of protocols bound to this port.
}
