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

package com.sun.connector.jaxr;

import java.io.Serializable;
import java.util.logging.Logger;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;

public class JaxrConnectionManager
  implements ConnectionManager, Serializable
{
  @LogMessagesResourceBundle
  private static final Logger log = Logger.getLogger("com.sun.connector.jaxr");
  
  public Object allocateConnection(ManagedConnectionFactory paramManagedConnectionFactory, ConnectionRequestInfo paramConnectionRequestInfo)
    throws ResourceException
  {
    this.log.fine("JAXRConnectionManager allocateConnection");
    this.log.fine("JAXRConnectionManager allocateConnection calling JAXRManagedConnectionFactory createManagedConnection");
    ManagedConnection localManagedConnection = paramManagedConnectionFactory.createManagedConnection(null, paramConnectionRequestInfo);
    this.log.fine("JAXRConnectionManager allocateConnection - calling managerConnection getConnection");
    return localManagedConnection.getConnection(null, paramConnectionRequestInfo);
  }
}
