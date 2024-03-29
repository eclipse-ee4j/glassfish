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

package com.sun.enterprise.deployment.node.runtime;

import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.runtime.connector.MapElement;
import com.sun.enterprise.deployment.runtime.connector.Principal;
import com.sun.enterprise.deployment.runtime.connector.ResourceAdapter;
import com.sun.enterprise.deployment.runtime.connector.RoleMap;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.deployment.common.Descriptor;

/**
 * This class is responsible for instantiating runtime Descriptor classes
 *
 * @author Jerome Dochez
 */
public class RuntimeDescriptorFactory {

    private static final Logger LOG = DOLUtils.getDefaultLogger();
    private static Map<String, Class<? extends Descriptor>> descriptorClasses;

    static {
        initMapping();
    }

    private RuntimeDescriptorFactory() {
        // This is a factory object no need for DescriptorFactory instance
    }


    private static void initMapping() {
        descriptorClasses = new ConcurrentHashMap<>();

        // weblogic DD
        register(new XMLElement(RuntimeTagNames.RESOURCE_DESCRIPTION), ResourceReferenceDescriptor.class);
        register(new XMLElement(RuntimeTagNames.RESOURCE_ENV_DESCRIPTION), ResourceEnvReferenceDescriptor.class);
        register(new XMLElement(RuntimeTagNames.EJB_REFERENCE_DESCRIPTION), EjbReferenceDescriptor.class);

        // connector related
        register(new XMLElement(RuntimeTagNames.PRINCIPAL), Principal.class);
        register(new XMLElement(RuntimeTagNames.BACKEND_PRINCIPAL), Principal.class);
        register(new XMLElement(RuntimeTagNames.MAP_ELEMENT), MapElement.class);
        register(new XMLElement(RuntimeTagNames.ROLE_MAP), RoleMap.class);
        register(new XMLElement(RuntimeTagNames.RESOURCE_ADAPTER), ResourceAdapter.class);

    }


    /**
     * Register a new descriptor class handling a particular XPATH in the DTD.
     *
     * @param xmlElement XML element
     * @param clazz the descriptor class to use
     */
    public static void register(final XMLElement xmlElement, final Class<? extends Descriptor> clazz) {
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
    public static <T extends Descriptor> Class<T> getDescriptorClass(final String xmlPath) {
        String xpathPart = xmlPath;
        do {
            LOG.log(Level.FINEST, "Looking descriptor class for {0}", xpathPart);
            // unchecked - clazz x xmlPath must be unique.
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) descriptorClasses.get(xpathPart);
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
