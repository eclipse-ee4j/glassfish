/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deployment.types.EjbReferenceContainer;
import com.sun.enterprise.deployment.types.MessageDestinationReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceEnvReferenceContainer;
import com.sun.enterprise.deployment.types.ResourceReferenceContainer;
import com.sun.enterprise.deployment.types.ServiceReferenceContainer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * Name of the EJB
     */
    @Override
    String getName();


    /**
     * @return {@link EjbBundleDescriptor} instance owning this descriptor.
     */
    EjbBundleDescriptor getEjbBundleDescriptor();

    /**
     * @param ejbBundleDescriptor {@link EjbBundleDescriptor} instance owning this descriptor.
     */
    void setEjbBundleDescriptor(EjbBundleDescriptor ejbBundleDescriptor);

    /**
     * @return classname of the Home interface of this ejb.
     */
    String getHomeClassName();

    /**
     * @return true if this is an EJB provides a no interface Local view.
     */
    boolean isLocalBean();

    /**
     * @return true if the EJB has 1 or more local business interfaces
     */
    boolean isLocalBusinessInterfacesSupported();

    /**
     * Returns a new set of local business interface names for this ejb.
     * If the bean does not expose a local business view, return a set of size 0.
     *
     * @return a new set of class names or empty set.
     */
    Set<String> getLocalBusinessClassNames();

    /**
     * @return the fully qualified class name for the local interface of this ejb
     */
    String getLocalClassName();

    /**
     * @return true if the EJB described has a LocalHome/Local interface
     */
    boolean isLocalInterfacesSupported();

    /**
     * @return the fully qualified class name for the local home interface of this ejb
     */
    String getLocalHomeClassName();

    /**
     * @return true if the EJB has a RemoteHome/Remote interface
     */
    boolean isRemoteInterfacesSupported();

    /**
     * @return true if the EJB has 1 or more remote business interfaces
     */
    boolean isRemoteBusinessInterfacesSupported();

    /**
     * Returns the set of remote business interface names for this ejb.
     * If the bean does not expose a remote business view, return a set of size 0.
     *
     * @return a new set of class names or empty set.
     */
    Set<String> getRemoteBusinessClassNames();

    /**
     * @return classname of the Remote interface of this ejb.
     */
    String getRemoteClassName();

    /**
     * The result is usually same as the {@link #getEjbClassName()}.
     * <p>
     * It is the same as the user-specified class in case of Message, Session and bean managed
     * Persistence Entity Beans but is different for Container Mananged Persistence Entity Bean
     * Therefore, the implementation in the base class is to return {@link #getEjbClassName()}
     * and the method is redefined in IASEjbCMPEntityDescriptor.
     *
     * @return the execution class.
     */
    default String getEjbImplClassName() {
        return getEjbClassName();
    }

    /**
     * @return true if this is an EJB that implements a web service endpoint.
     */
    boolean hasWebServiceEndpointInterface();

    /**
     * @return class name of a web service interface
     */
    String getWebServiceEndpointInterfaceName();

    /**
     * @param name class name of a web service interface
     */
    void setWebServiceEndpointInterfaceName(String name);

    /**
     * @return full set of method descriptors I have (from all the methods on my home and remote interfaces).
     */
    Set<MethodDescriptor> getMethodDescriptors();


    /**
     * @return class name of the EJB
     */
    String getEjbClassName();

    /**
     * @return string describing the type, ie. StatefulSessionBean or Message-driven
     */
    String getType();

    /**
     * @return application to which this ejb descriptor belongs.
     */
    Application getApplication();

    /**
     * @return record of all the Method Permissions exactly as they were in the`DD
     */
    Map<MethodPermission, ArrayList<MethodDescriptor>> getMethodPermissionsFromDD();

    /**
     * @return the Map of {@link MethodPermission} (keys) that have been assigned to
     *         {@link MethodDescriptor}s (elements)
     */
    Map<MethodPermission, Set<MethodDescriptor>> getPermissionedMethodsByPermission();

    /**
     * Add a new method permission to a method or a set of methods
     *
     * @param mp is the new method permission to assign
     * @param md describe the method or set of methods this permission apply to
     */
    void addPermissionedMethod(MethodPermission mp, MethodDescriptor md);

    /**
     * Removes the given {@link Role} object from me.
     *
     * @param role
     */
    void removeRole(Role role);

    /**
     * @return set of the role references
     */
    Set<RoleReference> getRoleReferences();

    /**
     * @param roleReferenceName
     * @return a matching {@link RoleReference} by name or null.
     */
    RoleReference getRoleReferenceByName(String roleReferenceName);

    /**
     * Adds a role reference.
     *
     * @param roleReference
     */
    void addRoleReference(RoleReference roleReference);

    /**
     * @return security business method descriptors of this EJB.
     */
    Set<MethodDescriptor> getSecurityBusinessMethodDescriptors();

    /**
     * @param flag true if this bean uses caller identity
     */
    void setUsesCallerIdentity(boolean flag);

    /**
     * @return Boolean.TRUE if this bean uses caller identity, null if this is called before
     *         validator visit
     */
    Boolean getUsesCallerIdentity();

    RunAsIdentityDescriptor getRunAsIdentity();

    void setRunAsIdentity(RunAsIdentityDescriptor desc);

    default String getEjbTypeForDisplay() {
        return getType();
    }

    boolean hasInterceptorClass(String interceptorClassName);

    void addInterceptorClass(EjbInterceptor interceptor);

    void appendToInterceptorChain(List<EjbInterceptor> chain);

    void addMethodLevelChain(List<EjbInterceptor> chain, Method m, boolean aroundInvoke);

    Set<MethodPermission> getMethodPermissionsFor(MethodDescriptor methodDescriptor);

    Set<Role> getPermissionedRoles();

    /**
     * @return transaction type of this ejb (Bean/Container)
     */
    String getTransactionType();

    Set<EjbIORConfigurationDescriptor> getIORConfigurationDescriptors();

    default void addFrameworkInterceptor(InterceptorDescriptor interceptor) {
        throw new UnsupportedOperationException("Not implemented for " + getClass());
    }

    /**
     * Called by WebArchivist to notify this EjbDescriptor that it has been associated with a web bundle.
     */
    // FIXME by srini - can we eliminate the need for this
    void notifyNewModule(WebBundleDescriptor wbd);

    /**
     * @return true if all the mechanisms defined in the CSIV2 (Common Secure Interoperability
     *         Protocol Version 2) CompoundSecMechList structure require protected invocations.
     */
    boolean allMechanismsRequireSSL();

    /**
     * @return generated unique id of the bean
     */
    long getUniqueId();

    /**
     * @param id generated unique id of the bean
     */
    void setUniqueId(long id);
}
