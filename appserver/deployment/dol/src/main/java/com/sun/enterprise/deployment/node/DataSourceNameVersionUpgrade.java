/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.node;

/**
 * This interface defines the processing used to upgrade
 * data-source-name to the latest version
 *
 * One element name is matched:
 * "weblogic-application/jdbc-connection-pool/data-source-name" is replaced by
 * "weblogic-application/jdbc-connection-pool/data-source-jndi-name".
 * @author  Gerald Ingalls
 * @version
 */
public class DataSourceNameVersionUpgrade extends ReplaceVersionUpgrade {
  private static String DATA_SOURCE_NAME =
    "weblogic-application/jdbc-connection-pool/data-source-name";
  private static String DATA_SOURCE_JNDI_NAME =
    "weblogic-application/jdbc-connection-pool/data-source-jndi-name";
  public DataSourceNameVersionUpgrade() {
    super(DATA_SOURCE_NAME, DATA_SOURCE_JNDI_NAME);
  }
}
