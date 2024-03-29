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

package org.glassfish.ejb.deployment.node;

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.Map;
import java.util.Set;

import org.glassfish.ejb.deployment.EjbTagNames;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.FieldDescriptor;
import org.glassfish.ejb.deployment.descriptor.IASEjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.QueryDescriptor;
import org.w3c.dom.Node;

/**
 * This class handles all information pertinent to CMP and BMP entity beans
 *
 * @author Jerome Dochez
 */
public class EjbEntityNode  extends InterfaceBasedEjbNode<EjbEntityDescriptor> {

    private EjbEntityDescriptor descriptor;

    public EjbEntityNode() {
        registerElementHandler(new XMLElement(EjbTagNames.CMP_FIELD), CmpFieldNode.class);
        registerElementHandler(new XMLElement(EjbTagNames.QUERY), QueryNode.class);
    }

    @Override
    public EjbEntityDescriptor getEjbDescriptor() {
        if (descriptor == null) {
            descriptor = new EjbEntityDescriptor();
            descriptor.setEjbBundleDescriptor((EjbBundleDescriptorImpl) getParentNode().getDescriptor());
        }
        return descriptor;
    }

    /**
     * @return an instance of an EjbCMPEntityDescriptor initialized with all the
     * fields already parsed.
     */
    private EjbCMPEntityDescriptor getCMPEntityDescriptor() {
        EjbDescriptor current = getEjbDescriptor();
        if (!(current instanceof EjbCMPEntityDescriptor)) {
            descriptor = new IASEjbCMPEntityDescriptor(current);
        }
        return (EjbCMPEntityDescriptor) descriptor;
    }


    @Override
    public void addDescriptor(Object newDescriptor) {
        if (newDescriptor instanceof FieldDescriptor) {
            getCMPEntityDescriptor().getPersistenceDescriptor().addCMPField((FieldDescriptor) newDescriptor);
        } else if (newDescriptor instanceof QueryDescriptor) {
            QueryDescriptor newQuery = (QueryDescriptor) newDescriptor;
            getCMPEntityDescriptor().getPersistenceDescriptor().setQueryFor(newQuery.getQueryMethodDescriptor(), newQuery);
        } else {
            super.addDescriptor(newDescriptor);
        }
    }

    @Override
    protected Map<String, String> getDispatchTable() {
        // no need to be synchronized for now
        Map<String, String> table = super.getDispatchTable();
        table.put(EjbTagNames.PERSISTENCE_TYPE, "setPersistenceType");
        table.put(EjbTagNames.PRIMARY_KEY_CLASS, "setPrimaryKeyClassName");
        table.put(EjbTagNames.REENTRANT, "setReentrant");
        return table;
    }

    @Override
    public void setElementValue(XMLElement element, String value) {
        if (EjbTagNames.CMP_VERSION.equals(element.getQName())) {
            if (EjbTagNames.CMP_1_VERSION.equals(value)) {
                getCMPEntityDescriptor().setCMPVersion(EjbCMPEntityDescriptor.CMP_1_1);
            } else if (EjbTagNames.CMP_2_VERSION.equals(value)) {
                getCMPEntityDescriptor().setCMPVersion(EjbCMPEntityDescriptor.CMP_2_x);
            }
        } else if (EjbTagNames.ABSTRACT_SCHEMA_NAME.equals(element.getQName())) {
            getCMPEntityDescriptor().setAbstractSchemaName(value);
        } else  if (EjbTagNames.PRIMARY_KEY_FIELD.equals(element.getQName())) {
            getCMPEntityDescriptor().setPrimaryKeyFieldDesc(new FieldDescriptor(value));
        } else {
            super.setElementValue(element, value);
        }
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, EjbEntityDescriptor ejbDesc) {
        Node ejbNode = super.writeDescriptor(parent, nodeName, ejbDesc);
        writeDisplayableComponentInfo(ejbNode, ejbDesc);
        writeCommonHeaderEjbDescriptor(ejbNode, ejbDesc);
        appendTextChild(ejbNode, EjbTagNames.PERSISTENCE_TYPE, ejbDesc.getPersistenceType());
        appendTextChild(ejbNode, EjbTagNames.PRIMARY_KEY_CLASS, ejbDesc.getPrimaryKeyClassName());
        appendTextChild(ejbNode, EjbTagNames.REENTRANT, ejbDesc.getReentrant());

        // cmp entity beans related tags
        if (ejbDesc instanceof EjbCMPEntityDescriptor) {
            EjbCMPEntityDescriptor cmpDesc = (EjbCMPEntityDescriptor) ejbDesc;
            if (cmpDesc.getCMPVersion()==EjbCMPEntityDescriptor.CMP_1_1) {
                appendTextChild(ejbNode, EjbTagNames.CMP_VERSION, EjbTagNames.CMP_1_VERSION);
            } else {
                appendTextChild(ejbNode, EjbTagNames.CMP_VERSION, EjbTagNames.CMP_2_VERSION);
            }

            appendTextChild(ejbNode, EjbTagNames.ABSTRACT_SCHEMA_NAME, cmpDesc.getAbstractSchemaName());
            // cmp-field*
            CmpFieldNode cmpNode = new CmpFieldNode();
            for (Object element : cmpDesc.getPersistenceDescriptor().getCMPFields()) {
                FieldDescriptor aField = (FieldDescriptor) element;
                cmpNode.writeDescriptor(ejbNode, EjbTagNames.CMP_FIELD, aField);
            }
            if ( cmpDesc.getPrimaryKeyFieldDesc()!=null) {
                appendTextChild(ejbNode, EjbTagNames.PRIMARY_KEY_FIELD, cmpDesc.getPrimaryKeyFieldDesc().getName());
            }
        }

        // env-entry*
        writeEnvEntryDescriptors(ejbNode, ejbDesc.getEnvironmentProperties().iterator());

        // ejb-ref * and ejb-local-ref*
        writeEjbReferenceDescriptors(ejbNode, ejbDesc.getEjbReferenceDescriptors().iterator());

        // service-ref*
        writeServiceReferenceDescriptors(ejbNode, ejbDesc.getServiceReferenceDescriptors().iterator());

        // resource-ref*
        writeResourceRefDescriptors(ejbNode, ejbDesc.getResourceReferenceDescriptors().iterator());

        // resource-env-ref*
        writeResourceEnvRefDescriptors(ejbNode, ejbDesc.getResourceEnvReferenceDescriptors().iterator());

        // message-destination-ref*
        writeMessageDestinationRefDescriptors(ejbNode, ejbDesc.getMessageDestinationReferenceDescriptors().iterator());

        // persistence-context-ref*
        writeEntityManagerReferenceDescriptors(ejbNode, ejbDesc.getEntityManagerReferenceDescriptors().iterator());

        // persistence-unit-ref*
        writeEntityManagerFactoryReferenceDescriptors(ejbNode, ejbDesc.getEntityManagerFactoryReferenceDescriptors().iterator());

        // post-construct
        writeLifeCycleCallbackDescriptors(ejbNode, TagNames.POST_CONSTRUCT, ejbDesc.getPostConstructDescriptors());

        // pre-destroy
        writeLifeCycleCallbackDescriptors(ejbNode, TagNames.PRE_DESTROY, ejbDesc.getPreDestroyDescriptors());

        // all descriptors (includes DSD, MSD, JMSCFD, JMSDD,AOD, CFD)*
        writeResourceDescriptors(ejbNode, ejbDesc.getAllResourcesDescriptors().iterator());

        // security-role-ref*
        writeRoleReferenceDescriptors(ejbNode, ejbDesc.getRoleReferences().iterator());

        // security-identity
        writeSecurityIdentityDescriptor(ejbNode, ejbDesc);

        // query
        if (ejbDesc instanceof EjbCMPEntityDescriptor) {
            EjbCMPEntityDescriptor cmpDesc = (EjbCMPEntityDescriptor) ejbDesc;
            Set<MethodDescriptor> queriedMethods = cmpDesc.getPersistenceDescriptor().getQueriedMethods();
            if (!queriedMethods.isEmpty()) {
                QueryNode queryNode = new QueryNode();
                for (MethodDescriptor queriedMethod : queriedMethods) {
                    queryNode.writeDescriptor(ejbNode, EjbTagNames.QUERY,
                        cmpDesc.getPersistenceDescriptor().getQueryFor(queriedMethod));
                }
            }
        }
        return ejbNode;
    }
}
