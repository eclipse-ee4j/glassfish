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

import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.MethodPermission;
import com.sun.enterprise.deployment.MethodPermissionDescriptor;
import com.sun.enterprise.deployment.node.AbstractBundleNode;
import com.sun.enterprise.deployment.node.DeploymentDescriptorNode;
import com.sun.enterprise.deployment.node.MessageDestinationNode;
import com.sun.enterprise.deployment.node.SaxParserHandler;
import com.sun.enterprise.deployment.node.SecurityRoleNode;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.TagNames;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.glassfish.ejb.deployment.descriptor.EjbApplicationExceptionInfo;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbMessageBeanDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;
import org.glassfish.ejb.deployment.descriptor.RelationshipDescriptor;
import org.glassfish.ejb.deployment.node.runtime.EjbBundleRuntimeNode;
import org.glassfish.ejb.deployment.node.runtime.GFEjbBundleRuntimeNode;
import org.glassfish.security.common.Role;
import org.jvnet.hk2.annotations.Service;
import org.w3c.dom.Node;

import static org.glassfish.ejb.deployment.EjbTagNames.APPLICATION_EXCEPTION;
import static org.glassfish.ejb.deployment.EjbTagNames.ASSEMBLY_DESCRIPTOR;
import static org.glassfish.ejb.deployment.EjbTagNames.CONTAINER_TRANSACTION;
import static org.glassfish.ejb.deployment.EjbTagNames.EJBS;
import static org.glassfish.ejb.deployment.EjbTagNames.EJB_BUNDLE_TAG;
import static org.glassfish.ejb.deployment.EjbTagNames.EJB_CLIENT_JAR;
import static org.glassfish.ejb.deployment.EjbTagNames.ENTITY;
import static org.glassfish.ejb.deployment.EjbTagNames.EXCLUDE_LIST;
import static org.glassfish.ejb.deployment.EjbTagNames.INTERCEPTOR;
import static org.glassfish.ejb.deployment.EjbTagNames.INTERCEPTORS;
import static org.glassfish.ejb.deployment.EjbTagNames.INTERCEPTOR_BINDING;
import static org.glassfish.ejb.deployment.EjbTagNames.MESSAGE_DRIVEN;
import static org.glassfish.ejb.deployment.EjbTagNames.METHOD_PERMISSION;
import static org.glassfish.ejb.deployment.EjbTagNames.RELATIONSHIPS;
import static org.glassfish.ejb.deployment.EjbTagNames.SESSION;

/**
 * This class handles ejb bundle xml files
 *
 * @author  Jerome Dochez
 * @version
 */
@Service
public class EjbBundleNode extends AbstractBundleNode<EjbBundleDescriptorImpl> {

    private static final XMLElement tag = new XMLElement(EJB_BUNDLE_TAG);
    private static final String PUBLIC_DTD_ID = "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN";
    private static final String PUBLIC_DTD_ID_12 = "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN";

    /** The system ID of an ejb-jar document.*/
    private static final String SYSTEM_ID = "http://java.sun.com/dtd/ejb-jar_2_0.dtd";
    private static final String SYSTEM_ID_12 = "http://java.sun.com/dtd/ejb-jar_1_1.dtd";
    private static final String SCHEMA_ID_21 = "ejb-jar_2_1.xsd";
    private static final String SCHEMA_ID_30 = "ejb-jar_3_0.xsd";
    private static final String SCHEMA_ID_31 = "ejb-jar_3_1.xsd";
    private static final String SCHEMA_ID_32 = "ejb-jar_3_2.xsd";
    private static final String SCHEMA_ID = "ejb-jar_4_0.xsd";
    public static final String SPEC_VERSION = "4.0";
    private static final List<String> SYSTEM_IDS = List.of(
        SCHEMA_ID, SCHEMA_ID_32, SCHEMA_ID_31, SCHEMA_ID_30, SCHEMA_ID_21);

    // Descriptor class we are using
    private EjbBundleDescriptorImpl descriptor;

    /**
     * register this node as a root node capable of loading entire DD files
     *
     * @param publicIDToDTD is a mapping between xml Public-ID to DTD
     * @return the doctype tag name
     */
    @Override
    public String registerBundle(Map<String, String> publicIDToDTD) {
        publicIDToDTD.put(PUBLIC_DTD_ID, SYSTEM_ID);
        publicIDToDTD.put(PUBLIC_DTD_ID_12, SYSTEM_ID_12);
        return tag.getQName();
    }

    @Override
    public Map<String, Class<?>> registerRuntimeBundle(
        final Map<String, String> publicIDToDTD,
        final Map<String, List<Class<?>>> versionUpgrades) {
        final Map<String, Class<?>> result = new HashMap<>();
        result.put(EjbBundleRuntimeNode.registerBundle(publicIDToDTD), EjbBundleRuntimeNode.class);
        result.put(GFEjbBundleRuntimeNode.registerBundle(publicIDToDTD), GFEjbBundleRuntimeNode.class);
        return result;
    }


    public EjbBundleNode() {
        // register sub XMLNodes
        registerElementHandler(new XMLElement(SESSION), EjbSessionNode.class);
        registerElementHandler(new XMLElement(ENTITY), EjbEntityNode.class);
        registerElementHandler(new XMLElement(MESSAGE_DRIVEN), MessageDrivenBeanNode.class);
        registerElementHandler(new XMLElement(METHOD_PERMISSION), MethodPermissionNode.class);
        registerElementHandler(new XMLElement(TagNames.ROLE), SecurityRoleNode.class, "addRole");
        registerElementHandler(new XMLElement(CONTAINER_TRANSACTION), ContainerTransactionNode.class);
        registerElementHandler(new XMLElement(EXCLUDE_LIST), ExcludeListNode.class);
        registerElementHandler(new XMLElement(RELATIONSHIPS), RelationshipsNode.class);
        registerElementHandler(new XMLElement(TagNames.MESSAGE_DESTINATION), MessageDestinationNode.class,
            "addMessageDestination");
        registerElementHandler(new XMLElement(APPLICATION_EXCEPTION), EjbApplicationExceptionNode.class,
            "addApplicationException");
        registerElementHandler(new XMLElement(INTERCEPTOR), EjbInterceptorNode.class, "addInterceptor");

        registerElementHandler(new XMLElement(INTERCEPTOR_BINDING), InterceptorBindingNode.class,
            "appendInterceptorBinding");

        SaxParserHandler.registerBundleNode(this, EJB_BUNDLE_TAG);
    }


    @Override
    public void addDescriptor(Object newDescriptor) {
        if (newDescriptor instanceof EjbDescriptor) {
            descriptor.addEjb((EjbDescriptor) newDescriptor);
        } else if (newDescriptor instanceof RelationshipDescriptor) {
            descriptor.addRelationship((RelationshipDescriptor) newDescriptor);
        } else if (newDescriptor instanceof MethodPermissionDescriptor) {
            MethodPermissionDescriptor nd = (MethodPermissionDescriptor) newDescriptor;
            MethodDescriptor[] array = nd.getMethods();
            for (MethodDescriptor element : array) {
                EjbDescriptor target = descriptor.getEjbByName(element.getEjbName());
                MethodPermission[] mps = nd.getMethodPermissions();
                for (MethodPermission mp : mps) {
                    DOLUtils.getDefaultLogger()
                        .fine("Adding mp " + mp + " to " + element + " for ejb " + element.getEjbName());
                    target.addPermissionedMethod(mp, element);
                }
            }
        } else {
            super.addDescriptor(newDescriptor);
        }
    }


    @Override
    public void setElementValue(XMLElement element, String value) {
        if (TagNames.MODULE_NAME.equals(element.getQName())) {
            EjbBundleDescriptorImpl bundleDesc = getDescriptor();
            // ejb-jar.xml <module-name> only applies if this is an ejb-jar
            if (bundleDesc.getModuleDescriptor().getDescriptor() instanceof EjbBundleDescriptorImpl) {
                bundleDesc.getModuleDescriptor().setModuleName(value);
            }
        } else {
            super.setElementValue(element, value);
        }
    }


    @Override
    public EjbBundleDescriptorImpl getDescriptor() {
        if (descriptor == null) {
            descriptor = new EjbBundleDescriptorImpl();
        }
        return descriptor;
    }


    @Override
    protected XMLElement getXMLRootTag() {
        return tag;
    }


    @Override
    protected Map<String, String> getDispatchTable() {
        // no need to be synchronized for now
        Map<String, String> table = super.getDispatchTable();
        table.put(EJB_CLIENT_JAR, "setEjbClientJarUri");
        return table;
    }


    @Override
    public Node writeDescriptor(Node parent, EjbBundleDescriptorImpl ejbDesc) {
        Node jarNode = super.writeDescriptor(parent, ejbDesc);
        Node entrepriseBeansNode = appendChild(jarNode, EJBS);
        for (EjbDescriptor ejb : ejbDesc.getEjbs()) {
            if (com.sun.enterprise.deployment.EjbSessionDescriptor.TYPE.equals(ejb.getType())) {
                EjbSessionNode subNode = new EjbSessionNode();
                subNode.writeDescriptor(entrepriseBeansNode, SESSION, (EjbSessionDescriptor) ejb);
            } else if (EjbEntityDescriptor.TYPE.equals(ejb.getType())) {
                EjbEntityNode subNode = new EjbEntityNode();
                subNode.writeDescriptor(entrepriseBeansNode, ENTITY, (EjbEntityDescriptor) ejb);
            } else if (com.sun.enterprise.deployment.EjbMessageBeanDescriptor.TYPE.equals(ejb.getType())) {
                MessageDrivenBeanNode subNode = new MessageDrivenBeanNode();
                subNode.writeDescriptor(entrepriseBeansNode, MESSAGE_DRIVEN, (EjbMessageBeanDescriptor) ejb);
            } else {
                throw new IllegalStateException("Unknow ejb type " + ejb.getType());
            }
        }

        if (ejbDesc.hasInterceptors()) {
            Node interceptorsNode = appendChild(jarNode, INTERCEPTORS);
            EjbInterceptorNode interceptorNode = new EjbInterceptorNode();
            for (EjbInterceptor next : ejbDesc.getInterceptors()) {
                interceptorNode.writeDescriptor(interceptorsNode, INTERCEPTOR, next);
            }
        }

        // relationships*
        if (ejbDesc.hasRelationships()) {
            RelationshipsNode.writeRelationships(jarNode, RELATIONSHIPS, ejbDesc);
        }

        // assembly-descriptor
        writeAssemblyDescriptor(jarNode, ejbDesc);

        appendTextChild(jarNode, EJB_CLIENT_JAR, ejbDesc.getEjbClientJarUri());
        return jarNode;
    }


    @Override
    public String getDocType() {
        return null;
    }


    @Override
    public String getSystemID() {
        return SCHEMA_ID;
    }


    @Override
    public List<String> getSystemIDs() {
        return SYSTEM_IDS;
    }


    @Override
    public String getSpecVersion() {
        return SPEC_VERSION;
    }


    /**
     * Write assembly-descriptor related xml information to the DOM tree
     */
    private void writeAssemblyDescriptor(Node parentNode, EjbBundleDescriptorImpl bundleDescriptor) {
        Node assemblyNode = parentNode.getOwnerDocument().createElement(ASSEMBLY_DESCRIPTOR);

        // security-role*
        SecurityRoleNode roleNode = new SecurityRoleNode();
        for (Object element : bundleDescriptor.getRoles()) {
            roleNode.writeDescriptor(assemblyNode, TagNames.ROLE, (Role) element);
        }

        // method-permission*
        Map<EjbDescriptor, Vector<MethodDescriptor>> excludedMethodsByEjb = new HashMap<>();
        MethodPermissionNode mpNode = new MethodPermissionNode();
        for (EjbDescriptor ejbDesc : bundleDescriptor.getEjbs()) {
            if (ejbDesc instanceof EjbMessageBeanDescriptor) {
                continue;
            }
            Vector<MethodDescriptor> excludedMethods = new Vector<>();
            addMethodPermissions(ejbDesc, ejbDesc.getPermissionedMethodsByPermission(), excludedMethods, mpNode,
                assemblyNode);
            addMethodPermissions(ejbDesc, ejbDesc.getStyledPermissionedMethodsByPermission(), excludedMethods, mpNode,
                assemblyNode);
            if (!excludedMethods.isEmpty()) {
                excludedMethodsByEjb.put(ejbDesc, excludedMethods);
            }
        }

        // container-transaction*
        for (EjbDescriptor ejbDesc : bundleDescriptor.getEjbs()) {
            ContainerTransactionNode.writeContainerTransactions(assemblyNode, CONTAINER_TRANSACTION,
                ejbDesc.getName(), ejbDesc.getMethodContainerTransactions());
        }

        // interceptor-binding*
        InterceptorBindingNode ibNode = new InterceptorBindingNode();
        for (EjbDescriptor ejbDesc : bundleDescriptor.getEjbs()) {
            if (!ejbDesc.getInterceptorClasses().isEmpty()) {
                ibNode.writeBindings(assemblyNode, ejbDesc);
            }
        }

        // message-destination*
        writeMessageDestinations(assemblyNode, bundleDescriptor.getMessageDestinations().iterator());

        // exclude-list*
        if (!excludedMethodsByEjb.isEmpty()) {
            Node excludeListNode = DeploymentDescriptorNode.appendChild(assemblyNode, EXCLUDE_LIST);
            for (Entry<EjbDescriptor, Vector<MethodDescriptor>> entry : excludedMethodsByEjb.entrySet()) {
                EjbDescriptor ejbDesc = entry.getKey();
                Vector<MethodDescriptor> excludedMethods = entry.getValue();
                MethodPermissionDescriptor mpd = new MethodPermissionDescriptor();
                mpd.addMethodPermission(MethodPermission.getDenyAllMethodPermission());
                mpd.addMethods(excludedMethods);
                mpNode.writeDescriptorInNode(excludeListNode, mpd, ejbDesc);
            }
        }

        for (EjbApplicationExceptionInfo next : bundleDescriptor.getApplicationExceptions().values()) {
            EjbApplicationExceptionNode node = new EjbApplicationExceptionNode();
            node.writeDescriptor(assemblyNode, APPLICATION_EXCEPTION, next);
        }

        if (assemblyNode.hasChildNodes()) {
            parentNode.appendChild(assemblyNode);
        }
    }

    private void addMethodPermissions(
            EjbDescriptor ejb,
            Map<MethodPermission, Set<MethodDescriptor>> mpToMethods,
            Vector<MethodDescriptor> excludedMethods,
            MethodPermissionNode mpNode,
            Node assemblyNode) {
        if (mpToMethods == null || mpToMethods.isEmpty()) {
            return;
        }

        for (Entry<MethodPermission, Set<MethodDescriptor>> entry : mpToMethods.entrySet()) {
            MethodPermission mp = entry.getKey();
            if (mp.isExcluded()) {
                // we need to be sure the method descriptors knows who owns them
                Set<MethodDescriptor> methods = entry.getValue();
                excludedMethods.addAll(methods);
            } else {
                MethodPermissionDescriptor mpd = new MethodPermissionDescriptor();
                mpd.addMethodPermission(mp);
                mpd.addMethods(mpToMethods.get(mp));
                mpNode.writeDescriptor(assemblyNode, METHOD_PERMISSION, mpd, ejb);
            }
        }
    }
}
