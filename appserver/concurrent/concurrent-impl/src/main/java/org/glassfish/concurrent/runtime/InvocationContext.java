/*
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
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.enterprise.concurrent.spi.ContextHandle;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;

import javax.security.auth.Subject;
import java.io.IOException;

public class InvocationContext implements ContextHandle {

    private transient ComponentInvocation invocation;
    private transient ClassLoader contextClassLoader;
    private transient SecurityContext securityContext;
    private boolean useTransactionOfExecutionThread;

    static final long serialVersionUID = 5642415011655486579L;

    public InvocationContext(ComponentInvocation invocation, ClassLoader contextClassLoader, SecurityContext securityContext,
                             boolean useTransactionOfExecutionThread) {
        this.invocation = invocation;
        this.contextClassLoader = contextClassLoader;
        this.securityContext = securityContext;
        this.useTransactionOfExecutionThread = useTransactionOfExecutionThread;
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

    public boolean isUseTransactionOfExecutionThread() {
        return useTransactionOfExecutionThread;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeBoolean(useTransactionOfExecutionThread);
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
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        useTransactionOfExecutionThread = in.readBoolean();
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
