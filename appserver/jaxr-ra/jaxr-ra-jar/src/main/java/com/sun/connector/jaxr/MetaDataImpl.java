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

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.IllegalStateException;
import javax.resource.spi.ManagedConnectionMetaData;

public class MetaDataImpl
  implements ManagedConnectionMetaData
{
  private static final String PRODUCT_NAME = "JAXR Resource Adapter";
  private static final String PRODUCT_VERSION = "1.0";
  private static final int MAX_CONNECTIONS = 150;
  private JaxrManagedConnection mc;
  
  public MetaDataImpl(JaxrManagedConnection paramJaxrManagedConnection)
  {
    this.mc = paramJaxrManagedConnection;
  }
  
  public String getEISProductName()
    throws ResourceException
  {
    try
    {
      return "JAXR Resource Adapter";
    }
    catch (Exception localException)
    {
      EISSystemException localEISSystemException = new EISSystemException(localException.getMessage());
      localEISSystemException.setLinkedException(localException);
      throw localEISSystemException;
    }
  }
  
  public String getEISProductVersion()
    throws ResourceException
  {
    try
    {
      return "1.0";
    }
    catch (Exception localException)
    {
      EISSystemException localEISSystemException = new EISSystemException(localException.getMessage());
      localEISSystemException.setLinkedException(localException);
      throw localEISSystemException;
    }
  }
  
  public int getMaxConnections()
    throws ResourceException
  {
    try
    {
      return 150;
    }
    catch (Exception localException)
    {
      EISSystemException localEISSystemException = new EISSystemException(localException.getMessage());
      localEISSystemException.setLinkedException(localException);
      throw localEISSystemException;
    }
  }
  
  public String getUserName()
    throws ResourceException
  {
    if (this.mc.isDestroyed()) {
      throw new IllegalStateException("ManagedConnection has been destroyed");
    }
    throw new NotSupportedException("Credentials not supported in JAXR Connector");
  }
}
