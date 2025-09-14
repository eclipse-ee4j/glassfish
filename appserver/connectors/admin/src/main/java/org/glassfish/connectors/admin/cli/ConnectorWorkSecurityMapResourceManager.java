/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.connectors.admin.cli;


import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.resource.ResourceException;

import java.beans.PropertyVetoException;
import java.util.Map.Entry;
import java.util.Properties;

import org.glassfish.api.I18n;
import org.glassfish.connectors.config.GroupMap;
import org.glassfish.connectors.config.PrincipalMap;
import org.glassfish.connectors.config.WorkSecurityMap;
import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resources.admin.cli.ResourceConstants;
import org.glassfish.resources.admin.cli.ResourceManager;
import org.glassfish.resources.api.ResourceAttributes;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;


@Service(name = ResourceConstants.WORK_SECURITY_MAP)
@I18n("add.resources")
public class ConnectorWorkSecurityMapResourceManager implements ResourceManager {

    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(
        ConnectorWorkSecurityMapResourceManager.class);
    private String raName;
    private Properties principalsMap;
    private Properties groupsMap;
    private String mapName;

    @Override
    public String getResourceType() {
        return ResourceConstants.WORK_SECURITY_MAP;
    }


    @Override
    public ResourceStatus create(Resources resources, ResourceAttributes attributes, final Properties properties,
        String target) throws Exception {
        setAttributes(attributes);

        ResourceStatus validationStatus = isValid(resources);
        if(validationStatus.getStatus() == ResourceStatus.FAILURE){
            return validationStatus;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {
                @Override
                public Object run(Resources param) throws PropertyVetoException,
                        TransactionFailure {
                    return createResource(param, properties);
                }
            }, resources);
        } catch (TransactionFailure tfe) {
            String msg = I18N.getLocalString(
                    "create.connector.work.security.map.fail",
                    "Unable to create connector work security map {0}.", mapName) +
                    " " + tfe.getLocalizedMessage();
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }
        String msg = I18N.getLocalString(
                "create.work.security.map.success",
                "Work security map {0} created.", mapName);
        return new ResourceStatus(ResourceStatus.SUCCESS, msg, true);
    }

    private ResourceStatus isValid(Resources resources){
        ResourceStatus status = new ResourceStatus(ResourceStatus.SUCCESS, "Validation Successful");
        if (mapName == null) {
            String msg = I18N.getLocalString(
                    "create.connector.work.security.map.noMapName",
                    "No mapname defined for connector work security map.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }

        if (raName == null) {
            String msg = I18N.getLocalString(
                    "create.connector.work.security.map.noRaName",
                    "No raname defined for connector work security map.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }

        if (principalsMap == null && groupsMap == null) {
            String msg = I18N.getLocalString(
                    "create.connector.work.security.map.noMap",
                    "No principalsmap or groupsmap defined for connector work security map.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }

        if (principalsMap != null && groupsMap != null) {
            String msg = I18N.getLocalString(
                    "create.connector.work.security.map.specifyPrincipalsOrGroupsMap",
                    "A work-security-map can have either (any number of) group mapping  " +
                            "or (any number of) principals mapping but not both. Specify" +
                            "--principalsmap or --groupsmap.");
            return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
        }

        // ensure we don't already have one of this name
        for (Resource resource : resources.getResources()) {
            if (resource instanceof WorkSecurityMap) {
                if (((WorkSecurityMap) resource).getName().equals(mapName) &&
                        ((WorkSecurityMap) resource).getResourceAdapterName().equals(raName)) {
                    String msg = I18N.getLocalString(
                            "create.connector.work.security.map.duplicate",
                            "A connector work security map named {0} for resource adapter {1} already exists.",
                            mapName, raName);
                    return new ResourceStatus(ResourceStatus.FAILURE, msg, true);
                }
            }
        }
        return status;
    }

    private WorkSecurityMap createConfigBean(Resources param) throws PropertyVetoException, TransactionFailure {
        WorkSecurityMap workSecurityMap =
                param.createChild(WorkSecurityMap.class);
        workSecurityMap.setName(mapName);
        workSecurityMap.setResourceAdapterName(raName);
        if (principalsMap != null) {
            for (Entry<Object, Object> e : principalsMap.entrySet()) {
                PrincipalMap principalMap = workSecurityMap.createChild(PrincipalMap.class);
                principalMap.setEisPrincipal((String) e.getKey());
                principalMap.setMappedPrincipal((String) e.getValue());
                workSecurityMap.getPrincipalMap().add(principalMap);
            }
        } else if (groupsMap != null) {
            for (Entry<Object, Object> e : groupsMap.entrySet()) {
                GroupMap groupMap = workSecurityMap.createChild(GroupMap.class);
                groupMap.setEisGroup((String) e.getKey());
                groupMap.setMappedGroup((String) e.getValue());
                workSecurityMap.getGroupMap().add(groupMap);
            }
        }
        return workSecurityMap;
    }

    private WorkSecurityMap createResource(Resources param, Properties props) throws PropertyVetoException,
            TransactionFailure {
        WorkSecurityMap newResource = createConfigBean(param);
        param.getResources().add(newResource);
        return newResource;
    }


    private void setAttributes(ResourceAttributes attrList) {
        raName = attrList.getString(ResourceConstants.WORK_SECURITY_MAP_RA_NAME);
        mapName = attrList.getString(ResourceConstants.WORK_SECURITY_MAP_NAME);
        principalsMap = attrList.getProperties(ResourceConstants.WORK_SECURITY_MAP_PRINCIPAL_MAP);
        groupsMap = attrList.getProperties(ResourceConstants.WORK_SECURITY_MAP_GROUP_MAP);
    }

    @Override
    public Resource createConfigBean(Resources resources, ResourceAttributes attributes, Properties properties,
        boolean validate) throws Exception {
        setAttributes(attributes);
        final ResourceStatus status;
        if (validate) {
            status = isValid(resources);
        } else {
            status = new ResourceStatus(ResourceStatus.SUCCESS, "");
        }
        if (status.getStatus() == ResourceStatus.SUCCESS) {
            return createConfigBean(resources);
        }
        throw new ResourceException(status.getMessage(), status.getException());
    }
}
