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

package org.glassfish.resources.admin.cli;

import com.sun.enterprise.config.serverbeans.Resources;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.glassfish.resourcebase.resources.api.ResourceStatus;
import org.glassfish.resources.api.Resource;

/**
 * This class serves as the API to creating new resources when an xml file
 * is supplied containing the resource definitions
 *
 * @author PRASHANTH ABBAGANI
 */
public class ResourcesManager {

     /**
     * Creating resources from sun-resources.xml file. This method is used by
     * the admin framework when the add-resources command is used to create
     * resources
     */
    public static ArrayList createResources(Resources resources, File resourceXMLFile,
            String target, org.glassfish.resources.admin.cli.ResourceFactory resourceFactory) throws Exception {
        ArrayList results = new ArrayList();
        org.glassfish.resources.admin.cli.ResourcesXMLParser resourcesParser =
            new org.glassfish.resources.admin.cli.ResourcesXMLParser(resourceXMLFile);
        List<Resource> vResources = resourcesParser.getResourcesList();
        //First add all non connector resources.
        Iterator<Resource> nonConnectorResources = org.glassfish.resources.admin.cli.ResourcesXMLParser.getNonConnectorResourcesList(vResources, false, false).iterator();
        while (nonConnectorResources.hasNext()) {
            Resource resource = (Resource) nonConnectorResources.next();
            HashMap attrList = resource.getAttributes();
            String desc = resource.getDescription();
            if (desc != null)
                attrList.put("description", desc);

            Properties props = resource.getProperties();

            ResourceStatus rs;
            try {
                org.glassfish.resources.admin.cli.ResourceManager rm = resourceFactory.getResourceManager(resource);
                rs = rm.create(resources, attrList, props, target);
            } catch (Exception e) {
                String msg = e.getMessage();
                rs = new ResourceStatus(ResourceStatus.FAILURE, msg);
            }
            results.add(rs);
        }

        //Now add all connector resources
        Iterator connectorResources = org.glassfish.resources.admin.cli.ResourcesXMLParser.getConnectorResourcesList(vResources, false, false).iterator();
        while (connectorResources.hasNext()) {
            Resource resource = (Resource) connectorResources.next();
            HashMap attrList = resource.getAttributes();
            String desc = resource.getDescription();
            if (desc != null)
                attrList.put("description", desc);

            Properties props = resource.getProperties();

            ResourceStatus rs;
            try {
                org.glassfish.resources.admin.cli.ResourceManager rm = resourceFactory.getResourceManager(resource);
                rs = rm.create(resources, attrList, props, target);
            } catch (Exception e) {
                String msg = e.getMessage();
                rs = new ResourceStatus(ResourceStatus.FAILURE, msg);
            }
            results.add(rs);
        }

        return results;
    }

}
