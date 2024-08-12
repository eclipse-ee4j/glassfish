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

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.deployment.ConnectorDescriptor;

import java.util.Properties;

/** Interface class of admin object interface parser methods.
 *  @author Srikanth P
 */
public interface AdminObjectConfigParser extends ConnectorConfigParser {

    /**
     *  Obtains the admin object intercface names of a given rar.
     *  @param desc ConnectorDescriptor pertaining to rar.
     *  @return Array of admin object interface names as strings
     *  @throws ConnectorRuntimeException If rar is not exploded or
     *                                    incorrect ra.xml
     */
    String[] getAdminObjectInterfaceNames(ConnectorDescriptor desc)
                      throws ConnectorRuntimeException;

    /**
     * gets the adminObjectClassNames pertaining to a rar & a specific
     * adminObjectInterfaceName
     *
     * @param desc ConnectorDescriptor pertaining to rar.
     * @param intfName admin-object-interface name
     * @return Array of AdminObjectInterface names as Strings
     * @throws ConnectorRuntimeException if parsing fails
     */
    String[] getAdminObjectClassNames(ConnectorDescriptor desc, String intfName)
            throws ConnectorRuntimeException ;

    /**
     *  Checks whether the provided interfacename and classname combination
     *  is present in any of the admin objects for the resource-adapter
     *  @param desc ConnectorDescriptor pertaining to rar.
     *  @param intfName interface-name
     *  @param className class-name
     *  @return boolean indicating the presence of adminobject
     *  @throws ConnectorRuntimeException If rar is not exploded or
     *                                    incorrect ra.xml
     */
    boolean hasAdminObject(ConnectorDescriptor desc, String intfName, String className)
        throws ConnectorRuntimeException;

    /**
     * Obtains the merged javabean properties (properties present in ra.xml
     * and introspected properties) of a specific configuration.
     *
     * @param desc              ConnectorDescriptor pertaining to rar .
     * @param adminObjectIntfName admin object interface .
     * @param adminObjectClassName admin object classname
     * @param rarName resource-adapter-name
     * @return Merged properties.
     *  @throws ConnectorRuntimeException If rar is not exploded or
     *                                    incorrect ra.xml
     */
    Properties getJavaBeanProps(ConnectorDescriptor desc,
                                String adminObjectIntfName, String adminObjectClassName,
                                String rarName) throws ConnectorRuntimeException;

}

