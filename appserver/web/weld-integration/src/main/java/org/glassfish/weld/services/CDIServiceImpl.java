/*
 * Copyright (c) 2021, 2023 Contributors to Eclipse Foundation.
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

import com.sun.ejb.containers.BaseContainer;
import com.sun.ejb.containers.EJBContextImpl;
import com.sun.enterprise.container.common.spi.CDIService;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.inject.Inject;
import jakarta.inject.Scope;
import jakarta.servlet.ServletContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.hk2.api.Rank;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;
import org.glassfish.weld.BeanDeploymentArchiveImpl;
import org.glassfish.weld.WeldDeployer;
import org.glassfish.weld.connector.WeldUtils;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.contexts.WeldCreationalContext;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.WeldInjectionTarget;
import org.jboss.weld.manager.api.WeldManager;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.FINE;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT;
import static org.glassfish.cdi.CDILoggerInfo.GET_BDA_FOR_BEAN_CLASS_SEARCH;
import static org.glassfish.cdi.CDILoggerInfo.SUB_BDA_CONTAINS_BEAN_CLASS_NAME;
import static org.glassfish.cdi.CDILoggerInfo.TOP_LEVEL_BDA_CONTAINS_BEAN_CLASS_NAME;

@Service
@Rank(10)
public class CDIServiceImpl implements CDIService {

    @LogMessagesResourceBundle
    public static final String SHARED_LOGMESSAGE_RESOURCE = "org.glassfish.cdi.LogMessages";

    @LoggerInfo(subsystem = "AS-WELD", description = "WELD", publish = true)
    public static final String WELD_LOGGER_SUBSYSTEM_NAME = "jakarta.enterprise.resource.weld";

    private static final Logger logger = Logger.getLogger(WELD_LOGGER_SUBSYSTEM_NAME, SHARED_LOGMESSAGE_RESOURCE);

    private static final Set<String> validScopes = new HashSet<>();
    static {
        validScopes.add(Scope.class.getName());
        validScopes.add(NormalScope.class.getName());
        validScopes.add(RequestScoped.class.getName());
        validScopes.add(SessionScoped.class.getName());
        validScopes.add(ApplicationScoped.class.getName());
        validScopes.add(ConversationScoped.class.getName());
    }

    private static final HashSet<String> excludedScopes = new HashSet<>();
    static {
        excludedScopes.add(Dependent.class.getName());
    }

    @Inject
    private WeldDeployer weldDeployer;

    @Inject
    private ComponentEnvManager compEnvManager;

    @Inject
    private InvocationManager invocationManager;

    @Override
    public boolean isCurrentModuleCDIEnabled() {
        BundleDescriptor bundle = null;

        ComponentInvocation componentInvocation = invocationManager.getCurrentInvocation();

        if (componentInvocation == null) {
            return false;
        }

        JndiNameEnvironment componentEnv = compEnvManager.getJndiNameEnvironment(componentInvocation.getComponentId());

        if (componentEnv != null) {
            if (componentEnv instanceof BundleDescriptor) {
                bundle = (BundleDescriptor) componentEnv;
            } else if (componentEnv instanceof EjbDescriptor) {
                bundle = ((EjbDescriptor) componentEnv).getEjbBundleDescriptor();
            }
        }

        return bundle != null ? isCDIEnabled(bundle) : false;

    }

    @Override
    public boolean isCDIEnabled(BundleDescriptor bundle) {

        // Get the top-level bundle descriptor from the given bundle.
        // E.g. allows EjbBundleDescriptor from a .war to be handled correctly.
        BundleDescriptor topLevelBundleDesc = (BundleDescriptor) bundle.getModuleDescriptor().getDescriptor();

        return weldDeployer.isCdiEnabled(topLevelBundleDesc);

    }

    @Override
    public boolean isCDIScoped(Class<?> clazz) {
        // Check all the annotations on the specified Class to determine if the class is annotated
        // with a supported CDI scope
        return WeldUtils.hasValidAnnotation(clazz, validScopes, excludedScopes);
    }

    @Override
    public void setELResolver(ServletContext servletContext) throws NamingException {
        InitialContext context = new InitialContext();
        BeanManager beanManager = (BeanManager) context.lookup(JNDI_CTX_JAVA_COMPONENT + "BeanManager");
        if (beanManager != null) {
            servletContext.setAttribute("org.glassfish.jsp.beanManagerELResolver", beanManager.getELResolver());
        }
    }

    @Override
    public <T> CDIInjectionContext<T> createCDIInjectionContext(EjbDescriptor ejbDesc, T instance, Map<Class, Object> ejbInfo) {
        return _createCDIInjectionContext(ejbDesc, instance, ejbInfo);
    }

    @Override
    public <T> CDIInjectionContext<T> createCDIInjectionContext(EjbDescriptor ejbDesc, Map<Class, Object> ejbInfo) {
        return _createCDIInjectionContext(ejbDesc, null, ejbInfo);
    }

    // instance could be null. If null, create a new one
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <T> CDIInjectionContext<T> _createCDIInjectionContext(EjbDescriptor ejb, T instance, Map<Class, Object> ejbInfo) {
        BaseContainer baseContainer = null;
        EJBContextImpl ejbContext = null;
        CDIInjectionContextImpl cdiContext = null;
        CreationalContext<?> creationalContext = null;
        if (ejbInfo != null) {
            baseContainer = (BaseContainer) ejbInfo.get(BaseContainer.class);
            ejbContext = (EJBContextImpl) ejbInfo.get(EJBContextImpl.class);
        }

        BundleDescriptor topLevelBundleDesc = (BundleDescriptor) ejb.getEjbBundleDescriptor().getModuleDescriptor().getDescriptor();

        // First get BeanDeploymentArchive for this ejb
        BeanDeploymentArchive beanDeploymentArchive = getBDAForBeanClass(topLevelBundleDesc, ejb.getEjbClassName());

        WeldBootstrap bootstrap = weldDeployer.getBootstrapForApp(ejb.getEjbBundleDescriptor().getApplication());
        WeldManager weldManager = bootstrap.getManager(beanDeploymentArchive);

        org.jboss.weld.ejb.spi.EjbDescriptor ejbDesc = weldManager.getEjbDescriptor(ejb.getName());

        // get or create the ejb's creational context
        if (ejbInfo != null) {
            cdiContext = (CDIInjectionContextImpl) ejbInfo.get(CDIService.CDIInjectionContext.class);
        }
        if (cdiContext != null) {
            creationalContext = cdiContext.getCreationalContext();
        }

        if (creationalContext == null) {
            // The creational context may have been created by interceptors because they are created first
            // (see createInterceptorInstance below.)
            // And we only want to create the ejb's creational context once or we will have a memory
            // leak there too.
            Bean<?> bean = weldManager.getBean(ejbDesc);
            creationalContext = weldManager.createCreationalContext(bean);
            cdiContext.setCreationalContext(creationalContext);
        }

        // Create the injection target

        InjectionTarget injectionTarget = null;
        if (ejbDesc.isMessageDriven()) {
            // message driven beans are non-contextual and therefore createInjectionTarget is not appropriate
            injectionTarget = createMdbInjectionTarget(weldManager, ejbDesc);
        } else {
            injectionTarget = weldManager.createInjectionTarget(ejbDesc);
        }
        if (cdiContext != null) {
            cdiContext.setInjectionTarget(injectionTarget);
        }

        // JJS: 7/20/17 We must perform the around_construct interception because Weld does not know about
        // interceptors defined by descriptors.
        WeldCreationalContext weldCreationalContext = (WeldCreationalContext) creationalContext;
        weldCreationalContext.setConstructorInterceptionSuppressed(true);

        CDIAroundConstructCallback aroundConstructCallback = new CDIAroundConstructCallback(baseContainer, ejbContext);
        weldCreationalContext.registerAroundConstructCallback(aroundConstructCallback);
        if (cdiContext != null) {
            cdiContext.setCDIAroundConstructCallback(aroundConstructCallback);
        }
        Object beanInstance = instance;

        if (beanInstance == null) {
            // Create instance , perform constructor injection.
            beanInstance = injectionTarget.produce(creationalContext);
        }
        if (cdiContext != null) {
            cdiContext.setInstance(beanInstance);
        }
        return cdiContext;
        // Injection is not performed yet. Separate injectEJBInstance() call is required.
    }

    private <T> InjectionTarget<T> createMdbInjectionTarget(WeldManager weldManager, org.jboss.weld.ejb.spi.EjbDescriptor<T> ejbDesc) {
        AnnotatedType<T> type = weldManager.createAnnotatedType(ejbDesc.getBeanClass());

        WeldInjectionTarget<T> target = weldManager.createInjectionTargetBuilder(type).setDecorationEnabled(false)
                .setInterceptionEnabled(false).setTargetClassLifecycleCallbacksEnabled(false).setBean(weldManager.getBean(ejbDesc)).build();

        return weldManager.fireProcessInjectionTarget(type, target);
    }

    private BeanDeploymentArchive getBDAForBeanClass(BundleDescriptor bundleDesc, String beanClassName) {
        if (logger.isLoggable(FINE)) {
            logger.log(FINE, GET_BDA_FOR_BEAN_CLASS_SEARCH, new Object[] { bundleDesc.getModuleName(), beanClassName });
        }

        BeanDeploymentArchive topLevelBeanDeploymentArchive = weldDeployer.getBeanDeploymentArchiveForBundle(bundleDesc);
        if (topLevelBeanDeploymentArchive.getBeanClasses().contains(beanClassName)) {
            if (logger.isLoggable(FINE)) {
                logger.log(FINE, TOP_LEVEL_BDA_CONTAINS_BEAN_CLASS_NAME,
                        new Object[] { topLevelBeanDeploymentArchive.getId(), beanClassName });
            }

            return topLevelBeanDeploymentArchive;
        }

        // for all sub-BDAs
        for (BeanDeploymentArchive bda : topLevelBeanDeploymentArchive.getBeanDeploymentArchives()) {
            if (bda.getBeanClasses().contains(beanClassName)) {
                if (logger.isLoggable(FINE)) {
                    logger.log(FINE, SUB_BDA_CONTAINS_BEAN_CLASS_NAME, new Object[] { bda.getId(), beanClassName });
                }
                return bda;
            }
        }

        // If not found in any BDA's subclasses, return topLevel BDA
        return topLevelBeanDeploymentArchive;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void injectEJBInstance(CDIInjectionContext<T> injectionCtx) {
        CDIInjectionContextImpl<T> injectionCtxImpl = (CDIInjectionContextImpl<T>) injectionCtx;

        // Perform injection and call initializers
        injectionCtxImpl.injectionTarget.inject(injectionCtxImpl.instance, injectionCtxImpl.creationalContext);

        // NOTE : PostConstruct is handled by ejb container
    }

    @Override
    public <T> CDIInjectionContext<T> createManagedObject(Class<T> managedClass, BundleDescriptor bundle) {
        return createManagedObject(managedClass, bundle, true);
    }

    /**
     * Perform CDI style injection on the <code>managedObject</code> argument.
     *
     * @param managedObject the managed object
     * @param bundle the bundle descriptor
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void injectManagedObject(Object managedObject, BundleDescriptor bundle) {
        BeanManager beanManager = getBeanManagerFromBundle(bundle);

        InjectionTargetFactory injectionTargetFactory = beanManager
                .getInjectionTargetFactory(beanManager.createAnnotatedType(managedObject.getClass()));

        injectionTargetFactory.createInjectionTarget(null).inject(managedObject, beanManager.createCreationalContext(null));
    }

    private Interceptor findEjbInterceptor(Class<?> interceptorClass, Set<EjbInterceptor> ejbInterceptors) {
        for (EjbInterceptor oneInterceptor : ejbInterceptors) {
            Interceptor interceptor = oneInterceptor.getInterceptor();
            if (interceptor != null) {
                if (interceptor.getBeanClass().equals(interceptorClass)) {
                    return oneInterceptor.getInterceptor();
                }
            }
        }

        return null;
    }

    /**
     *
     * @param interceptorClass The interceptor class.
     * @param ejb The ejb descriptor.
     * @param ejbContext The ejb cdi context. This context is only used to store any contexts for interceptors not bound to
     * the ejb. Nothing else in this context will be used in this method as they are most likely null.
     * @param ejbInterceptors All of the ejb interceptors for the ejb.
     *
     * @return The interceptor instance.
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> T createInterceptorInstance(Class<T> interceptorClass, EjbDescriptor ejb, CDIService.CDIInjectionContext ejbContext,
            Set<EjbInterceptor> ejbInterceptors) {
        BundleDescriptor topLevelBundleDesc = (BundleDescriptor) ejb.getEjbBundleDescriptor().getModuleDescriptor().getDescriptor();

        // First get BeanDeploymentArchive for this ejb
        BeanDeploymentArchive bda = getBDAForBeanClass(topLevelBundleDesc, ejb.getEjbClassName());

        WeldBootstrap bootstrap = weldDeployer.getBootstrapForApp(ejb.getEjbBundleDescriptor().getApplication());
        WeldManager beanManager = bootstrap.getManager(bda);

        org.jboss.weld.ejb.spi.EjbDescriptor ejbDesc = beanManager.getEjbDescriptor(ejb.getName());

        // get or create the ejb's creational context
        CreationalContext<?> creationalContext = ejbContext.getCreationalContext();
        if (creationalContext == null) {
            // We have to do this because interceptors are created before the ejb but in certain cases we must associate
            // the interceptors with the ejb so that they are cleaned up correctly.
            // And we only want to create the ejb's creational context once or we will have a memory
            // leak there too.
            Bean<?> bean = beanManager.getBean(ejbDesc);
            creationalContext = beanManager.createCreationalContext(bean);
            ejbContext.setCreationalContext(creationalContext);
        }

        // first see if there's an Interceptor object defined for the interceptorClass
        // This happens when @Interceptor or @InterceptorBinding is used.
        Interceptor interceptor = findEjbInterceptor(interceptorClass, ejbInterceptors);
        if (interceptor != null) {
            // Using the ejb's creationalContext so we don't have to do any cleanup.
            // the cleanup will be handled by weld when it cleans up the ejb.

            Object instance = null;
            if (beanManager instanceof BeanManagerImpl) {
                // Since Weld 5.1.1 the default beanManager.getReference method doesn't create a creational context
                // for the Interceptor.
                BeanManagerImpl beanManagerImpl = (BeanManagerImpl) beanManager;
                instance = beanManagerImpl.getReference(interceptor, interceptorClass, creationalContext, false);
            } else {
                instance = beanManager.getReference(interceptor, interceptorClass, creationalContext);
            }

            return (T) instance;
        }

        // Check to see if the interceptor was defined as a Bean.
        // This can happen when using @Interceptors to define the interceptors.
        Set<Bean<?>> availableBeans = beanManager.getBeans(interceptorClass);
        if (availableBeans != null && !availableBeans.isEmpty()) {
            // using the ejb's creationalContext so we don't have to do any cleanup.
            // the cleanup will be handled by weld when it clean's up the ejb.
            Bean<?> interceptorBean = beanManager.resolve(availableBeans);
            Object instance = beanManager.getReference(interceptorBean, interceptorClass, creationalContext);
            return (T) instance;
        }

        // There are other interceptors like SessionBeanInterceptor that are
        // defined via code and they are not beans.
        // Cannot use the ejb's creationalContext.
        creationalContext = beanManager.createCreationalContext(null);

        InjectionTarget injectionTarget = beanManager.getInjectionTargetFactory(beanManager.createAnnotatedType(interceptorClass))
                .createInterceptorInjectionTarget();

        T interceptorInstance = (T) injectionTarget.produce(creationalContext);
        injectionTarget.inject(interceptorInstance, creationalContext);

        // Make sure the interceptor's cdi objects get cleaned up when the ejb is cleaned up.
        ejbContext.addDependentContext(new CDIInjectionContextImpl<>(injectionTarget, creationalContext, interceptorInstance));

        return interceptorInstance;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> CDIInjectionContext<T> createManagedObject(Class<T> managedClass, BundleDescriptor bundle, boolean invokePostConstruct) {
        // First get BeanDeploymentArchive
        BeanDeploymentArchiveImpl beanDeploymentArchive = getBeanDeploymentArchiveFromBundle(bundle);
        WeldManager beanManager = getWeldManagerFromBundle(bundle, beanDeploymentArchive);

        AnnotatedType annotatedType = beanManager.createAnnotatedType(managedClass);
        if (!invokePostConstruct) {
            annotatedType = new NoPostConstructPreDestroyAnnotatedType(annotatedType);
        }

        InjectionTarget injectionTarget = beanDeploymentArchive.getInjectionTarget(annotatedType);
        if (injectionTarget == null) {
            injectionTarget = beanManager.fireProcessInjectionTarget(annotatedType);
        }

        CreationalContext creationalContext = beanManager.createCreationalContext(null);

        Object managedObject = injectionTarget.produce(creationalContext);

        injectionTarget.inject(managedObject, creationalContext);

        if (invokePostConstruct) {
            injectionTarget.postConstruct(managedObject);
        }

        return new CDIInjectionContextImpl(injectionTarget, creationalContext, managedObject);
    }

    private BeanDeploymentArchiveImpl getBeanDeploymentArchiveFromBundle(BundleDescriptor bundle) {
        return (BeanDeploymentArchiveImpl) weldDeployer
                .getBeanDeploymentArchiveForBundle((BundleDescriptor) bundle.getModuleDescriptor().getDescriptor());
    }

    private BeanManager getBeanManagerFromBundle(BundleDescriptor bundle) {
        BundleDescriptor topLevelBundleDesc = (BundleDescriptor) bundle.getModuleDescriptor().getDescriptor();

        // First get BeanDeploymentArchive for this Enterprise Bean
        BeanDeploymentArchive beanDeploymentArchive = weldDeployer.getBeanDeploymentArchiveForBundle(topLevelBundleDesc);

        return getWeldManagerFromBundle(bundle, beanDeploymentArchive);
    }

    private WeldManager getWeldManagerFromBundle(BundleDescriptor bundle, BeanDeploymentArchive beanDeploymentArchive) {
        return weldDeployer.getBootstrapForApp(bundle.getApplication()).getManager(beanDeploymentArchive);
    }

    /**
     * This class is here to exclude the post-construct and pre-destroy methods from the AnnotatedType. This is done in
     * cases where Weld will not be calling those methods and we therefore do NOT want Weld to validate them, as they may be
     * of the form required for interceptors rather than Managed objects
     *
     * @author jwells
     *
     * @param <X>
     */
    private static class NoPostConstructPreDestroyAnnotatedType<X> implements AnnotatedType<X> {
        private final AnnotatedType<X> delegate;

        private NoPostConstructPreDestroyAnnotatedType(AnnotatedType<X> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Type getBaseType() {
            return delegate.getBaseType();
        }

        @Override
        public Set<Type> getTypeClosure() {
            return delegate.getTypeClosure();
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            return delegate.getAnnotation(annotationType);
        }

        @Override
        public Set<Annotation> getAnnotations() {
            return delegate.getAnnotations();
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return delegate.isAnnotationPresent(annotationType);
        }

        @Override
        public Class<X> getJavaClass() {
            return delegate.getJavaClass();
        }

        @Override
        public Set<AnnotatedConstructor<X>> getConstructors() {
            return delegate.getConstructors();
        }

        @Override
        public Set<AnnotatedMethod<? super X>> getMethods() {
            HashSet<AnnotatedMethod<? super X>> retVal = new HashSet<>();
            for (AnnotatedMethod<? super X> m : delegate.getMethods()) {
                if (m.isAnnotationPresent(PostConstruct.class) || m.isAnnotationPresent(PreDestroy.class)) {
                    // Do not include the post-construct or pre-destroy
                    continue;
                }

                retVal.add(m);
            }
            return retVal;
        }

        @Override
        public Set<AnnotatedField<? super X>> getFields() {
            return delegate.getFields();
        }

    }

    @Override
    public CDIInjectionContext createEmptyCDIInjectionContext() {
        return new CDIInjectionContextImpl();
    }

    @SuppressWarnings("rawtypes")
    private static class CDIInjectionContextImpl<T> implements CDIInjectionContext<T> {
        InjectionTarget injectionTarget;
        CreationalContext creationalContext;
        T instance;

        private final List<CDIInjectionContext> dependentContexts = new ArrayList<>();
        private CDIAroundConstructCallback cdiAroundConstructCallback;

        public CDIInjectionContextImpl() {
        }

        public CDIInjectionContextImpl(InjectionTarget it, CreationalContext cc, T i) {
            this.injectionTarget = it;
            this.creationalContext = cc;
            this.instance = i;
        }

        @Override
        public T getInstance() {
            return instance;
        }

        @Override
        public void setInstance(T instance) {
            this.instance = instance;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void cleanup(boolean callPreDestroy) {
            for (CDIInjectionContext context : dependentContexts) {
                context.cleanup(true);
            }

            if (callPreDestroy) {
                if (injectionTarget != null) {
                    injectionTarget.preDestroy(instance);
                }
            }

            if (injectionTarget != null) {
                injectionTarget.dispose(instance);
            }

            if (creationalContext != null) {
                creationalContext.release();
            }
        }

        @Override
        public InjectionTarget<T> getInjectionTarget() {
            return injectionTarget;
        }

        @Override
        public void setInjectionTarget(InjectionTarget<T> injectionTarget) {
            this.injectionTarget = injectionTarget;
        }

        @Override
        public CreationalContext<T> getCreationalContext() {
            return creationalContext;
        }

        @Override
        public void setCreationalContext(CreationalContext<T> creationalContext) {
            this.creationalContext = creationalContext;
        }

        @Override
        public void addDependentContext(CDIInjectionContext dependentContext) {
            dependentContexts.add(dependentContext);
        }

        @Override
        public Collection<CDIInjectionContext> getDependentContexts() {
            return dependentContexts;
        }

        @Override
        public T createEjbAfterAroundConstruct() {
            if (cdiAroundConstructCallback != null) {
                setInstance((T) cdiAroundConstructCallback.createEjb());
            }

            return instance;
        }

        public void setCDIAroundConstructCallback(CDIAroundConstructCallback cdiAroundConstructCallback) {
            this.cdiAroundConstructCallback = cdiAroundConstructCallback;
        }
    }
}
