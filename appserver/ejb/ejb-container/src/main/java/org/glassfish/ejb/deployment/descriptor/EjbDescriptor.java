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

import jakarta.inject.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.LogRecord;
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

    private EjbBundleDescriptorImpl bundleDescriptor;
    private boolean usesDefaultTransaction;

    private String securityIdentityDescription;

    private Hashtable<MethodDescriptor, ContainerTransaction> methodContainerTransactions;
    private final Set<String> noInterfaceLocalBeanClassNames = new HashSet<>();

    private final Set<LifecycleCallbackDescriptor> aroundInvokeDescs = new HashSet<>();
    private final Set<LifecycleCallbackDescriptor> aroundTimeoutDescs = new HashSet<>();

    // Late-binding system-level interceptors for this EJB. These can be set
    // as late as initialization time, so they are not part of the interceptor
    // binding translation that happens for application-defined interceptors.
    private final List<InterceptorDescriptor> frameworkInterceptors = new LinkedList<>();

    private String remoteHomeImplClassName;
    private String ejbObjectImplClassName;
    private String localHomeImplClassName;
    private String ejbLocalObjectImplClassName;

    // Ludo 12/10/2001 extra DTD info only for iAS
    private final IASEjbExtraDescriptors iASEjbExtraDescriptors = new IASEjbExtraDescriptors();


    /**
     * Default constructor.
     */
    protected EjbDescriptor() {
    }

    public EjbDescriptor(EjbDescriptor other) {
        super(other);
        copyEjbDescriptor(other);
    }



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
        this.methodContainerTransactions = new Hashtable<>(other.getMethodContainerTransactions());
    }

    /**
     * Add a classname for a no-interface view of the local ejb
     *
     * @param className fully qualified class name for the interface
     */
    public void addNoInterfaceLocalBeanClass(String className) {
        this.noInterfaceLocalBeanClassNames.add(className);
    }


    /**
     * @return all the public classes of this no-interface local ejb
     */
    public Set<String> getNoInterfaceLocalBeanClasses() {
        return this.noInterfaceLocalBeanClassNames;
    }


    /**
     * Sets the remote home implementation classname of the ejb.
     */
    public void setRemoteHomeImplClassName(String name) {
        this.remoteHomeImplClassName = name;
    }

    /**
     * @return the classname of the remote home impl.
     */
    public String getRemoteHomeImplClassName() {
        return this.remoteHomeImplClassName;
    }

    /**
     * Sets the Local home implementation classname of the ejb.
     */
    public void setLocalHomeImplClassName(String name) {
        this.localHomeImplClassName = name;
    }

    /**
     * @return the classname of the Local home impl.
     */
    public String getLocalHomeImplClassName() {
        return this.localHomeImplClassName;
    }

    /**
     * Sets the EJBLocalObject implementation classname of the ejb.
     */
    public void setEJBLocalObjectImplClassName(String name) {
        this.ejbLocalObjectImplClassName = name;
    }

    /**
     * @return the classname of the EJBLocalObject impl.
     */
    public String getEJBLocalObjectImplClassName() {
        return this.ejbLocalObjectImplClassName;
    }

    /**
     * Sets the EJBObject implementation classname of the ejb.
     */
    public void setEJBObjectImplClassName(String name) {
        this.ejbObjectImplClassName = name;
    }

    /**
     * @return the classname of the EJBObject impl.
     */
    public String getEJBObjectImplClassName() {
        return this.ejbObjectImplClassName;
    }


    /**
     * @return the set of transaction attributes that can be assigned to methods of this ejb when in
     *         CMT mode. Elements are of type ContainerTransaction
     */
    public Vector<ContainerTransaction> getPossibleTransactionAttributes() {
        Vector<ContainerTransaction> txAttributes = new Vector<>();
        txAttributes.add(new ContainerTransaction(ContainerTransaction.MANDATORY, ""));
        txAttributes.add(new ContainerTransaction(ContainerTransaction.NEVER, ""));
        txAttributes.add(new ContainerTransaction(ContainerTransaction.NOT_SUPPORTED, ""));
        txAttributes.add(new ContainerTransaction(ContainerTransaction.REQUIRED, ""));
        txAttributes.add(new ContainerTransaction(ContainerTransaction.REQUIRES_NEW, ""));
        txAttributes.add(new ContainerTransaction(ContainerTransaction.SUPPORTS, ""));
        return txAttributes;
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

    public Set<LifecycleCallbackDescriptor> getAroundTimeoutDescriptors() {
        return aroundTimeoutDescs;
    }

    public void addAroundTimeoutDescriptor(LifecycleCallbackDescriptor aroundTimeoutDesc) {
        String className = aroundTimeoutDesc.getLifecycleCallbackClass();
        boolean found = false;
        for (LifecycleCallbackDescriptor next : getAroundTimeoutDescriptors()) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                found = true;
                break;
            }
        }
        if (!found) {
            getAroundTimeoutDescriptors().add(aroundTimeoutDesc);
        }
    }

    public LifecycleCallbackDescriptor getAroundTimeoutDescriptorByClass(String className) {

        for (LifecycleCallbackDescriptor next : getAroundTimeoutDescriptors()) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                return next;
            }
        }
        return null;
    }

    public boolean hasAroundTimeoutMethod() {
        return !getAroundTimeoutDescriptors().isEmpty();
    }

    @Override
    public void addFrameworkInterceptor(InterceptorDescriptor interceptor) {
        boolean found = false;
        for (InterceptorDescriptor next : frameworkInterceptors) {
            if (next.getInterceptorClassName().equals(interceptor.getInterceptorClassName())) {
                found = true;
                break;
            }
        }

        if (!found) {
            frameworkInterceptors.add(interceptor);
        }

    }

    public List<InterceptorDescriptor> getFrameworkInterceptors() {
        return frameworkInterceptors;
    }

    /**
     * Since ejb-class is optional, in some cases the lifecycle-class for AroundInvoke, PostConstruct, etc. methods on the
     * bean-class is not known at processing time and must be applied lazily. As such, this method should only be called if
     * the ejb-class has been set on this EjbDescriptor.
     */
    public void applyDefaultClassToLifecycleMethods() {
        Set<LifecycleCallbackDescriptor> lifecycleMethods = getLifecycleCallbackDescriptors();
        lifecycleMethods.addAll(getAroundInvokeDescriptors());
        lifecycleMethods.addAll(getAroundTimeoutDescriptors());
        for (LifecycleCallbackDescriptor next : lifecycleMethods) {
            if (next.getLifecycleCallbackClass() == null) {
                next.setLifecycleCallbackClass(getEjbClassName());
            }
        }
    }

    public Set<LifecycleCallbackDescriptor> getLifecycleCallbackDescriptors() {
        Set<LifecycleCallbackDescriptor> lifecycleMethods = new HashSet<>();
        lifecycleMethods.addAll(getPostConstructDescriptors());
        lifecycleMethods.addAll(getPreDestroyDescriptors());
        if (getType().equals(com.sun.enterprise.deployment.EjbSessionDescriptor.TYPE)) {
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
     * @return the ordered list of interceptor info for AroundInvoke behavior of a particular
     *         business method.
     *         This list *does* include the info on any bean class interceptor.
     *         If present, this would always be the last element in the list because of the
     *         precedence defined by the spec.
     */
    public List<EjbInterceptor> getAroundInvokeInterceptors(MethodDescriptor businessMethod) {

        LinkedList<EjbInterceptor> aroundInvokeInterceptors = new LinkedList<>();

        List<EjbInterceptor> classOrMethodInterceptors = getClassOrMethodInterceptors(businessMethod);

        for (EjbInterceptor next : classOrMethodInterceptors) {
            if (next.getAroundInvokeDescriptors().size() > 0) {
                aroundInvokeInterceptors.add(next);
            }
        }

        if (hasAroundInvokeMethod()) {

            EjbInterceptor interceptorInfo = new EjbInterceptor();
            interceptorInfo.setFromBeanClass(true);
            interceptorInfo.addAroundInvokeDescriptors(getAroundInvokeDescriptors());
            interceptorInfo.setInterceptorClassName(getEjbImplClassName());

            aroundInvokeInterceptors.add(interceptorInfo);
        }

        return aroundInvokeInterceptors;
    }


    /**
     * @return the ordered list of interceptor info for AroundTimeout behavior of a particular
     *         business method.
     *         This list *does* include the info on any bean class interceptor.
     *         If present, this would always be the last element in the list because of the
     *         precedence defined by the spec.
     */
    public List<EjbInterceptor> getAroundTimeoutInterceptors(MethodDescriptor businessMethod) {
        LinkedList<EjbInterceptor> aroundTimeoutInterceptors = new LinkedList<>();

        List<EjbInterceptor> classOrMethodInterceptors = getClassOrMethodInterceptors(businessMethod);

        for (EjbInterceptor next : classOrMethodInterceptors) {
            if (next.getAroundTimeoutDescriptors().size() > 0) {
                aroundTimeoutInterceptors.add(next);
            }
        }

        if (hasAroundTimeoutMethod()) {

            EjbInterceptor interceptorInfo = new EjbInterceptor();
            interceptorInfo.setFromBeanClass(true);
            interceptorInfo.addAroundTimeoutDescriptors(getAroundTimeoutDescriptors());
            interceptorInfo.setInterceptorClassName(getEjbImplClassName());

            aroundTimeoutInterceptors.add(interceptorInfo);
        }

        return aroundTimeoutInterceptors;
    }


    /**
     * Return the ordered list of interceptor info for a particular callback event type. This list
     * *does* include the info
     * on any bean class callback. If present, this would always be the last element in the list
     * because of the precedence
     * defined by the spec.
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


    protected final LinkedList<EjbInterceptor> getCallbackInterceptors(CallbackType type,
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
     * Set the description field of security-identity
     */
    public void setSecurityIdentityDescription(String s) {
        securityIdentityDescription = s;
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
     * Have default method transaction if isBoundsChecking is on.
     */
    public void setUsesDefaultTransaction() {
        usesDefaultTransaction = true;
    }

    /**
     * @return a state to indicate whether default method transaction is used if isBoundsChecking is on.
     */
    public boolean isUsesDefaultTransaction() {
        return usesDefaultTransaction;
    }

    /**
     * @return a copy of the mapping held internally of method descriptors to container transaction objects.
     */
    public Hashtable<MethodDescriptor, ContainerTransaction> getMethodContainerTransactions() {
        if (this.methodContainerTransactions == null) {
            this.methodContainerTransactions = new Hashtable<>();
        }
        return methodContainerTransactions;
    }

    /**
     * Sets the container transaction for the given method descriptor. Throws an Illegal argument if this ejb has
     * transaction type BEAN_TRANSACTION_TYPE.
     */
    public void setContainerTransactionFor(MethodDescriptor methodDescriptor, ContainerTransaction containerTransaction) {
        ContainerTransaction oldValue = this.getContainerTransactionFor(methodDescriptor);
        if (oldValue == null || !oldValue.equals(containerTransaction)) {
            String txType = this.getTransactionType();
            if (txType == null) {
                setTransactionType(CONTAINER_TRANSACTION_TYPE);
            } else if (BEAN_TRANSACTION_TYPE.equals(txType)) {
                throw new IllegalArgumentException(
                        I18N.getLocalString("enterprise.deployment.exceptiontxattrbtnotspecifiedinbeanwithtxtype",
                                "Method level transaction attributes may not be specified on a bean with transaction type {0}",
                                new Object[] { com.sun.enterprise.deployment.EjbDescriptor.BEAN_TRANSACTION_TYPE }));
            }
            // LOG.log(Level.FINE,"put " + methodDescriptor + " " + containerTransaction);
            getMethodContainerTransactions().put(methodDescriptor, containerTransaction);
        }
    }

    /**
     * Sets the container transactions for all the method descriptors of this ejb. The Hashtable is keyed by method
     * descriptor and the values are the corresponding container transaction objects.. Throws an Illegal argument if this
     * ejb has transaction type BEAN_TRANSACTION_TYPE.
     */
    public void setMethodContainerTransactions(Hashtable<MethodDescriptor, ContainerTransaction> methodContainerTransactions) {
        if (methodContainerTransactions != null) {
            for (Enumeration<MethodDescriptor> e = methodContainerTransactions.keys(); e.hasMoreElements();) {
                MethodDescriptor methodDescriptor = e.nextElement();
                ContainerTransaction containerTransaction = methodContainerTransactions.get(methodDescriptor);
                setContainerTransactionFor(methodDescriptor, containerTransaction);
            }
        }
    }

    Set<MethodDescriptor> getAllMethodDescriptors() {
        Set<MethodDescriptor> allMethodDescriptors = new HashSet<>();
        for (Enumeration<MethodDescriptor> e = getMethodContainerTransactions().keys(); e.hasMoreElements();) {
            allMethodDescriptors.add(e.nextElement());
        }
        for (MethodPermission nextPermission : this.getPermissionedMethodsByPermission().keySet()) {
            Set<MethodDescriptor> permissionedMethods = this.getPermissionedMethodsByPermission().get(nextPermission);
            for (MethodDescriptor permissionedMethod : permissionedMethods) {
                allMethodDescriptors.add(permissionedMethod);
            }
        }
        return allMethodDescriptors;
    }

    /**
     * Fetches the assigned container transaction object for the given method object or null.
     */
    public ContainerTransaction getContainerTransactionFor(MethodDescriptor methodDescriptor) {
        ContainerTransaction containerTransaction = null;
        if (this.needToConvertMethodContainerTransactions()) {
            this.convertMethodContainerTransactions();
        }
        containerTransaction = this.getMethodContainerTransactions().get(methodDescriptor);
        if (containerTransaction != null) {
            return containerTransaction;
        }
        if (Descriptor.isBoundsChecking() && usesDefaultTransaction) {
            containerTransaction = new ContainerTransaction(ContainerTransaction.REQUIRED, "");
            this.getMethodContainerTransactions().put(methodDescriptor, containerTransaction);
        }
        return containerTransaction;
    }

    private boolean needToConvertMethodContainerTransactions() {
        if (this.getEjbBundleDescriptor() != null) {
            for (Enumeration<MethodDescriptor> e = this.getMethodContainerTransactions().keys(); e.hasMoreElements();) {
                MethodDescriptor md = e.nextElement();
                if (!md.isExact()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void convertMethodContainerTransactions() {
        // container transactions first
        // Hashtable transactions = this.getMethodContainerTransactions();
        // LOG.log(Level.FINE,"Pre conversion = " + transactions);
        Hashtable<MethodDescriptor, ContainerTransaction> convertedTransactions = new Hashtable<>();
        convertMethodContainerTransactionsOfStyle(1, convertedTransactions);
        convertMethodContainerTransactionsOfStyle(2, convertedTransactions);
        convertMethodContainerTransactionsOfStyle(3, convertedTransactions);
        // LOG.log(Level.FINE,"Post conversion = " + convertedTransactions);
        this.methodContainerTransactions = convertedTransactions;
    }


    private void convertMethodContainerTransactionsOfStyle(int requestedStyleForConversion,
        Hashtable<MethodDescriptor, ContainerTransaction> convertedMethods) {
        Collection<MethodDescriptor> transactionMethods = this.getTransactionMethodDescriptors();
        Hashtable<MethodDescriptor, ContainerTransaction> transactions = this.getMethodContainerTransactions();
        for (Enumeration<MethodDescriptor> e = transactions.keys(); e.hasMoreElements();) {
            MethodDescriptor md = e.nextElement();
            if (md.getStyle() == requestedStyleForConversion) {
                ContainerTransaction ct = getMethodContainerTransactions().get(md);
                for (MethodDescriptor next : md.doStyleConversion(this, transactionMethods)) {
                    convertedMethods.put(next, new ContainerTransaction(ct));
                }
            }
        }
    }

    /**
     * returns a ContainerTransaction if all the transactional methods on the ejb descriptor have the same transaction type
     * else return null
     */
    public ContainerTransaction getContainerTransaction() {
        Vector<MethodDescriptor> transactionalMethods = new Vector<>(this.getTransactionMethodDescriptors());
        MethodDescriptor md = transactionalMethods.firstElement();
        if (md != null) {
            ContainerTransaction first = this.getContainerTransactionFor(md);
            for (MethodDescriptor next : transactionalMethods) {
                ContainerTransaction nextCt = this.getContainerTransactionFor(next);
                if (nextCt != null && !nextCt.equals(first)) {
                    return null;
                }
            }
            return first;
        }
        return null;
    }

    /**
     * @return true if this ejb descriptor has resource references that are resolved.
     */
    public boolean hasResolvedResourceReferences() {
        if (!this.getResourceReferenceDescriptors().isEmpty()) {
            return false;
        }
        for (ResourceReferenceDescriptor resourceReference : getResourceReferenceDescriptors()) {
            if (resourceReference.isResolved()) {
                return true;
            }
        }
        return false;
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
        } catch (Throwable t) {
            throw new RuntimeException(getEjbClassName(), t);
        }
    }

    /**
     * @return the set of method objects representing no-interface view
     */
    public Set<Method> getOptionalLocalBusinessMethods() {
        Set<Method> methods = new HashSet<>();
        try {
            Class<?> c = getEjbBundleDescriptor().getClassLoader().loadClass(getEjbClassName());
            Method[] ms = c.getMethods();
            for (Method m : ms) {
                if (m.getDeclaringClass() != Object.class) {
                    methods.add(m);
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(getEjbClassName(), t);
        }

        return methods;
    }

    public abstract String getContainerFactoryQualifier();

    /**
     * @return the set of method objects on my home and remote interfaces.
     */
    public Vector<Method> getMethods() {
        return getMethods(getEjbBundleDescriptor().getClassLoader());
    }

    /**
     * @return the ejb method objects, i.e. the methods on the home and remote interfaces.
     */
    public Vector<Method> getMethods(ClassLoader classLoader) {
        try {
            BeanMethodCalculatorImpl bmc = new BeanMethodCalculatorImpl();
            return bmc.getMethodsFor(this, classLoader);
        } catch (Throwable t) {
            throw new RuntimeException(getEjbClassName(), t);
        }
    }

    /**
     * @return a Vector of the Field objetcs of this ejb.
     */
    public Vector<Field> getFields() {
        Vector<Field> fieldsVector = new Vector<>();
        Class<?> ejb = null;
        try {
            ClassLoader cl = getEjbBundleDescriptor().getClassLoader();
            ejb = cl.loadClass(this.getEjbClassName());
        } catch (Throwable t) {
            LogRecord log = new LogRecord(Level.SEVERE, "enterprise.deployment.backend.methodClassLoadFailure");
            log.setParameters(new Object[] { this.getEjbClassName() });
            LOG.log(log);
            return fieldsVector;
        }
        Field[] fields = ejb.getFields();
        for (Field field : fields) {
            fieldsVector.addElement(field);
        }
        return fieldsVector;

    }

    public Vector<FieldDescriptor> getFieldDescriptors() {
        Vector<Field> fields = this.getFields();
        Vector<FieldDescriptor> fieldDescriptors = new Vector<>();
        for (int fieldIndex = 0; fieldIndex < fields.size(); fieldIndex++) {
            Field field = fields.elementAt(fieldIndex);
            fieldDescriptors.insertElementAt(new FieldDescriptor(field), fieldIndex);
        }
        return fieldDescriptors;
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
        LOG.log(Level.CONFIG, "public visit(aVisitor={0})", aVisitor);
        if (aVisitor instanceof EjbVisitor) {
            // warn: don't inline, EjbVisitor.accept overlaps with ComponentVisitor.accept!
            EjbVisitor visitor = (EjbVisitor) aVisitor;
            visitor.accept(this);
        } else {
            super.visit(aVisitor);
        }
    }
}
