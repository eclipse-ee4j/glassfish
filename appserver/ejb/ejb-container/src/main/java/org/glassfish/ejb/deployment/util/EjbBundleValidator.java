/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package org.glassfish.ejb.deployment.util;

import com.sun.ejb.containers.EJBTimerSchedule;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.InjectionCapable;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.ResourceEnvReferenceDescriptor;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.RoleReference;
import com.sun.enterprise.deployment.ScheduledTimerDescriptor;
import com.sun.enterprise.deployment.ServiceReferenceDescriptor;
import com.sun.enterprise.deployment.WebService;
import com.sun.enterprise.deployment.types.EjbReference;
import com.sun.enterprise.deployment.types.MessageDestinationReferencer;
import com.sun.enterprise.deployment.util.ComponentValidator;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.deployment.util.EjbBundleVisitor;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.ejb.LogFacade;
import org.glassfish.ejb.deployment.descriptor.DummyEjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbMessageBeanDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;
import org.glassfish.ejb.deployment.descriptor.FieldDescriptor;
import org.glassfish.ejb.deployment.descriptor.InterceptorBindingDescriptor;
import org.glassfish.ejb.deployment.descriptor.PersistenceDescriptor;
import org.glassfish.ejb.deployment.descriptor.RelationshipDescriptor;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.deployment.AnnotationTypesProvider;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.security.common.Role;

import static com.sun.enterprise.deployment.MethodDescriptor.EJB_BEAN;
import static com.sun.enterprise.deployment.MethodDescriptor.EJB_LOCAL;

/**
 * This class validates a EJB Bundle descriptor once loaded from an .jar file
 *
 * @author Jerome Dochez
 */
public class EjbBundleValidator extends ComponentValidator implements EjbBundleVisitor, EjbVisitor {
    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(EjbBundleValidator.class);
    private static final Logger LOG  = LogFacade.getLogger();

    @LogMessageInfo(
        message = "Passivation-capable value of stateful session bean [{0}] is false, " +
                "it should not have any PrePassivate nor PostActivate configuration, " +
                "but you have configuration at [{1}].",
        level = "WARNING")
    private static final String REDUNDANT_PASSIVATION_CALLBACK_METADATA = "AS-EJB-00048";

    protected EjbBundleDescriptorImpl ejbBundleDescriptor;
    protected EjbDescriptor ejb;


    @Override
    public void accept(BundleDescriptor descriptor) {
        this.bundleDescriptor = descriptor;
        this.application = descriptor.getApplication();
        if (descriptor instanceof EjbBundleDescriptorImpl) {
            EjbBundleDescriptorImpl ejbBundle = (EjbBundleDescriptorImpl) descriptor;
            accept(ejbBundle);

            for (EjbDescriptor anEjb : ejbBundle.getEjbs()) {
                anEjb.visit(getSubDescriptorVisitor(anEjb));
            }
            if (ejbBundle.hasRelationships()) {
                for (Object element : ejbBundle.getRelationships()) {
                    RelationshipDescriptor rd = (RelationshipDescriptor) element;
                    accept(rd);
                }
            }
            for (WebService aWebService : ejbBundle.getWebServices().getWebServices()) {
                accept(aWebService);
            }

            // Ejb-jar level dependencies

            // Visit all injectables first.  In some cases, basic type
            // information has to be derived from target inject method or
            // inject field.
            for (InjectionCapable injectable : ejbBundle.getInjectableResources(ejbBundle)) {
                accept(injectable);
            }
        }
        super.accept(descriptor);
    }

    @Override
    public void accept(EjbBundleDescriptor bundleDesc) {
        this.application = bundleDesc.getApplication();
        EjbBundleDescriptorImpl bundle = (EjbBundleDescriptorImpl) bundleDesc;
        if (bundle.getEjbs().isEmpty()) {
            throw new IllegalArgumentException(I18N.getLocalString(
                "enterprise.deployment.util.no_ejb_in_ejb_jar",
                "Invalid ejb jar {0}: it contains zero ejb. A valid ejb jar requires at least one session/entity/message driven bean.",
                new Object[] {bundle.getModuleDescriptor().getArchiveUri()}));
        }

        if (!areResourceReferencesValid(bundle)) {
            throw new RuntimeException("Incorrectly resolved role references");
        }

        this.ejbBundleDescriptor = bundle;

        // Now that we have a classloader, we have to check for any
        // interceptor bindings that were specified in .xml to use
        // the syntax that refers to all overloaded methods with a
        // given name.
        handleOverloadedInterceptorMethodBindings(bundle);

        InterceptorBindingTranslator bindingTranslator = new InterceptorBindingTranslator(bundle);

        for (EjbDescriptor ejb0 : bundle.getEjbs()) {
            if (ejb0.isRemoteInterfacesSupported()
                && (ejb0.getRemoteClassName() == null || ejb0.getRemoteClassName().isBlank())) {
                throw new IllegalArgumentException(
                    I18N.getLocalString("enterprise.deployment.util.componentInterfaceMissing",
                        "{0} Component interface is missing in EJB [{1}]", "Remote", ejb0.getName()));
            }
            if (ejb0.isLocalInterfacesSupported()
                && (ejb0.getLocalClassName() == null || ejb0.getLocalClassName().isBlank())) {
                throw new IllegalArgumentException(
                    I18N.getLocalString("enterprise.deployment.util.componentInterfaceMissing",
                        "{0} Component interface is missing in EJB [{1}]", EJB_LOCAL, ejb0.getName()));
            }

            if (!EjbEntityDescriptor.TYPE.equals(ejb0.getType())) {
                ejb0.applyInterceptors(bindingTranslator);
            }
        }
    }


    /**
     * @return true if all ejb role references link to roles specified here.
     */
    private boolean areResourceReferencesValid(EjbBundleDescriptorImpl bundle) {
        // run through each of the ejb's role references, checking that the roles exist in bundle
        for (EjbDescriptor ejbDescriptor : bundle.getEjbs()) {
            for (RoleReference reference : ejbDescriptor.getRoleReferences()) {
                Role referredRole = reference.getRole();
                Set<Role> roles = bundle.getRoles();
                if (!referredRole.getName().isEmpty() && !roles.contains(referredRole)) {
                    LOG.log(Level.WARNING, "Bad role reference to {0}, roles: {1}", new Object[] {referredRole, roles});
                    return false;
                }
            }
        }
        return true;
    }


    private void handleOverloadedInterceptorMethodBindings(EjbBundleDescriptorImpl bundleDesc) {
        List<InterceptorBindingDescriptor> origBindings = bundleDesc.getInterceptorBindings();
        if (origBindings.isEmpty()) {
            return;
        }

        ClassLoader cl = bundleDesc.getClassLoader();
        List<InterceptorBindingDescriptor> newBindings = new ArrayList<>();
        for (InterceptorBindingDescriptor next : origBindings) {
            if (!next.getNeedsOverloadResolution()) {
                newBindings.add(next);
                continue;
            }
            MethodDescriptor overloadedMethodDesc = next.getBusinessMethod();
            String methodName = overloadedMethodDesc.getName();
            // For method-specific interceptors, there must be an ejb-name.
            String ejbName = next.getEjbName();
            EjbDescriptor ejbDesc = bundleDesc.getEjbByName(ejbName);
            Class<?> ejbClass = null;
            try {
                ejbClass = cl.loadClass(ejbDesc.getEjbClassName());
            } catch (Exception e) {
                throw new RuntimeException("Error loading ejb class " + ejbDesc.getEjbClassName(), e);
            }

            boolean isMethod = false;
            for (Method ejbClassMethod : ejbClass.getDeclaredMethods()) {
                if (ejbClassMethod.getName().equals(methodName)) {
                    isMethod = true;
                    InterceptorBindingDescriptor newInterceptorBinding = new InterceptorBindingDescriptor();
                    MethodDescriptor newMethodDesc = new MethodDescriptor(ejbClassMethod, EJB_BEAN);
                    newInterceptorBinding.setEjbName(ejbName);
                    newInterceptorBinding.setBusinessMethod(newMethodDesc);
                    for (String interceptorClass : next.getInterceptorClasses()) {
                        newInterceptorBinding.appendInterceptorClass(interceptorClass);
                    }
                    newInterceptorBinding.setIsTotalOrdering(next.getIsTotalOrdering());
                    newInterceptorBinding.setExcludeDefaultInterceptors(next.getExcludeDefaultInterceptors());
                    newInterceptorBinding.setExcludeClassInterceptors(next.getExcludeClassInterceptors());
                    newBindings.add(newInterceptorBinding);
                }

            }

            // We didn't find a method with this name in class methods,
            // check if it's a constructor
            if (!isMethod && methodName.equals(ejbClass.getSimpleName())) {
                // Constructor - will resolve via implicit comparison
                newBindings.add(next);
            }
        }
        bundleDesc.setInterceptorBindings(newBindings);
    }


    /**
     * Visits an ejb descriptor
     *
     * @param ejb descriptor
     */
    @Override
    public void accept(EjbDescriptor ejb) {
        // all the DummyEjbDescriptor which stored partial information from
        // xml should already be resolved to actual ejb descriptors.
        // if not, this means there is a referencing error in the user
        // application
        if (ejb instanceof DummyEjbDescriptor) {
            throw new IllegalArgumentException("Referencing error: this bundle has no bean of name: " + ejb.getName());
        }

        this.ejb =ejb;
        setDOLDefault(ejb);
        computeRuntimeDefault(ejb);
        checkDependsOn(ejb);

        validateConcurrencyMetadata(ejb);
        validateStatefulTimeout(ejb);
        validatePassivationConfiguration(ejb);

        try {

            ClassLoader cl = ejb.getEjbBundleDescriptor().getClassLoader();
            Class<?> ejbClass = cl.loadClass(ejb.getEjbClassName());

            if (Globals.getDefaultHabitat() == null) {
                return;
            }

            // Perform 2.x style TimedObject processing if the class
            // hasn't already been identified as a timed object.
            AnnotationTypesProvider provider = Globals.getDefaultHabitat().getService(AnnotationTypesProvider.class, "EJB");
            if (provider == null) {
                throw new IllegalStateException("Cannot find AnnotationTypesProvider named 'EJB'");
            }

            if (ejb.getEjbTimeoutMethod() == null
                && provider.getType("jakarta.ejb.TimedObject").isAssignableFrom(ejbClass)) {
                MethodDescriptor timedObjectMethod = new MethodDescriptor("ejbTimeout", "TimedObject timeout method",
                    new String[] {"jakarta.ejb.Timer"}, MethodDescriptor.TIMER_METHOD);
                ejb.setEjbTimeoutMethod(timedObjectMethod);

            } else if (ejb.getEjbTimeoutMethod() != null) {
                // If timeout-method was only processed from the descriptor,
                // we need to create a MethodDescriptor using the actual
                // Method object corresponding to the timeout method. The
                // timeout method can have any access type and be anywhere
                // in the bean class hierarchy.
                MethodDescriptor timeoutMethodDescOrig = ejb.getEjbTimeoutMethod();
                MethodDescriptor timeoutMethodDesc = processTimeoutMethod(ejb, timeoutMethodDescOrig, provider, ejbClass);
                ejb.setEjbTimeoutMethod(timeoutMethodDesc);
            }

            for (ScheduledTimerDescriptor sd : ejb.getScheduledTimerDescriptors()) {
                try {
                    // This method creates new schedule and attempts to calculate next timeout.
                    // The second part ensures that all values that are not verified up-front
                    // are also validated.
                    // It does not check that such timeout date is a valid date.
                    EJBTimerSchedule.isValid(sd);
                } catch (Exception e) {
                    throw new RuntimeException(ejb.getName() + ": Invalid schedule " + "defined on method "
                        + sd.getTimeoutMethod().getFormattedString() + ": " + e.getMessage());
                }

                MethodDescriptor timeoutMethodDescOrig = sd.getTimeoutMethod();
                MethodDescriptor timeoutMethodDesc = processTimeoutMethod(ejb, timeoutMethodDescOrig, provider, ejbClass);
                sd.setTimeoutMethod(timeoutMethodDesc);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error processing EjbDescriptor", e);
        }

        // Visit all injectables first.  In some cases, basic type information
        // has to be derived from target inject method or inject field.
        for (InjectionCapable injectable : ejb.getEjbBundleDescriptor().getInjectableResources(ejb)) {
            accept(injectable);
        }

        for (EjbReference aRef : ejb.getEjbReferenceDescriptors()) {
            accept(aRef);
        }

        for (ResourceReferenceDescriptor element : ejb.getResourceReferenceDescriptors()) {
            accept(element);
        }

        for (ResourceEnvReferenceDescriptor element : ejb.getResourceEnvReferenceDescriptors()) {
            accept(element);
        }

        for (MessageDestinationReferencer element : ejb.getMessageDestinationReferenceDescriptors()) {
            accept(element);
        }

        // If this is a message bean, it can be a message destination referencer as well.
        if (ejb.getType().equals(com.sun.enterprise.deployment.EjbMessageBeanDescriptor.TYPE)) {
            if (ejb instanceof MessageDestinationReferencer) {
                MessageDestinationReferencer msgDestReferencer = (MessageDestinationReferencer) ejb;
                if (msgDestReferencer.getMessageDestinationLinkName() != null) {
                    accept(msgDestReferencer);
                }
            }
        }

        Set<ServiceReferenceDescriptor> serviceRefs = ejb.getServiceReferenceDescriptors();
        for (ServiceReferenceDescriptor serviceRef : serviceRefs) {
            accept(serviceRef);
        }

        if (ejb instanceof EjbCMPEntityDescriptor) {
            EjbCMPEntityDescriptor cmp = (EjbCMPEntityDescriptor)ejb;
            PersistenceDescriptor persistenceDesc = cmp.getPersistenceDescriptor();
            for (FieldDescriptor element : persistenceDesc.getCMPFields()) {
                accept(element);
            }
        }
    }

    public void accept(WebService webService) {
    }


    private void validateConcurrencyMetadata(EjbDescriptor ejb) {

        if (ejb instanceof EjbSessionDescriptor) {
            EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor) ejb;
            List<EjbSessionDescriptor.AccessTimeoutHolder> accessTimeoutInfo = sessionDesc.getAccessTimeoutInfo();
            for (EjbSessionDescriptor.AccessTimeoutHolder accessTimeoutHolder : accessTimeoutInfo) {
                MethodDescriptor accessTimeoutMethodDesc = accessTimeoutHolder.method;
                Method accessTimeoutMethod = accessTimeoutMethodDesc.getMethod(ejb);
                if (accessTimeoutMethod == null) {
                    throw new RuntimeException("Invalid AccessTimeout method signature " + accessTimeoutMethodDesc
                        + " . Method could not be resolved to a bean class method for bean " + ejb.getName());
                }
            }

            for (MethodDescriptor lockMethodDesc : sessionDesc.getReadAndWriteLockMethods()) {
                Method readLockMethod = lockMethodDesc.getMethod(sessionDesc);
                if (readLockMethod == null) {
                    throw new RuntimeException("Invalid Lock method signature " + lockMethodDesc
                        + " . Method could not be resolved to a bean class method for bean " + ejb.getName());
                }
            }
        }
    }


    /**
     * Validates @StatefulTimeout or <stateful-timeout> values.
     * Any value less than -1 is invalid.
     */
    private void validateStatefulTimeout(EjbDescriptor ejb) {
        if (ejb instanceof EjbSessionDescriptor) {
            EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor) ejb;
            Long statefulTimeoutValue = sessionDesc.getStatefulTimeoutValue();
            if (statefulTimeoutValue != null && statefulTimeoutValue < -1) {
                throw new IllegalArgumentException(I18N.getLocalString(
                    "enterprise.deployment.invalid_stateful_timeout_value",
                    "Invalid value [{0}] for @StatefulTimeout or <stateful-timeout> element in EJB [{1}]."
                    + " Values less than -1 are not valid.",
                    new Object[] {statefulTimeoutValue, sessionDesc.getName()}));
            }
        }
    }


    /**
     * Check when passivation-capable of sfsb is false,
     * PrePassivate and PostActivate configurations are not recommended.
     */
    private void validatePassivationConfiguration(EjbDescriptor ejb) {
        if (ejb instanceof EjbSessionDescriptor) {
            EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor) ejb;
            if (!sessionDesc.isStateful() || sessionDesc.isPassivationCapable()) {
                return;
            }

            String callbackInfo = getAllPrePassivatePostActivateCallbackInfo(sessionDesc);
            if (callbackInfo.length() > 0) {
                LOG.log(Level.WARNING, REDUNDANT_PASSIVATION_CALLBACK_METADATA,
                    new Object[] {ejb.getName(), callbackInfo});
            }
        }
    }


    private String getAllPrePassivatePostActivateCallbackInfo(EjbSessionDescriptor sessionDesc) {
        List<LifecycleCallbackDescriptor> descriptors = new ArrayList<>();
        descriptors.addAll(sessionDesc.getPrePassivateDescriptors());
        descriptors.addAll(sessionDesc.getPostActivateDescriptors());
        for (EjbInterceptor interceptor : sessionDesc.getInterceptorClasses()) {
            descriptors.addAll(interceptor.getCallbackDescriptors(LifecycleCallbackDescriptor.CallbackType.PRE_PASSIVATE));
            descriptors.addAll(interceptor.getCallbackDescriptors(LifecycleCallbackDescriptor.CallbackType.POST_ACTIVATE));
        }

        StringBuilder result = new StringBuilder();
        for (LifecycleCallbackDescriptor each : descriptors) {
            result.append(each.getLifecycleCallbackClass());
            result.append(".");
            result.append(each.getLifecycleCallbackMethod());
            result.append(", ");
        }

        if (result.length() > 2) {
            return result.substring(0, result.length() - 2);
        }
        return result.toString();
    }


    private void checkDependsOn(EjbDescriptor ejb) {
        if (ejb instanceof EjbSessionDescriptor) {
            EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor) ejb;
            if (sessionDesc.hasDependsOn()) {
                if (!sessionDesc.isSingleton()) {
                    throw new RuntimeException("Illegal usage of DependsOn for EJB " + ejb.getName()
                        + ". DependsOn is only supported for Singleton beans");
                }
                String[] dependsOn = sessionDesc.getDependsOn();
                for (String next : dependsOn) {

                    // TODO support new EJB 3.1 syntax

                    boolean fullyQualified = next.contains("#");

                    EjbBundleDescriptor sessionEjbBundleDescriptor = sessionDesc.getEjbBundleDescriptor();
                    Application app = sessionEjbBundleDescriptor.getApplication();

                    if (fullyQualified) {

                        int indexOfHash = next.indexOf("#");
                        String ejbName = next.substring(indexOfHash + 1);
                        String relativeJarPath = next.substring(0, indexOfHash);

                        BundleDescriptor bundle = app.getRelativeBundle(sessionEjbBundleDescriptor, relativeJarPath);

                        if (bundle == null) {
                            throw new IllegalStateException(
                                "Invalid @DependOn value = " + next + " for Singleton " + sessionDesc.getName());
                        }

                        EjbBundleDescriptorImpl ejbBundle = bundle.getModuleType() == null
                            || !bundle.getModuleType().equals(DOLUtils.warType())
                            ? (EjbBundleDescriptorImpl) bundle
                            : bundle.getExtensionsDescriptors(EjbBundleDescriptorImpl.class).iterator().next();

                        if (!ejbBundle.hasEjbByName(ejbName)) {
                            throw new RuntimeException(
                                "Invalid DependsOn dependency '" + next + "' for EJB " + ejb.getName());
                        }

                    } else {

                        EjbBundleDescriptor bundle = ejb.getEjbBundleDescriptor();
                        if (!bundle.hasEjbByName(next) ) {
                            throw new RuntimeException("Invalid DependsOn dependency '" +
                               next + "' for EJB " + ejb.getName());
                        }
                    }
                }
            }
        }

    }


    @Override
    protected EjbBundleDescriptorImpl getEjbBundleDescriptor() {
        return ejbBundleDescriptor;
    }


    @Override
    protected EjbDescriptor getEjbDescriptor() {
        return this.ejb;
    }


    /**
     * @return the Application object if any
     */
    @Override
    protected Application getApplication() {
        return application;
    }


    /**
     * @return the bundleDescriptor we are validating
     */
    @Override
    protected BundleDescriptor getBundleDescriptor() {
        return ejbBundleDescriptor;
    }


    /**
     * Set default value for EjbDescriptor.
     */
    private void setDOLDefault(EjbDescriptor ejb) {
        if (ejb.getUsesCallerIdentity() == null) {
            if (ejb instanceof EjbMessageBeanDescriptor) {
                ejb.setUsesCallerIdentity(false);
            } else {
                ejb.setUsesCallerIdentity(true);
            }
        }
        // for ejb 3.0
        if (ejb.getTransactionType() == null) {
            ejb.setTransactionType(com.sun.enterprise.deployment.EjbDescriptor.CONTAINER_TRANSACTION_TYPE);
        }
        ejb.setUsesDefaultTransaction();
    }


    /**
     * Set runtime default value for EjbDescriptor.
     */
    private void computeRuntimeDefault(EjbDescriptor ejb) {

        String intfName = null;
        if (ejb.getJndiName() == null || ejb.getJndiName().isEmpty()) {
            if (ejb.isRemoteInterfacesSupported() && ejb.isRemoteBusinessInterfacesSupported()) {
                // can't use a default.
            } else if (ejb.isRemoteInterfacesSupported()) {
                // For 2.x view, use the Home as the basis for the default
                intfName = ejb.getHomeClassName();
            } else if (ejb.isRemoteBusinessInterfacesSupported()) {
                Set<String> classNames = ejb.getRemoteBusinessClassNames();
                if (classNames.size() == 1) {
                    intfName = classNames.iterator().next();
                }
            }
        }

        if (intfName != null) {
            ejb.setJndiName(SimpleJndiName.of(intfName));
        }

        if (Boolean.FALSE.equals(ejb.getUsesCallerIdentity())) {
            computeRunAsPrincipalDefault(ejb.getRunAsIdentity(), ejb.getApplication());
        }
    }


    private MethodDescriptor processTimeoutMethod(EjbDescriptor ejb, MethodDescriptor timeoutMethodDescOrig,
        AnnotationTypesProvider provider, Class<?> ejbClass) throws ClassNotFoundException {
        Method m = timeoutMethodDescOrig.getDeclaredMethod(ejb);
        if (m == null) {
            // In case deployment descriptor didn't specify "jakarta.ejb.Timer"
            // as the method-params, and we were not relying on it before,
            // check explicitly for a method with "jakarta.ejb.Timer" param type.
            Class<?>[] params = new Class[1];
            params[0] = provider.getType("jakarta.ejb.Timer");

            m = timeoutMethodDescOrig.getDeclaredMethod(ejb, params);
        }

        if (m == null) {
            throw new RuntimeException("Class " + ejbClass.getName() + " does not define timeout method "
                + timeoutMethodDescOrig.getFormattedString());
        }
        return new MethodDescriptor(m, MethodDescriptor.TIMER_METHOD);
    }
}
