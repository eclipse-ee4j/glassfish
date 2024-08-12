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

package com.sun.enterprise.connectors.service;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.connectors.ActiveOutboundResourceAdapter;
import com.sun.enterprise.connectors.ActiveResourceAdapter;

import java.util.Properties;
import java.util.logging.Level;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.naming.ResourceNamingService;

/**
 * AdminObject administration service. It performs the functionality of
 * creating and deleting the Admin Objects
 *
 * @author Binod P.G and Srikanth P
 */


public class ConnectorAdminObjectAdminServiceImpl extends
        ConnectorService {


    public ConnectorAdminObjectAdminServiceImpl() {
    }

    public void addAdminObject(
            String appName,
            String connectorName,
            ResourceInfo resourceInfo,
            String adminObjectType,
            String adminObjectClassName,
            Properties props)
            throws ConnectorRuntimeException {
        ActiveResourceAdapter ar =
                _registry.getActiveResourceAdapter(connectorName);
        if (ar == null) {
            ifSystemRarLoad(connectorName);
            ar = _registry.getActiveResourceAdapter(connectorName);
        }
        if (ar instanceof ActiveOutboundResourceAdapter) {
            ActiveOutboundResourceAdapter aor =
                    (ActiveOutboundResourceAdapter) ar;
            aor.addAdminObject(appName, connectorName, resourceInfo,
                    adminObjectType, adminObjectClassName, props);
        } else {
            ConnectorRuntimeException cre = new ConnectorRuntimeException(
                    "This adapter is not 1.5 compliant");
            _logger.log(Level.SEVERE,
                    "rardeployment.non_1.5_compliant_rar", resourceInfo);
            throw cre;
        }
    }

    public void deleteAdminObject(ResourceInfo resourceInfo)
            throws ConnectorRuntimeException {

        ResourceNamingService namingService = _runtime.getResourceNamingService();
        try {
            namingService.unpublishObject(resourceInfo, resourceInfo.getName());
        }
        catch (NamingException ne) {
            /* TODO V3 JMS RA ?
            ResourcesUtil resutil = ResourcesUtil.createInstance();
            if (resutil.adminObjectBelongsToSystemRar(jndiName)) {
                return;
            }
            */
            if (ne instanceof NameNotFoundException) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE,
                            "rardeployment.admin_object_delete_failure", resourceInfo);
                    _logger.log(Level.FINE, "", ne);
                }
                return;
            }
            ConnectorRuntimeException cre = new ConnectorRuntimeException(
                    "Failed to delete admin object from jndi");
            cre.initCause(ne);
            _logger.log(Level.SEVERE,
                    "rardeployment.admin_object_delete_failure", resourceInfo);
            _logger.log(Level.SEVERE, "", cre);
            throw cre;
        }
    }
}
