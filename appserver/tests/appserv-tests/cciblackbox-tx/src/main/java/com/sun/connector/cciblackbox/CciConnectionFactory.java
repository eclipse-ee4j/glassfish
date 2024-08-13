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

import jakarta.resource.Referenceable;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.ConnectionFactory;
import jakarta.resource.cci.ConnectionSpec;
import jakarta.resource.cci.RecordFactory;
import jakarta.resource.cci.ResourceAdapterMetaData;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnectionFactory;

import java.io.Serializable;

import javax.naming.Reference;

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

  public jakarta.resource.cci.Connection getConnection() throws ResourceException {
    jakarta.resource.cci.Connection con = null;
    con = (jakarta.resource.cci.Connection) cm.allocateConnection(mcf, null);
    return con;
  }

  public jakarta.resource.cci.Connection getConnection(ConnectionSpec properties)
      throws ResourceException {
    jakarta.resource.cci.Connection con = null;
    ConnectionRequestInfo info = new CciConnectionRequestInfo(
        ((CciConnectionSpec) properties).getUser(), ((CciConnectionSpec) properties).getPassword());
    con = (jakarta.resource.cci.Connection) cm.allocateConnection(mcf, info);
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
