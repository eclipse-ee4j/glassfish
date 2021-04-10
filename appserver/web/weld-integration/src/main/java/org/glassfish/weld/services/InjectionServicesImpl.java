/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld.services;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.glassfish.ejb.api.EjbContainerServices;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.weld.DeploymentImpl;
import org.glassfish.weld.connector.WeldUtils;
import org.jboss.weld.injection.spi.InjectionContext;
import org.jboss.weld.injection.spi.InjectionServices;

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.container.common.spi.util.InjectionException;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.InjectionCapable;
import com.sun.enterprise.deployment.InjectionInfo;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.ManagedBeanDescriptor;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;

public class InjectionServicesImpl implements InjectionServices {

    private InjectionManager injectionManager;

    // Associated bundle context
    private BundleDescriptor bundleContext;

    private DeploymentImpl deployment;

    private WsInjectionHandler wsHandler;

    public InjectionServicesImpl(InjectionManager injectionMgr, BundleDescriptor context, DeploymentImpl deployment) {
        injectionManager = injectionMgr;
        bundleContext = context;
        this.deployment = deployment;
    }

    private boolean isInterceptor(Class beanClass) {
        HashSet<String> annos = new HashSet<>();
        annos.add(jakarta.interceptor.Interceptor.class.getName());
        boolean res = false;
        while (!res && beanClass != Object.class) {
            res = WeldUtils.hasValidAnnotation(beanClass, annos, null);
            beanClass = beanClass.getSuperclass();
        }
        return res;
    }

    @Override
    public <T> void aroundInject(InjectionContext<T> injectionContext) {
        try {
            ServiceLocator serviceLocator = Globals.getDefaultHabitat();
            ComponentEnvManager compEnvManager = serviceLocator.getService(ComponentEnvManager.class);
            EjbContainerServices containerServices = serviceLocator.getService(EjbContainerServices.class);

            JndiNameEnvironment componentEnv = compEnvManager.getCurrentJndiNameEnvironment();

            ManagedBeanDescriptor mbDesc = null;

            JndiNameEnvironment injectionEnv = (JndiNameEnvironment) bundleContext;

            AnnotatedType annotatedType = injectionContext.getAnnotatedType();
            Class targetClass = annotatedType.getJavaClass();
            String targetClassName = targetClass.getName();
            Object target = injectionContext.getTarget();

            if (isInterceptor(targetClass) && !componentEnv.equals(injectionEnv)) {
                // Resources injected into interceptors must come from the environment in which the interceptor is
                // intercepting, not the environment in which the interceptor resides (for everything else!)
                // Must use the injectionEnv to get the injection info to determine where in jndi to look for the objects to inject.
                // must use the current jndi component env to lookup the objects to inject
                injectionManager.inject(targetClass, target, injectionEnv, null, false);
            } else {
                if (componentEnv == null) {
                    //throw new IllegalStateException("No valid EE environment for injection of " + targetClassName);
                    System.err.println("No valid EE environment for injection of " + targetClassName);
                    injectionContext.proceed();
                    return;
                }

                // Perform EE-style injection on the target.  Skip PostConstruct since
                // in this case 299 impl is responsible for calling it.

                if (componentEnv instanceof EjbDescriptor) {

                    EjbDescriptor ejbDesc = (EjbDescriptor) componentEnv;

                    if (containerServices.isEjbManagedObject(ejbDesc, targetClass)) {
                        injectionEnv = componentEnv;
                    } else if (bundleContext instanceof EjbBundleDescriptor) {

                        // Check if it's a @ManagedBean class within an ejb-jar.  In that case,
                        // special handling is needed to locate the EE env dependencies
                        mbDesc = bundleContext.getManagedBeanByBeanClass(targetClassName);
                    }
                }

                if (mbDesc != null) {
                    injectionManager.injectInstance(target, mbDesc.getGlobalJndiName(), false);
                } else if (injectionEnv instanceof EjbBundleDescriptor) {

                    // CDI-style managed bean that doesn't have @ManagedBean annotation but
                    // is injected within the context of an ejb.  Need to explicitly
                    // set the environment of the ejb bundle.
                    if (target == null) {
                        injectionManager.injectClass(targetClass, compEnvManager.getComponentEnvId(injectionEnv), false);
                    } else {
                        injectionManager.injectInstance(target, compEnvManager.getComponentEnvId(injectionEnv), false);
                    }
                } else {
                    if (target == null) {
                        injectionManager.injectClass(targetClass, injectionEnv, false);
                    } else {
                        injectionManager.injectInstance(target, injectionEnv, false);
                    }
                }

            }

            injectionContext.proceed();

        } catch (InjectionException ie) {
            throw new IllegalStateException(ie.getMessage(), ie);
        }
    }

    @Override
    public <T> void registerInjectionTarget(InjectionTarget<T> injectionTarget, AnnotatedType<T> annotatedType) {
        if (bundleContext instanceof EjbBundleDescriptor) {
            // we can't handle validting producer fields for ejb bundles because the JNDI environment is not setup
            // yet for ejbs and so we can't get the correct JndiNameEnvironment to call getInjectionInfoByClass.
            // getInjectionInfoByClass caches the results and so causes subsequent calls to return invalid information.
            return;
        }

        // We are only validating producer fields of resources.  See spec section 3.7.1
        Class annotatedClass = annotatedType.getJavaClass();
        JndiNameEnvironment jndiNameEnvironment = (JndiNameEnvironment) bundleContext;

        InjectionInfo injectionInfo = jndiNameEnvironment.getInjectionInfoByClass(annotatedClass);
        List<InjectionCapable> injectionResources = injectionInfo.getInjectionResources();

        for (AnnotatedField<? super T> annotatedField : annotatedType.getFields()) {
            if (annotatedField.isAnnotationPresent(Produces.class)) {
                if (annotatedField.isAnnotationPresent(EJB.class)) {
                    validateEjbProducer(annotatedClass, annotatedField, injectionResources);
                } else if (annotatedField.isAnnotationPresent(Resource.class)) {
                    validateResourceProducer(annotatedClass, annotatedField, injectionResources);
                } else if (annotatedField.isAnnotationPresent(PersistenceUnit.class)) {
                    validateResourceClass(annotatedField, EntityManagerFactory.class);
                } else if (annotatedField.isAnnotationPresent(PersistenceContext.class)) {
                    validateResourceClass(annotatedField, EntityManager.class);
                } else if (getWsHandler().handles(annotatedField)) {
                    getWsHandler().validateWebServiceRef(annotatedField);
                }
            }
        }

    }

    private void validateEjbProducer(Class annotatedClass, AnnotatedField annotatedField, List<InjectionCapable> injectionResources) {
        EJB ejbAnnotation = annotatedField.getAnnotation(EJB.class);
        if (ejbAnnotation != null) {
            String lookupName = getLookupName(annotatedClass, annotatedField, injectionResources);

            EjbDescriptor foundEjb = null;
            Collection<EjbDescriptor> ejbs = deployment.getDeployedEjbs();
            for (EjbDescriptor oneEjb : ejbs) {
                String jndiName = oneEjb.getJndiName();
                if (lookupName.contains(jndiName)) {
                    foundEjb = oneEjb;
                    break;
                }
            }
            if (foundEjb != null) {
                String className = foundEjb.getEjbImplClassName();
                try {
                    Class clazz = Class.forName(className, false, annotatedClass.getClassLoader());
                    validateResourceClass(annotatedField, clazz);
                } catch (ClassNotFoundException ignore) {
                }
            }
        }
    }

    private void validateResourceProducer(Class annotatedClass, AnnotatedField annotatedField, List<InjectionCapable> injectionResources) {
        Resource resourceAnnotation = annotatedField.getAnnotation(Resource.class);
        if (resourceAnnotation != null) {
            String lookupName = getLookupName(annotatedClass, annotatedField, injectionResources);
            if (lookupName.equals("java:comp/BeanManager")) {
                validateResourceClass(annotatedField, BeanManager.class);
            } else {
                boolean done = false;
                for (InjectionCapable injectionCapable : injectionResources) {
                    for (com.sun.enterprise.deployment.InjectionTarget target : injectionCapable.getInjectionTargets()) {
                        if (target.isFieldInjectable()) { // make sure it's a field and not a method
                            if (annotatedClass.getName().equals(target.getClassName())
                                    && target.getFieldName().equals(annotatedField.getJavaMember().getName())) {
                                String type = injectionCapable.getInjectResourceType();
                                try {
                                    Class clazz = Class.forName(type, false, annotatedClass.getClassLoader());
                                    validateResourceClass(annotatedField, clazz);
                                } catch (ClassNotFoundException ignore) {
                                } finally {
                                    done = true;
                                }
                            }
                        }
                        if (done) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private void validateResourceClass(AnnotatedField annotatedField, Class resourceClass) {
        if (!annotatedField.getJavaMember().getType().isAssignableFrom(resourceClass)) {
            throwProducerDefinitionExeption(annotatedField.getJavaMember().getName(), annotatedField.getJavaMember().getType().getName(),
                    resourceClass.getName());
        }
    }

    private void throwProducerDefinitionExeption(String annotatedFieldName, String annotatedFieldType, String resourceClassName) {
        throw new DefinitionException("The type of the injection point " + annotatedFieldName + " is " + annotatedFieldType
                + ".  The type of the physical resource is " + resourceClassName + " They are incompatible. ");
    }

    private String getComponentEnvName(Class annotatedClass, String fieldName, List<InjectionCapable> injectionResources) {
        for (InjectionCapable injectionCapable : injectionResources) {
            for (com.sun.enterprise.deployment.InjectionTarget target : injectionCapable.getInjectionTargets()) {
                if (target.isFieldInjectable()) { // make sure it's a field and not a method
                    if (annotatedClass.getName().equals(target.getClassName()) && target.getFieldName().equals(fieldName)) {
                        String name = injectionCapable.getComponentEnvName();
                        if (!name.startsWith("java:")) {
                            name = "java:comp/env/" + name;
                        }

                        return name;
                    }
                }
            }
        }

        return null;
    }

    private String getLookupName(Class annotatedClass, AnnotatedField annotatedField, List<InjectionCapable> injectionResources) {
        String lookupName = null;
        if (annotatedField.isAnnotationPresent(Resource.class)) {
            Resource resource = annotatedField.getAnnotation(Resource.class);
            lookupName = getJndiName(resource.lookup(), resource.mappedName(), resource.name());
        } else if (annotatedField.isAnnotationPresent(EJB.class)) {
            EJB ejb = annotatedField.getAnnotation(EJB.class);
            lookupName = getJndiName(ejb.lookup(), ejb.mappedName(), ejb.name());
        } else if (getWsHandler().handles(annotatedField)) {
            lookupName = getWsHandler().getJndiName(annotatedField);
        } else if (annotatedField.isAnnotationPresent(PersistenceUnit.class)) {
            PersistenceUnit persistenceUnit = annotatedField.getAnnotation(PersistenceUnit.class);
            lookupName = getJndiName(persistenceUnit.unitName(), null, persistenceUnit.name());
        } else if (annotatedField.isAnnotationPresent(PersistenceContext.class)) {
            PersistenceContext persistenceContext = annotatedField.getAnnotation(PersistenceContext.class);
            lookupName = getJndiName(persistenceContext.unitName(), null, persistenceContext.name());
        }

        if (lookupName == null || lookupName.trim().length() == 0) {
            lookupName = getComponentEnvName(annotatedClass, annotatedField.getJavaMember().getName(), injectionResources);
        }
        return lookupName;
    }

    static String getJndiName(String lookup, String mappedName, String name) {
        String jndiName = lookup;
        if (jndiName == null || jndiName.length() == 0) {
            jndiName = mappedName;
            if (jndiName == null || jndiName.length() == 0) {
                jndiName = name;
            }
        }

        return jndiName;
    }

    private WsInjectionHandler getWsHandler() {
        if (wsHandler == null) {
            try {
                //TODO: define this properly so that the ServiceLocator can be used instead
                // and (optional) dependency on webservices-apis can be dropped
                wsHandler = (WsInjectionHandler) Class.forName("org.glassfish.weld.services.WsInjectionHandlerImpl").getConstructor()
                        .newInstance();
            } catch (ReflectiveOperationException | SecurityException t) {
                // not loaded due to missing jakarta.xml.ws packages => likely web profile
                // let's use noop handler
                wsHandler = WsInjectionHandler.NOOP;
            }
        }
        return wsHandler;
    }

    @Override
    public void cleanup() {

    }
}
