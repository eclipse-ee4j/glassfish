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

package com.sun.enterprise.connectors.util;

import com.sun.enterprise.deployment.*;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;

/** Interface class of managed connection factory parser methods.
 *  @author Srikanth P
 */
public interface MCFConfigParser extends ConnectorConfigParser {

    /**
     *  Obtains the connection definition names of a given rar.
     *  @param desc ConnectorDescriptor pertaining to rar.
     *  @return Array of connection definiton names as strings
     *  @throws ConnectorRuntimeException If rar is not exploded or
     *                                    incorrect ra.xml
     */
    public String[] getConnectionDefinitionNames(ConnectorDescriptor desc)
                      throws ConnectorRuntimeException;
}
