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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.AdminObject;
import com.sun.enterprise.deployment.AuthMechanism;
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.EntityManagerFactoryReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerReferenceDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.InboundResourceAdapter;
import com.sun.enterprise.deployment.LicenseDescriptor;
import com.sun.enterprise.deployment.MessageListener;
import com.sun.enterprise.deployment.NameValuePairDescriptor;
import com.sun.enterprise.deployment.OutboundResourceAdapter;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.SecurityPermission;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.ConnectorTagNames;
import com.sun.enterprise.deployment.xml.PersistenceTagNames;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;
import com.sun.enterprise.deployment.xml.TagNames;
import com.sun.enterprise.deployment.xml.WebServicesTagNames;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.deployment.common.Descriptor;

/**
 * This class is responsible for instantiating  Descriptor classes
 *
 * @author  Jerome Dochez
 */
public class DescriptorFactory {

    private static final Logger LOG = DOLUtils.getDefaultLogger();
    private static Map<String, Class<? extends Descriptor>> descriptorClasses;

    static {
        initMapping();
    }


    /** This is a factory object no need for DescriptorFactory instance */
    protected DescriptorFactory() {
    }


    private static void initMapping() {
        descriptorClasses = new HashMap<>();

        // Application
        register(new XMLElement(RuntimeTagNames.APPLICATION_PARAM), EnvironmentProperty.class);

        //connector
        register(new XMLElement(ConnectorTagNames.CONNECTOR), ConnectorDescriptor.class);
        register(new XMLElement(ConnectorTagNames.OUTBOUND_RESOURCE_ADAPTER), OutboundResourceAdapter.class);
        register(new XMLElement(ConnectorTagNames.INBOUND_RESOURCE_ADAPTER), InboundResourceAdapter.class);
        register(new XMLElement(ConnectorTagNames.RESOURCE_ADAPTER), OutboundResourceAdapter.class);
        register(new XMLElement(ConnectorTagNames.AUTH_MECHANISM), AuthMechanism.class);
        register(new XMLElement(ConnectorTagNames.SECURITY_PERMISSION), SecurityPermission.class);
        register(new XMLElement(ConnectorTagNames.LICENSE), LicenseDescriptor.class);
        register(new XMLElement(ConnectorTagNames.CONFIG_PROPERTY), ConnectorConfigProperty.class);
        register(new XMLElement(ConnectorTagNames.REQUIRED_CONFIG_PROP), ConnectorConfigProperty.class);
        register(new XMLElement(ConnectorTagNames.MSG_LISTENER), MessageListener.class);
        register(new XMLElement(ConnectorTagNames.ACTIVATION_SPEC),MessageListener.class);
        register(new XMLElement(ConnectorTagNames.ADMIN_OBJECT), AdminObject.class);
        register(new XMLElement(ConnectorTagNames.CONNECTION_DEFINITION), ConnectionDefDescriptor.class);

        // JSR 109 integration
        register(new XMLElement(WebServicesTagNames.SERVICE_REF), ServiceReferenceDescriptor.class);
        register(new XMLElement(WebServicesTagNames.PORT_INFO), com.sun.enterprise.deployment.ServiceRefPortInfo.class);
        register(new XMLElement(WebServicesTagNames.STUB_PROPERTY), NameValuePairDescriptor.class);
        register(new XMLElement(WebServicesTagNames.CALL_PROPERTY), NameValuePairDescriptor.class);

        // persistence.xsd related entries (JSR 220)
        // Note we do not register PersistenceUnitsDescriptor, because that
        // is created by PersistenceDeploymentDescriptorFile.getRootXMLNode().
        register(new XMLElement(PersistenceTagNames.PERSISTENCE_UNIT), PersistenceUnitDescriptor.class);
        register(new XMLElement(TagNames.PERSISTENCE_CONTEXT_REF), EntityManagerReferenceDescriptor.class);
        register(new XMLElement(TagNames.PERSISTENCE_UNIT_REF), EntityManagerFactoryReferenceDescriptor.class);
    }


    /**
     * Register a new descriptor class handling a particular XPATH in the DTD.
     *
     * @param xmlElement XML element
     * @param clazz the descriptor class to use
     */
    public static void register(XMLElement xmlElement, Class<? extends Descriptor> clazz) {
        LOG.log(Level.CONFIG, "Registering {0} to handle xml path {1}", new Object[] {clazz, xmlElement});
        descriptorClasses.put(xmlElement.getQName(), clazz);
    }


    /**
     * @param xmlPath
     * @param <T> expected descriptor type
     * @return a new instance of a registered descriptor class for the supplied XPath
     */
    public static <T extends Descriptor> T getDescriptor(String xmlPath) {
        try {
            Class<T> c = getDescriptorClass(xmlPath);
            if (c == null) {
                return null;
            }
            return c.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Could not create a descriptor instance for " + xmlPath, e);
        }
    }


    /**
     * @param xmlPath
     * @param <T> expected {@link Descriptor} class
     * @return the descriptor tag for a particular XPath
     */
    private static <T extends Descriptor> Class<T> getDescriptorClass(final String xmlPath) {
        String xpathPart = xmlPath;
        do {
            LOG.log(Level.FINEST, "Looking descriptor class for {0}", xpathPart);
            // unchecked - clazz x xmlPath must be unique.
            @SuppressWarnings("unchecked")
            final Class<T> clazz = (Class<T>) descriptorClasses.get(xpathPart);
            if (clazz != null) {
                return clazz;
            }
            if (xpathPart.indexOf('/') == -1) {
                xpathPart = null;
            } else {
                xpathPart = xpathPart.substring(xpathPart.indexOf('/') + 1);
            }
        } while (xpathPart != null);

        throw new IllegalStateException("No descriptor registered for " + xmlPath);
    }
}
