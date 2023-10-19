/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.ee;

import org.glassfish.api.container.Container;
import org.glassfish.deployment.common.SecurityRoleMapperFactory;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.ServerContext;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.deployment.interfaces.SecurityRoleMapperFactoryMgr;
import com.sun.enterprise.security.PolicyLoader;
import com.sun.enterprise.security.ee.web.integration.WebSecurityManagerFactory;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

/**
 * Security container service
 *
 */
@Service(name = "com.sun.enterprise.security.ee.ee.SecurityContainer")
public class SecurityContainer implements Container, PostConstruct {

    @Inject
    private PolicyLoader policyLoader;

    @Inject
    private ServerContext serverContext;

    @Inject
    private ServiceLocator habitat;

    @Inject
    private Provider<ClassLoaderHierarchy> classLoaderHierarchyProvider;

    @Inject
    private Provider<WebSecurityManagerFactory> webSecurityManagerFactoryProvider;

    static {
        initRoleMapperFactory();
    }

    /**
     * The system-assigned default web module's name/identifier.
     *
     * This has to be the same value as is in j2ee/WebModule.cpp.
     */
    public static final String DEFAULT_WEB_MODULE_NAME = "__default-web-module";

    @Override
    public String getName() {
        return "Security";
    }

    @Override
    public Class<? extends org.glassfish.api.deployment.Deployer> getDeployer() {
        return SecurityDeployer.class;
    }

    @Override
    public void postConstruct() {
        /*
         * This is handled by SecurityDeployer //Generate Policy for the Dummy Module WebBundleDescriptor wbd = new
         * WebBundleDescriptor(); Application application = Application.createApplication(); application.setVirtual(true);
         * application.setName(DEFAULT_WEB_MODULE_NAME); application.setRegistrationName(DEFAULT_WEB_MODULE_NAME);
         * wbd.setApplication(application); generatePolicy(wbd);
         */
    }
    /*
     * private void generatePolicy(WebBundleDescriptor wbd) { String name = null; ClassLoader oldTcc =
     * Thread.currentThread().getContextClassLoader(); try { //TODO: workaround here. Once fixed in V3 we should be able to
     * use //Context ClassLoader instead. ClassLoaderHierarchy hierarchy = classLoaderHierarchyProvider.get(); ClassLoader
     * tcc = hierarchy.getCommonClassLoader(); Thread.currentThread().setContextClassLoader(tcc);
     *
     * policyLoader.loadPolicy();
     *
     * WebSecurityManagerFactory wsmf = webSecurityManagerFactoryProvider.get(); // this should create all permissions
     * wsmf.createManager(wbd,true,serverContext); // for an application the securityRoleMapper should already be //
     * created. I am just creating the web permissions and handing // it to the security component. name =
     * WebSecurityManager.getContextID(wbd); SecurityUtil.generatePolicyFile(name);
     * websecurityProbeProvider.policyCreationEvent(name);
     *
     * } catch (IASSecurityException se) { String msg = "Error in generating security policy for " + name; throw new
     * RuntimeException(msg, se); } finally { Thread.currentThread().setContextClassLoader(oldTcc); } }
     */

    private static void initRoleMapperFactory() // throws Exception
    {
        Object o = null;
        Class c = null;
        // this should never fail.
        try {
            c = Class.forName("com.sun.enterprise.security.ee.acl.RoleMapperFactory");
            if (c != null) {
                o = c.newInstance();
                if (o != null && o instanceof SecurityRoleMapperFactory) {
                    SecurityRoleMapperFactoryMgr.registerFactory((SecurityRoleMapperFactory) o);
                }
            }
            if (o == null) {
                // _logger.log(Level.SEVERE,_localStrings.getLocalString("j2ee.norolemapper", "Cannot instantiate the
                // SecurityRoleMapperFactory"));
            }
        } catch (Exception cnfe) {
            //            _logger.log(Level.SEVERE,
            //            _localStrings.getLocalString("j2ee.norolemapper", "Cannot instantiate the SecurityRoleMapperFactory"),
            //            cnfe);
            //        cnfe.printStackTrace();
            //        throw new RuntimeException(cnfe);
            // throw cnfe;
        }
    }
}
