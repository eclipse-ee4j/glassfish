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

import java.sql.SQLException;

import javax.naming.Context;
import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.ConnectionMetaData;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.ResultSetInfo;
import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.IllegalStateException;
import javax.rmi.PortableRemoteObject;

import wlstest.functional.connector.common.apps.ejb.test_proxy.ConnectorTest;
import weblogic.jndi.Environment;

/**
 * This implementation class represents an application level connection
 *handle that is used by a component to access an EIS instance.
 *
 * @author Sheetal Vartak
 */
public class CciConnection implements jakarta.resource.cci.Connection {

  private boolean destroyed;

  private CciManagedConnection mc;

  // if mc is null, means connection is invalid

  CciConnection(CciManagedConnection mc) {
    this.mc = mc;
  }

  CciManagedConnection getManagedConnection() {
    return mc;
  }

  public Interaction createInteraction() throws ResourceException {
    return new CciSQLInteraction(this);
  }

  public jakarta.resource.cci.LocalTransaction getLocalTransaction() throws ResourceException {
    try {
      java.sql.Connection con = getJdbcConnection();
      if (con.getTransactionIsolation() == con.TRANSACTION_NONE) {
        throw new ResourceException("Local Transaction not supported!!");
      }
    }
    catch (Exception e) {
      throw new ResourceException(e.getMessage());
    }
    return new CciLocalTransactionImpl(mc);
  }

  public void setAutoCommit(boolean autoCommit) throws ResourceException {

    try {
      java.sql.Connection con = getJdbcConnection();
      if (con.getTransactionIsolation() == con.TRANSACTION_NONE) {
        throw new ResourceException("Local Transaction not " + "supported!!");
      }
      con.setAutoCommit(autoCommit);
    }
    catch (Exception e) {
      throw new ResourceException(e.getMessage());
    }
  }

  public boolean getAutoCommit() throws ResourceException {

    boolean val = false;
    try {
      java.sql.Connection con = getJdbcConnection();
      if (con.getTransactionIsolation() == con.TRANSACTION_NONE) {
        throw new ResourceException("Local Transaction not " + "supported!!");
      }
      val = con.getAutoCommit();
    }
    catch (SQLException e) {
      throw new ResourceException(e.getMessage());
    }
    return val;
  }

  public ResultSetInfo getResultSetInfo() throws ResourceException {
    throw new NotSupportedException("ResultSet is not supported.");
  }

  public void close() throws ResourceException {
    if (mc == null) return; // already be closed
    mc.removeCciConnection(this);
    mc.sendEvent(ConnectionEvent.CONNECTION_CLOSED, null, this);
    mc = null;
  }

  public ConnectionMetaData getMetaData() throws ResourceException {
    return new CciConnectionMetaDataImpl(mc);
  }

  void associateConnection(CciManagedConnection newMc) throws ResourceException {

    try {
      checkIfValid();
    }
    catch (ResourceException ex) {
      throw new IllegalStateException("Connection is invalid");
    }
    // dissociate handle with current managed connection
    mc.removeCciConnection(this);
    // associate handle with new managed connection
    newMc.addCciConnection(this);
    mc = newMc;
  }

  void checkIfValid() throws ResourceException {
    if (mc == null) {
      throw new ResourceException("Connection is invalid");
    }
  }

  java.sql.Connection getJdbcConnection() throws SQLException {

    java.sql.Connection con = null;
    try {
      checkIfValid();
      //  mc.getJdbcConnection() returns a SQL connection object
      con = mc.getJdbcConnection();
    }
    catch (ResourceException ex) {
      throw new SQLException("Connection is invalid.");
    }
    return con;
  }

  void invalidate() {
    mc = null;
  }

  private void checkIfDestroyed() throws ResourceException {
    if (destroyed) {
      throw new IllegalStateException("Managed connection is closed");
    }
  }

  class Internal {
    public Object narrow(Object ref, Class c) {
      return PortableRemoteObject.narrow(ref, c);
    }
  }

  public boolean calcMultiply(String serverUrl, String testUser, String testPassword,
      String testJndiName, int num1, int num2) {

    Context ctx = null;
    ConnectorTest connectorTest = null;
    Environment env = null;
    boolean result;
    try {
      System.out.println("###  calcMultiply");
      env = new Environment();
      env.setProviderUrl(serverUrl);
      env.setSecurityPrincipal(testUser);
      env.setSecurityCredentials(testPassword);
      ctx = env.getInitialContext();
      System.out.println("Lookup for " + testJndiName);
      connectorTest = (ConnectorTest) ctx.lookup(testJndiName);
      //Internal intenalRef = new Internal();
      System.out.println("ConnectorTest is " + connectorTest);
      //ConnectorTest connectorTestRemote = (ConnectorTest) intenalRef.narrow(connectorTestHome.create(), ConnectorTest.class);
      if (connectorTest.calcMultiply(num1, num2) == (num1 * num2)) {
        result = true;
      } else {
        result = false;
      }
    }
    catch (Exception e) {

      result = false;
      System.out.println("Exception in calcMultiply ");
      e.printStackTrace();
    }
    return result;
  }
}
