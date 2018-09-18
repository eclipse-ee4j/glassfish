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
import java.util.Collection;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.FederatedConnection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.UnsupportedCapabilityException;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;

public class JaxrConnectionFactory
  extends ConnectionFactory
  implements Serializable, Referenceable
{
  private ManagedConnectionFactory mcf;
  private ConnectionManager cm;
  private Reference reference;
  private Properties properties;
  @LogMessagesResourceBundle
  private static final Logger log = Logger.getLogger("com.sun.connector.jaxr");
  
  public JaxrConnectionFactory(ManagedConnectionFactory paramManagedConnectionFactory, ConnectionManager paramConnectionManager)
  {
    this.mcf = paramManagedConnectionFactory;
    log.fine("JAXRConnectionFactory constructor - ManagedConnectionFactory and ConnectionManager are parameters");
    this.cm = new JaxrConnectionManager();
  }
  
  public Connection getConnection()
    throws JAXRException
  {
    try
    {
      JaxrConnectionRequestInfo localJaxrConnectionRequestInfo = null;
      if (this.properties != null) {
        localJaxrConnectionRequestInfo = new JaxrConnectionRequestInfo(this.properties);
      }
      log.fine("JAXRConnectionFactory getConnection - ConnectionManager calling allocateConnection");
      return (Connection)this.cm.allocateConnection(this.mcf, localJaxrConnectionRequestInfo);
    }
    catch (ResourceException localResourceException)
    {
      throw new JAXRException(localResourceException.getMessage());
    }
  }
  
  public Connection getConnection(String paramString1, String paramString2)
    throws JAXRException
  {
    throw new JAXRException(ResourceBundle.getBundle("com/sun/connector/jaxr/LocalStrings").getString("Getting_a_connection_with_username,_password_parameters_is_not_supported"));
  }
  
  Connection getConnection(Properties paramProperties)
    throws JAXRException
  {
    try
    {
      JaxrConnectionRequestInfo localJaxrConnectionRequestInfo = new JaxrConnectionRequestInfo(paramProperties);
      return (Connection)this.cm.allocateConnection(this.mcf, localJaxrConnectionRequestInfo);
    }
    catch (ResourceException localResourceException)
    {
      throw new JAXRException(localResourceException.getMessage());
    }
  }
  
  public void setProperties(Properties paramProperties)
    throws JAXRException
  {
    this.properties = paramProperties;
  }
  
  public Properties getProperties()
    throws JAXRException
  {
    return this.properties;
  }
  
  public Connection createConnection()
    throws JAXRException
  {
    log.fine("JAXRConnectionFactory createConnection calling getConnection -");
    return getConnection();
  }
  
  public FederatedConnection createFederatedConnection(Collection paramCollection)
    throws JAXRException
  {
    throw new UnsupportedCapabilityException();
  }
  
  public void setReference(Reference paramReference)
  {
    this.reference = paramReference;
  }
  
  public Reference getReference()
  {
    return this.reference;
  }
}
