/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.concurrent.runtime;

import com.sun.enterprise.security.SecurityContext;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import javax.security.auth.Subject;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.concurro.spi.ContextHandle;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;

public class InvocationContext implements ContextHandle {
    private static final Logger LOG = System.getLogger(InvocationContext.class.getName());

    private transient ComponentInvocation invocation;
    private transient ClassLoader contextClassLoader;
    private transient SecurityContext securityContext;

    private ThreadMgmtData threadCtxData;
    private final boolean useTxOfExecutionThread;

    public InvocationContext(ComponentInvocation invocation, ClassLoader contextClassLoader,
        SecurityContext securityContext, boolean useTxOfExecutionThread, ThreadMgmtData threadManagement) {
        LOG.log(Level.TRACE,
            "InvocationContext(\n  invocation={0}\n  contextClassLoader={1}\n  securityContext={2}"
                + "\n  useTxOfExecutionThread={3}\n  threadCtxData={4}\n)",
            invocation, contextClassLoader, securityContext, threadManagement);
        this.invocation = invocation;
        this.contextClassLoader = contextClassLoader;
        this.securityContext = securityContext;
        this.useTxOfExecutionThread = useTxOfExecutionThread;
        this.threadCtxData = threadManagement;
    }


    public ComponentInvocation getInvocation() {
        return invocation;
    }


    public ClassLoader getContextClassLoader() {
        return contextClassLoader;
    }


    public SecurityContext getSecurityContext() {
        return securityContext;
    }


    public ThreadMgmtData getContextData() {
        return threadCtxData;
    }


    public boolean isUseTransactionOfExecutionThread() {
        return useTxOfExecutionThread;
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        // write values for invocation
        String componentId = null;
        String appName = null;
        String moduleName = null;
        if (invocation != null) {
            componentId = invocation.getComponentId();
            appName = invocation.getAppName();
            moduleName = invocation.getModuleName();
        }
        out.writeObject(componentId);
        out.writeObject(appName);
        out.writeObject(moduleName);
        // write values for securityContext
        String principalName = null;
        boolean defaultSecurityContext = false;
        Subject subject = null;
        if (securityContext != null) {
            if (securityContext.getCallerPrincipal() != null) {
                principalName = securityContext.getCallerPrincipal().getName();
                subject = securityContext.getSubject();
                // Clear principal set to avoid ClassNotFoundException during deserialization.
                // It will be set by new SecurityContext in readObject().
                subject.getPrincipals().clear();
            }
            if (securityContext == SecurityContext.getDefaultSecurityContext()) {
                defaultSecurityContext = true;
            }
        }
        out.writeObject(principalName);
        out.writeBoolean(defaultSecurityContext);
        out.writeObject(subject);
        out.writeObject(threadCtxData);
    }


    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // reconstruct invocation
        String componentId = (String) in.readObject();
        String appName = (String) in.readObject();
        String moduleName = (String) in.readObject();
        invocation = createComponentInvocation(componentId, appName, moduleName);
        // reconstruct securityContext
        String principalName = (String) in.readObject();
        boolean defaultSecurityContext = in.readBoolean();
        Subject subject = (Subject) in.readObject();
        if (principalName != null) {
            if (defaultSecurityContext) {
                securityContext = SecurityContext.getDefaultSecurityContext();
            }
            else {
                securityContext = new SecurityContext(principalName, subject, null);
            }
        }
        // reconstruct contextClassLoader
        ApplicationRegistry applicationRegistry = ConcurrentRuntime.getRuntime().getApplicationRegistry();
        if (appName != null) {
            ApplicationInfo applicationInfo = applicationRegistry.get(appName);
            if (applicationInfo != null) {
                contextClassLoader = applicationInfo.getAppClassLoader();
            }
        }
        threadCtxData = (ThreadMgmtData) in.readObject();
    }

    private ComponentInvocation createComponentInvocation(String componentId, String appName, String moduleName) {
        if (componentId == null && appName == null && moduleName == null) {
            return null;
        }
        ComponentInvocation newInv = new ComponentInvocation(
                componentId,
                ComponentInvocation.ComponentInvocationType.SERVLET_INVOCATION,
                null,
                appName,
                moduleName
        );
        return newInv;
    }
}
