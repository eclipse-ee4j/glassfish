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

import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import javax.resource.ResourceException;
import javax.resource.spi.IllegalStateException;
import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;

public class JaxrConnection
  implements Connection
{
  private JaxrManagedConnection mc;

  @LogMessagesResourceBundle
  private static final Logger log = Logger.getLogger("com.sun.connector.jaxr");
  
  public JaxrConnection(JaxrManagedConnection paramJaxrManagedConnection)
  {
    this.mc = paramJaxrManagedConnection;
  }
  
  public RegistryService getRegistryService()
    throws JAXRException
  {
    log.fine("Getting RegistryService");
    return getJaxrConnection().getRegistryService();
  }
  
  public void close()
    throws JAXRException
  {
    log.fine("JAXRConnection close - delegating to managedConnection");
    if (this.mc == null) {
      return;
    }
    log.fine("ManagedConnection removing JAXR Connection");
    this.mc.removeJaxrConnection(this);
    log.fine("ManagedConnection sending connection closed Event");
    log.fine("ManagedConnection - connection closed Event Sent");
    this.mc = null;
  }
  
  public boolean isClosed()
    throws JAXRException
  {
    return this.mc == null ? true : getJaxrConnection().isClosed();
  }
  
  public boolean isSynchronous()
    throws JAXRException
  {
    return getJaxrConnection().isSynchronous();
  }
  
  public void setSynchronous(boolean paramBoolean)
    throws JAXRException
  {
    getJaxrConnection().setSynchronous(paramBoolean);
  }
  
  public void setCredentials(Set paramSet)
    throws JAXRException
  {
    getJaxrConnection().setCredentials(paramSet);
  }
  
  public Set getCredentials()
    throws JAXRException
  {
    return getJaxrConnection().getCredentials();
  }
  
  void associateConnection(JaxrManagedConnection paramJaxrManagedConnection)
    throws ResourceException
  {
    try
    {
      checkIfValid();
    }
    catch (JAXRException localJAXRException)
    {
      throw new IllegalStateException(ResourceBundle.getBundle("com/sun/connector/jaxr/LocalStrings").getString("Unable_to_associate_JAXR_Connection,_Connection_is_invalid"));
    }
    this.mc.removeJaxrConnection(this);
    paramJaxrManagedConnection.addJaxrConnection(this);
    this.mc = paramJaxrManagedConnection;
  }
  
  void checkIfValid()
    throws JAXRException
  {
    if (this.mc == null) {
      throw new JAXRException(ResourceBundle.getBundle("com/sun/connector/jaxr/LocalStrings").getString("Connection_is_invalid"));
    }
  }
  
  Connection getJaxrConnection()
    throws JAXRException
  {
    checkIfValid();
    try
    {
      return this.mc.getJaxrConnection();
    }
    catch (ResourceException localResourceException)
    {
      throw new JAXRException(ResourceBundle.getBundle("com/sun/connector/jaxr/LocalStrings").getString("Unable_to_obtain_JAXR_Connection_") + localResourceException.getMessage());
    }
  }
  
  void invalidate()
  {
    this.mc = null;
  }
}

