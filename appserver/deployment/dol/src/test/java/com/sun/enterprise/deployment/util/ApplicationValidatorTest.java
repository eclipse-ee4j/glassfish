/*
 * Copyright (c) 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.DataSourceDefinitionDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbIORConfigurationDescriptor;
import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerFactoryReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerReferenceDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.InjectionCapable;
import com.sun.enterprise.deployment.InjectionInfo;
import com.sun.enterprise.deployment.InterceptorDescriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.MessageDestinationReferenceDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.MethodPermission;
import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.RoleReference;
import com.sun.enterprise.deployment.RunAsIdentityDescriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.test.DolJunit5Extension;

import jakarta.inject.Inject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.security.common.Role;
import org.glassfish.tests.utils.junit.Classes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author David Matejcek
 */
@ExtendWith(DolJunit5Extension.class)
@Classes({Application.class})
class ApplicationValidatorTest {

    @Inject
    private Application application;


    @Test
    void invalid() {
        ApplicationValidator validator = new ApplicationValidator();
        assertThrows(IllegalArgumentException.class, () -> validator.accept((BundleDescriptor) application));
    }


    @Test
    void valid_warWithEjbIncluded() {
        application.setAppName("testAppName");
        application.setName("test");

        FakeWebBundleDescriptor war = new FakeWebBundleDescriptor();
        war.setApplication(application);
        war.setName("test-war-name");
        application.addBundleDescriptor(war);

        DataSourceDefinitionDescriptor ds = new DataSourceDefinitionDescriptor();
        ds.setName("java:app/jdbc/testdb");
        war.addResourceDescriptor(ds);

        ApplicationValidator validator = new ApplicationValidator();
        assertDoesNotThrow(() -> validator.accept((BundleDescriptor) application));
    }


    @Test
    void valid_warAndEjbJar() {
        application.setAppName("testAppName");
        application.setName("test");

        BundleDescriptor war = new FakeWebBundleDescriptor();
        war.setApplication(application);
        war.setName("test-war-name");
        application.addBundleDescriptor(war);

        FakeEjbBundleDescriptor ejbJar = new FakeEjbBundleDescriptor();
        ejbJar.setName("test-ejb-jar-name");
        ModuleDescriptor<RootDeploymentDescriptor> ejbModDescriptor = new ModuleDescriptor<>();
        ejbModDescriptor.setModuleName("test-ejb-jar");
        ejbJar.setModuleDescriptor(ejbModDescriptor);
        DataSourceDefinitionDescriptor ds = new DataSourceDefinitionDescriptor();
        ds.setName("java:app/jdbc/testdb");
        ejbJar.addResourceDescriptor(ds);

        application.addBundleDescriptor(ejbJar);

        ApplicationValidator validator = new ApplicationValidator();
        assertDoesNotThrow(() -> validator.accept((BundleDescriptor) application));
    }


    private static final class FakeEjbDescriptor implements EjbDescriptor {

        private final String name;


        /**
         * @param ejbName
         */
        FakeEjbDescriptor(String ejbName) {
            this.name = ejbName;
        }


        @Override
        public SimpleJndiName getJndiName() {
            return new SimpleJndiName(name);
        }


        @Override
        public void setJndiName(SimpleJndiName jndiName) {
            // TODO Auto-generated method stub

        }


        @Override
        public void addEnvironmentProperty(EnvironmentProperty environmentProperty) {
            // TODO Auto-generated method stub

        }


        @Override
        public void removeEnvironmentProperty(EnvironmentProperty environmentProperty) {
            // TODO Auto-generated method stub

        }


        @Override
        public void addEjbReferenceDescriptor(EjbReferenceDescriptor ejbReference) {
            // TODO Auto-generated method stub

        }


        @Override
        public void removeEjbReferenceDescriptor(EjbReferenceDescriptor ejbReference) {
            // TODO Auto-generated method stub

        }


        @Override
        public void addResourceReferenceDescriptor(ResourceReferenceDescriptor resourceReference) {
            // TODO Auto-generated method stub

        }


        @Override
        public void removeResourceReferenceDescriptor(ResourceReferenceDescriptor resourceReference) {
            // TODO Auto-generated method stub

        }


        @Override
        public void addResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvReference) {
            // TODO Auto-generated method stub

        }


        @Override
        public void removeResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvReference) {
            // TODO Auto-generated method stub

        }


        @Override
        public void addMessageDestinationReferenceDescriptor(MessageDestinationReferenceDescriptor msgDestRef) {
            // TODO Auto-generated method stub

        }


        @Override
        public void removeMessageDestinationReferenceDescriptor(MessageDestinationReferenceDescriptor msgDestRef) {
            // TODO Auto-generated method stub

        }


        @Override
        public void addPostConstructDescriptor(LifecycleCallbackDescriptor postConstructDesc) {
            // TODO Auto-generated method stub

        }


        @Override
        public void addPreDestroyDescriptor(LifecycleCallbackDescriptor preDestroyDesc) {
            // TODO Auto-generated method stub

        }


        @Override
        public void addServiceReferenceDescriptor(ServiceReferenceDescriptor serviceReference) {
            // TODO Auto-generated method stub

        }


        @Override
        public void removeServiceReferenceDescriptor(ServiceReferenceDescriptor serviceReference) {
            // TODO Auto-generated method stub

        }


        @Override
        public void addEntityManagerFactoryReferenceDescriptor(EntityManagerFactoryReferenceDescriptor reference) {
            // TODO Auto-generated method stub

        }


        @Override
        public void addEntityManagerReferenceDescriptor(EntityManagerReferenceDescriptor reference) {
            // TODO Auto-generated method stub

        }


        @Override
        public void addResourceDescriptor(ResourceDescriptor reference) {
            // TODO Auto-generated method stub

        }


        @Override
        public void removeResourceDescriptor(ResourceDescriptor reference) {
            // TODO Auto-generated method stub

        }


        @Override
        public Set<EnvironmentProperty> getEnvironmentProperties() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public EnvironmentProperty getEnvironmentPropertyByName(String name) throws IllegalArgumentException {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Set<EjbReferenceDescriptor> getEjbReferenceDescriptors() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Set<ServiceReferenceDescriptor> getServiceReferenceDescriptors() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public ServiceReferenceDescriptor getServiceReferenceByName(String name) {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Set<ResourceReferenceDescriptor> getResourceReferenceDescriptors() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Set<ResourceEnvReferenceDescriptor> getResourceEnvReferenceDescriptors() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public ResourceEnvReferenceDescriptor getResourceEnvReferenceByName(String name) {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Set<MessageDestinationReferenceDescriptor> getMessageDestinationReferenceDescriptors() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public MessageDestinationReferenceDescriptor getMessageDestinationReferenceByName(String name) {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Set<LifecycleCallbackDescriptor> getPostConstructDescriptors() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public LifecycleCallbackDescriptor getPostConstructDescriptorByClass(String className) {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Set<LifecycleCallbackDescriptor> getPreDestroyDescriptors() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Set<ResourceDescriptor> getResourceDescriptors(JavaEEResourceType type) {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Set<ResourceDescriptor> getAllResourcesDescriptors(Class<?> givenClass) {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Set<ResourceDescriptor> getAllResourcesDescriptors() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public LifecycleCallbackDescriptor getPreDestroyDescriptorByClass(String className) {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Set<EntityManagerFactoryReferenceDescriptor> getEntityManagerFactoryReferenceDescriptors() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public EntityManagerFactoryReferenceDescriptor getEntityManagerFactoryReferenceByName(String name) {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Set<EntityManagerReferenceDescriptor> getEntityManagerReferenceDescriptors() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public EntityManagerReferenceDescriptor getEntityManagerReferenceByName(String name) {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public List<InjectionCapable> getInjectableResourcesByClass(String className) {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public InjectionInfo getInjectionInfoByClass(Class<?> clazz) {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public EjbReferenceDescriptor getEjbReference(String name) {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public ResourceReferenceDescriptor getResourceReferenceByName(String name) {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public String getName() {
            return name;
        }


        @Override
        public EjbBundleDescriptor getEjbBundleDescriptor() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public void setEjbBundleDescriptor(EjbBundleDescriptor ejbBundleDescriptor) {
            // TODO Auto-generated method stub

        }


        @Override
        public boolean isRemoteInterfacesSupported() {
            // TODO Auto-generated method stub
            return false;
        }


        @Override
        public boolean isLocalInterfacesSupported() {
            // TODO Auto-generated method stub
            return false;
        }


        @Override
        public boolean isRemoteBusinessInterfacesSupported() {
            // TODO Auto-generated method stub
            return false;
        }


        @Override
        public boolean isLocalBusinessInterfacesSupported() {
            // TODO Auto-generated method stub
            return false;
        }


        @Override
        public boolean hasWebServiceEndpointInterface() {
            // TODO Auto-generated method stub
            return false;
        }


        @Override
        public boolean isLocalBean() {
            // TODO Auto-generated method stub
            return false;
        }


        @Override
        public String getHomeClassName() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public String getLocalHomeClassName() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public String getEjbImplClassName() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public String getWebServiceEndpointInterfaceName() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public void setWebServiceEndpointInterfaceName(String name) {
            // TODO Auto-generated method stub

        }


        @Override
        public void addEjbReferencer(EjbReferenceDescriptor ref) {
            // TODO Auto-generated method stub

        }


        @Override
        public Set<String> getLocalBusinessClassNames() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Set<String> getRemoteBusinessClassNames() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public String getLocalClassName() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Set<MethodDescriptor> getMethodDescriptors() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Map<MethodPermission, ArrayList<MethodDescriptor>> getMethodPermissionsFromDD() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public String getEjbClassName() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public String getType() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Application getApplication() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public long getUniqueId() {
            // TODO Auto-generated method stub
            return 0;
        }


        @Override
        public void setUniqueId(long id) {
            // TODO Auto-generated method stub

        }


        @Override
        public Set<RoleReference> getRoleReferences() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public RoleReference getRoleReferenceByName(String roleReferenceName) {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public void removeRole(Role role) {
            // TODO Auto-generated method stub

        }


        @Override
        public Set<MethodDescriptor> getSecurityBusinessMethodDescriptors() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Map<MethodPermission, Set<MethodDescriptor>> getPermissionedMethodsByPermission() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public void addPermissionedMethod(MethodPermission mp, MethodDescriptor md) {
            // TODO Auto-generated method stub

        }


        @Override
        public void setUsesCallerIdentity(boolean flag) {
            // TODO Auto-generated method stub

        }


        @Override
        public Boolean getUsesCallerIdentity() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public RunAsIdentityDescriptor getRunAsIdentity() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public String getRemoteClassName() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public void removeEjbReferencer(EjbReferenceDescriptor ref) {
            // TODO Auto-generated method stub

        }


        @Override
        public void addRoleReference(RoleReference roleReference) {
            // TODO Auto-generated method stub

        }


        @Override
        public void setRunAsIdentity(RunAsIdentityDescriptor desc) {
            // TODO Auto-generated method stub

        }


        @Override
        public String getEjbTypeForDisplay() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public boolean hasInterceptorClass(String interceptorClassName) {
            // TODO Auto-generated method stub
            return false;
        }


        @Override
        public void addInterceptorClass(EjbInterceptor interceptor) {
            // TODO Auto-generated method stub

        }


        @Override
        public void appendToInterceptorChain(List<EjbInterceptor> chain) {
            // TODO Auto-generated method stub

        }


        @Override
        public void addMethodLevelChain(List<EjbInterceptor> chain, Method m, boolean aroundInvoke) {
            // TODO Auto-generated method stub

        }


        @Override
        public Set<MethodPermission> getMethodPermissionsFor(MethodDescriptor methodDescriptor) {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Set<Role> getPermissionedRoles() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public String getTransactionType() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public Set<EjbIORConfigurationDescriptor> getIORConfigurationDescriptors() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public void addFrameworkInterceptor(InterceptorDescriptor interceptor) {
            // TODO Auto-generated method stub

        }


        @Override
        public void notifyNewModule(WebBundleDescriptor wbd) {
            // TODO Auto-generated method stub

        }


        @Override
        public boolean allMechanismsRequireSSL() {
            // TODO Auto-generated method stub
            return false;
        }
    }

    private static final class FakeEjbBundleDescriptor extends EjbBundleDescriptor {

        private static final long serialVersionUID = 1L;

        @Override
        public String getDefaultSpecVersion() {
            return "0.0";
        }


        @Override
        protected EjbDescriptor createDummyEjbDescriptor(String ejbName) {
            return new FakeEjbDescriptor(ejbName);
        }
    }

    private static final class FakeWebBundleDescriptor extends WebBundleDescriptor {

        private static final long serialVersionUID = 1L;

        @Override
        public void addJndiNameEnvironment(JndiNameEnvironment env) {
            throw new UnsupportedOperationException("Merging other descriptors is not supported");
        }


        @Override
        protected void addCommonWebBundleDescriptor(WebBundleDescriptor wbd, boolean defaultDescriptor) {
            throw new UnsupportedOperationException("Merging other descriptors is not supported");
        }


        @Override
        public String getDefaultSpecVersion() {
            return "0.0";
        }
    }
}
