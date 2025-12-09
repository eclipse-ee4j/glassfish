/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.cdi.transaction;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;
import jakarta.enterprise.util.AnnotationLiteral;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

/**
 * This class contains utility methods used for TransactionScoped related CDI event processing.
 *
 * @author <a href="mailto:arjav.desai@oracle.com">Arjav Desai</a>
 */
public class TransactionScopedCDIUtil {

    public static final String INITIALIZED_EVENT = "INITIALIZED_EVENT";
    public static final String DESTORYED_EVENT = "DESTORYED_EVENT";

    @LogMessagesResourceBundle
    public static final String SHARED_LOGMESSAGE_RESOURCE = "org.glassfish.cdi.LogMessages";

    @LoggerInfo(subsystem = "AS-CDI-JTA", description = "CDI-JTA", publish = true)
    public static final String CDI_JTA_LOGGER_SUBSYSTEM_NAME = "jakarta.enterprise.resource.jta";
    private static final Logger _logger = Logger.getLogger(CDI_JTA_LOGGER_SUBSYSTEM_NAME, SHARED_LOGMESSAGE_RESOURCE);

    public static void log(String message) {
        _logger.log(Level.WARNING, message);
    }

    /* Copied from JSF */
    public static Bean createHelperBean(BeanManager beanManager, Class<?> beanClass) {
        BeanWrapper result = null;
        AnnotatedType annotatedType = beanManager.createAnnotatedType(beanClass);
        result = new BeanWrapper(beanClass);

        //use this to create the class and inject dependencies
        InjectionTargetFactory factory = beanManager.getInjectionTargetFactory(annotatedType);
        final InjectionTarget injectionTarget = factory.createInjectionTarget(result);
        result.setInjectionTarget(injectionTarget);

        return result;
    }

    public static void fireEvent(String eventType) {
        BeanManager beanManager = null;
        try {
            beanManager = CDI.current().getBeanManager();
        } catch (Exception e) {
            TransactionScopedCDIUtil.log("Can't get instance of BeanManager to process TransactionScoped CDI Event!");
        }
        if (beanManager != null) {
            //TransactionScopedCDIEventHelperImpl AnnotatedType is created in Extension
            Set<Bean<?>> availableBeans = beanManager.getBeans(TransactionScopedCDIEventHelperImpl.class);
            if (null != availableBeans && !availableBeans.isEmpty()) {
                Bean<?> bean = beanManager.resolve(availableBeans);
                TransactionScopedCDIEventHelper eventHelper = (TransactionScopedCDIEventHelper) beanManager.getReference(bean,
                        bean.getBeanClass(), beanManager.createCreationalContext(null));
                if (eventType.equalsIgnoreCase(INITIALIZED_EVENT)) {
                    eventHelper.fireInitializedEvent(new TransactionScopedCDIEventPayload());
                } else {
                    eventHelper.fireDestroyedEvent(new TransactionScopedCDIEventPayload());
                }
            }
        } else {
            TransactionScopedCDIUtil.log("Can't get instance of BeanManager to process TransactionScoped CDI Event!");
        }
    }

    /* Copied from JSF */
    private static class BeanWrapper implements Bean {
        private Class beanClass;
        private InjectionTarget injectionTarget = null;

        public BeanWrapper(Class beanClass) {
            this.beanClass = beanClass;

        }

        private void setInjectionTarget(InjectionTarget injectionTarget) {
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
            return null;
        }

        @Override
        public Set<Annotation> getQualifiers() {
            Set<Annotation> qualifiers = new HashSet<>();
            qualifiers.add(new DefaultAnnotationLiteral());
            qualifiers.add(new AnyAnnotationLiteral());
            return qualifiers;
        }

        public static class DefaultAnnotationLiteral extends AnnotationLiteral<Default> {
            private static final long serialVersionUID = -9065007202240742004L;

        }

        public static class AnyAnnotationLiteral extends AnnotationLiteral<Any> {
            private static final long serialVersionUID = -4700109250603725375L;
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return Dependent.class;
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return Collections.emptySet();
        }

        @Override
        public Set<Type> getTypes() {
            Set<Type> types = new HashSet<>();
            types.add(beanClass);
            types.add(Object.class);
            return types;
        }

        @Override
        public boolean isAlternative() {
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
    }
}
