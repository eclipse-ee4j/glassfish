/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.ejb.deployment.descriptor;

import com.sun.enterprise.container.common.spi.CDIService;
import com.sun.enterprise.deployment.EjbBeanDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerFactoryReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerReferenceDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.InterceptorDescriptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType;
import com.sun.enterprise.deployment.MessageDestinationReferenceDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.MethodPermission;
import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EJBObject;
import jakarta.inject.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.DescriptorVisitor;
import org.glassfish.ejb.deployment.BeanMethodCalculatorImpl;
import org.glassfish.ejb.deployment.descriptor.runtime.IASEjbExtraDescriptors;
import org.glassfish.ejb.deployment.util.EjbVisitor;
import org.glassfish.ejb.deployment.util.InterceptorBindingTranslator;
import org.glassfish.ejb.deployment.util.InterceptorBindingTranslator.TranslationResults;
import org.glassfish.internal.api.Globals;

import static com.sun.enterprise.deployment.MethodDescriptor.EJB_BEAN;

/**
 * This abstract class encapsulates the meta-information describing Entity, Session and MessageDriven EJBs.
 *
 * @author Danny Coward
 * @author Sanjeev Krishnan
 */
public abstract class EjbDescriptor extends EjbBeanDescriptor {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = DOLUtils.getDefaultLogger();
    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(EjbDescriptor.class);

    private final Set<String> noInterfaceLocalBeanClassNames = new HashSet<>();
    private final Set<LifecycleCallbackDescriptor> aroundInvokeDescs = new HashSet<>();
    private final Set<LifecycleCallbackDescriptor> aroundTimeoutDescs = new HashSet<>();

    // Late-binding system-level interceptors for this EJB. These can be set
    // as late as initialization time, so they are not part of the interceptor
    // binding translation that happens for application-defined interceptors.
    private final List<InterceptorDescriptor> frameworkInterceptors = new LinkedList<>();

    // Ludo 12/10/2001 extra DTD info only for iAS
    private final IASEjbExtraDescriptors iASEjbExtraDescriptors = new IASEjbExtraDescriptors();

    private EjbBundleDescriptorImpl bundleDescriptor;
    private boolean usesDefaultTransaction;

    private String ejbObjectImplClassName;
    private String ejbLocalObjectImplClassName;
    private String localHomeImplClassName;
    private String remoteHomeImplClassName;

    private String securityIdentityDescription;

    private Hashtable<MethodDescriptor, ContainerTransaction> methodContainerTransactions = new Hashtable<>();

    /**
     * Default constructor.
     */
    protected EjbDescriptor() {
    }


    public EjbDescriptor(EjbDescriptor other) {
        super(other);
        copyEjbDescriptor(other);
    }


    public abstract String getContainerFactoryQualifier();


    /**
     * @return the extra iAS specific info (not in the RI DID) in the iAS DTD. no setter.
     *         You have to modify some fields of the returned object to change it.
     */
    public IASEjbExtraDescriptors getIASEjbExtraDescriptors() {
        return iASEjbExtraDescriptors;
    }


    public void copyEjbDescriptor(EjbDescriptor other) {
        this.bundleDescriptor = other.bundleDescriptor;
        copyEjbDescriptor(other, this);
        this.methodContainerTransactions = new Hashtable<>(other.methodContainerTransactions);
    }


    @Override
    public final EjbBundleDescriptorImpl getEjbBundleDescriptor() {
        return bundleDescriptor;
    }


    @Override
    public void setEjbBundleDescriptor(EjbBundleDescriptor bundleDescriptor) {
        // We accept just this type.
        this.bundleDescriptor = (EjbBundleDescriptorImpl) bundleDescriptor;
    }


    /**
     * @return the classname of the {@link EJBLocalObject} impl.
     */
    public String getEJBLocalObjectImplClassName() {
        return this.ejbLocalObjectImplClassName;
    }


    /**
     * @param className the {@link EJBLocalObject} implementation classname of the ejb.
     */
    public void setEJBLocalObjectImplClassName(String className) {
        this.ejbLocalObjectImplClassName = className;
    }


    /**
     * @return the classname of the {@link EJBObject} impl.
     */
    public String getEJBObjectImplClassName() {
        return this.ejbObjectImplClassName;
    }


    /**
     * @param className {@link EJBObject} implementation classname of the ejb.
     */
    public void setEJBObjectImplClassName(String className) {
        this.ejbObjectImplClassName = className;
    }


    /**
     * @return the classname of the Local home impl.
     */
    public String getLocalHomeImplClassName() {
        return this.localHomeImplClassName;
    }


    /**
     * @param className the Local home implementation classname of the ejb.
     */
    public void setLocalHomeImplClassName(String className) {
        this.localHomeImplClassName = className;
    }


    /**
     * @return the classname of the remote home impl.
     */
    public String getRemoteHomeImplClassName() {
        return this.remoteHomeImplClassName;
    }


    /**
     * @param className remote home implementation classname of the ejb.
     */
    public void setRemoteHomeImplClassName(String className) {
        this.remoteHomeImplClassName = className;
    }


    /**
     * @return all the public classes of this no-interface local ejb
     */
    public Set<String> getNoInterfaceLocalBeanClasses() {
        return this.noInterfaceLocalBeanClassNames;
    }


    /**
     * Add a classname for a no-interface view of the local ejb
     *
     * @param className fully qualified class name for the interface
     */
    public void addNoInterfaceLocalBeanClass(String className) {
        this.noInterfaceLocalBeanClassNames.add(className);
    }


    public boolean hasAroundInvokeMethod() {
        return !aroundInvokeDescs.isEmpty();
    }


    public final Set<LifecycleCallbackDescriptor> getAroundInvokeDescriptors() {
        return aroundInvokeDescs;
    }


    /**
     * @param className
     * @return {@link LifecycleCallbackDescriptor} with
     *         {@link LifecycleCallbackDescriptor#getLifecycleCallbackClass()} of a given
     */
    public LifecycleCallbackDescriptor getAroundInvokeDescriptorByClass(String className) {
        for (LifecycleCallbackDescriptor aroundInvoke : aroundInvokeDescs) {
            if (aroundInvoke.getLifecycleCallbackClass().equals(className)) {
                return aroundInvoke;
            }
        }
        return null;
    }


    /**
     * @param businessMethod intercepted method
     * @return the ordered list of interceptor info for AroundInvoke behavior of a particular
     *         business method.
     *         This list *does* include the info on any bean class interceptor.
     *         If present, this would always be the last element in the list because of the
     *         precedence defined by the spec.
     */
    public List<EjbInterceptor> getAroundInvokeInterceptors(MethodDescriptor businessMethod) {
        LinkedList<EjbInterceptor> aroundInvokeInterceptors = new LinkedList<>();
        List<EjbInterceptor> classOrMethodInterceptors = getClassOrMethodInterceptors(businessMethod);
        for (EjbInterceptor interceptor : classOrMethodInterceptors) {
            if (!interceptor.getAroundInvokeDescriptors().isEmpty()) {
                aroundInvokeInterceptors.add(interceptor);
            }
        }

        if (hasAroundInvokeMethod()) {
            EjbInterceptor interceptorInfo = new EjbInterceptor();
            interceptorInfo.setFromBeanClass(true);
            interceptorInfo.addAroundInvokeDescriptors(aroundInvokeDescs);
            interceptorInfo.setInterceptorClassName(getEjbImplClassName());
            aroundInvokeInterceptors.add(interceptorInfo);
        }
        return aroundInvokeInterceptors;
    }


    /**
     * Adds the descriptor if there is no other with the same lifecycle callback class.
     *
     * @param aroundInvokeDesc
     */
    public void addAroundInvokeDescriptor(LifecycleCallbackDescriptor aroundInvokeDesc) {
        String className = aroundInvokeDesc.getLifecycleCallbackClass();
        for (LifecycleCallbackDescriptor aroundInvoke : aroundInvokeDescs) {
            if (aroundInvoke.getLifecycleCallbackClass().equals(className)) {
                return;
            }
        }
        aroundInvokeDescs.add(aroundInvokeDesc);
    }


    public boolean hasAroundTimeoutMethod() {
        return !aroundTimeoutDescs.isEmpty();
    }


    public final Set<LifecycleCallbackDescriptor> getAroundTimeoutDescriptors() {
        return aroundTimeoutDescs;
    }


    public LifecycleCallbackDescriptor getAroundTimeoutDescriptorByClass(String className) {
        for (LifecycleCallbackDescriptor callback : aroundTimeoutDescs) {
            if (callback.getLifecycleCallbackClass().equals(className)) {
                return callback;
            }
        }
        return null;
    }


    /**
     * @param businessMethod intercepted method
     * @return the ordered list of interceptor info for AroundTimeout behavior of a particular
     *         business method.
     *         This list *does* include the info on any bean class interceptor.
     *         If present, this would always be the last element in the list because of the
     *         precedence defined by the spec.
     */
    public List<EjbInterceptor> getAroundTimeoutInterceptors(MethodDescriptor businessMethod) {
        LinkedList<EjbInterceptor> aroundTimeoutInterceptors = new LinkedList<>();
        List<EjbInterceptor> classOrMethodInterceptors = getClassOrMethodInterceptors(businessMethod);
        for (EjbInterceptor interceptor : classOrMethodInterceptors) {
            if (!interceptor.getAroundTimeoutDescriptors().isEmpty()) {
                aroundTimeoutInterceptors.add(interceptor);
            }
        }

        if (hasAroundTimeoutMethod()) {
            EjbInterceptor interceptorInfo = new EjbInterceptor();
            interceptorInfo.setFromBeanClass(true);
            interceptorInfo.addAroundTimeoutDescriptors(aroundTimeoutDescs);
            interceptorInfo.setInterceptorClassName(getEjbImplClassName());
            aroundTimeoutInterceptors.add(interceptorInfo);
        }
        return aroundTimeoutInterceptors;
    }


    public void addAroundTimeoutDescriptor(LifecycleCallbackDescriptor aroundTimeoutDesc) {
        String className = aroundTimeoutDesc.getLifecycleCallbackClass();
        for (LifecycleCallbackDescriptor callback : aroundTimeoutDescs) {
            if (callback.getLifecycleCallbackClass().equals(className)) {
                return;
            }
        }
        aroundTimeoutDescs.add(aroundTimeoutDesc);
    }


    public List<InterceptorDescriptor> getFrameworkInterceptors() {
        return frameworkInterceptors;
    }


    @Override
    public void addFrameworkInterceptor(InterceptorDescriptor interceptor) {
        for (InterceptorDescriptor existing : frameworkInterceptors) {
            if (existing.getInterceptorClassName().equals(interceptor.getInterceptorClassName())) {
                return;
            }
        }
        frameworkInterceptors.add(interceptor);
    }


    /**
     * @return the description field of security-identity
     */
    public String getSecurityIdentityDescription() {
        if (securityIdentityDescription == null) {
            securityIdentityDescription = "";
        }
        return securityIdentityDescription;
    }


    /**
     * @param securityIdentityDescription the description field of security-identity
     */
    public void setSecurityIdentityDescription(String securityIdentityDescription) {
        this.securityIdentityDescription = securityIdentityDescription;
    }


    /**
     * Have default method transaction if isBoundsChecking is on.
     */
    public void setUsesDefaultTransaction() {
        usesDefaultTransaction = true;
    }


    /**
     * Since ejb-class is optional, in some cases the lifecycle-class for AroundInvoke, PostConstruct, etc. methods on the
     * bean-class is not known at processing time and must be applied lazily. As such, this method should only be called if
     * the ejb-class has been set on this EjbDescriptor.
     */
    public void applyDefaultClassToLifecycleMethods() {
        Set<LifecycleCallbackDescriptor> lifecycleMethods = getLifecycleCallbackDescriptors();
        lifecycleMethods.addAll(aroundInvokeDescs);
        lifecycleMethods.addAll(aroundTimeoutDescs);
        for (LifecycleCallbackDescriptor method : lifecycleMethods) {
            if (method.getLifecycleCallbackClass() == null) {
                method.setLifecycleCallbackClass(getEjbClassName());
            }
        }
    }


    public Set<LifecycleCallbackDescriptor> getLifecycleCallbackDescriptors() {
        Set<LifecycleCallbackDescriptor> lifecycleMethods = new HashSet<>();
        lifecycleMethods.addAll(getPostConstructDescriptors());
        lifecycleMethods.addAll(getPreDestroyDescriptors());
        if (com.sun.enterprise.deployment.EjbSessionDescriptor.TYPE.equals(getType())) {
            EjbSessionDescriptor sfulDesc = (EjbSessionDescriptor) this;
            lifecycleMethods.addAll(sfulDesc.getPrePassivateDescriptors());
            lifecycleMethods.addAll(sfulDesc.getPostActivateDescriptors());
        }

        return lifecycleMethods;
    }


    /**
     * Derive all interceptors that are applicable to this bean.
     */
    public void applyInterceptors(InterceptorBindingTranslator bindingTranslator) {

        // Apply this ejb to the ordered set of all interceptor bindings
        // for this ejb-jar. The results will contain all interceptor
        // information that applies to the ejb. There is no notion of
        // default interceptors within the results. Default interceptors
        // are used during the translation process but once we derive
        // the per-ejb interceptor information there is only a notion of
        // class-level ordering and method-level ordering. Any applicable
        // default interceptors will have been applied to the class-level.
        TranslationResults results = bindingTranslator.apply(getName());
        setInterceptorClasses(results.allInterceptorClasses);
        setInterceptorChain(results.classInterceptorChain);
        setMethodInterceptorsMap(results.methodInterceptorsMap);

        for (EjbInterceptor interceptor : results.allInterceptorClasses) {
            for (EjbReferenceDescriptor ejbRefObj : interceptor.getEjbReferenceDescriptors()) {
                addEjbReferenceDescriptor(ejbRefObj);
            }

            for (MessageDestinationReferenceDescriptor msgDestRefObj : interceptor.getMessageDestinationReferenceDescriptors()) {
                addMessageDestinationReferenceDescriptor(msgDestRefObj);
            }

            for (EnvironmentProperty envPropObj : interceptor.getEnvironmentProperties()) {
                addOrMergeEnvironmentProperty(envPropObj);
            }

            for (ServiceReferenceDescriptor servRefObj : interceptor.getServiceReferenceDescriptors()) {
                addServiceReferenceDescriptor(servRefObj);
            }

            for (ResourceReferenceDescriptor resRefObj : interceptor.getResourceReferenceDescriptors()) {
                addResourceReferenceDescriptor(resRefObj);
            }

            for (ResourceEnvReferenceDescriptor resourceEnvRefObj : interceptor.getResourceEnvReferenceDescriptors()) {
                addResourceEnvReferenceDescriptor(resourceEnvRefObj);
            }

            for (EntityManagerFactoryReferenceDescriptor entMgrFacRef : interceptor.getEntityManagerFactoryReferenceDescriptors()) {
                addEntityManagerFactoryReferenceDescriptor(entMgrFacRef);
            }

            for (EntityManagerReferenceDescriptor entMgrRef : interceptor.getEntityManagerReferenceDescriptors()) {
                addEntityManagerReferenceDescriptor(entMgrRef);
            }
        }
    }

    /**
     * @param type {@link CallbackType} used as a filter
     * @return the ordered list of interceptor info for a particular callback event type.
     *         This list *does* include the info on any bean class callback.
     *         If present, this would always be the last element in the list because
     *         of the precedence defined by the spec.
     */
    public List<EjbInterceptor> getCallbackInterceptors(CallbackType type) {
        Set<LifecycleCallbackDescriptor> callbackDescriptors = null;
        switch (type) {
            case AROUND_CONSTRUCT:
                break;
            case POST_CONSTRUCT:
                callbackDescriptors = getPostConstructDescriptors();
                break;
            case PRE_DESTROY:
                callbackDescriptors = getPreDestroyDescriptors();
                break;
            case PRE_PASSIVATE:
                callbackDescriptors = ((EjbSessionDescriptor) this).getPrePassivateDescriptors();
                break;
            case POST_ACTIVATE:
                callbackDescriptors = ((EjbSessionDescriptor) this).getPostActivateDescriptors();
                break;
            default:
                throw new IllegalStateException(I18N.getLocalString("enterprise.deployment.invalidcallbacktype",
                    "Invalid callback type: [{0}]", type));
        }
        return getCallbackInterceptors(type, callbackDescriptors);
    }


    private LinkedList<EjbInterceptor> getCallbackInterceptors(CallbackType type,
        Set<LifecycleCallbackDescriptor> callbackDescriptors) {
        LinkedList<EjbInterceptor> callbackInterceptors = new LinkedList<>();

        ClassLoader classLoader = getEjbBundleDescriptor().getClassLoader();
        List<EjbInterceptor> classOrMethodInterceptors = type.equals(CallbackType.AROUND_CONSTRUCT)
            ? getConstructorInterceptors(classLoader)
            : getInterceptorChain();

        for (EjbInterceptor interceptor : classOrMethodInterceptors) {
            if (!interceptor.getCallbackDescriptors(type).isEmpty()) {
                callbackInterceptors.add(interceptor);
            }
        }

        if (callbackDescriptors != null && !callbackDescriptors.isEmpty()) {
            EjbInterceptor beanClassCallbackInfo = new EjbInterceptor();
            beanClassCallbackInfo.setFromBeanClass(true);
            beanClassCallbackInfo.addCallbackDescriptors(type, callbackDescriptors);
            beanClassCallbackInfo.setInterceptorClassName(getEjbImplClassName());
            callbackInterceptors.add(beanClassCallbackInfo);
        }

        return callbackInterceptors;
    }


    /**
     * Return bean constructor for AroundConstruct interceptors
     */
    private List<EjbInterceptor> getConstructorInterceptors(ClassLoader classLoader) {
        final String ejbClassName = getEjbClassName();
        final String shortClassName;
        int i = ejbClassName.lastIndexOf('.');
        if (i > -1) {
            shortClassName = ejbClassName.substring(i + 1);
        } else {
            shortClassName = ejbClassName;
        }

        List<EjbInterceptor> callbackInterceptors = null;
        CDIService cdiService = Globals.getStaticBaseServiceLocator().getService(CDIService.class);
        if (cdiService != null && cdiService.isCDIEnabled(getEjbBundleDescriptor())) {
            try {
                Class<?> beanClass = classLoader.loadClass(ejbClassName);
                Constructor<?>[] ctors = beanClass.getDeclaredConstructors();

                String[] parameterClassNames = null;
                MethodDescriptor dummy = new MethodDescriptor();
                for (Constructor<?> ctor : ctors) {
                    if (ctor.getAnnotation(Inject.class) != null) {
                        // @Inject constructor
                        Class<?>[] ctorParamTypes = ctor.getParameterTypes();
                        parameterClassNames = dummy.getParameterClassNamesFor(null, ctorParamTypes);
                        callbackInterceptors = getClassOrMethodInterceptors(
                                new MethodDescriptor(shortClassName, null, parameterClassNames, EJB_BEAN));
                        break;
                    }
                }
            } catch (Throwable t) {
                throw new RuntimeException(ejbClassName, t);
            }
        }
        if (callbackInterceptors == null) {
            // non-CDI or no @Inject constructor - use no-arg constructor
            callbackInterceptors = getClassOrMethodInterceptors(
                new MethodDescriptor(shortClassName, null, new String[0], EJB_BEAN));
        }

        return callbackInterceptors;
    }


    private List<EjbInterceptor> getClassOrMethodInterceptors(MethodDescriptor businessMethod) {
        List<EjbInterceptor> classOrMethodInterceptors = null;
        for (Entry<MethodDescriptor, List<EjbInterceptor>> entry : getMethodInterceptorsMap().entrySet()) {
            MethodDescriptor methodDesc = entry.getKey();
            if (methodDesc.implies(businessMethod)) {
                classOrMethodInterceptors = entry.getValue();
            }
        }
        if (classOrMethodInterceptors == null) {
            return getInterceptorChain();
        }
        return classOrMethodInterceptors;
    }


    /**
     * @return a copy of the mapping held internally of method descriptors to container transaction objects.
     */
    public final Hashtable<MethodDescriptor, ContainerTransaction> getMethodContainerTransactions() {
        return methodContainerTransactions;
    }


    /**
     * Sets the container transactions for all the method descriptors of this ejb.
     * The Hashtable is keyed by method descriptor and the values are the corresponding container
     * transaction objects..
     *
     * @throws IllegalArgumentException if this ejb has transaction type BEAN_TRANSACTION_TYPE.
     */
    public void setMethodContainerTransactions(
        Hashtable<MethodDescriptor, ContainerTransaction> methodContainerTransactions) throws IllegalArgumentException {
        if (methodContainerTransactions != null) {
            for (Entry<MethodDescriptor, ContainerTransaction> entry : methodContainerTransactions.entrySet()) {
                setContainerTransactionFor(entry.getKey(), entry.getValue());
            }
        }
    }


    /**
     * Sets the container transaction for the given method descriptor.
     *
     * @throws IllegalArgumentException if this ejb has transaction type BEAN_TRANSACTION_TYPE.
     */
    public void setContainerTransactionFor(MethodDescriptor methodDescriptor, ContainerTransaction containerTx)
        throws IllegalArgumentException {
        ContainerTransaction oldValue = getContainerTransactionFor(methodDescriptor);
        if (oldValue != null && oldValue.equals(containerTx)) {
            return;
        }
        String txType = getTransactionType();
        if (txType == null) {
            setTransactionType(CONTAINER_TRANSACTION_TYPE);
        } else if (BEAN_TRANSACTION_TYPE.equals(txType)) {
            throw new IllegalArgumentException(
                I18N.getLocalString("enterprise.deployment.exceptiontxattrbtnotspecifiedinbeanwithtxtype",
                    "Method level transaction attributes may not be specified on a bean with transaction type {0}",
                    new Object[] {com.sun.enterprise.deployment.EjbDescriptor.BEAN_TRANSACTION_TYPE}));
        }
        methodContainerTransactions.put(methodDescriptor, containerTx);
    }


    /**
     * Fetches the assigned container transaction object for the given method object.
     *
     * @return {@link ContainerTransaction} or null.
     */
    public ContainerTransaction getContainerTransactionFor(MethodDescriptor methodDescriptor) {
        if (needToConvertMethodContainerTransactions()) {
            this.methodContainerTransactions = convertMethodContainerTransactions();
        }
        ContainerTransaction containerTransaction = getMethodContainerTransactions().get(methodDescriptor);
        if (containerTransaction != null) {
            return containerTransaction;
        }
        if (Descriptor.isBoundsChecking() && usesDefaultTransaction) {
            containerTransaction = new ContainerTransaction(ContainerTransaction.REQUIRED, "");
            getMethodContainerTransactions().put(methodDescriptor, containerTransaction);
        }
        return containerTransaction;
    }

    private boolean needToConvertMethodContainerTransactions() {
        if (getEjbBundleDescriptor() != null) {
            for (MethodDescriptor method : getMethodContainerTransactions().keySet()) {
                if (!method.isExact()) {
                    return true;
                }
            }
        }
        return false;
    }


    private Hashtable<MethodDescriptor, ContainerTransaction> convertMethodContainerTransactions() {
        Hashtable<MethodDescriptor, ContainerTransaction> convertedTransactions = new Hashtable<>();
        convertMethodContainerTransactionsOfStyle(1, convertedTransactions);
        convertMethodContainerTransactionsOfStyle(2, convertedTransactions);
        convertMethodContainerTransactionsOfStyle(3, convertedTransactions);
        return convertedTransactions;
    }


    private void convertMethodContainerTransactionsOfStyle(int requestedStyleForConversion,
        Hashtable<MethodDescriptor, ContainerTransaction> convertedMethods) {
        Collection<MethodDescriptor> transactionMethods = getTransactionMethodDescriptors();
        for (Entry<MethodDescriptor, ContainerTransaction> entry : methodContainerTransactions.entrySet()) {
            MethodDescriptor method = entry.getKey();
            if (method.getStyle() == requestedStyleForConversion) {
                for (MethodDescriptor next : method.doStyleConversion(this, transactionMethods)) {
                    convertedMethods.put(next, new ContainerTransaction(entry.getValue()));
                }
            }
        }
    }


    private void addOrMergeEnvironmentProperty(EnvironmentProperty environmentProperty) {
        try {
            EnvironmentProperty existing = getEnvironmentPropertyByName(environmentProperty.getName());
            for (InjectionTarget next : environmentProperty.getInjectionTargets()) {
                existing.addInjectionTarget(next);
            }
        } catch (IllegalArgumentException e) {
            addEnvironmentProperty(environmentProperty);
        }
    }


    /**
     * @return the collection of MethodDescriptors to which ContainerTransactions may be assigned.
     */
    public Collection<MethodDescriptor> getTransactionMethodDescriptors() {
        return getTransactionMethods(getEjbBundleDescriptor().getClassLoader());
    }


    /**
     * @return a collection of MethodDescriptor for methods which may have a associated transaction attribute
     */
    protected Collection<MethodDescriptor> getTransactionMethods(ClassLoader classLoader) {
        try {
            BeanMethodCalculatorImpl bmc = new BeanMethodCalculatorImpl();
            return bmc.getTransactionalMethodsFor(this, classLoader);
        } catch (Exception e) {
            throw new IllegalStateException(getEjbClassName(), e);
        }
    }


    /**
     * @return the ejb method objects, i.e. the methods on the home and remote interfaces.
     */
    public List<Method> getMethods() {
        return getMethods(getEjbBundleDescriptor().getClassLoader());
    }


    /**
     * @return the ejb method objects, i.e. the methods on the home and remote interfaces.
     */
    private List<Method> getMethods(ClassLoader classLoader) {
        try {
            BeanMethodCalculatorImpl bmc = new BeanMethodCalculatorImpl();
            return bmc.getMethodsFor(this, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(getEjbClassName(), e);
        }
    }


    @Override
    public void print(StringBuffer toStringBuffer) {
        super.print(toStringBuffer);
        toStringBuffer.append("\n methodContainerTransactions ").append(getMethodContainerTransactions());
        for (Object element : this.getPermissionedMethodsByPermission().keySet()) {
            MethodPermission nextPermission = (MethodPermission) element;
            toStringBuffer.append("\n method-permission->method: ");
            nextPermission.print(toStringBuffer);
            toStringBuffer.append(" -> ").append(this.getPermissionedMethodsByPermission().get(nextPermission));
        }
    }


    @Override
    public final void visit(DescriptorVisitor aVisitor) {
        LOG.log(Level.CONFIG, "visit(aVisitor={0})", aVisitor);
        if (aVisitor instanceof EjbVisitor) {
            // warn: don't inline, EjbVisitor.accept overlaps with ComponentVisitor.accept!
            EjbVisitor visitor = (EjbVisitor) aVisitor;
            visitor.accept(this);
        } else {
            super.visit(aVisitor);
        }
    }
}
