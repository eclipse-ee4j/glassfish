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

import java.util.Properties;
import javax.resource.spi.ConnectionRequestInfo;

public class JaxrConnectionRequestInfo
  implements ConnectionRequestInfo
{
  private String user;
  private String password;
  private Properties properties;
  
  public JaxrConnectionRequestInfo(String paramString1, String paramString2)
  {
    this.user = paramString1;
    this.password = paramString2;
  }
  
  public JaxrConnectionRequestInfo(Properties paramProperties)
  {
    this.properties = paramProperties;
  }
  
  public void setProperties(Properties paramProperties)
  {
    this.properties = paramProperties;
  }
  
  public Properties getProperties()
  {
    return this.properties;
  }
  
  public String getUser()
  {
    return this.user;
  }
  
  public String getPassword()
  {
    return this.password;
  }
  
  public boolean equals(Object paramObject)
  {
    if (paramObject == null) {
      return false;
    }
    if ((paramObject instanceof JaxrConnectionRequestInfo))
    {
      JaxrConnectionRequestInfo localJaxrConnectionRequestInfo = (JaxrConnectionRequestInfo)paramObject;
      Properties localProperties = localJaxrConnectionRequestInfo.getProperties();
      return (isEqual(this.properties, localProperties)) && (this.properties.hashCode() == localJaxrConnectionRequestInfo.getProperties().hashCode());
    }
    return false;
  }
  
  public int hashCode()
  {
    return this.properties == null ? "".hashCode() : this.properties.hashCode();
  }
  
  private boolean isEqual(Object paramObject1, Object paramObject2)
  {
    if (paramObject1 == null) {
      return paramObject2 == null;
    }
    return paramObject1.equals(paramObject2);
  }
}
