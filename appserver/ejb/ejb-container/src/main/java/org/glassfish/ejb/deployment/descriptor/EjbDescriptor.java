/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.CommonResourceDescriptor;
import com.sun.enterprise.deployment.EjbIORConfigurationDescriptor;
import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerFactoryReferenceDescriptor;
import com.sun.enterprise.deployment.EntityManagerReferenceDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.InjectionCapable;
import com.sun.enterprise.deployment.InjectionInfo;
import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.InterceptorDescriptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType;
import com.sun.enterprise.deployment.MessageDestinationReferenceDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.MethodPermission;
import com.sun.enterprise.deployment.OrderedSet;
import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.RoleReference;
import com.sun.enterprise.deployment.RunAsIdentityDescriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WritableJndiNameEnvironment;
import com.sun.enterprise.deployment.core.ResourceDescriptor;
import com.sun.enterprise.deployment.types.EjbReference;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.DescriptorVisitor;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.glassfish.ejb.deployment.BeanMethodCalculatorImpl;
import org.glassfish.ejb.deployment.descriptor.runtime.IASEjbExtraDescriptors;
import org.glassfish.ejb.deployment.util.EjbVisitor;
import org.glassfish.ejb.deployment.util.InterceptorBindingTranslator;
import org.glassfish.ejb.deployment.util.InterceptorBindingTranslator.TranslationResults;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.security.common.Role;

import static com.sun.enterprise.deployment.MethodDescriptor.EJB_BEAN;

/**
 * This abstract class encapsulates the meta-information describing Entity, Session and MessageDriven EJBs.
 *
 * @author Danny Coward
 * @author Sanjeev Krishnan
 */
public abstract class EjbDescriptor extends CommonResourceDescriptor implements com.sun.enterprise.deployment.EjbDescriptor {

    // Used in <transaction-scope> element in XML
    public static final String LOCAL_TRANSACTION_SCOPE = "Local";
    public static final String DISTRIBUTED_TRANSACTION_SCOPE = "Distributed";

    private static final long serialVersionUID = 1L;
    private String homeClassName;
    private String remoteClassName;
    private String localHomeClassName;
    private String localClassName;

    private Set<String> remoteBusinessClassNames = new HashSet<>();
    private Set<String> localBusinessClassNames = new HashSet<>();
    private final Set<String> noInterfaceLocalBeanClassNames = new HashSet<>();

    // This is the value of the EJB 2.1 deployment descriptor entry
    // for service endpoint interface.
    private String webServiceEndpointInterfaceName;

    private String jndiName = "";
    private String mappedName = "";

    // Is set to true if this bean exposes a no-interface view
    private boolean localBean;

    protected String transactionType;
    private boolean usesDefaultTransaction;
    private Hashtable<MethodDescriptor, ContainerTransaction> methodContainerTransactions;
    private Map<MethodPermission, Set<MethodDescriptor>> permissionedMethodsByPermission;
    private HashMap<MethodPermission, ArrayList<MethodDescriptor>> methodPermissionsFromDD;
    private final Set<EnvironmentProperty> environmentProperties = new HashSet<>();
    private final Set<EjbReferenceDescriptor> ejbReferences = new HashSet<>();
    private final Set<ResourceEnvReferenceDescriptor> resourceEnvReferences = new HashSet<>();
    private final Set<MessageDestinationReferenceDescriptor> messageDestReferences = new HashSet<>();
    private final Set<ResourceReferenceDescriptor> resourceReferences = new HashSet<>();
    private final Set<ServiceReferenceDescriptor> serviceReferences = new HashSet<>();

    private final Set<LifecycleCallbackDescriptor> postConstructDescs = new HashSet<>();
    private final Set<LifecycleCallbackDescriptor> preDestroyDescs = new HashSet<>();

    // if non-null, refer all environment refs here
    private WritableJndiNameEnvironment env;

    private final Set<LifecycleCallbackDescriptor> aroundInvokeDescs = new HashSet<>();
    private final Set<LifecycleCallbackDescriptor> aroundTimeoutDescs = new HashSet<>();

    // Late-binding system-level interceptors for this EJB. These can be set
    // as late as initialization time, so they are not part of the interceptor
    // binding translation that happens for application-defined interceptors.
    private final List<InterceptorDescriptor> frameworkInterceptors = new LinkedList<>();

    private final Set<EntityManagerFactoryReferenceDescriptor> entityManagerFactoryReferences = new HashSet<>();

    private final Set<EntityManagerReferenceDescriptor> entityManagerReferences = new HashSet<>();

    private Set<RoleReference> roleReferences = new HashSet<>();
    private EjbBundleDescriptorImpl bundleDescriptor;
    // private EjbIORConfigurationDescriptor iorConfigDescriptor = new EjbIORConfigurationDescriptor();
    private final Set<EjbIORConfigurationDescriptor> iorConfigDescriptors = new OrderedSet<>();
    // private Set methodDescriptors = new HashSet();
    private String ejbClassName;

    // EjbRefs from all components in this app who point to me
    private final Set<EjbReferenceDescriptor> ejbReferencersPointingToMe = new HashSet<>();

    // For EJB2.0
    private Boolean usesCallerIdentity;
    private String securityIdentityDescription;
    private boolean isDistributedTxScope = true;
    private RunAsIdentityDescriptor runAsIdentity;

    // sets of method descriptor that can be of style 1 or style 2
    // we initialize it so we force at least on method conversion
    // to fill up unspecified method with the unchecked permission
    private Map<MethodDescriptor, Set<MethodPermission>> styledMethodDescriptors = new HashMap<>();

    private long uniqueId;
    private String remoteHomeImplClassName;
    private String ejbObjectImplClassName;
    private String localHomeImplClassName;
    private String ejbLocalObjectImplClassName;

    private MethodDescriptor timedObjectMethod;

    private List<ScheduledTimerDescriptor> timerSchedules = new ArrayList<>();

    private List<MethodDescriptor> timerMethodDescriptors = new ArrayList<>();

    //
    // The set of all interceptor classes applicable to this bean. This
    // includes any interceptor class that is present at *either* the class
    // level or method-level.
    //
    private final Set<EjbInterceptor> allInterceptorClasses = new HashSet<>();

    // Ordered list of class-level interceptors for this bean.
    private final List<EjbInterceptor> interceptorChain = new LinkedList<>();

    //
    // Interceptor info per business method. If the map does not
    // contain an entry for the business method, there is no method-specific
    // interceptor information for that method. In that case the standard
    // class-level interceptor information applies.
    //
    // If there is an entry for the business method, the corresponding list
    // represents the *complete* ordered list of interceptor classes for that
    // method. An empty list would mean all the interceptors have been
    // disabled for that particular business method.
    //
    private final Map<MethodDescriptor, List<EjbInterceptor>> methodInterceptorsMap = new HashMap<>();

    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(EjbDescriptor.class);

    static Logger _logger = DOLUtils.getDefaultLogger();

    // Ludo 12/10/2001 extra DTD info only for iAS
    private final IASEjbExtraDescriptors iASEjbExtraDescriptors = new IASEjbExtraDescriptors();

    private final ServiceLocator sl = Globals.getDefaultHabitat();

    /**
     * Default constructor.
     */
    protected EjbDescriptor() {
    }

    public EjbDescriptor(EjbDescriptor other) {
        super(other);
        this.homeClassName = other.homeClassName;
        this.remoteClassName = other.remoteClassName;
        this.remoteBusinessClassNames = new HashSet<>(other.remoteBusinessClassNames);
        this.localHomeClassName = other.localHomeClassName;
        this.localClassName = other.localClassName;
        this.localBusinessClassNames = new HashSet<>(other.localBusinessClassNames);
        this.webServiceEndpointInterfaceName = other.webServiceEndpointInterfaceName;
        this.localBean = other.localBean;
        this.jndiName = other.jndiName;
        addEjbDescriptor(other);
    }

    @Override
    public abstract String getEjbTypeForDisplay();


    /**
     * @return the extra iAS specific info (not in the RI DID) in the iAS DTD. no setter.
     *         You have to modify some fields of the returned object to change it.
     */
    public IASEjbExtraDescriptors getIASEjbExtraDescriptors() {
        return iASEjbExtraDescriptors;
    }


    public void addEjbDescriptor(EjbDescriptor other) {
        setEjbBundleDescriptor(other.bundleDescriptor);
        this.transactionType = other.transactionType;
        this.methodContainerTransactions = new Hashtable<>(other.getMethodContainerTransactions());
        this.permissionedMethodsByPermission = new Hashtable<>(other.getPermissionedMethodsByPermission());
        if (other.env == null) {
            // only add this information if it's contained in
            // the other EjbDescriptor
            this.getEnvironmentProperties().addAll(other.getEnvironmentProperties());
            this.getEjbReferenceDescriptors().addAll(other.getEjbReferenceDescriptors());
            this.getResourceEnvReferenceDescriptors().addAll(other.getResourceEnvReferenceDescriptors());
            this.getMessageDestinationReferenceDescriptors().addAll(other.getMessageDestinationReferenceDescriptors());
            this.getResourceReferenceDescriptors().addAll(other.getResourceReferenceDescriptors());
            this.getServiceReferenceDescriptors().addAll(other.getServiceReferenceDescriptors());
            // XXX - why not addAll?
            Set<ResourceDescriptor> allDescriptors = other.getAllResourcesDescriptors();
            if (allDescriptors.size() > 0) {
                for (ResourceDescriptor desc : allDescriptors) {
                    this.addResourceDescriptor(desc);
                }
            }

            this.getEntityManagerFactoryReferenceDescriptors().addAll(other.getEntityManagerFactoryReferenceDescriptors());
            this.getEntityManagerReferenceDescriptors().addAll(other.getEntityManagerReferenceDescriptors());
        }
        this.getRoleReferences().addAll(other.getRoleReferences());
        this.getIORConfigurationDescriptors().addAll(other.getIORConfigurationDescriptors());
        this.transactionType = other.transactionType;
        this.ejbClassName = other.ejbClassName;
        this.usesCallerIdentity = other.usesCallerIdentity;
        this.timerSchedules = new ArrayList<>(other.timerSchedules);
        this.timerMethodDescriptors = new ArrayList<>(other.timerMethodDescriptors);
    }

    public abstract void setType(String type);

    /**
     * Returns the classname of the Home interface of this ejb.
     */
    @Override
    public String getHomeClassName() {
        return this.homeClassName;
    }

    /**
     * Sets the classname of the Home interface of this ejb.
     */
    public void setHomeClassName(String homeClassName) {
        this.homeClassName = homeClassName;
    }

    /**
     * Sets the classname of the Remote interface of this ejb.
     */
    public void setRemoteClassName(String remoteClassName) {
        this.remoteClassName = remoteClassName;
    }

    /**
     * Returns the classname of the Remote interface of this ejb.
     */
    @Override
    public String getRemoteClassName() {
        return this.remoteClassName;
    }

    /**
     * Sets the classname for the local home interface of this ejb
     *
     * @param localHomeClassName fully qualified class name for the interface
     */
    public void setLocalHomeClassName(String localHomeClassName) {
        this.localHomeClassName = localHomeClassName;
    }

    /**
     * @return the fully qualified class name for the local home interface of this ejb
     */
    @Override
    public String getLocalHomeClassName() {
        return localHomeClassName;
    }

    /**
     * Sets the classname for the local interface of this ejb
     *
     * @param localClassName fully qualified class name for the interface
     */
    public void setLocalClassName(String localClassName) {
        this.localClassName = localClassName;
    }

    /**
     * @return the fully qualified class name for the local interface of this ejb
     */
    @Override
    public String getLocalClassName() {
        return localClassName;
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

    public void addRemoteBusinessClassName(String className) {
        remoteBusinessClassNames.add(className);
    }

    public void addLocalBusinessClassName(String className) {
        localBusinessClassNames.add(className);
    }

    /**
     * Returns the set of remote business interface names for this ejb. If the bean does not expose a remote business view,
     * return a set of size 0.
     */
    @Override
    public Set<String> getRemoteBusinessClassNames() {
        return new HashSet<>(remoteBusinessClassNames);
    }

    /**
     * Returns the set of local business interface names for this ejb. If the bean does not expose a local business view,
     * return a set of size 0.
     */
    @Override
    public Set<String> getLocalBusinessClassNames() {
        return new HashSet<>(localBusinessClassNames);
    }

    @Override
    public void setWebServiceEndpointInterfaceName(String name) {
        this.webServiceEndpointInterfaceName = name;
    }

    @Override
    public String getWebServiceEndpointInterfaceName() {
        return webServiceEndpointInterfaceName;
    }

    @Override
    public String getJndiName() {
        if (this.jndiName == null) {
            this.jndiName = "";
        }
        return jndiName.isEmpty() ? getMappedName() : jndiName;
    }

    @Override
    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
        if (this.getName().isEmpty()) {
            super.setName(jndiName);
        }
    }

    public String getMappedName() {
        return mappedName == null ? "" : mappedName;
    }

    public void setMappedName(String mappedName) {
        this.mappedName = mappedName;
    }

    /**
     * Marks this ejb as a LocalBean.
     */
    public void setLocalBean(boolean localBean) {
        this.localBean = localBean;
    }

    /**
     * @return true if the EJB described has a LocalHome/Local interface
     */
    @Override
    public boolean isLocalInterfacesSupported() {
        return getLocalHomeClassName() != null;
    }

    /**
     * @return true if the EJB has 1 or more local business interfaces
     */
    @Override
    public boolean isLocalBusinessInterfacesSupported() {
        return !localBusinessClassNames.isEmpty();
    }

    /**
     * @return true if the EJB has a RemoteHome/Remote interface
     */
    @Override
    public boolean isRemoteInterfacesSupported() {
        return getHomeClassName() != null;
    }

    /**
     * @return true if the EJB has 1 or more remote business interfaces
     */
    @Override
    public boolean isRemoteBusinessInterfacesSupported() {
        return !remoteBusinessClassNames.isEmpty();
    }

    /**
     * @return true if this is an EJB that implements a web service endpoint.
     */
    @Override
    public boolean hasWebServiceEndpointInterface() {
        return getWebServiceEndpointInterfaceName() != null;
    }

    /**
     * @return true if this is an EJB provides a no interface Local view.
     */
    @Override
    public boolean isLocalBean() {
        return localBean;
    }

    /**
     * Sets the classname of the ejb.
     */
    public void setEjbClassName(String ejbClassName) {
        this.ejbClassName = ejbClassName;
    }

    /**
     * Returns the classname of the ejb.
     */
    @Override
    public String getEjbClassName() {
        return this.ejbClassName;
    }

    /**
     * IASRI 4725194 Returns the Execution class ,which is same as the user-specified class in case of Message,Session and
     * Bean Managed Persistence Entity Beans but is different for Container Mananged Persistence Entity Bean Therefore,the
     * implementation in the base class is to return getEjbClassName() and the method is redefined in IASEjbCMPDescriptor.
     */
    @Override
    public String getEjbImplClassName() {
        return this.getEjbClassName();
    }

    /**
     * Sets the remote home implementation classname of the ejb.
     */
    public void setRemoteHomeImplClassName(String name) {
        this.remoteHomeImplClassName = name;
    }

    /**
     * Returns the classname of the remote home impl.
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
     * Returns the classname of the Local home impl.
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
     * Returns the classname of the EJBLocalObject impl.
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
     * Returns the classname of the EJBObject impl.
     */
    public String getEJBObjectImplClassName() {
        return this.ejbObjectImplClassName;
    }

    /**
     * The transaction type of this ejb.
     */
    @Override
    public String getTransactionType() {
        return this.transactionType;
    }

    /**
     * Set the transaction type of this ejb.
     */
    public abstract void setTransactionType(String transactionType);


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

    public boolean isTimedObject() {
        return timedObjectMethod != null || !timerSchedules.isEmpty();
    }

    public MethodDescriptor getEjbTimeoutMethod() {
        return timedObjectMethod;
    }

    public void setEjbTimeoutMethod(MethodDescriptor method) {
        timedObjectMethod = method;
    }

    public void addScheduledTimerDescriptor(ScheduledTimerDescriptor scheduleDescriptor) {
        timerSchedules.add(scheduleDescriptor);
    }

    /**
     * Special method for overrides because more than one schedule can be specified on a single method
     */
    public void addScheduledTimerDescriptorFromDD(ScheduledTimerDescriptor scheduleDescriptor) {
        timerMethodDescriptors.add(scheduleDescriptor.getTimeoutMethod());
        timerSchedules.add(scheduleDescriptor);
    }

    public boolean hasScheduledTimerMethodFromDD(Method timerMethod) {
        boolean match = false;

        for (MethodDescriptor next : timerMethodDescriptors) {
            if (next.getName().equals(timerMethod.getName()) && (next.getParameterClassNames() == null
                    || next.getParameterClassNames().length == timerMethod.getParameterTypes().length)) {
                match = true;
                break;
            }
        }

        return match;
    }

    public List<ScheduledTimerDescriptor> getScheduledTimerDescriptors() {
        return timerSchedules;
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

        allInterceptorClasses.clear();
        allInterceptorClasses.addAll(results.allInterceptorClasses);

        interceptorChain.clear();
        interceptorChain.addAll(results.classInterceptorChain);

        methodInterceptorsMap.clear();
        methodInterceptorsMap.putAll(results.methodInterceptorsMap);

        for (EjbInterceptor interceptor : allInterceptorClasses) {
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

    @Override
    public boolean hasInterceptorClass(String interceptorClassName) {

        for (String next : getInterceptorClassNames()) {
            if (next.equals(interceptorClassName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void addInterceptorClass(EjbInterceptor interceptor) {
        allInterceptorClasses.add(interceptor);
    }

    @Override
    public void appendToInterceptorChain(List<EjbInterceptor> chain) {
        interceptorChain.addAll(chain);
    }

    /**
     * Return an unordered set of interceptor descriptors for this bean. This list does not include interceptor info for the
     * bean class itself, even if the bean class declares AroundInvoke methods and/or callbacks.
     */
    public Set<EjbInterceptor> getInterceptorClasses() {
        Set<EjbInterceptor> classes = new HashSet<>(allInterceptorClasses);
        return classes;
    }

    /**
     * Return an unordered set of the names of all interceptor classes for this bean. This list does not include the name of
     * the bean class itself, even if the bean class declares AroundInvoke methods and/or callbacks.
     */
    public Set<String> getInterceptorClassNames() {

        HashSet<String> classNames = new HashSet<>();

        for (EjbInterceptor ei : getInterceptorClasses()) {
            classNames.add(ei.getInterceptorClassName());
        }

        return classNames;
    }

    public Map<MethodDescriptor, List<EjbInterceptor>> getMethodInterceptorsMap() {
        return new HashMap<>(methodInterceptorsMap);
    }

    public List<EjbInterceptor> getInterceptorChain() {
        return new LinkedList<>(interceptorChain);
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

    @Override
    public void addMethodLevelChain(List<EjbInterceptor> chain, Method m, boolean aroundInvoke) {

        if (chain.size() == 0) {
            return;
        }

        MethodDescriptor methodDesc = new MethodDescriptor(m);

        List<EjbInterceptor> existingChain = null;

        for (MethodDescriptor next : methodInterceptorsMap.keySet()) {
            if (next.implies(methodDesc)) {
                existingChain = methodInterceptorsMap.get(methodDesc);
                break;
            }
        }

        if (existingChain != null) {
            existingChain.addAll(chain);
        } else {
            List<EjbInterceptor> newChain = new LinkedList<>();
            for (EjbInterceptor interceptor : interceptorChain) {
                boolean include = aroundInvoke ? interceptor.hasAroundInvokeDescriptor() : interceptor.hasAroundTimeoutDescriptor();
                if (include) {
                    newChain.add(interceptor);
                }
            }
            newChain.addAll(chain);
            methodInterceptorsMap.put(methodDesc, newChain);
        }
    }

    private List<EjbInterceptor> getClassOrMethodInterceptors(MethodDescriptor businessMethod) {

        List<EjbInterceptor> classOrMethodInterceptors = null;

        Iterator<Map.Entry<MethodDescriptor, List<EjbInterceptor>>> entryIterator = methodInterceptorsMap.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<MethodDescriptor, List<EjbInterceptor>> entry = entryIterator.next();
            MethodDescriptor methodDesc = entry.getKey();
            if (methodDesc.implies(businessMethod)) {
                classOrMethodInterceptors = entry.getValue();
            }
        }

        if (classOrMethodInterceptors == null) {
            classOrMethodInterceptors = interceptorChain;
        }

        return classOrMethodInterceptors;
    }

    /**
     * Return the ordered list of interceptor info for a particular callback event type. This list *does* include the info
     * on any bean class callback. If present, this would always be the last element in the list because of the precedence
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
            throw new IllegalStateException(
                    localStrings.getLocalString("enterprise.deployment.invalidcallbacktype", "Invalid callback type: [{0}]", type));
        }

        return getCallbackInterceptors(type, callbackDescriptors);
    }

    /**
     * Common code to add the bean class as a LC interceptor
     */
    private LinkedList<EjbInterceptor> getCallbackInterceptors(CallbackType type, Set<LifecycleCallbackDescriptor> callbackDescriptors) {

        LinkedList<EjbInterceptor> callbackInterceptors = new LinkedList<>();

        ClassLoader classLoader = getEjbBundleDescriptor().getClassLoader();
        List<EjbInterceptor> classOrMethodInterceptors = (type.equals(CallbackType.AROUND_CONSTRUCT))
                ? getConstructorInterceptors(classLoader)
                : interceptorChain;

        for (EjbInterceptor next : classOrMethodInterceptors) {
            if (!next.getCallbackDescriptors(type).isEmpty()) {
                callbackInterceptors.add(next);
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
        List<EjbInterceptor> callbackInterceptors = null;
        String shortClassName = ejbClassName;
        int i = ejbClassName.lastIndexOf('.');
        if (i > -1) {
            shortClassName = ejbClassName.substring(i + 1);
        }

        CDIService cdiService = sl == null ? null : sl.getService(CDIService.class);
        if (cdiService != null && cdiService.isCDIEnabled(getEjbBundleDescriptor())) {
            try {
                Class<?> beanClass = classLoader.loadClass(getEjbClassName());
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
                throw new RuntimeException(getEjbClassName(), t);
            }
        }
        if (callbackInterceptors == null) {
            // non-CDI or no @Inject constructor - use no-arg constructor
            callbackInterceptors = getClassOrMethodInterceptors(
                new MethodDescriptor(shortClassName, null, new String[0], EJB_BEAN));
        }

        return callbackInterceptors;
    }

    /**
     * Gets the transaction scope of this ejb.
     *
     * @return true if bean has distributed tx scope (default).
     */
    public boolean isDistributedTransactionScope() {
        return isDistributedTxScope;
    }

    /**
     * Set the transaction scope of this ejb.
     */
    public void setDistributedTransactionScope(boolean scope) {
        isDistributedTxScope = scope;
    }

    /**
     * Set the usesCallerIdentity flag
     */
    @Override
    public void setUsesCallerIdentity(boolean flag) {
        usesCallerIdentity = flag;
    }

    /**
     * Get the usesCallerIdentity flag
     *
     * @return Boolean.TRUE if this bean uses caller identity null if this is called before validator visit
     */
    @Override
    public Boolean getUsesCallerIdentity() {
        return usesCallerIdentity;
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
     * Set the description field of security-identity
     */
    public void setSecurityIdentityDescription(String s) {
        securityIdentityDescription = s;
    }

    @Override
    public void setRunAsIdentity(RunAsIdentityDescriptor desc) {
        if (usesCallerIdentity == null || usesCallerIdentity) {
            throw new IllegalStateException(
                    localStrings.getLocalString("exceptioncannotsetrunas", "Cannot set RunAs identity when using caller identity"));
        }
        this.runAsIdentity = desc;
    }

    @Override
    public RunAsIdentityDescriptor getRunAsIdentity() {
        if (usesCallerIdentity == null || usesCallerIdentity) {
            throw new IllegalStateException(
                    localStrings.getLocalString("exceptioncannotgetrunas", "Cannot get RunAs identity when using caller identity"));
        }
        return runAsIdentity;
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
        if (oldValue == null || (!(oldValue.equals(containerTransaction)))) {
            String transactionType = this.getTransactionType();
            if (transactionType == null) {
                setTransactionType(CONTAINER_TRANSACTION_TYPE);
            } else if (BEAN_TRANSACTION_TYPE.equals(transactionType)) {
                throw new IllegalArgumentException(
                        localStrings.getLocalString("enterprise.deployment.exceptiontxattrbtnotspecifiedinbeanwithtxtype",
                                "Method level transaction attributes may not be specified on a bean with transaction type {0}",
                                new Object[] { com.sun.enterprise.deployment.EjbDescriptor.BEAN_TRANSACTION_TYPE }));
            }
            // _logger.log(Level.FINE,"put " + methodDescriptor + " " + containerTransaction);
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
        // _logger.log(Level.FINE,"Pre conversion = " + transactions);
        Hashtable<MethodDescriptor, ContainerTransaction> convertedTransactions = new Hashtable<>();
        convertMethodContainerTransactionsOfStyle(1, convertedTransactions);
        convertMethodContainerTransactionsOfStyle(2, convertedTransactions);
        convertMethodContainerTransactionsOfStyle(3, convertedTransactions);
        // _logger.log(Level.FINE,"Post conversion = " + convertedTransactions);
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

    @Override
    public Set<EjbIORConfigurationDescriptor> getIORConfigurationDescriptors() {
        return iorConfigDescriptors;
    }

    public void addIORConfigurationDescriptor(EjbIORConfigurationDescriptor val) {
        iorConfigDescriptors.add(val);
    }

    /**
     * @eturn the set of roles to which have been assigned method permissions.
     */
    @Override
    public Set<Role> getPermissionedRoles() {
        if (needToConvertMethodPermissions()) {
            convertMethodPermissions();
        }
        Set<Role> allPermissionedRoles = new HashSet<>();
        for (MethodPermission pm : getPermissionedMethodsByPermission().keySet()) {
            if (pm.isRoleBased()) {
                allPermissionedRoles.add(pm.getRole());
            }
        }
        return allPermissionedRoles;
    }

    /**
     * @return the Map of MethodPermission (keys) that have been assigned to MethodDescriptors (elements)
     */
    public Map<MethodPermission, Set<MethodDescriptor>> getPermissionedMethodsByPermission() {
        if (permissionedMethodsByPermission == null) {
            permissionedMethodsByPermission = new Hashtable<>();
        }
        return permissionedMethodsByPermission;
    }

    /**
     * Add a new method permission to a method or a set of methods
     *
     * @param mp is the new method permission to assign
     * @param md describe the method or set of methods this permission apply to
     */
    @Override
    public void addPermissionedMethod(MethodPermission mp, MethodDescriptor md) {
        if (getEjbBundleDescriptor() == null) {
            throw new IllegalArgumentException(
                localStrings.getLocalString("enterprise.deployment.exceptioncannotaddrolesdescriptor",
                    "Cannot add roles when the descriptor is not part of a bundle"));
        }
        if (mp.isRoleBased()) {
            if (!getEjbBundleDescriptor().getRoles().contains(mp.getRole())) {
                // Check for the any authenticated user role '**' as this role
                // will be implicitly defined when not listed as a security-role
                if (!"**".equals(mp.getRole().getName())) {
                    throw new IllegalArgumentException(
                        localStrings.getLocalString("enterprise.deployment.exceptioncannotaddrolesbundle",
                            "Cannot add roles when the bundle does not have them"));
                }
            }
        }

        if (md.isExact()) {
            updateMethodPermissionForMethod(mp, md);
        } else {
            addMethodPermissionForStyledMethodDescriptor(mp, md);
        }

        saveMethodPermissionFromDD(mp, md);
    }

    /**
     * Keep a record of all the Method Permissions exactly as they were in the DD
     */
    private void saveMethodPermissionFromDD(MethodPermission methodPermission, MethodDescriptor methodDescriptor) {
        if (methodPermissionsFromDD == null) {
            methodPermissionsFromDD = new HashMap<>();
        }

        // We organize by permission, makes it easier...
        // Use Array List as opposed to HashMap or Table because MethodDescriptor
        // Equality once did not take into account differences in
        // method interface, and will process sequentially.

        methodPermissionsFromDD.computeIfAbsent(methodPermission, e -> new ArrayList<>()).add(methodDescriptor);
    }

    /**
     * Get a record of all the Method Permissions exactly as they were in the`DD
     */
    @Override
    public HashMap<MethodPermission, ArrayList<MethodDescriptor>> getMethodPermissionsFromDD() {
        return methodPermissionsFromDD;
    }

    private void addMethodPermissionForMethod(MethodPermission mp, MethodDescriptor md) {

        if (getPermissionedMethodsByPermission().containsKey(mp)) {
            Set<MethodDescriptor> alreadyPermissionedMethodsForThisRole = getPermissionedMethodsByPermission().get(mp);
            alreadyPermissionedMethodsForThisRole.add(md);
            this.getPermissionedMethodsByPermission().put(mp, alreadyPermissionedMethodsForThisRole);
        } else {
            Set<MethodDescriptor> permissionedMethodsForThisRole = new HashSet<>();
            permissionedMethodsForThisRole.add(md);
            this.getPermissionedMethodsByPermission().put(mp, permissionedMethodsForThisRole);
        }

    }

    /**
     * Remove a method permission from a method or a set of methods
     *
     * @param mp is the method permission to remove
     * @param md describe the method or set of methods this permission apply to
     */
    public void removePermissionedMethod(MethodPermission mp, MethodDescriptor md) {
        if (this.getEjbBundleDescriptor() == null) {
            throw new IllegalArgumentException(localStrings.getLocalString("enterprise.deployment.exceptioncanotaddrolesdescriptor",
                    "Cannot add roles when the descriptor is not part of a bundle"));
        }
        if (mp.isRoleBased()) {
            if (!getEjbBundleDescriptor().getRoles().contains(mp.getRole())) {
                throw new IllegalArgumentException(
                        localStrings.getLocalString("enterprise.deployment.exceptioncannotaddrolesbndledoesnothave",
                                "Cannot add roles when the bundle does not have them"));
            }
        }

        if (this.getPermissionedMethodsByPermission().containsKey(mp)) {
            Set<MethodDescriptor> alreadyPermissionedMethodsForThisRole = this.getPermissionedMethodsByPermission().get(mp);
            alreadyPermissionedMethodsForThisRole.remove(md);
            this.getPermissionedMethodsByPermission().put(mp, alreadyPermissionedMethodsForThisRole);
        }

    }

    /**
     * add a style 1 or 2 in our tables
     */
    private void addMethodPermissionForStyledMethodDescriptor(MethodPermission mp, MethodDescriptor md) {

        if (styledMethodDescriptors == null) {
            styledMethodDescriptors = new HashMap<>();
        }

        // we organize per method descriptors, makes it easier...
        Set<MethodPermission> permissions = styledMethodDescriptors.get(md);
        if (permissions == null) {
            permissions = new HashSet<>();
        }
        permissions.add(mp);
        styledMethodDescriptors.put(md, permissions);
    }

    /**
     * @return a map of permission to style 1 or 2 method descriptors
     */
    public Map<MethodPermission, Set<MethodDescriptor>> getStyledPermissionedMethodsByPermission() {
        if (styledMethodDescriptors == null) {
            return null;
        }

        // the current info is structured as MethodDescriptors as keys to
        // method permission, let's reverse this to make the Map using the
        // method permission as a key.
        Map<MethodPermission, Set<MethodDescriptor>> styledMethodDescriptorsByPermission = new HashMap<>();
        for (Entry<MethodDescriptor, Set<MethodPermission>> md : styledMethodDescriptors.entrySet()) {
            Set<MethodPermission> methodPermissions = md.getValue();
            for (MethodPermission mp : methodPermissions) {
                Set<MethodDescriptor> methodDescriptors = styledMethodDescriptorsByPermission.get(mp);
                if (methodDescriptors == null) {
                    methodDescriptors = new HashSet<>();
                }
                methodDescriptors.add(md.getKey());
                styledMethodDescriptorsByPermission.put(mp, methodDescriptors);
            }
        }
        return styledMethodDescriptorsByPermission;
    }

    /**
     * @return a Set of method descriptors for all the methods associated with an unchecked method permission
     */
    public Set<MethodDescriptor> getUncheckedMethodDescriptors() {
        if (needToConvertMethodPermissions()) {
            convertMethodPermissions();
        }
        Map<MethodPermission, Set<MethodDescriptor>> all = getPermissionedMethodsByPermission();
        return all.get(MethodPermission.getPermitAllMethodPermission());
    }

    /**
     * @return a Set of method descriptors for all the methoda assoicated with an excluded method permission
     */
    public Set<MethodDescriptor> getExcludedMethodDescriptors() {
        if (needToConvertMethodPermissions()) {
            convertMethodPermissions();
        }
        Map<MethodPermission, Set<MethodDescriptor>> all = getPermissionedMethodsByPermission();
        return all.get(MethodPermission.getDenyAllMethodPermission());
    }

    /**
     * convert all style 1 and style 2 method descriptors contained in our tables into style 3 method descriptors.
     */
    private void convertMethodPermissions() {
        if (styledMethodDescriptors == null) {
            return;
        }
        Set<MethodDescriptor> allMethods = getMethodDescriptors();
        Set<MethodDescriptor> unpermissionedMethods = getMethodDescriptors();
        Set<Entry<MethodDescriptor, Set<MethodPermission>>> methodDescriptors = styledMethodDescriptors.entrySet();
        for (Entry<MethodDescriptor, Set<MethodPermission>> entry : methodDescriptors) {
            MethodDescriptor styledMd = entry.getKey();

            // Get the new permissions we are trying to set for this
            // method(s)
            Set<MethodPermission> newPermissions = entry.getValue();

            // Convert to style 3 method descriptors
            Vector<MethodDescriptor> mds = styledMd.doStyleConversion(this, allMethods);
            for (MethodDescriptor md : mds) {
                // remove it from the list of unpermissioned methods.
                // it will be used at the end to set all remaining methods
                // with the unchecked method permission
                unpermissionedMethods.remove(md);

                // iterator over the new set of method permissions for that
                // method descriptor and update the table
                for (MethodPermission newMp : newPermissions) {
                    updateMethodPermissionForMethod(newMp, md);
                }
            }
        }

        // All remaining methods should now be defined as unchecked...
        MethodPermission permitAll = MethodPermission.getPermitAllMethodPermission();
        Iterator<MethodDescriptor> iterator = unpermissionedMethods.iterator();
        while (iterator.hasNext()) {
            MethodDescriptor md = iterator.next();
            if (getMethodPermissions(md).isEmpty()) {
                addMethodPermissionForMethod(permitAll, md);
            }
        }

        // finally we reset the list of method descriptors that need style conversion
        styledMethodDescriptors = null;
    }

    /**
     * Update a method descriptor set of method permission with a new method permission The new method permission is added
     * to the list of existing method permissions given it respect the EJB 2.0 paragraph 21.3.2 on priorities of method
     * permissions
     *
     * @param mp is the method permission to be added
     * @param md is the method descriptor (style3 only) to add the method permission to
     */
    private void updateMethodPermissionForMethod(MethodPermission mp, MethodDescriptor md) {

        // Get the current set of method permissions for that method
        Set<MethodPermission> oldPermissions = getMethodPermissions(md);

        if (oldPermissions.isEmpty()) {
            // this is easy, just add the new one
            addMethodPermissionForMethod(mp, md);
            return;
        }

        // The order of method permssion setting is very important
        // EJB 2.0 Spec 21.3.2
        // excluded method permission is always used when multiple methos permission are present
        // unchecked is considered like a role based method permission and is added to the list
        // therefore making the method callable by anyone.

        if (mp.isExcluded()) {
            // Excluded methods takes precedence on any other form of method permission
            // remove all existing method permission...
            for (MethodPermission oldMp : oldPermissions) {
                removePermissionedMethod(oldMp, md);
            }
            // add the excluded
            addMethodPermissionForMethod(mp, md);
        } else {
            if (mp.isUnchecked()) {
                // we are trying to add an unchecked method permisison, all role-based
                // method permission should be removed since unchecked is now used, if a
                // particular method has an excluded method permision, we do not add it
                for (MethodPermission oldMp : oldPermissions) {
                    if (!oldMp.isExcluded()) {
                        removePermissionedMethod(oldMp, md);
                        addMethodPermissionForMethod(mp, md);
                    }
                }
            } else {
                // we are trying to add a role based method permission. Check that
                // unchecked or excluded method permissions have not been set
                // and add it to the current list of role based permission
                for (MethodPermission oldMp : oldPermissions) {
                    if (!oldMp.isExcluded()) {
                        if (!oldMp.isUnchecked()) {
                            addMethodPermissionForMethod(mp, md);
                        }
                    }
                }
            }
        }
    }

    /**
     * @return true if we have unconverted style 1 or style 2 method descriptors
     */
    private boolean needToConvertMethodPermissions() {
        return styledMethodDescriptors != null;
    }

    /**
     * @return the set of method permission assigned to a ejb method descriptor.
     */
    @Override
    public Set<MethodPermission> getMethodPermissionsFor(MethodDescriptor methodDescriptor) {
        if (needToConvertMethodPermissions()) {
            convertMethodPermissions();
        }

        return getMethodPermissions(methodDescriptor);
    }

    private Set<MethodPermission> getMethodPermissions(MethodDescriptor methodDescriptor) {
        Set<MethodPermission> methodPermissionsForMethod = new HashSet<>();
        for (MethodPermission nextPermission : this.getPermissionedMethodsByPermission().keySet()) {
            Set<MethodDescriptor> permissionedMethods = this.getPermissionedMethodsByPermission().get(nextPermission);
            for (MethodDescriptor md : permissionedMethods) {
                if (md.equals(methodDescriptor)) {
                    methodPermissionsForMethod.add(nextPermission);
                }
            }
        }

        return methodPermissionsForMethod;
    }

    // BEGIN WritableJndiNameEnvironment methods

    /**
     * Return the set of ejb references this ejb declares.
     */
    @Override
    public final Set<EjbReferenceDescriptor> getEjbReferenceDescriptors() {
        if (env == null) {
            return ejbReferences;
        }
        return env.getEjbReferenceDescriptors();
    }

    /**
     * Adds a reference to another ejb to me.
     */
    @Override
    public final void addEjbReferenceDescriptor(EjbReferenceDescriptor ejbReference) {
        try {
            EjbReference existing = getEjbReference(ejbReference.getName());
            for (InjectionTarget next : ejbReference.getInjectionTargets()) {
                existing.addInjectionTarget(next);
            }
        } catch (IllegalArgumentException e) {
            if (env == null) {
                ejbReferences.add(ejbReference);
            } else {
                env.addEjbReferenceDescriptor(ejbReference);
            }
            ejbReference.setReferringBundleDescriptor(getEjbBundleDescriptor());
        }
    }

    @Override
    public final void removeEjbReferenceDescriptor(EjbReferenceDescriptor ejbReference) {
        if (env == null) {
            ejbReferences.remove(ejbReference);
        } else {
            env.removeEjbReferenceDescriptor(ejbReference);
        }
        ejbReference.setReferringBundleDescriptor(null);
    }

    @Override
    public final Set<LifecycleCallbackDescriptor> getPostConstructDescriptors() {
        return postConstructDescs;
    }

    @Override
    public final void addPostConstructDescriptor(LifecycleCallbackDescriptor postConstructDesc) {
        String className = postConstructDesc.getLifecycleCallbackClass();
        boolean found = false;
        for (LifecycleCallbackDescriptor next : getPostConstructDescriptors()) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                found = true;
                break;
            }
        }
        if (!found) {
            getPostConstructDescriptors().add(postConstructDesc);
        }
    }

    @Override
    public final LifecycleCallbackDescriptor getPostConstructDescriptorByClass(String className) {
        return bundleDescriptor.getPostConstructDescriptorByClass(className, this);
    }

    @Override
    public final Set<LifecycleCallbackDescriptor> getPreDestroyDescriptors() {
        return preDestroyDescs;
    }

    @Override
    public final void addPreDestroyDescriptor(LifecycleCallbackDescriptor preDestroyDesc) {
        String className = preDestroyDesc.getLifecycleCallbackClass();
        boolean found = false;
        for (LifecycleCallbackDescriptor next : getPreDestroyDescriptors()) {
            if (next.getLifecycleCallbackClass().equals(className)) {
                found = true;
                break;
            }
        }
        if (!found) {
            getPreDestroyDescriptors().add(preDestroyDesc);
        }
    }

    @Override
    public final LifecycleCallbackDescriptor getPreDestroyDescriptorByClass(String className) {
        return bundleDescriptor.getPreDestroyDescriptorByClass(className, this);
    }

    @Override
    public final Set<ServiceReferenceDescriptor> getServiceReferenceDescriptors() {
        if (env == null) {
            return serviceReferences;
        }
        return env.getServiceReferenceDescriptors();
    }

    @Override
    public final void addServiceReferenceDescriptor(ServiceReferenceDescriptor serviceRef) {
        try {
            ServiceReferenceDescriptor existing = this.getServiceReferenceByName(serviceRef.getName());
            for (InjectionTarget next : serviceRef.getInjectionTargets()) {
                existing.addInjectionTarget(next);
            }

        } catch (IllegalArgumentException e) {
            if (env != null) {
                env.addServiceReferenceDescriptor(serviceRef);
            } else {
                serviceReferences.add(serviceRef);
            }
            serviceRef.setBundleDescriptor(getEjbBundleDescriptor());
        }
    }

    @Override
    public final void removeServiceReferenceDescriptor(ServiceReferenceDescriptor serviceRef) {
        if (env != null) {
            env.removeServiceReferenceDescriptor(serviceRef);
        } else {
            serviceReferences.remove(serviceRef);
        }
    }

    /**
     * Looks up an service reference with the given name. Throws an IllegalArgumentException if it is not found.
     */
    @Override
    public final ServiceReferenceDescriptor getServiceReferenceByName(String name) {
        if (env != null) {
            return env.getServiceReferenceByName(name);
        }
        for (Object element : this.getServiceReferenceDescriptors()) {
            ServiceReferenceDescriptor srd = (ServiceReferenceDescriptor) element;
            if (srd.getName().equals(name)) {
                return srd;
            }
        }
        throw new IllegalArgumentException(localStrings.getLocalString("enterprise.deployment.exceptionejbhasnoservicerefbyname",
                "This ejb [{0}] has no service reference by the name of [{1}]", new Object[] { getName(), name }));
    }

    @Override
    public final Set<MessageDestinationReferenceDescriptor> getMessageDestinationReferenceDescriptors() {
        if (env == null) {
            return messageDestReferences;
        }
        return env.getMessageDestinationReferenceDescriptors();
    }

    @Override
    public final void addMessageDestinationReferenceDescriptor(MessageDestinationReferenceDescriptor messageDestRef) {

        try {
            MessageDestinationReferenceDescriptor existing = getMessageDestinationReferenceByName(messageDestRef.getName());
            for (InjectionTarget next : messageDestRef.getInjectionTargets()) {
                existing.addInjectionTarget(next);
            }
        } catch (IllegalArgumentException e) {
            if (env == null) {
                messageDestReferences.add(messageDestRef);
            } else {
                env.addMessageDestinationReferenceDescriptor(messageDestRef);
            }
            if (getEjbBundleDescriptor() != null) {
                messageDestRef.setReferringBundleDescriptor(getEjbBundleDescriptor());
            }
        }
    }

    @Override
    public final void removeMessageDestinationReferenceDescriptor(MessageDestinationReferenceDescriptor msgDestRef) {
        if (env == null) {
            messageDestReferences.remove(msgDestRef);
        } else {
            env.removeMessageDestinationReferenceDescriptor(msgDestRef);
        }
    }

    /**
     * Looks up an message destination reference with the given name. Throws an IllegalArgumentException if it is not found.
     */
    @Override
    public final MessageDestinationReferenceDescriptor getMessageDestinationReferenceByName(String name) {
        if (env != null) {
            return env.getMessageDestinationReferenceByName(name);
        }
        for (MessageDestinationReferenceDescriptor mdr : messageDestReferences) {
            if (mdr.getName().equals(name)) {
                return mdr;
            }
        }
        throw new IllegalArgumentException(localStrings.getLocalString("exceptionejbhasnomsgdestrefbyname",
                "This ejb [{0}] has no message destination reference by the name of [{1}]", new Object[] { getName(), name }));
    }

    @Override
    public final Set<ResourceDescriptor> getResourceDescriptors(JavaEEResourceType type) {
        if (env == null) {
            return super.getResourceDescriptors(type);
        }
        return env.getResourceDescriptors(type);
    }

    @Override
    public final void addResourceDescriptor(ResourceDescriptor descriptor) {
        if (env == null) {
            super.addResourceDescriptor(descriptor);
        } else {
            env.addResourceDescriptor(descriptor);
        }
    }

    @Override
    public final void removeResourceDescriptor(ResourceDescriptor descriptor) {
        if (env == null) {
            super.removeResourceDescriptor(descriptor);
        } else {
            env.removeResourceDescriptor(descriptor);
        }
    }

    @Override
    public final Set<ResourceDescriptor> getAllResourcesDescriptors() {
        if (env == null) {
            return super.getAllResourcesDescriptors();
        }
        return env.getAllResourcesDescriptors();
    }

    @Override
    public final Set<ResourceDescriptor> getAllResourcesDescriptors(Class givenClazz) {
        if (env == null) {
            return super.getAllResourcesDescriptors(givenClazz);
        }
        return env.getAllResourcesDescriptors(givenClazz);
    }

    /**
     * Return the set of resource environment references this ejb declares.
     */
    @Override
    public final Set<ResourceEnvReferenceDescriptor> getResourceEnvReferenceDescriptors() {
        if (env == null) {
            return resourceEnvReferences;
        }
        return env.getResourceEnvReferenceDescriptors();
    }

    @Override
    public final void addResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvReference) {

        try {
            ResourceEnvReferenceDescriptor existing = getResourceEnvReferenceByName(resourceEnvReference.getName());
            for (InjectionTarget next : resourceEnvReference.getInjectionTargets()) {
                existing.addInjectionTarget(next);
            }
        } catch (IllegalArgumentException e) {
            if (env == null) {
                resourceEnvReferences.add(resourceEnvReference);
            } else {
                env.addResourceEnvReferenceDescriptor(resourceEnvReference);
            }
        }

    }

    @Override
    public final void removeResourceEnvReferenceDescriptor(ResourceEnvReferenceDescriptor resourceEnvReference) {
        if (env == null) {
            resourceEnvReferences.remove(resourceEnvReference);
        } else {
            env.removeResourceEnvReferenceDescriptor(resourceEnvReference);
        }
    }

    @Override
    public final ResourceEnvReferenceDescriptor getResourceEnvReferenceByName(String name) {
        for (ResourceEnvReferenceDescriptor jdr : getResourceEnvReferenceDescriptors()) {
            if (jdr.getName().equals(name)) {
                return jdr;
            }
        }
        throw new IllegalArgumentException(localStrings.getLocalString("enterprise.deployment.exceptionbeanhasnoresourceenvrefbyname",
                "This bean {0} has no resource environment reference by the name of {1}", new Object[] { getName(), name }));
    }

    /**
     * Return the set of resource references this ejb declares.
     */
    @Override
    public final Set<ResourceReferenceDescriptor> getResourceReferenceDescriptors() {
        if (env == null) {
            return resourceReferences;
        }
        return env.getResourceReferenceDescriptors();
    }

    /**
     * Adds a resource reference to me.
     */
    @Override
    public final void addResourceReferenceDescriptor(ResourceReferenceDescriptor resourceReference) {
        try {
            ResourceReferenceDescriptor existing = getResourceReferenceByName(resourceReference.getName());
            for (InjectionTarget next : resourceReference.getInjectionTargets()) {
                existing.addInjectionTarget(next);
            }
        } catch (IllegalArgumentException e) {
            if (env == null) {
                resourceReferences.add(resourceReference);
            } else {
                env.addResourceReferenceDescriptor(resourceReference);
            }
        }
    }

    /**
     * Removes the given resource reference from me.
     */
    @Override
    public final void removeResourceReferenceDescriptor(ResourceReferenceDescriptor resourceReference) {
        if (env == null) {
            resourceReferences.remove(resourceReference);
        } else {
            env.removeResourceReferenceDescriptor(resourceReference);
        }
    }

    /**
     * Returns the environment property object searching on the supplied key. throws an illegal argument exception if no
     * such environment property exists.
     */
    @Override
    public final EnvironmentProperty getEnvironmentPropertyByName(String name) {
        if (env != null) {
            return env.getEnvironmentPropertyByName(name);
        }
        for (EnvironmentProperty ev : this.getEnvironmentProperties()) {
            if (ev.getName().equals(name)) {
                return ev;
            }
        }
        throw new IllegalArgumentException(localStrings.getLocalString("enterprise.deployment.exceptionbeanhasnoenvpropertybyname",
                "This bean {0} has no environment property by the name of {1}", new Object[] { getName(), name }));
    }

    /**
     * Return a copy of the structure holding the environment properties.
     */
    @Override
    public final Set<EnvironmentProperty> getEnvironmentProperties() {
        if (env == null) {
            return environmentProperties;
        }
        return env.getEnvironmentProperties();
    }

    /**
     * Add the supplied environment property to the ejb descriptor's list.
     */
    @Override
    public final void addEnvironmentProperty(EnvironmentProperty environmentProperty) {
        if (env != null) {
            env.addEnvironmentProperty(environmentProperty);
            return;
        }
        if (environmentProperties.contains(environmentProperty)) {
            // XXX - this makes no sense!
            removeEnvironmentProperty(environmentProperty);
            addEnvironmentProperty(environmentProperty);
        } else {
            environmentProperties.add(environmentProperty);
        }
    }

    /**
     * Removes the given environment property from me.
     */
    @Override
    public final void removeEnvironmentProperty(EnvironmentProperty environmentProperty) {
        if (env == null) {
            getEnvironmentProperties().remove(environmentProperty);
        } else {
            env.removeEnvironmentProperty(environmentProperty);
        }
    }

    @Override
    public final Set<EntityManagerFactoryReferenceDescriptor> getEntityManagerFactoryReferenceDescriptors() {
        if (env == null) {
            return entityManagerFactoryReferences;
        }
        return env.getEntityManagerFactoryReferenceDescriptors();
    }

    /**
     * Return the entity manager factory reference descriptor corresponding to the given name.
     */
    @Override
    public final EntityManagerFactoryReferenceDescriptor getEntityManagerFactoryReferenceByName(String name) {
        if (env != null) {
            return env.getEntityManagerFactoryReferenceByName(name);
        }
        for (EntityManagerFactoryReferenceDescriptor next : getEntityManagerFactoryReferenceDescriptors()) {
            if (next.getName().equals(name)) {
                return next;
            }
        }
        throw new IllegalArgumentException(localStrings.getLocalString("enterprise.deployment.exceptionbeanhasnoentitymgrfactoryrefbyname",
                "This ejb {0} has no entity manager factory reference by the name of {1}", new Object[] { getName(), name }));
    }

    @Override
    public final void addEntityManagerFactoryReferenceDescriptor(EntityManagerFactoryReferenceDescriptor reference) {
        try {
            EntityManagerFactoryReferenceDescriptor existing = getEntityManagerFactoryReferenceByName(reference.getName());
            for (InjectionTarget next : reference.getInjectionTargets()) {
                existing.addInjectionTarget(next);
            }
        } catch (IllegalArgumentException e) {
            if (getEjbBundleDescriptor() != null) {
                reference.setReferringBundleDescriptor(getEjbBundleDescriptor());
            }
            if (env != null) {
                env.addEntityManagerFactoryReferenceDescriptor(reference);
            } else {
                entityManagerFactoryReferences.add(reference);
            }
        }
    }

    @Override
    public final Set<EntityManagerReferenceDescriptor> getEntityManagerReferenceDescriptors() {
        if (env == null) {
            return entityManagerReferences;
        }
        return env.getEntityManagerReferenceDescriptors();
    }

    /**
     * Return the entity manager factory reference descriptor corresponding to the given name.
     */
    @Override
    public final EntityManagerReferenceDescriptor getEntityManagerReferenceByName(String name) {
        if (env != null) {
            return env.getEntityManagerReferenceByName(name);
        }
        for (EntityManagerReferenceDescriptor next : getEntityManagerReferenceDescriptors()) {
            if (next.getName().equals(name)) {
                return next;
            }
        }
        throw new IllegalArgumentException(localStrings.getLocalString("enterprise.deployment.exceptionbeanhasnoentitymgrrefbyname",
                "This ejb {0} has no entity manager reference by the name of {1}", new Object[] { getName(), name }));
    }

    @Override
    public final void addEntityManagerReferenceDescriptor(EntityManagerReferenceDescriptor reference) {
        try {
            EntityManagerReferenceDescriptor existing = this.getEntityManagerReferenceByName(reference.getName());
            for (InjectionTarget next : reference.getInjectionTargets()) {
                existing.addInjectionTarget(next);
            }
        } catch (IllegalArgumentException e) {
            if (getEjbBundleDescriptor() != null) {
                reference.setReferringBundleDescriptor(getEjbBundleDescriptor());
            }
            if (env == null) {
                getEntityManagerReferenceDescriptors().add(reference);
            } else {
                env.addEntityManagerReferenceDescriptor(reference);
            }
        }

    }

    @Override
    public final List<InjectionCapable> getInjectableResourcesByClass(String className) {
        if (env == null) {
            return bundleDescriptor.getInjectableResourcesByClass(className, this);
        }
        return env.getInjectableResourcesByClass(className);
    }

    @Override
    public final InjectionInfo getInjectionInfoByClass(Class clazz) {
        if (env == null) {
            return bundleDescriptor.getInjectionInfoByClass(clazz, this);
        }
        return env.getInjectionInfoByClass(clazz);
    }

    // END WritableJndiNameEnvirnoment methods

    // BEGIN methods closely related to WritableJndiNameEnvironment

    public boolean hasPostConstructMethod() {
        return !getPostConstructDescriptors().isEmpty();
    }

    public boolean hasPreDestroyMethod() {
        return !getPreDestroyDescriptors().isEmpty();
    }

    /**
     * Return the set of resource references this ejb declares that have been resolved.
     */
    public Set<ResourceReferenceDescriptor> getResourceReferenceDescriptors(boolean resolved) {
        Set<ResourceReferenceDescriptor> toReturn = new HashSet<>();
        for (ResourceReferenceDescriptor next : getResourceReferenceDescriptors()) {
            if (next.isResolved() == resolved) {
                toReturn.add(next);
            }
        }
        return toReturn;
    }

    /**
     * Return the resource object corresponding to the supplied name or throw an illegal argument exception.
     */
    @Override // ResourceReferenceContainer
    public ResourceReferenceDescriptor getResourceReferenceByName(String name) {
        for (ResourceReferenceDescriptor next : getResourceReferenceDescriptors()) {
            if (next.getName().equals(name)) {
                return next;
            }
        }
        throw new IllegalArgumentException(localStrings.getLocalString("enterprise.deployment.exceptionbeanhasnoresourcerefbyname",
                "This bean {0} has no resource reference by the name of {1}", new Object[] { getName(), name }));
    }

    /**
     * Returns true if this ejb descriptor has resource references that are resolved.
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

    // END methods closely related to WritableJndiNameEnvironment

    /**
     * Return a reference to another ejb by the same name or throw an IllegalArgumentException.
     */
    @Override
    public EjbReferenceDescriptor getEjbReference(String name) {
        for (EjbReferenceDescriptor er : getEjbReferenceDescriptors()) {
            if (er.getName().equals(name)) {
                return er;
            }
        }
        throw new IllegalArgumentException(
            localStrings.getLocalString("enterprise.deployment.exceptionbeanhasnoejbrefbyname",
                "This bean {0} has no ejb reference by the name of {1}", new Object[] {getName(), name}));
    }

    void removeRole(Role role) {
        this.getPermissionedMethodsByPermission().remove(new MethodPermission(role));
        Set<RoleReference> refs = new HashSet<>(this.getRoleReferences());
        for (RoleReference roleReference : refs) {
            if (roleReference.getRole().equals(role)) {
                roleReference.setValue("");
            }
        }
    }

    /**
     * Return a copy of the role references set.
     */
    public Set<RoleReference> getRoleReferences() {
        if (roleReferences == null) {
            roleReferences = new HashSet<>();
        }
        return roleReferences;
    }

    /**
     * Adds a role reference.
     */

    @Override
    public void addRoleReference(RoleReference roleReference) {
        this.getRoleReferences().add(roleReference);
    }

    /**
     * Removes a role reference.
     */
    public void removeRoleReference(RoleReference roleReference) {
        this.getRoleReferences().remove(roleReference);
    }

    /**
     * Returns a matching role reference by name or throw an IllegalArgumentException.
     */
    @Override
    public RoleReference getRoleReferenceByName(String roleReferenceName) {
        for (RoleReference nextRR : this.getRoleReferences()) {
            if (nextRR.getName().equals(roleReferenceName)) {
                return nextRR;
            }
        }
        return null;
    }

    /**
     * Gets the containing ejb bundle descriptor..
     */
    @Override
    public EjbBundleDescriptorImpl getEjbBundleDescriptor() {
        return bundleDescriptor;
    }

    public void setEjbBundleDescriptor(EjbBundleDescriptorImpl bundleDescriptor) {
        this.bundleDescriptor = bundleDescriptor;
    }

    /**
     * Called by WebArchivist to notify this EjbDescriptor that it has been associated with a web bundle.
     */
    @Override
    public void notifyNewModule(WebBundleDescriptor wbd) {
        // add our JNDI entries to the web bundle
        wbd.addJndiNameEnvironment(this);
        // clear our entries
        environmentProperties.clear();
        ejbReferences.clear();
        resourceEnvReferences.clear();
        messageDestReferences.clear();
        resourceReferences.clear();
        serviceReferences.clear();
        entityManagerFactoryReferences.clear();
        entityManagerReferences.clear();
        // switch to the web bundle as the source of JNDI entries
        env = wbd;

    }

    /**
     * Gets the application to which this ejb descriptor belongs.
     */
    @Override
    public Application getApplication() {
        if (getEjbBundleDescriptor() == null) {
            return null;
        }
        return getEjbBundleDescriptor().getApplication();
    }

    /**
     * Returns the full set of method descriptors I have (from all the methods on my home and remote interfaces).
     */
    @Override
    public Set<MethodDescriptor> getMethodDescriptors() {
        ClassLoader classLoader = getEjbBundleDescriptor().getClassLoader();
        Set<MethodDescriptor> methods = getBusinessMethodDescriptors();
        try {
            if (isRemoteInterfacesSupported()) {
                addAllInterfaceMethodsIn(methods, classLoader.loadClass(getHomeClassName()), MethodDescriptor.EJB_HOME);
            }
            if (isLocalInterfacesSupported()) {
                addAllInterfaceMethodsIn(methods, classLoader.loadClass(getLocalHomeClassName()), MethodDescriptor.EJB_LOCALHOME);
            }
        } catch (Throwable t) {
            _logger.log(Level.SEVERE, "enterprise.deployment.backend.methodClassLoadFailure",
                    new Object[] { "(EjbDescriptor.getMethods())" });

            throw new RuntimeException(t);
        }
        return methods;
    }

    /**
     * @return the full set of transactional business method descriptors I have.
     */
    public Set<MethodDescriptor> getTxBusinessMethodDescriptors() {
        Set<MethodDescriptor> txBusMethods = getBusinessMethodDescriptors();
        if (isTimedObject()) {
            if (timedObjectMethod != null) {
                txBusMethods.add(timedObjectMethod);
            }
            // XXX TODO - add schedule methods
        }
        return txBusMethods;
    }


    /**
     * Returns the full set of security business method descriptors I have.
     */
    @Override
    public Set<MethodDescriptor> getSecurityBusinessMethodDescriptors() {
        return getBusinessMethodDescriptors();
    }


    /**
     * @return the set of local/remote/no-interface view business method descriptors I have.
     */
    public Set<MethodDescriptor> getClientBusinessMethodDescriptors() {
        return getLocalRemoteBusinessMethodDescriptors();
    }

    /**
     * Returns the full set of business method descriptors I have
     */
    private Set<MethodDescriptor> getLocalRemoteBusinessMethodDescriptors() {

        ClassLoader classLoader = getEjbBundleDescriptor().getClassLoader();

        Set<MethodDescriptor> methods = new HashSet<>();

        try {
            if (isRemoteInterfacesSupported()) {
                addAllInterfaceMethodsIn(methods, classLoader.loadClass(getRemoteClassName()), MethodDescriptor.EJB_REMOTE);
            }

            if (isRemoteBusinessInterfacesSupported()) {
                for (String intf : getRemoteBusinessClassNames()) {
                    addAllInterfaceMethodsIn(methods, classLoader.loadClass(intf), MethodDescriptor.EJB_REMOTE);
                }
            }

            if (isLocalInterfacesSupported()) {
                addAllInterfaceMethodsIn(methods, classLoader.loadClass(getLocalClassName()), MethodDescriptor.EJB_LOCAL);
            }

            if (isLocalBusinessInterfacesSupported()) {
                for (String intf : getLocalBusinessClassNames()) {
                    addAllInterfaceMethodsIn(methods, classLoader.loadClass(intf), MethodDescriptor.EJB_LOCAL);
                }
            }

            if (isLocalBean()) {
                addAllInterfaceMethodsIn(methods, classLoader.loadClass(getEjbClassName()), MethodDescriptor.EJB_LOCAL);
            }
        } catch (Throwable t) {
            _logger.log(Level.SEVERE, "enterprise.deployment.backend.methodClassLoadFailure",
                    new Object[] { "(EjbDescriptor.getBusinessMethodDescriptors())" });

            throw new RuntimeException(t);
        }

        return methods;
    }

    /**
     * Returns the full set of business method descriptors I have
     */
    private Set<MethodDescriptor> getBusinessMethodDescriptors() {
        ClassLoader classLoader = getEjbBundleDescriptor().getClassLoader();
        Set<MethodDescriptor> methods = getLocalRemoteBusinessMethodDescriptors();

        try {
            if (hasWebServiceEndpointInterface()) {
                addAllInterfaceMethodsIn(methods, classLoader.loadClass(getWebServiceEndpointInterfaceName()),
                        MethodDescriptor.EJB_WEB_SERVICE);
            }
        } catch (Throwable t) {
            _logger.log(Level.SEVERE, "enterprise.deployment.backend.methodClassLoadFailure",
                    new Object[] { "(EjbDescriptor.getBusinessMethodDescriptors())" });

            throw new RuntimeException(t);
        }

        return methods;
    }

    protected void addAllInterfaceMethodsIn(Collection<MethodDescriptor> methodDescriptors, Class<?> c, String methodIntf) {
        Method[] methods = c.getMethods();
        for (Method method : methods) {
            if (method.getDeclaringClass() != java.lang.Object.class) {
                methodDescriptors.add(new MethodDescriptor(method, methodIntf));
            }
        }
    }

    /**
     * @return the MethodDescriptor for the given Method object
     */
    public MethodDescriptor getBusinessMethodDescriptorFor(Method m, String methodIntf) {
        Set<MethodDescriptor> businessMethodDescriptors = getBusinessMethodDescriptors();
        MethodDescriptor methodDesc = new MethodDescriptor(m, methodIntf);

        MethodDescriptor match = null;
        for (MethodDescriptor nextMethodDesc : businessMethodDescriptors) {
            if (nextMethodDesc.equals(methodDesc)) {
                match = nextMethodDesc;
                break;
            }
        }

        return match;
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
            _logger.log(Level.SEVERE, "enterprise.deployment.backend.methodClassLoadFailure",
                    new Object[] { "(EjbDescriptor.getMethods())" });
            throw new RuntimeException(t);
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
            _logger.log(Level.SEVERE, "enterprise.deployment.backend.methodClassLoadFailure",
                    new Object[] { "(EjbDescriptor.getMethods())" });
            throw new RuntimeException(t);
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
            _logger.log(Level.SEVERE, "enterprise.deployment.backend.methodClassLoadFailure",
                    new Object[] { "(EjbDescriptor.getMethods())" });
            throw new RuntimeException(t);
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
            _logger.log(Level.SEVERE, "enterprise.deployment.backend.methodClassLoadFailure", new Object[] { this.getEjbClassName() });
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

    void doMethodDescriptorConversions() throws Exception {
        Hashtable<MethodDescriptor, ContainerTransaction> transactions = this.getMethodContainerTransactions();
        Hashtable<MethodDescriptor, ContainerTransaction> convertedTransactions = new Hashtable<>();
        Collection<MethodDescriptor> transactionMethods = this.getTransactionMethodDescriptors();
        for (Enumeration<MethodDescriptor> e = transactions.keys(); e.hasMoreElements();) {
            MethodDescriptor md = e.nextElement();
            ContainerTransaction ct = transactions.get(md);
            for (MethodDescriptor next : md.doStyleConversion(this, transactionMethods)) {
                convertedTransactions.put(next, new ContainerTransaction(ct));
            }
        }
        setMethodContainerTransactions(convertedTransactions);
        convertMethodPermissions();
    }

    @Override
    public void removeEjbReferencer(EjbReferenceDescriptor ref) {
        ejbReferencersPointingToMe.remove(ref);
    }

    // called from EjbReferenceDescriptor.setEjbDescriptor
    @Override
    public void addEjbReferencer(EjbReferenceDescriptor ref) {
        ejbReferencersPointingToMe.add(ref);
    }

    // called from EjbEntityDescriptor.replaceEntityDescriptor etc
    public Set<EjbReferenceDescriptor> getAllEjbReferencers() {
        return ejbReferencersPointingToMe;
    }

    // Called from EjbBundleDescriptor only
    @Override
    public void setUniqueId(long id) {
        uniqueId = id;
    }

    @Override
    public long getUniqueId() {
        return uniqueId;
    }

    /**
     * Returns a formatted String of the attributes of this object.
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        super.print(toStringBuffer);
        toStringBuffer.append("\n homeClassName ").append(homeClassName);
        toStringBuffer.append("\n remoteClassName ").append(remoteClassName);
        toStringBuffer.append("\n remoteBusinessIntfs ").append(remoteBusinessClassNames).append("\n");
        toStringBuffer.append("\n localhomeClassName ").append(localHomeClassName);
        toStringBuffer.append("\n localClassName ").append(localClassName);
        toStringBuffer.append("\n localBusinessIntfs ").append(localBusinessClassNames);
        toStringBuffer.append("\n isLocalBean ").append(isLocalBean()).append("\n");
        toStringBuffer.append("\n jndiName ").append(jndiName).append("\n");
        toStringBuffer.append("\n ejbClassName ").append(ejbClassName);
        toStringBuffer.append("\n transactionType ").append(transactionType);
        toStringBuffer.append("\n methodContainerTransactions ").append(getMethodContainerTransactions());
        toStringBuffer.append("\n environmentProperties ");
        if (environmentProperties != null) {
            printDescriptorSet(environmentProperties, toStringBuffer);
        }
        toStringBuffer.append("\n ejbReferences ");
        if (ejbReferences != null) {
            printDescriptorSet(ejbReferences, toStringBuffer);
        }
        toStringBuffer.append("\n resourceEnvReferences ");
        if (resourceEnvReferences != null) {
            printDescriptorSet(resourceEnvReferences, toStringBuffer);
        }
        toStringBuffer.append("\n messageDestReferences ");
        if (messageDestReferences != null) {
            printDescriptorSet(messageDestReferences, toStringBuffer);
        }
        toStringBuffer.append("\n resourceReferences ");
        if (resourceReferences != null) {
            printDescriptorSet(resourceReferences, toStringBuffer);
        }
        toStringBuffer.append("\n serviceReferences ");
        if (serviceReferences != null) {
            printDescriptorSet(serviceReferences, toStringBuffer);
        }
        toStringBuffer.append("\n roleReferences ");
        if (roleReferences != null) {
            printDescriptorSet(roleReferences, toStringBuffer);
        }
        for (Object element : this.getPermissionedMethodsByPermission().keySet()) {
            MethodPermission nextPermission = (MethodPermission) element;
            toStringBuffer.append("\n method-permission->method: ");
            nextPermission.print(toStringBuffer);
            toStringBuffer.append(" -> ").append(this.getPermissionedMethodsByPermission().get(nextPermission));
        }
    }

    private void printDescriptorSet(Set<? extends Descriptor> descSet, StringBuffer sbuf) {
        for (Descriptor obj : descSet) {
            obj.print(sbuf);
        }
    }

    /**
     * visit the descriptor and all sub descriptors with a DOL visitor implementation
     *
     * @param aVisitor a visitor to traverse the descriptors
     */
    @Override
    public void visit(DescriptorVisitor aVisitor) {
        if (aVisitor instanceof EjbVisitor) {
            visit((EjbVisitor) aVisitor);
        } else {
            super.visit(aVisitor);
        }
    }

    /**
     * visit the descriptor and all sub descriptors with a DOL visitor implementation
     *
     * @param aVisitor a visitor to traverse the descriptors
     */
    public void visit(EjbVisitor aVisitor) {
        aVisitor.accept(this);
    }

    /**
     * This method determines if all the mechanisms defined in the CSIV2 CompoundSecMechList structure require protected
     * invocations.
     */
    @Override
    public boolean allMechanismsRequireSSL() {
        if (iorConfigDescriptors == null || iorConfigDescriptors.isEmpty()) {
            return false;
        }
        for (EjbIORConfigurationDescriptor iorDesc : iorConfigDescriptors) {
            if (EjbIORConfigurationDescriptor.REQUIRED.equalsIgnoreCase(iorDesc.getConfidentiality())) {
                continue;
            } else if (EjbIORConfigurationDescriptor.REQUIRED.equalsIgnoreCase(iorDesc.getConfidentiality())) {
                continue;
            } else if (EjbIORConfigurationDescriptor.REQUIRED.equalsIgnoreCase(iorDesc.getEstablishTrustInTarget())) {
                continue;
            } else if (EjbIORConfigurationDescriptor.REQUIRED.equalsIgnoreCase(iorDesc.getEstablishTrustInClient())) {
                continue;
            }
            return false;
        }
        return true;
    }
}
