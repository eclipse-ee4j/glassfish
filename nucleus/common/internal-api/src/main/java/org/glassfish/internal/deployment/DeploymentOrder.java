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

import com.sun.enterprise.config.serverbeans.Application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class DeploymentOrder {

  /**
   * Deployment ordering among different deployment types.
   *
   * For deployment ordering within a particular type, please refer
   * to <CODE>DeploymentType</CODE>.
   */
  /*
  public final static DeploymentType[] DEPLOYMENT_ORDER = {
    DeploymentType.INTERNAL_APP,
    DeploymentType.JDBC_SYS_RES,
    DeploymentType.DEPLOYMENT_HANDLER,
    DeploymentType.JMS_SYS_RES,
    DeploymentType.RESOURCE_DEPENDENT_DEPLOYMENT_HANDLER,
    DeploymentType.STARTUP_CLASS,
    DeploymentType.WLDF_SYS_RES,
    DeploymentType.LIBRARY,
    DeploymentType.CONNECTOR,
    DeploymentType.DEFAULT_APP,
    DeploymentType.COHERENCE_CLUSTER_SYS_RES,
    DeploymentType.CUSTOM_SYS_RES
  };
  */

  public final static DeploymentType[] APPLICATION_DEPLOYMENT_ORDER = {
    DeploymentType.INTERNAL_APP,
    DeploymentType.CONNECTOR,
    DeploymentType.DEFAULT_APP
  };

  /**
   * A comparator that imposes deployment ordering as defined by
   * <CODE>DEPLOYMENT_ORDER</CODE> above (for ordering among deployment
   * types) and by the various DeploymentTypes (for ordering within
   * deployment types).
   */
  public final static Comparator APPLICATION_COMPARATOR = new Comparator() {
    public int compare(Object o1, Object o2) {
      if (o1 == o2) {
        return 0;
      }
      for (int i = 0; i < APPLICATION_DEPLOYMENT_ORDER.length; i++) {
        DeploymentType depType = APPLICATION_DEPLOYMENT_ORDER[i];
        if (depType.isInstance(o1) && !depType.isInstance(o2)) {
          return -1;
        } else if (!depType.isInstance(o1) && depType.isInstance(o2)) {
          return 1;
        } else if (depType.isInstance(o1) && depType.isInstance(o2)) {
          return depType.compare(o1, o2);
        }
      }
      // unrecognized type
      throw new RuntimeException("unrecognized type");
    };
  };

  private static final TreeSet application_deployments =
    new TreeSet(APPLICATION_COMPARATOR);

  public static void addApplicationDeployment(ApplicationOrderInfo app) {
    application_deployments.add(app);
  }

  public static Iterator getApplicationDeployments() {
    List<Application> appList = new ArrayList<Application>();
    Iterator<ApplicationOrderInfo> it = application_deployments.iterator();
    while (it.hasNext()) {
      ApplicationOrderInfo appOrderInfo = it.next();
      appList.add(appOrderInfo.getApplication());
    }
    return appList.iterator();
  }
}
