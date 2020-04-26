/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jms.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import jakarta.enterprise.context.*;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.*;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.transaction.TransactionScoped;

/*
 * This CDI portable extension can register JMSContext beans to be system-level
 * that can be injected into all applications.
 */
public class JMSCDIExtension implements Extension {
    public JMSCDIExtension() {
    }

    private Bean createLocalBean(BeanManager beanManager, Class beanClass) {
        AnnotatedType annotatedType = beanManager.createAnnotatedType(beanClass);
        LocalPassivationCapableBean localBean = new LocalPassivationCapableBean(beanClass);
        InjectionTargetFactory injectionTargetFactory = beanManager.getInjectionTargetFactory(annotatedType);
        localBean.setInjectionTarget(injectionTargetFactory.createInjectionTarget(localBean));
        return localBean;
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscoveryEvent, BeanManager beanManager) {
        Bean requestManagerBean = createLocalBean(beanManager, RequestedJMSContextManager.class);
        afterBeanDiscoveryEvent.addBean(requestManagerBean);
        Bean transactionManagerBean = createLocalBean(beanManager, TransactedJMSContextManager.class);
        afterBeanDiscoveryEvent.addBean(transactionManagerBean);
        Bean contextBean = createLocalBean(beanManager, InjectableJMSContext.class);
        afterBeanDiscoveryEvent.addBean(contextBean);
    }

    void addScope(@Observes final BeforeBeanDiscovery event) {
    }

    void afterBeanDiscovery(@Observes final AfterBeanDiscovery event) {
    }

    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event) {
    }

    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event, BeanManager beanManager) {
    }

    public  void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
    }

    public void beforeShutdown(@Observes BeforeShutdown event, BeanManager beanManager) {
    }

    public <T> void processInjectionTarget(@Observes ProcessInjectionTarget<T> pit) {
    }

    public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
    }

    public <T, X> void processProducer(@Observes ProcessProducer<T, X> event) {
    }

    static AnnotationLiteral<Default> getDefaultAnnotationLiteral() {
        return new AnnotationLiteral<Default>() {};
    }

    static AnnotationLiteral<Any> getAnyAnnotationLiteral() {
        return new AnnotationLiteral<Any>() {};
    }

    public static class LocalPassivationCapableBean implements Bean, PassivationCapable {
        private String id = UUID.randomUUID().toString();
        private Class beanClass;
        private InjectionTarget injectionTarget;

        public LocalPassivationCapableBean(Class beanClass) {
            this.beanClass = beanClass;
        }

        public LocalPassivationCapableBean(Class beanClass, InjectionTarget injectionTarget) {
            this.beanClass = beanClass;
            this.injectionTarget = injectionTarget;
        }

        public void setInjectionTarget(InjectionTarget injectionTarget) {
            this.injectionTarget = injectionTarget;
        }

        @Override
        public Class<?> getBeanClass() {
            return beanClass;
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return injectionTarget.getInjectionPoints();
        }

        @Override
        public String getName() {
            return beanClass.getName();
        }

        @Override
        public Set<Annotation> getQualifiers() {
            Set<Annotation> qualifiers = new HashSet<Annotation>();
            qualifiers.add(JMSCDIExtension.getDefaultAnnotationLiteral());
            qualifiers.add(JMSCDIExtension.getAnyAnnotationLiteral());
            return qualifiers;
        }

        @Override
        public Class<? extends Annotation> getScope() {
            if (beanClass.isAnnotationPresent(RequestScoped.class))
                return RequestScoped.class;
            else if (beanClass.isAnnotationPresent(TransactionScoped.class))
                return TransactionScoped.class;
            else
                return Dependent.class;
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return Collections.emptySet();
        }

        @Override
        public Set<Type> getTypes() {
            Set<Type> types = new HashSet<Type>();
            types.add(beanClass);
            boolean loop = true;
            Class clazz = beanClass;
            while (loop) {
                Class[] interfaces = clazz.getInterfaces();
                for (Class t : interfaces) {
                    types.add(t);
                }
                clazz = clazz.getSuperclass();
                if (clazz == null) {
                    loop = false;
                    break;
                } else {
                    types.add(clazz);
                }
            }
            return types;
        }

        @Override
        public boolean isAlternative() {
            return false;
        }

        @Override
        public boolean isNullable() {
            return false;
        }

        @Override
        public Object create(CreationalContext ctx) {
            Object instance = injectionTarget.produce(ctx);
            injectionTarget.inject(instance, ctx);
            injectionTarget.postConstruct(instance);
            return instance;
        }

        @Override
        public void destroy(Object instance, CreationalContext ctx) {
            injectionTarget.preDestroy(instance);
            injectionTarget.dispose(instance);
            ctx.release();
        }

        @Override
        public String getId() {
            return id;
        }
    }
}
