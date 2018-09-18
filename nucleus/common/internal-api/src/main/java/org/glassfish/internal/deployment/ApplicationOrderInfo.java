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

/**
 * This class holds the Application and the order that it occurs
 * in domain.xml.  When the server starts, applications are loaded
 * according to the deployment-order attribute, but for applications
 * that have the same deployment-order, the application that occurs
 * first in domain.xml is loaded first.
 */
public class ApplicationOrderInfo {
  private Application application;
  private int originalOrder;

  public ApplicationOrderInfo(Application app,
                              int order) {
    application = app;
    originalOrder = order;
  }

  public Application getApplication() {
    return application;
  }

  public int getOriginalOrder() {
    return originalOrder;
  }
}


