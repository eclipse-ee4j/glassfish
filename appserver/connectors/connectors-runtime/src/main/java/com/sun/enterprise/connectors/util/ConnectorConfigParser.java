/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.deployment.ConnectorDescriptor;

import java.util.List;
import java.util.Properties;

/**
 * Interface class of connector configuration parser methods.
 * Implemented by specific configuration parser classes.
 *
 * @author Srikanth P
 */
public interface ConnectorConfigParser {

    /**
     * Admin object type.
     */
    String AOR = ConnectorConstants.AO;

    /**
     * Managed connection factory type.
     */
    String MCF = ConnectorConstants.MCF;

    /**
     * Resource adapter type.
     */
    String RA = ConnectorConstants.RAR;

    /**
     * Message listener type.
     */
    String MSL = ConnectorConstants.MSL;

    /**
     * Instances of util classes. Used as composition pattern.
     */
    ConnectorDDTransformUtils ddTransformUtil = new ConnectorDDTransformUtils();
    ConnectorConfigParserUtils configParserUtil =
            new ConnectorConfigParserUtils();

    /**
     * Obtains the merged javabean properties (properties present in ra.xml
     * and introspected properties) of a specific configuration.
     *
     * @param desc              ConnectorDescriptor pertaining to rar .
     * @param connectionDefName Connection definition name or
     *                          admin object interface .
     * @return Merged properties.
     */
    Properties getJavaBeanProps(ConnectorDescriptor desc,
                                String connectionDefName, String rarName) throws ConnectorRuntimeException;

    /**
     * Gi
     * @param desc
     * @param rarName
     * @param keyFields
     * @return
     * @throws ConnectorRuntimeException
     */
    List<String> getConfidentialProperties(ConnectorDescriptor desc, String rarName,
                                           String... keyFields) throws ConnectorRuntimeException ;
}
