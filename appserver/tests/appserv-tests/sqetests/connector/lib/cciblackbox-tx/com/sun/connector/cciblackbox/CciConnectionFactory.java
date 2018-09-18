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

import java.io.Serializable;

import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;

/**
 * This implementation class provides provides an inteface for getting 
 * connection to an EIS instance.
 * @author Sheetal Vartak
 *
 */
public class CciConnectionFactory implements ConnectionFactory, Serializable, Referenceable {

  private ManagedConnectionFactory mcf;

  private ConnectionManager cm;

  private Reference reference;

  public CciConnectionFactory(ManagedConnectionFactory mcf, ConnectionManager cm) {
    this.mcf = mcf;
    if (cm == null) {
      this.cm = new CciConnectionManager();
    } else {
      this.cm = cm;
    }
  }

  public CciConnectionFactory(ManagedConnectionFactory mcf) {
    this.mcf = mcf;
  }

  public javax.resource.cci.Connection getConnection() throws ResourceException {
    javax.resource.cci.Connection con = null;
    con = (javax.resource.cci.Connection) cm.allocateConnection(mcf, null);
    return con;
  }

  public javax.resource.cci.Connection getConnection(ConnectionSpec properties)
      throws ResourceException {
    javax.resource.cci.Connection con = null;
    ConnectionRequestInfo info = new CciConnectionRequestInfo(
        ((CciConnectionSpec) properties).getUser(), ((CciConnectionSpec) properties).getPassword());
    con = (javax.resource.cci.Connection) cm.allocateConnection(mcf, info);
    return con;
  }

  public ResourceAdapterMetaData getMetaData() throws ResourceException {
    return new CciResourceAdapterMetaData();
  }

  public RecordFactory getRecordFactory() throws ResourceException {
    return new CciRecordFactory();
  }

  public void setReference(Reference reference) {
    this.reference = reference;
  }

  public Reference getReference() {
    return reference;
  }
}
