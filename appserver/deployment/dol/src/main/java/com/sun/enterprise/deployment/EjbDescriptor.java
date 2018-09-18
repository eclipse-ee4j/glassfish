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

package com.sun.enterprise.deployment;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.enterprise.deployment.types.EjbReferenceContainer;
import com.sun.enterprise.deployment.types.MessageDestinationReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceEnvReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceReferenceContainer;
import com.sun.enterprise.deployment.types.ServiceReferenceContainer;
import org.glassfish.security.common.Role;

public interface EjbDescriptor extends NamedDescriptor,
        WritableJndiNameEnvironment,
        EjbReferenceContainer,
        ResourceEnvReferenceContainer,
        ResourceReferenceContainer,
        ServiceReferenceContainer,
        MessageDestinationReferenceContainer {

    /**
     * Indicates the bean will manage its own transactions.
     */
    String BEAN_TRANSACTION_TYPE = "Bean";

    /**
     * Indicates the bean expects the server to manage its transactions.
     */
    String CONTAINER_TRANSACTION_TYPE = "Container";

    EjbBundleDescriptor getEjbBundleDescriptor();

    boolean isRemoteInterfacesSupported();

    boolean isLocalInterfacesSupported();

    boolean isRemoteBusinessInterfacesSupported();

    boolean isLocalBusinessInterfacesSupported();

    boolean hasWebServiceEndpointInterface();

    boolean isLocalBean();

    String getHomeClassName();

    String getLocalHomeClassName();

    String getEjbImplClassName();

    String getWebServiceEndpointInterfaceName();

    void setWebServiceEndpointInterfaceName(String name);

    void addEjbReferencer(EjbReferenceDescriptor ref);

    Set<String> getLocalBusinessClassNames();

    Set<String> getRemoteBusinessClassNames();

    String getLocalClassName();

    Set getMethodDescriptors();

    Map getMethodPermissionsFromDD();

    String getEjbClassName();

    String getType();

    Application getApplication();

    long getUniqueId();

    void setUniqueId(long id);

    RoleReference getRoleReferenceByName(String roleReferenceName);

    Set getSecurityBusinessMethodDescriptors();

    void addPermissionedMethod(MethodPermission mp, MethodDescriptor md);

    void setUsesCallerIdentity(boolean flag);

    Boolean getUsesCallerIdentity();

    RunAsIdentityDescriptor getRunAsIdentity();

    String getRemoteClassName();

    void removeEjbReferencer(EjbReferenceDescriptor ref);

    void addRoleReference(RoleReference roleReference);

    void setRunAsIdentity(RunAsIdentityDescriptor desc);

    String getEjbTypeForDisplay();

    boolean hasInterceptorClass(String interceptorClassName);

    void addInterceptorClass(EjbInterceptor interceptor);

    void appendToInterceptorChain(List<EjbInterceptor> chain);

    void addMethodLevelChain(List<EjbInterceptor> chain, Method m, boolean aroundInvoke);

    Set getMethodPermissionsFor(MethodDescriptor methodDescriptor);

    Set<Role> getPermissionedRoles();

    String getTransactionType();

    Set<EjbIORConfigurationDescriptor> getIORConfigurationDescriptors(); // FIXME by srini - consider ejb-internal-api

    void addFrameworkInterceptor(InterceptorDescriptor interceptor); // FIXME by srini - consider ejb-internal-api

    void notifyNewModule(WebBundleDescriptor wbd); // FIXME by srini - can we eliminate the need for this

    /**
     * This method determines if all the mechanisms defined in the
     * CSIV2 CompoundSecMechList structure require protected
     * invocations.
     */
    boolean allMechanismsRequireSSL();
}
