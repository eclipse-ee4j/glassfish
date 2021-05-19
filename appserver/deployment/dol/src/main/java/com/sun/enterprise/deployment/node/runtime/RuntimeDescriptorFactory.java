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

package com.sun.enterprise.deployment.node.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.types.EjbReference;
import com.sun.enterprise.deployment.runtime.connector.MapElement;
import com.sun.enterprise.deployment.runtime.connector.Principal;
import com.sun.enterprise.deployment.runtime.connector.ResourceAdapter;
import com.sun.enterprise.deployment.runtime.connector.RoleMap;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

/**
 * This class is responsible for instantiating  runtime Descriptor classes
 *
 * @author  Jerome Dochez
 * @version
 */
public class RuntimeDescriptorFactory {

    static Map descriptorClasses;

    /** This is a factory object no need for DescriptorFactory instance */
    protected RuntimeDescriptorFactory() {
    }


    private static void initMapping() {
        descriptorClasses = new HashMap();

        // weblogic DD
        register(new XMLElement(RuntimeTagNames.RESOURCE_DESCRIPTION), ResourceReferenceDescriptor.class);
        register(new XMLElement(RuntimeTagNames.RESOURCE_ENV_DESCRIPTION), ResourceEnvReferenceDescriptor.class);
        register(new XMLElement(RuntimeTagNames.EJB_REFERENCE_DESCRIPTION), EjbReference.class);

        // connector related
        register(new XMLElement(RuntimeTagNames.PRINCIPAL), Principal.class);
        register(new XMLElement(RuntimeTagNames.BACKEND_PRINCIPAL), Principal.class);
        register(new XMLElement(RuntimeTagNames.MAP_ELEMENT), MapElement.class);
        register(new XMLElement(RuntimeTagNames.ROLE_MAP), RoleMap.class);
        register(new XMLElement(RuntimeTagNames.RESOURCE_ADAPTER), ResourceAdapter.class);

    }

    /**
     * register a new descriptor class handling a particular XPATH in the DTD.
     *
     * @param xmlPath absolute or relative XPath
     * @param clazz the descriptor class to use
     */
    public static void register(XMLElement  xmlPath, Class clazz) {
        if (DOLUtils.getDefaultLogger().isLoggable(Level.FINE)) {
            DOLUtils.getDefaultLogger().fine("Register " + clazz + " to handle " + xmlPath.getQName());
        }
        descriptorClasses.put(xmlPath.getQName(), clazz);
    }

    /**
     * @return the descriptor tag for a particular XPath
     */
    public static Class getDescriptorClass(String xmlPath) {
        String s = xmlPath;
        do {
            if (DOLUtils.getDefaultLogger().isLoggable(Level.FINER)) {
                DOLUtils.getDefaultLogger().finer("looking for " + xmlPath + " in " + descriptorClasses);
            }
            if (descriptorClasses.containsKey(xmlPath)) {
                return (Class) descriptorClasses.get(xmlPath);
            }
            if (xmlPath.indexOf('/') != -1) {
                xmlPath = xmlPath.substring(xmlPath.indexOf('/') + 1);
            } else {
                xmlPath = null;
            }
        } while (xmlPath != null);

        if (DOLUtils.getDefaultLogger().isLoggable(Level.FINE)) {
            DOLUtils.getDefaultLogger().fine("No descriptor registered for " + s);
        }
        return null;
    }

    /**
     * @return a new instance of a registered descriptor class for the
     * supplied XPath
     */
    public static Object  getDescriptor(String xmlPath) {
        try {
            Class c = getDescriptorClass(xmlPath);
            if (c != null) {
                return c.newInstance();
            }
        } catch (Throwable t) {
            Logger.getAnonymousLogger().log(Level.WARNING, "Error occurred", t);
        }
        return null;
    }

    static {
        initMapping();
    }
}
