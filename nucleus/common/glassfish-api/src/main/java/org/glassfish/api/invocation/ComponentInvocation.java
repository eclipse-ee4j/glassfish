/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.invocation;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

@PerLookup
@Service
public class ComponentInvocation implements Cloneable {

    public enum ComponentInvocationType {
        SERVLET_INVOCATION, EJB_INVOCATION, APP_CLIENT_INVOCATION, UN_INITIALIZED, SERVICE_STARTUP
    }

    private ComponentInvocationType invocationType = ComponentInvocationType.UN_INITIALIZED;

    private boolean preInvokeDoneStatus;

    private Boolean auth;

    // the component instance, type Servlet, Filter or EnterpriseBean
    public Object instance;

    // the name of this instance
    private String instanceName;

    // ServletContext for servlet, Container for EJB
    public Object container;

    public Object jndiEnvironment;

    public void setJNDIEnvironment(Object val) {
        jndiEnvironment = val;
    }

    public Object getJNDIEnvironment() {
        return jndiEnvironment;
    }

    public String componentId;

    public Object transaction;

    // true if transaction commit or rollback is
    // happening for this invocation context
    private boolean transactionCompleting;

    // security context coming in a call
    // security context changes on a runas call - on a run as call
    // the old logged in security context is stored in here.
    public Object oldSecurityContext;

    private Object resourceTableKey;

    private ResourceHandler resourceHandler;

    /**
     * Registry to be carried with this invocation
     */
    private Map<Class, Object> registry;

    protected String appName;

    protected String moduleName;

    public ComponentInvocation() {

    }

    public ComponentInvocation(String componentId, ComponentInvocationType invocationType, Object container, String appName, String moduleName) {
        this.componentId = componentId;
        this.invocationType = invocationType;
        this.container = container;
        this.appName = appName;
        this.moduleName = moduleName;
    }

    public ComponentInvocation(String componentId, ComponentInvocationType invocationType, Object instance, Object container, Object transaction) {
        this.componentId = componentId;
        this.invocationType = invocationType;
        this.instance = instance;
        this.container = container;
        this.transaction = transaction;
    }

    public ComponentInvocationType getInvocationType() {
        return invocationType;
    }

    public void setComponentInvocationType(ComponentInvocationType t) {
        this.invocationType = t;
    }

    public Object getInstance() {
        return instance;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getComponentId() {
        return this.componentId;
    }

    public Object getContainer() {
        return container;
    }

    public Object getContainerContext() {
        return container;
    }

    public Object getTransaction() {
        return transaction;
    }

    public void setTransaction(Object t) {
        this.transaction = t;
    }

    private Object transactionOperationsManager;

    public void setTransactionOperationsManager(Object transactionOperationsManager) {
        this.transactionOperationsManager = transactionOperationsManager;
    }

    public Object getTransactionOperationsManager() {
        return transactionOperationsManager;
    }

    /**
     * Sets the security context of the call coming in
     */
    public void setOldSecurityContext(Object sc) {
        this.oldSecurityContext = sc;
    }

    /**
     * gets the security context of the call that came in before a new context for runas is made
     */
    public Object getOldSecurityContext() {
        return oldSecurityContext;
    }

    public boolean isTransactionCompleting() {
        return transactionCompleting;
    }

    public void setTransactionCompeting(boolean value) {
        transactionCompleting = value;
    }

    public void setResourceTableKey(Object key) {
        this.resourceTableKey = key;
    }

    public Object getResourceTableKey() {
        return resourceTableKey;
    }

    public void setResourceHandler(ResourceHandler h) {
        resourceHandler = h;
    }

    public ResourceHandler getResourceHandler() {
        return resourceHandler;
    }

    /**
     * @return Registry associated with this invocation for the given <code>key</code>
     */
    public Object getRegistryFor(Class key) {
        if (registry == null) {
            return null;
        } else {
            return registry.get(key);
        }
    }

    /**
     * Associate given <code></code>registry</code> with given <code>key</code> for this invocation
     */
    public void setRegistryFor(Class key, Object payLoad) {
        if (registry == null) {
            registry = new HashMap<>();
        }
        registry.put(key, payLoad);
    }

    // In most of the cases we don't want registry entries from being reused in the cloned
    // invocation, in which case, this method must be called. I am not sure if async
    // ejb invocation must call this (It never did and someone in ejb team must investigate
    // if clearRegistry() must be called from EjbAsyncInvocationManager)
    public void clearRegistry() {
        if (registry != null) {
            registry.clear();
        }
    }

    public boolean isPreInvokeDone() {
        return preInvokeDoneStatus;
    }

    public void setPreInvokeDone(boolean value) {
        preInvokeDoneStatus = value;
    }

    public Boolean getAuth() {
        return auth;
    }

    public void setAuth(boolean value) {
        auth = value;
    }

    /**
     * Returns the appName for the current invocation, equivalent to the value bound to java:app/AppName, without the cost
     * of lookup. For standalone modules, returns the same value as getModuleName(). For invocations that are not on Java EE
     * components, returns null.
     */
    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * Returns the moduleName for the current invocation, equivalent to the value bound to java:module/ModuleName, without
     * the cost of lookup. For invocations that are not on Jakarta EE components, returns null.
     */
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public ComponentInvocation clone() {
        ComponentInvocation newInv = null;
        try {
            newInv = (ComponentInvocation) super.clone();
        } catch (CloneNotSupportedException cnsEx) {
            // Shouldn't happen as we implement Cloneable
            throw new Error(cnsEx);
        }

        newInv.auth = null;
        newInv.preInvokeDoneStatus = false;
        newInv.instance = null;
        newInv.transaction = null;
        newInv.transactionCompleting = false;

        return newInv;
    }
}
