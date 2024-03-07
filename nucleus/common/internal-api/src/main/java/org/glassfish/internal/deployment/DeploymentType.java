/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.internal.deployment;

import java.util.Comparator;

import com.sun.enterprise.config.serverbeans.ServerTags;

/**
 * DeploymentType defines the various deployment entity types,
 * and deployment ordering within the types.
 * <p>
 * For an ordering among the various types, please refer to
 * <CODE>DeploymentOrder</CODE>.
 * <p>
 * Note that the comparator of the deployment types should
 * return a consistent value during the lifetime of the
 * deployment objects, e.g. it should not depend on values of
 * mutable fields of the deployment objects.
 * <p>
 * The list of deployment types are as follows:
 * <p><ol>
 * <li> INTERNAL_APP
 * <li> JDBC_SYS_RES
 * <li> DEPLOYMENT_HANDLER
 * <li> JMS_SYS_RES
 * <li> RESOURCE_DEPENDENT_DEPLOYMENT_HANDLER
 * <li> STARTUP_CLASS
 * <li> WLDF_SYS_RES
 * <li> LIBRARY
 * <li> CONNECTOR
 * <li> DEFAULT_APP
 * <li> COHERENCE_CLUSTER_SYS_RES
 * <li> CUSTOM_SYS_RES
 * </ol>
 *
 */

public class DeploymentType implements Comparator {

  public static final String SYSTEM_PREFIX = "system-";
  public static final String USER = "user";
  public final static String INTERNAL_APP_NAME = "InternalApp";
  public final static String CONNECTOR_NAME = "Connector";
  public final static String DEFAULT_APP_NAME = "DefaultApp";

  private final String name;
  private final Class cls;

  public final static DeploymentType INTERNAL_APP =
    new DeploymentType(INTERNAL_APP_NAME, ApplicationOrderInfo.class) {
      public boolean isInstance(Object obj) {
        if (super.isInstance(obj)) {
          ApplicationOrderInfo appOrderInfo = (ApplicationOrderInfo)obj;
          if (appOrderInfo.getApplication().getObjectType().startsWith(SYSTEM_PREFIX)) {
            return true;
          }
        }
        return false;
      }
    };

  public final static DeploymentType DEFAULT_APP =
    new DeploymentType(DEFAULT_APP_NAME, ApplicationOrderInfo.class) {
      public boolean isInstance(Object obj) {
        if (super.isInstance(obj)) {
          ApplicationOrderInfo appOrderInfo = (ApplicationOrderInfo)obj;
          if (appOrderInfo.getApplication().getObjectType().equals(USER)) {
            return true;
          }
        }
        return false;
      }
    };

  public final static DeploymentType CONNECTOR =
    new DeploymentType(CONNECTOR_NAME, ApplicationOrderInfo.class) {
      public boolean isInstance(Object obj) {
        if (super.isInstance(obj)) {
          ApplicationOrderInfo appOrderInfo = (ApplicationOrderInfo)obj;
          if ((appOrderInfo.getApplication().containsSnifferType(ServerTags.CONNECTOR)) &&
              (appOrderInfo.getApplication().isStandaloneModule())) {
            return true;
          }
        }
        return false;
      }
    };

  private DeploymentType(String name, Class cls) {
    this.name = name;
    this.cls = cls;
  }

  public String toString() { return name; }
  public boolean isInstance(Object obj) { return cls.isInstance(obj); }
  public Comparator getComparator() { return this; }

  // Compares two instances of the current type
  public int compare(Object o1, Object o2) {
    if ((o1 instanceof ApplicationOrderInfo) && (o2 instanceof ApplicationOrderInfo)) {
      return compare((ApplicationOrderInfo)o1, (ApplicationOrderInfo)o2);
    }
    return defaultCompare(o1, o2);
  }

  protected int defaultCompare(Object o1, Object o2) {
    if (o1 instanceof ApplicationOrderInfo && o2 instanceof ApplicationOrderInfo ) {
      ApplicationOrderInfo o1App = (ApplicationOrderInfo)o1;
      ApplicationOrderInfo o2App = (ApplicationOrderInfo)o2;
      return o1App.getOriginalOrder() - o2App.getOriginalOrder();
    }
    /*
     * The following is for WLS compatibility where ties amone
     * applications with the same deployment order are resolved
     * by comparing the application name.
     */
    /*
    if (o1 instanceof ApplicationName && o2 instanceof ApplicationName ) {
      return ((ApplicationName)o1).getName().compareTo(((ApplicationName)o2).getName());
    }
    */
    return 0;
  }

  protected int compare(ApplicationOrderInfo d1, ApplicationOrderInfo d2) {
    int comp = new Integer(d1.getApplication().getDeploymentOrder()).compareTo(new Integer(d2.getApplication().getDeploymentOrder()));
    if (comp == 0) {
      return defaultCompare(d1,d2);
    }
    return comp;
  }
}


