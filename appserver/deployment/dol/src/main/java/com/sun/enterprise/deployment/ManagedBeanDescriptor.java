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

import com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType;
import com.sun.enterprise.deployment.util.ApplicationValidator;
import com.sun.enterprise.deployment.util.ManagedBeanVisitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.api.naming.SimpleJndiName;

import static com.sun.enterprise.deployment.MethodDescriptor.EJB_BEAN;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_APP;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_GLOBAL;

/**
 * Descriptor representing a Jakarta EE Managed Bean.
 *
 * @author Kenneth Saks
 */
public final class ManagedBeanDescriptor extends JndiEnvironmentRefsGroupDescriptor {

    private static final long serialVersionUID = 1L;

    // *Optional* managed bean name.  Only non-null if the
    // bean has been assigned a name by the developer.
    // (E.g., via the @ManagedBean name() attribute)
    private String name;

    // fully-qualified class name of managed bean class
    private String beanClassName;

    // Module in which managed bean is defined
    private BundleDescriptor enclosingBundle;

    private Object interceptorBuilder;
    private final Collection<Object> beanInstances = new HashSet<>();
    private final Map<Object, Object> beanSupportingInfo = new HashMap<>();

    private List<InterceptorDescriptor> classInterceptorChain = new LinkedList<>();

    private final Set<LifecycleCallbackDescriptor> aroundInvokeDescs = new HashSet<>();

    //
    // Interceptor info per business method.  If the map does not
    // contain an entry for the business method, there is no method-specific
    // interceptor information for that method.  In that case the standard
    // class-level interceptor information applies.
    //
    // If there is an entry for the business method, the corresponding list
    // represents the *complete* ordered list of interceptor classes for that
    // method.  An empty list would mean all the interceptors have been
    // disabled for that particular business method.
    //
    private final Map<MethodDescriptor, List<InterceptorDescriptor>> methodInterceptorsMap = new HashMap<>();

    /**
     * Default constructor.
     */
    public ManagedBeanDescriptor() {
    }


    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isNamed() {
        return (name != null);
    }

    public void setBeanClassName(String className) {
        beanClassName = className;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBundle(BundleDescriptor bundle) {
        enclosingBundle = bundle;
        super.setBundleDescriptor(bundle);
    }

    public BundleDescriptor getBundle() {
        return enclosingBundle;
    }

    public void setInterceptorBuilder(Object b) {
        interceptorBuilder = b;
    }

    public Object getInterceptorBuilder() {
        return interceptorBuilder;
    }

    public boolean hasInterceptorBuilder() {
        return interceptorBuilder != null;
    }


    public void addBeanInstanceInfo(Object o) {
        addBeanInstanceInfo(o, null);
    }


    // InterceptorInfo can be null
    public void addBeanInstanceInfo(Object o, Object supportingInfo) {
        beanInstances.add(o);
        if (supportingInfo != null) {
            beanSupportingInfo.put(o, supportingInfo);
        }
    }


    public Collection<Object> getBeanInstances() {
        return new HashSet(beanInstances);
    }


    public Object getSupportingInfoForBeanInstance(Object o) {
        return beanSupportingInfo.get(o);
    }


    public void clearBeanInstanceInfo(Object beanInstance) {
        beanInstances.remove(beanInstance);
        beanSupportingInfo.remove(beanInstance);
    }

    public void clearAllBeanInstanceInfo() {
        beanInstances.clear();
        beanSupportingInfo.clear();
        interceptorBuilder = null;
    }

    public Set<String> getAllInterceptorClasses() {
        Set<String> classes = new HashSet<>();
        for (InterceptorDescriptor desc : classInterceptorChain) {
            classes.add(desc.getInterceptorClassName());
        }

        for (List<InterceptorDescriptor> intList : methodInterceptorsMap.values()) {
            for (InterceptorDescriptor interceptor : intList) {
                classes.add(interceptor.getInterceptorClassName());
            }
        }
        return classes;
    }


    public void setClassInterceptorChain(List<InterceptorDescriptor> chain) {
        classInterceptorChain = new LinkedList<>(chain);
    }


    public void setMethodLevelInterceptorChain(MethodDescriptor m, List<InterceptorDescriptor> chain) {
        methodInterceptorsMap.put(m, chain);
    }


    /**
     * @return the ordered list of AroundConstruct interceptors
     */
    public List<InterceptorDescriptor> getAroundConstructCallbackInterceptors(Class clz, Constructor ctor) {
        LinkedList<InterceptorDescriptor> callbackInterceptors = new LinkedList<>();
        Class<?>[] ctorParamTypes = ctor.getParameterTypes();
        String[] parameterClassNames = new MethodDescriptor().getParameterClassNamesFor(null, ctorParamTypes);
        MethodDescriptor mDesc = new MethodDescriptor(clz.getSimpleName(), null, parameterClassNames, EJB_BEAN);

        List<InterceptorDescriptor> interceptors = methodInterceptorsMap.get(mDesc);
        if (interceptors == null) {
            interceptors = classInterceptorChain;
        }

        for (InterceptorDescriptor next : interceptors) {
            if (!next.getCallbackDescriptors(CallbackType.AROUND_CONSTRUCT).isEmpty()) {
                callbackInterceptors.add(next);
            }
        }

        // There are no bean-level AroundConstruct interceptors
        return callbackInterceptors;
    }


    /**
     * @return the ordered list of interceptor info for a particular
     *         callback event type. This list *does* include the info
     *         on any bean class callback. If present, this would always be the
     *         last element in the list because of the precedence defined by the spec.
     */
    public List<InterceptorDescriptor> getCallbackInterceptors(CallbackType type) {
        LinkedList<InterceptorDescriptor> callbackInterceptors = new LinkedList<>();

        for (InterceptorDescriptor next : classInterceptorChain) {
            if (!next.getCallbackDescriptors(type).isEmpty()) {
                callbackInterceptors.add(next);
            }
        }

        if (this.hasCallbackDescriptor(type)) {
            InterceptorDescriptor beanClassCallbackInfo = new InterceptorDescriptor();
            beanClassCallbackInfo.setFromBeanClass(true);
            beanClassCallbackInfo.addCallbackDescriptors(type, this.getCallbackDescriptors(type));
            beanClassCallbackInfo.setInterceptorClassName(getBeanClassName());
            callbackInterceptors.add(beanClassCallbackInfo);
        }

        return callbackInterceptors;
    }


    public Set<LifecycleCallbackDescriptor> getAroundInvokeDescriptors() {
        return aroundInvokeDescs;
    }


    public void addAroundInvokeDescriptor(LifecycleCallbackDescriptor aroundInvokeDesc) {
        String className = aroundInvokeDesc.getLifecycleCallbackClass();
        boolean found = false;
        for (LifecycleCallbackDescriptor next : getAroundInvokeDescriptors()) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                found = true;
                break;
            }
        }
        if (!found) {
            getAroundInvokeDescriptors().add(aroundInvokeDesc);
        }
    }


    public LifecycleCallbackDescriptor getAroundInvokeDescriptorByClass(String className) {
        for (LifecycleCallbackDescriptor next : getAroundInvokeDescriptors()) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                return next;
            }
        }
        return null;
    }

    public boolean hasAroundInvokeMethod() {
        return !getAroundInvokeDescriptors().isEmpty();
    }

    /**
     * Return the ordered list of interceptor info for AroundInvoke behavior
     * of a particular business method.  This list *does* include the info
     * on any bean class interceptor.  If present, this would always be the
     * last element in the list because of the precedence defined by the spec.
     */
    public List<InterceptorDescriptor> getAroundInvokeInterceptors(Method m) {
        MethodDescriptor mDesc = new MethodDescriptor(m);

        // See if there's any method-level setting (either a chain
        // or a empty list ). If not, use class-level chain
        List<InterceptorDescriptor> aroundInvokeInterceptors = methodInterceptorsMap.get(mDesc);

        if (aroundInvokeInterceptors == null) {
            aroundInvokeInterceptors = new LinkedList<>();
            for (InterceptorDescriptor desc : classInterceptorChain) {
                if (desc.hasAroundInvokeDescriptor()) {
                    aroundInvokeInterceptors.add(desc);
                }
            }
        }

        // Add any managed bean around invokes
        if (hasAroundInvokeMethod()) {

            EjbInterceptor interceptorInfo = new EjbInterceptor();
            interceptorInfo.setFromBeanClass(true);
            interceptorInfo.addAroundInvokeDescriptors(getAroundInvokeDescriptors());
            interceptorInfo.setInterceptorClassName(beanClassName);

            aroundInvokeInterceptors.add(interceptorInfo);
        }

        return aroundInvokeInterceptors;
    }


    /**
     * @return can be null or {@link SimpleJndiName}
     */
    public SimpleJndiName getGlobalJndiName() {
        if (enclosingBundle == null) {
            return null;
        }

        Application app = enclosingBundle.getApplication();
        String appName = app.isVirtual() ? null : enclosingBundle.getApplication().getAppName();
        String modName = enclosingBundle.getModuleDescriptor().getModuleName();

        StringBuilder javaGlobalPrefix = new StringBuilder(64).append(JNDI_CTX_JAVA_GLOBAL);
        if (appName != null) {
            javaGlobalPrefix.append(appName);
            javaGlobalPrefix.append('/');
        }
        javaGlobalPrefix.append(modName);
        javaGlobalPrefix.append('/');


        // If the managed bean is named, use the name for the final component
        // of the managed bean global name.  Otherwise, use a derived internal
        // name since we'll still need a way to register and lookup the bean
        // from within the container.

        String componentName = isNamed() ? name : "___internal_managed_bean_" + beanClassName;
        javaGlobalPrefix.append(componentName);
        return new SimpleJndiName(javaGlobalPrefix.toString());
    }


    public SimpleJndiName getAppJndiName() {
        if (enclosingBundle == null) {
            return null;
        }

        String modName = enclosingBundle.getModuleDescriptor().getModuleName();
        StringBuilder javaAppPrefix = new StringBuilder().append(JNDI_CTX_JAVA_APP);
        javaAppPrefix.append(modName);
        javaAppPrefix.append('/');

        // If the managed bean is named, use the name for the final component
        // of the managed bean global name.  Otherwise, use a derived internal
        // name since we'll still need a way to register and lookup the bean
        // from within the container.

        String componentName = isNamed() ? name : "___internal_managed_bean_" + beanClassName;
        javaAppPrefix.append(componentName);
        return new SimpleJndiName(javaAppPrefix.toString());

    }


    @Override
    public void print(StringBuffer toStringBuffer) {
    }


    public void validate() {
        visit(new ApplicationValidator());
    }


    public void visit(ManagedBeanVisitor aVisitor) {
        aVisitor.accept(this);
    }


    @Override
    public List<InjectionCapable> getInjectableResourcesByClass(String className) {
        List<InjectionCapable> injectables = new LinkedList<>();
        for (EnvironmentProperty envEntry : getEnvironmentProperties()) {
            if (envEntry.hasContent()) {
                injectables.add(envEntry);
            }
        }

        injectables.addAll(getEjbReferenceDescriptors());
        injectables.addAll(getServiceReferenceDescriptors());
        injectables.addAll(getResourceReferenceDescriptors());
        injectables.addAll(getResourceEnvReferenceDescriptors());
        injectables.addAll(getMessageDestinationReferenceDescriptors());

        injectables.addAll(getEntityManagerFactoryReferenceDescriptors());
        injectables.addAll(getEntityManagerReferenceDescriptors());

        List<InjectionCapable> injectablesByClass = new LinkedList<>();
        for (InjectionCapable next : injectables ) {
            if (next.isInjectable()) {
                for (InjectionTarget target : next.getInjectionTargets()) {
                    if (target.getClassName().equals(className)) {
                        injectablesByClass.add(next);
                    }
                }
            }
        }

        return injectablesByClass;
    }

    @Override
    public InjectionInfo getInjectionInfoByClass(Class<?> clazz) {
        String className = clazz.getName();
        LifecycleCallbackDescriptor postConstructDesc = getPostConstructDescriptorByClass(className);
        String postConstructMethodName = postConstructDesc == null ? null : postConstructDesc.getLifecycleCallbackMethod();
        LifecycleCallbackDescriptor preDestroyDesc = getPreDestroyDescriptorByClass(className);
        String preDestroyMethodName = preDestroyDesc == null ? null : preDestroyDesc.getLifecycleCallbackMethod();
        InjectionInfo injectionInfo = new InjectionInfo(className, postConstructMethodName, preDestroyMethodName,
            getInjectableResourcesByClass(className));

        return injectionInfo;
    }

    @Override
    public LifecycleCallbackDescriptor getPostConstructDescriptorByClass(String className) {
        for (LifecycleCallbackDescriptor next : getPostConstructDescriptors()) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                return next;
            }
        }
        return null;
    }

    @Override
    public LifecycleCallbackDescriptor getPreDestroyDescriptorByClass(String className) {
        for (LifecycleCallbackDescriptor next : getPreDestroyDescriptors()) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                return next;
            }
        }
        return null;
    }
}

