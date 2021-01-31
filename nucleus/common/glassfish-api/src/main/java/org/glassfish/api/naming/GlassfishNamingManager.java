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

package org.glassfish.api.naming;

import java.rmi.Remote;
import java.util.Collection;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;

import org.jvnet.hk2.annotations.Contract;
import org.omg.CORBA.ORB;

/**
 * The NamingManager provides an interface for various components to use naming functionality. It provides methods for
 * binding and unbinding environment properties, resource and ejb references.
 */

@Contract
public interface GlassfishNamingManager {

    String LOGICAL_NAME = "com.sun.enterprise.naming.logicalName";

    String NAMESPACE_METADATA_KEY = "NamespacePrefixes";

    /**
     * Get the initial context.
     */

    Context getInitialContext();

    /**
     *
     * Lookup a naming entry for a particular componentId
     */
    Object lookup(String componentId, String name) throws NamingException;

    /**
     *
     * Lookup a naming entry in a particular application's namespace
     *
     * @param appName application-name
     * @param name name of the object
     * @param env Environment
     * @return Object found by the name
     * @throws javax.naming.NamingException when unable to find the object
     */
    Object lookupFromAppNamespace(String appName, String name, Hashtable env) throws NamingException;

    /**
     *
     * Lookup a naming entry in a particular application's module's namespace
     *
     * @param appName application-name
     * @param moduleName module-name
     * @param name name of the object
     * @param env Environment
     * @return Object found by the name
     * @throws javax.naming.NamingException when unable to find the object
     */
    Object lookupFromModuleNamespace(String appName, String moduleName, String name, Hashtable env) throws NamingException;

    /**
     * Publish an object in the naming service.
     *
     * @param name Object that needs to be bound.
     * @param obj Name that the object is bound as.
     * @param rebind operation is a bind or a rebind.
     * @throws Exception
     */

    void publishObject(String name, Object obj, boolean rebind) throws NamingException;

    /**
     * Publish an object in the naming service.
     *
     * @param name Object that needs to be bound.
     * @param obj Name that the object is bound as.
     * @param rebind operation is a bind or a rebind.
     * @throws Exception
     */

    void publishObject(Name name, Object obj, boolean rebind) throws NamingException;

    /**
     * Publish a CosNaming object. The object is published to both the server's CosNaming service and the global naming
     * service. Objects published with this method must be unpublished via unpublishCosNamingObject.
     *
     * @param name Object that needs to be bound.
     * @param obj Name that the object is bound as.
     * @param rebind operation is a bind or a rebind.
     * @throws Exception
     */

    void publishCosNamingObject(String name, Object obj, boolean rebind) throws NamingException;

    /**
     * This method enumerates the env properties, ejb and resource references etc for a J2EE component and binds them in the
     * applicable java: namespace.
     *
     * @param treatComponentAsModule true if java:comp and java:module refer to the same namespace
     *
     */
    void bindToComponentNamespace(String appName, String moduleName, String componentId, boolean treatComponentAsModule,
            Collection<? extends JNDIBinding> bindings) throws NamingException;

    /**
     * Binds the bindings to module namespace of an application<br>
     * Typically, to get access to application's namespace, invocation context must be set to appropriate application's
     * context.<br>
     * This API is useful in cases where containers within GlassFish need to bind the objects in application's name-space
     * and do not have application's invocation context<br>
     *
     * @param appName application-name
     * @param bindings list of bindings
     * @throws NamingException when unable to bind the bindings
     */
    void bindToAppNamespace(String appName, Collection<? extends JNDIBinding> bindings) throws NamingException;

    /**
     * Binds the bindings to module namespace of an application<br>
     * Typically, to get access to application's module namespace, invocation context must be set to appropriate
     * application's context.<br>
     * This API is useful in cases where containers within GlassFish need to bind the objects in application's module
     * name-space and do not have application's invocation context<br>
     *
     * @param appName application-name
     * @param moduleName module-name
     * @param bindings list of bindings
     * @throws NamingException when unable to bind the bindings
     */
    void bindToModuleNamespace(String appName, String moduleName, Collection<? extends JNDIBinding> bindings) throws NamingException;

    /**
     * Remove an object from the naming service.
     *
     * @param name Name that the object is bound as.
     * @throws Exception
     */
    void unpublishObject(String name) throws NamingException;

    /**
     * Remove an object from the CosNaming service and global naming service.
     *
     * @param name Name that the object is bound as.
     * @throws Exception
     */
    void unpublishCosNamingObject(String name) throws NamingException;

    /**
     * Remove an object from the application's namespace.<br>
     * Typically, to get access to application's namespace, invocation context must be set to appropriate application's
     * context.<br>
     * This API is useful in cases where containers within GlassFish need to unbind the objects in application's name-space
     * and do not have application's invocation context<br>
     *
     * @param name Name that the object is bound as.
     * @param appName application-name
     * @throws NamingException when unable to unbind the object
     */
    void unbindAppObject(String appName, String name) throws NamingException;

    /**
     * Remove an object from the module name-space of an application<br>
     * Typically, to get access to application's module namespace, invocation context must be set to appropriate
     * application's context.<br>
     * This API is useful in cases where containers within GlassFish need to unbind the objects in application's module
     * name-space and do not have application's invocation context<br>
     *
     * @param name Name that the object is bound as.
     * @param appName application-name
     * @param moduleName module-name
     * @throws NamingException when unable to unbind the object
     */
    void unbindModuleObject(String appName, String moduleName, String name) throws NamingException;

    /**
     * Remove an object from the naming service.
     *
     * @param name Name that the object is bound as.
     * @throws Exception
     */
    void unpublishObject(Name name) throws NamingException;

    /**
     *
     * Unbind component-level bindings
     */
    void unbindComponentObjects(String componentId) throws NamingException;

    /**
     * Unbind app and module level bindings for the given app name.
     */
    void unbindAppObjects(String appName) throws NamingException;

    /**
     * Recreate a context for java:comp/env or one of its sub-contexts given the context name.
     */
    Context restoreJavaCompEnvContext(String contextName) throws NamingException;

    /**
     * Initialize RMI-IIOP naming services
     *
     * @param orb
     * @return RemoteSerialProvider object instance
     */
    Remote initializeRemoteNamingSupport(ORB orb) throws NamingException;

}
