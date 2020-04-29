/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld;

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.api.naming.NamespacePrefixes;
import org.glassfish.hk2.api.ServiceLocator;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jvnet.hk2.annotations.Service;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorContext;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

/**
 * Proxy for jakarta.validation based lookups (java:comp/Validator, java:comp/ValidatorFactory) when CDI enabled
 */
@Service
@Named("ValidationNamingProxy")
public class ValidationNamingProxy implements NamedNamingObjectProxy {

    @Inject
    ServiceLocator serviceLocator;

    @Inject
    private ComponentEnvManager compEnvManager;

    @Inject
    private InvocationManager invocationManager;

    @Inject
    private WeldDeployer weldDeployer;

    static final String VALIDATOR_CONTEXT = "java:comp/Validator";
    static final String VALIDATOR_FACTORY_CONTEXT = "java:comp/ValidatorFactory";

    /**
     * get and create an instance of a bean from the beanManager
     *
     * @param beanManager
     * @param clazz
     * @return
     */
    private static final Object getAndCreateBean(BeanManager beanManager, Class clazz) {

        Set<Bean<?>> beans = beanManager.getBeans(clazz);

        if (!beans.isEmpty()) {
            Bean<?> bean = beans.iterator().next();

            return bean.create(null);
        }

        return null;
    }

    @Override
    public Object handle(String name) throws NamingException {

        // delegate to the java:comp/BeanManager handler to obtain the appropriate BeanManager
        BeanManager beanManager = obtainBeanManager();

        if (beanManager == null) {
          return null; // There is no bean manager available, return and let BeanValidatorNamingProxy handle lookup..
        }

        if (VALIDATOR_FACTORY_CONTEXT.equals(name)) {

            try {

                ValidatorFactory validatorFactory = (ValidatorFactory) getAndCreateBean(beanManager, ValidatorFactory.class);

                if (validatorFactory != null) {
                    return validatorFactory;
                } else {
                    throw new NamingException("Error retrieving " + name);
                }

            } catch (Throwable t) {
                NamingException ne = new NamingException("Error retrieving " + name);
                ne.initCause(t);
                throw ne;
            }
        } else if (VALIDATOR_CONTEXT.equals(name)) {

            try {

                Validator validator = (Validator) getAndCreateBean(beanManager, Validator.class);

                if (validator != null) {
                    return validator;
                } else {
                    throw new NamingException("Error retrieving " + name);
                }

            } catch (Throwable t) {
                NamingException ne = new NamingException("Error retrieving " + name);
                ne.initCause(t);
                throw ne;
            }
        } else {
            throw new NamingException("wrong handler for " + name);
        }
    }

    /**
     * Obtain the BeanManagerNamingProxy from hk2, so the BeanManager can be looked up
     *
     * @throws NamingException
     */
    private synchronized BeanManager obtainBeanManager() throws NamingException {

        BeanManager beanManager = null;

        // Use invocation context to find applicable BeanDeploymentArchive.
        ComponentInvocation inv = invocationManager.getCurrentInvocation();

        if( inv != null ) {

            JndiNameEnvironment componentEnv = compEnvManager.getJndiNameEnvironment(inv.getComponentId());

            if( componentEnv != null ) {

                BundleDescriptor bundle = null;

                if( componentEnv instanceof EjbDescriptor) {
                    bundle = (BundleDescriptor)
                            ((EjbDescriptor) componentEnv).getEjbBundleDescriptor().
                                    getModuleDescriptor().getDescriptor();

                } else if( componentEnv instanceof WebBundleDescriptor) {
                    bundle = (BundleDescriptor) componentEnv;

                }

                if( bundle != null ) {
                    BeanDeploymentArchive bda = weldDeployer.getBeanDeploymentArchiveForBundle(bundle);
                    if( bda != null ) {
                        WeldBootstrap bootstrap = weldDeployer.getBootstrapForApp(bundle.getApplication());

                        beanManager = bootstrap.getManager(bda);
                    }
                }

            }
        }

        return beanManager;
    }
}
