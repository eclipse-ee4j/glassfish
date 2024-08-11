/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.connector.cciblackbox;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;

import java.io.Serializable;

/**
 * The default ConnectionManager implementation for the
 * non-managed scenario
 * This provieds a hook for a resource adapter to pass a connection
 * request to an application server.
 *
 * @author Sheetal Vartak
 */
public class CciConnectionManager implements ConnectionManager, Serializable {

  public CciConnectionManager() {
  }

  public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo info)
      throws ResourceException {
    ManagedConnection mc = mcf.createManagedConnection(null, info);
    return mc.getConnection(null, info);
  }
}
