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

package org.glassfish.ejb.deployment.node.runtime;

import com.sun.enterprise.deployment.NameValuePairDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.node.PropertiesNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.DefaultResourcePrincipalNode;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.node.runtime.common.RuntimeNameValuePairNode;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.w3c.dom.Node;

/**
 * This node handles the cmp-resource runtime xml tag
 *
 * @author  Jerome Dochez
 * @version
 */
public class CmpResourceNode extends RuntimeDescriptorNode<ResourceReferenceDescriptor> {

    private ResourceReferenceDescriptor descriptor;

    public CmpResourceNode() {
        registerElementHandler(new XMLElement(RuntimeTagNames.DEFAULT_RESOURCE_PRINCIPAL),
                               DefaultResourcePrincipalNode.class, "setResourcePrincipal");
        registerElementHandler(new XMLElement(RuntimeTagNames.PROPERTY),
                               RuntimeNameValuePairNode.class, "addProperty");
        registerElementHandler(new XMLElement(RuntimeTagNames.SCHEMA_GENERATOR_PROPERTIES),
                                PropertiesNode.class, "setSchemaGeneratorProperties");
    }

    @Override
    public ResourceReferenceDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new ResourceReferenceDescriptor();
        }
        return descriptor;
    }

    @Override
    protected Map getDispatchTable() {
        Map<String, String> table = super.getDispatchTable();
        table.put(RuntimeTagNames.JNDI_NAME, "setJndiName");
        table.put(RuntimeTagNames.CREATE_TABLES_AT_DEPLOY, "setCreateTablesAtDeploy");
        table.put(RuntimeTagNames.DROP_TABLES_AT_UNDEPLOY, "setDropTablesAtUndeploy");
        table.put(RuntimeTagNames.DATABASE_VENDOR_NAME, "setDatabaseVendorName");
        return table;
    }

    @Override
    public void postParsing() {
        EjbBundleDescriptorImpl bd = (EjbBundleDescriptorImpl) getParentNode().getDescriptor();
        if (bd == null) {
            DOLUtils.getDefaultLogger().log(Level.SEVERE, DOLUtils.ADD_DESCRIPTOR_FAILURE, new Object[] {descriptor});
            return;
        }
        bd.setCMPResourceReference(descriptor);
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, ResourceReferenceDescriptor descriptor) {
        Node cmp = super.writeDescriptor(parent, nodeName, descriptor);
        appendTextChild(cmp, RuntimeTagNames.JNDI_NAME, descriptor.getJndiName());
        if (descriptor.getResourcePrincipal() != null) {
            DefaultResourcePrincipalNode drpNode = new DefaultResourcePrincipalNode();
            drpNode.writeDescriptor(cmp, RuntimeTagNames.DEFAULT_RESOURCE_PRINCIPAL,
                descriptor.getResourcePrincipal());
        }
        // properties*
        Iterator properties = descriptor.getProperties();
        if (properties!=null) {
            RuntimeNameValuePairNode propNode = new RuntimeNameValuePairNode();
            while (properties.hasNext()) {
                NameValuePairDescriptor aProp = (NameValuePairDescriptor) properties.next();
                propNode.writeDescriptor(cmp, RuntimeTagNames.PROPERTY, aProp);
            }
        }

        // createTableAtDeploy, dropTableAtUndeploy
        if (descriptor.isCreateTablesAtDeploy()) {
            appendTextChild(cmp, RuntimeTagNames.CREATE_TABLES_AT_DEPLOY, RuntimeTagNames.TRUE);
        }
        if (descriptor.isDropTablesAtUndeploy()) {
            appendTextChild(cmp, RuntimeTagNames.DROP_TABLES_AT_UNDEPLOY, RuntimeTagNames.TRUE);
        }
        // database vendor name
        appendTextChild(cmp, RuntimeTagNames.DATABASE_VENDOR_NAME, descriptor.getDatabaseVendorName());

        // schema-generator-properties?
        Properties schemaGeneratorProps = descriptor.getSchemaGeneratorProperties();
        if (schemaGeneratorProps!=null) {
            PropertiesNode pn = new PropertiesNode();
            pn.writeDescriptor(cmp, RuntimeTagNames.SCHEMA_GENERATOR_PROPERTIES, schemaGeneratorProps);
        }

        return cmp;
    }
}
