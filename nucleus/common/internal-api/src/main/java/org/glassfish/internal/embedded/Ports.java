/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.util.Collection;

import org.jvnet.hk2.annotations.Contract;

/**
 * Management interfaces for all embedded ports
 *
 * @author Jerome Dochez
 */
@Contract
public interface Ports {


    /**
     * Creates a port, binds it to a port number and returns it
     * @param number the port number
     * @return the bound port to the port number
     * @throws IOException if the port is already taken or another network exception occurs
     */
    Port createPort(int number) throws IOException;


    /**
     * Returns the list of allocated ports
     *
     * @return the allocated ports
     */
    Collection<Port> getPorts();
}
