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

package org.glassfish.resources.naming;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.ObjectFactory;

import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resourcebase.resources.util.ResourceUtil;

/**
 * A proxy object factory for an external JNDI factory
 */
public class JndiProxyObjectFactory implements ObjectFactory {

    // for every external-jndi-resource there is an InitialContext
    // created from the factory and environment properties
    private static Hashtable<ResourceInfo, Context> contextMap = new Hashtable<>();

    public static Context removeInitialContext(ResourceInfo resourceInfo) {
        return contextMap.remove(resourceInfo);
    }


    /**
     * load the context factory
     */
    private Context loadInitialContext(String factoryClass, Hashtable env) {
        Object factory = ResourceUtil.loadObject(factoryClass);
        if (factory == null) {
            System.err.println("Cannot load external-jndi-resource " + "factory-class '" + factoryClass + "'");
            return null;
        } else if (!(factory instanceof InitialContextFactory)) {

            System.err.println("external-jndi-resource factory-class '" + factoryClass + "' must be of type "
                + "javax.naming.spi.InitialContextFactory");
            return null;
        }

        Context context = null;
        try {
            context = ((InitialContextFactory) factory).getInitialContext(env);
        } catch (NamingException ne) {
            System.err.println("Exception thrown creating initial context " + "for external JNDI factory '"
                + factoryClass + "' " + ne.getMessage());
        }

        return context;
    }

    /**
    * create the object instance from the factory
    */
    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment)
        throws NamingException {
        // name to lookup in the external factory
        String jndiLookupName = "";
        String jndiFactoryClass = null;
        ResourceInfo resourceInfo = null;

        // get the target initial naming context and the lookup name
        Reference ref = (Reference) obj;
        Enumeration addrs = ref.getAll();
        while (addrs.hasMoreElements()) {
            RefAddr addr = (RefAddr) addrs.nextElement();

            String prop = addr.getType();
            if (prop.equals("resourceInfo")) {
                resourceInfo = (ResourceInfo) addr.getContent();
            } else if (prop.equals("jndiLookupName")) {
                jndiLookupName = (String) addr.getContent();
            } else if (prop.equals("jndiFactoryClass")) {
                jndiFactoryClass = (String) addr.getContent();
            }
        }

        if (resourceInfo == null) {
            throw new NamingException("JndiProxyObjectFactory: no resourceInfo context info");
        }

        ProxyRefAddr contextAddr = (ProxyRefAddr) ref.get(resourceInfo.getName().toString());
        Hashtable env = null;
        if (contextAddr == null || jndiFactoryClass == null || (env = (Hashtable) (contextAddr.getContent())) == null) {
            throw new NamingException("JndiProxyObjectFactory: no info in the " +
                    "reference about the target context; contextAddr = " + contextAddr + " " +
                    "env = " + env + " factoryClass = " + jndiFactoryClass);
        }

        // Context of the external naming factory
        Context context = contextMap.get(resourceInfo);
        if (context == null) {
            synchronized (contextMap) {
                context = contextMap.get(resourceInfo);
                if (context == null) {
                    context = loadInitialContext(jndiFactoryClass, env);
                    contextMap.put(resourceInfo, context);
                }
            }
        }

        // use the name to lookup in the external JNDI naming context
        Object retObj = null;
        try {
            retObj = context.lookup(jndiLookupName);
        } catch (NameNotFoundException e) {
            //Fixing issue: http://java.net/jira/browse/GLASSFISH-15447
            throw new ExternalNameNotFoundException(e);
        }  catch (javax.naming.NamingException ne) {
            //Fixing issue: http://java.net/jira/browse/GLASSFISH-15447
            context = loadInitialContext(jndiFactoryClass, env);
            if (context == null) {
                throw new NamingException ("JndiProxyObjectFactory no InitialContext" + jndiFactoryClass);
            }
            contextMap.put(resourceInfo, context);
            try {
                retObj = context.lookup(jndiLookupName);
            } catch (NameNotFoundException e) {
                throw new ExternalNameNotFoundException(e);
            }
        }
        return retObj;
    }
}
