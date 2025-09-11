/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.resources.module;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.resourcebase.resources.api.ResourceConflictException;
import org.glassfish.resources.admin.cli.SunResourcesXML;
import org.glassfish.resources.api.Resource;
import org.glassfish.resources.api.ResourceAttributes;

import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_POOL_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.RESOURCE_ADAPTER_CONFIG_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.SECURITY_MAP_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.WORK_SECURITY_MAP_NAME;

/**
 * A class that holds utility/helper routines. Expected to contain static
 * methods to perform utility operations.
 *
 * @since Appserver 9.0
 */
public class ResourceUtilities {

    private final static Logger _logger = LogDomains.getLogger(ResourceUtilities.class, LogDomains.RSR_LOGGER);
    private final static StringManager localStrings = StringManager.getManager(ResourceUtilities.class);

    private ResourceUtilities()/* disallowed */ {
    }


    private static SimpleJndiName getIdToCompare(final Resource res) {
        final ResourceAttributes attrs = res.getAttributes();
        final String type = res.getType();
        final String id;
        if (org.glassfish.resources.api.Resource.JDBC_CONNECTION_POOL.equals(type) ||
            Resource.CONNECTOR_CONNECTION_POOL.equals(type)){
            id = getNamedAttributeValue(attrs, CONNECTION_POOL_NAME);   // this should come from refactored stuff TBD
        }
        else if (org.glassfish.resources.api.Resource.CONNECTOR_SECURITY_MAP.equals(type)) {
            id = getNamedAttributeValue(attrs, SECURITY_MAP_NAME);  // this should come from refactored stuff TBD
        }
        else if (org.glassfish.resources.api.Resource.RESOURCE_ADAPTER_CONFIG.equals(type)) {
            id = getNamedAttributeValue(attrs, RESOURCE_ADAPTER_CONFIG_NAME);  // this should come from refactored stuff TBD
        }
        else if(org.glassfish.resources.api.Resource.CONNECTOR_WORK_SECURITY_MAP.equals(type)){
            id = getNamedAttributeValue(attrs, WORK_SECURITY_MAP_NAME);
        }
        else {
            //it is OK to assume that this Resource will one of the *RESOURCEs?
            id = getNamedAttributeValue(attrs, JNDI_NAME); // this should come from refactored stuff TBD
        }
        return SimpleJndiName.of(id);
    }

    private static String getNamedAttributeValue(final ResourceAttributes attrs, final String name) {
        return attrs.getString(name);
    }

    /**
     * Resolves all duplicates and conflicts within an archive and returns a set
     * of resources that needs to be created for the archive being deployed. The
     * deployment backend would then use these set of resources to check for
     * conflicts with resources existing in domain.xml and then continue
     * with deployment.
     *
     * All resource duplicates within an archive found are flagged with a
     * WARNING and only one resource is added in the final <code>Resource</code>
     * <code>Set</code> returned.
     *
     * We currently do not handle any resource conflicts found within the archive
     * and the method throws an exception when this condition is detected.
     *
     * @param sunResList a list of <code>SunResourcesXML</code> corresponding to
     * sun-resources.xml found within an archive.
     *
     * @return a Set of <code>Resource</code>s that have been resolved of
     * duplicates and conflicts.
     *
     * @throws ResourceConflictException an exception is thrown when an archive is found to
     * have two or more resources that conflict with each other.
     */
    public static Set<org.glassfish.resources.api.Resource> resolveResourceDuplicatesConflictsWithinArchive(
        List<org.glassfish.resources.admin.cli.SunResourcesXML> sunResList) throws ResourceConflictException {
        StringBuilder conflictingResources = new StringBuilder();
        Set<org.glassfish.resources.api.Resource> resourceSet = new HashSet<>();
        for (SunResourcesXML sunResXML : sunResList) {
            List<org.glassfish.resources.api.Resource> resources = sunResXML.getResourcesList();

            //for each resource mentioned
            for (Resource res : resources) {
                boolean addResource = true;
                //check if a duplicate has already been added
                for (Resource existingRes : resourceSet) {
                    if(existingRes.equals(res)){
                        //duplicate within an archive
                        addResource = false;
                        _logger.warning(localStrings.getString("duplicate.resource.sun.resource.xml",
                            getIdToCompare(res), sunResXML.getXMLPath()));
                        break;
                    }
                    //check if another existing resource conflicts with the
                    //resource being added
                    if(existingRes.isAConflict(res)){
                        //conflict within an archive
                        addResource = false;
                        conflictingResources.append("\n");
                        String message = localStrings.getString("conflict.resource.sun.resource.xml",
                            getIdToCompare(res), sunResXML.getXMLPath());
                        conflictingResources.append(message);
                        _logger.warning(message);
                        _logger.fine(localStrings.getString("resource.attributes", res.getAttributes()));
                    }
                }
                if(addResource) {
                    resourceSet.add(res);
                }
            }
        }
        if(conflictingResources.toString().length() > 0){
            throw new ResourceConflictException(conflictingResources.toString());
        }
        return resourceSet;
    }
}
