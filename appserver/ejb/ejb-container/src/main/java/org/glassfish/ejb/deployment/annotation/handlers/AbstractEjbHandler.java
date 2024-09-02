/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.deployment.annotation.handlers;

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.annotation.context.EjbBundleContext;
import com.sun.enterprise.deployment.annotation.context.EjbContext;
import com.sun.enterprise.deployment.annotation.context.EjbsContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractHandler;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.Local;
import jakarta.ejb.LocalBean;
import jakarta.ejb.LocalHome;
import jakarta.ejb.Remote;
import jakarta.ejb.RemoteHome;
import jakarta.ejb.TimedObject;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.glassfish.apf.AnnotatedElementHandler;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.apf.context.AnnotationContext;
import org.glassfish.ejb.deployment.descriptor.DummyEjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.deployment.AnnotationTypesProvider;

/**
 * This is an abstract class for EJB annotation handler.
 * Concrete subclass handlers need to implements the following methods:
 *     public Class&lt;? extends Annotation&gt; getAnnotationType();
 *     protected String getAnnotatedName(Annotation annotation );
 *     protected boolean isValidEjbDescriptor(EjbDescriptor ejbDesc);
 *         Annotation annotation);
 *     protected EjbDescriptor createEjbDescriptor(String elementName,
 *         AnnotationInfo ainfo) throws AnnotationProcessorException;
 *     protected HandlerProcessingResult setEjbDescriptorInfo(
 *         EjbDescriptor ejbDesc, AnnotationInfo ainfo)
 *         throws AnnotationProcessorException;
 *
 * @author Shing Wai Chan
 */
public abstract class AbstractEjbHandler extends AbstractHandler {

    private AnnotationTypesProvider provider;

    protected final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(AbstractEjbHandler.class);

    public AbstractEjbHandler() {
        ServiceLocator h = Globals.getDefaultHabitat();
        if( h != null ) {
            provider = h.getService(AnnotationTypesProvider.class, "EJB");
        }
    }
    /**
     * Return the name attribute of given annotation.
     * @param annotation
     * @return name
     */
    protected abstract String getAnnotatedName(Annotation annotation);

    /**
     * check if the given EjbDescriptor matches the given Annotation.
     * @param ejbDesc
     * @param annotation
     * @return boolean check for validity of EjbDescriptor
     */
    protected abstract boolean isValidEjbDescriptor(EjbDescriptor ejbDesc, Annotation annotation);

    /**
     * Create a new EjbDescriptor for a given elementName and AnnotationInfo.
     * @param elementName
     * @param ainfo
     * @return a new EjbDescriptor
     */
    protected abstract EjbDescriptor createEjbDescriptor(String elementName,
            AnnotationInfo ainfo) throws AnnotationProcessorException;

    /**
     * Set Annotation information to Descriptor.
     * This method will also be invoked for an existing descriptor with
     * annotation as user may not specific a complete xml.
     * @param ejbDesc
     * @param ainfo
     * @return HandlerProcessingResult
     */
    protected abstract HandlerProcessingResult setEjbDescriptorInfo(
            EjbDescriptor ejbDesc, AnnotationInfo ainfo)
            throws AnnotationProcessorException;

    /**
     * Process a particular annotation which type is the same as the
     * one returned by @see getAnnotationType(). All information
     * pertinent to the annotation and its context is encapsulated
     * in the passed AnnotationInfo instance.
     * This is a method in interface AnnotationHandler.
     *
     * @param ainfo the annotation information
     */
    @Override
    public HandlerProcessingResult processAnnotation(AnnotationInfo ainfo)
            throws AnnotationProcessorException {



        Class<?> ejbClass = (Class<?>) ainfo.getAnnotatedElement();
        Annotation annotation = ainfo.getAnnotation();
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("@ process ejb annotation " +
                annotation + " in " + ejbClass);
        }
        AnnotatedElementHandler aeHandler = ainfo.getProcessingContext().getHandler();
        if (aeHandler != null && aeHandler instanceof EjbContext) {
            EjbContext context = (EjbContext) aeHandler;
            EjbDescriptor desc = (EjbDescriptor) context.getDescriptor();
            if (isValidEjbDescriptor(desc, annotation)) {
                return getDefaultProcessedResult();
            } else {
                log(Level.SEVERE, ainfo,
                    localStrings.getLocalString(
                    "enterprise.deployment.annotation.handlers.notcompsuperclass",
                    "The annotation symbol defined in super-class is not compatible with {0} ejb {1}.",
                    new Object[] { desc.getType(), desc.getName() }));
                return getDefaultFailedResult();
            }
        } else if (aeHandler == null || !(aeHandler instanceof EjbBundleContext)) {
            return getInvalidAnnotatedElementHandlerResult(
                ainfo.getProcessingContext().getHandler(), ainfo);
        }

        EjbBundleContext ctx = (EjbBundleContext) aeHandler;

        logger.log(Level.FINE, "My context is {0}", ctx);

        String elementName = getAnnotatedName(annotation);
        if (elementName.isEmpty()) {
            elementName = ejbClass.getSimpleName();
        }

        EjbBundleDescriptorImpl currentBundle = ctx.getDescriptor();
        EjbDescriptor ejbDesc = null;
        try {
            ejbDesc = currentBundle.getEjbByName(elementName);
        } catch(IllegalArgumentException ex) {
            //getEjbByName throws IllegalArgumentException when no ejb is found
        }

        if (ejbDesc != null && !(ejbDesc instanceof DummyEjbDescriptor)) {
            // element has already been defined in the standard DDs,
            // overriding rules applies
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Overriding rules apply for " + ejbClass.getName());
            }

            // don't allow ejb-jar.xml overwrite ejb type
            if (!isValidEjbDescriptor(ejbDesc, annotation)) {
                // this is an error
                log(Level.SEVERE, ainfo,
                    localStrings.getLocalString(
                    "enterprise.deployment.annotation.handlers.wrongejbtype",
                    "Wrong annotation symbol for ejb {0}",
                     ejbDesc ));
                return getDefaultFailedResult();
            }

            // <ejb-class> is optional if a component-defining
            // annotation is used.  If present, <ejb-class> element
            // must match the class on which the component defining annotation
            // appears.
            String descriptorEjbClass = ejbDesc.getEjbClassName();
            if (descriptorEjbClass == null) {
                ejbDesc.setEjbClassName(ejbClass.getName());
                ejbDesc.applyDefaultClassToLifecycleMethods();
            } else if( !descriptorEjbClass.equals(ejbClass.getName()) ) {
                log(Level.SEVERE, ainfo,
                    localStrings.getLocalString(
                    "enterprise.deployment.annotation.handlers.ejbclsmismatch",
                    "",
                    new Object[] { descriptorEjbClass, elementName, ejbClass.getName() }));
                return getDefaultFailedResult();
            }


        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Creating a new descriptor for " + ejbClass.getName());
            }

            EjbDescriptor dummyEjbDesc = ejbDesc;

            ejbDesc = createEjbDescriptor(elementName, ainfo);

            // create the actual ejb descriptor using annotation info and
            // the information from dummy ejb descriptor if applicable
            if (dummyEjbDesc != null) {
                currentBundle.removeEjb(dummyEjbDesc);
                ejbDesc.copyEjbDescriptor(dummyEjbDesc);
                // reset ejbClassName on ejbDesc
                ejbDesc.setEjbClassName(ejbClass.getName());
            }

            // add the actual ejb descriptor to the ejb bundle
            currentBundle.addEjb(ejbDesc);

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("New " + getAnnotationType().getName() + " bean " + elementName);
            }
        }

        // We need to include all ejbs of the same name in the annotation processing context
        // in order to handle the case that a bean class has both a component-defining
        // annotation and there are other ejb-jar.xml-defined beans with the same bean class.


        com.sun.enterprise.deployment.EjbDescriptor[] ejbDescs = currentBundle.getEjbByClassName(ejbClass.getName());
        HandlerProcessingResult procResult = null;
        for (com.sun.enterprise.deployment.EjbDescriptor ejb : ejbDescs) {
            procResult = setEjbDescriptorInfo((EjbDescriptor) ejb, ainfo);
            doTimedObjectProcessing(ejbClass, (EjbDescriptor) ejb);
        }

        final AnnotationContext annContext;
        if (ejbDescs.length == 1) {
            annContext = new EjbContext(ejbDesc, ejbClass);
        } else {
            annContext = new EjbsContext(ejbDescs, ejbClass);
        }

        // we push the new context on the stack...
        ctx.getProcessingContext().pushHandler(annContext);

        return procResult;
    }

    /**
     * Process TimedObject and @Timeout annotation.  It's better to do it
     * when processing the initial bean type since Timeout method is a
     * business method that should be included in any tx processing defaulting
     * that takes place.
     */
    private void doTimedObjectProcessing(Class ejbClass,
                                         EjbDescriptor ejbDesc) {

        // Timeout methods can be declared on the bean class or any
        // super-class and can be public, protected, private, or
        // package access.  There can be at most one timeout method for
        // the entire bean class hierarchy, so we start from the bean
        // class and go up, stopping when we find the first one.

        MethodDescriptor timeoutMethodDesc = null;
        Class nextClass = ejbClass;
        while((nextClass != Object.class) && (nextClass != null)
              && (timeoutMethodDesc == null) ) {
            Method[] methods = nextClass.getDeclaredMethods();
            for(Method m : methods) {
                if( (m.getAnnotation(Timeout.class) != null) ) {
                    timeoutMethodDesc =
                        new MethodDescriptor(m, MethodDescriptor.TIMER_METHOD);
                    break;
                }
            }
            nextClass = nextClass.getSuperclass();
        }

        if ((timeoutMethodDesc == null) && TimedObject.class.isAssignableFrom(ejbClass)) {
            // If the class implements the TimedObject interface, it must
            // be ejbTimeout.
            timeoutMethodDesc = new MethodDescriptor("ejbTimeout", "@Timeout method",
                new String[] {Timer.class.getName()}, MethodDescriptor.TIMER_METHOD);
        }

        if( timeoutMethodDesc != null ) {
            ejbDesc.setEjbTimeoutMethod(timeoutMethodDesc);
        }

        return;
    }

    /**
     * MessageDriven bean does not need to invoke this API.
     * @param ejbDesc
     * @param ainfo  for error handling
     * @return HandlerProcessingResult
     */
    protected HandlerProcessingResult setBusinessAndHomeInterfaces(
            EjbDescriptor ejbDesc, AnnotationInfo ainfo)
            throws AnnotationProcessorException {

        Set<Class>  localBusIntfs  = new HashSet<>();
        Set<Class>  remoteBusIntfs  = new HashSet<>();

        Set<Class> clientInterfaces = new HashSet<>();

        Class ejbClass = (Class)ainfo.getAnnotatedElement();

        // First check for annotations specifying remote/local business intfs.
        // We analyze them here because they are needed during the
        // implements clause processing for beans that specify
        // @Stateless/@Stateful.  In addition, they should *not* be processed
        // if there is no @Stateful/@Stateless annotation.

        Remote remoteBusAnn = (Remote) ejbClass.getAnnotation(Remote.class);
        boolean emptyRemoteBusAnn = false;
        boolean emptyLocalBusAnn = false;
        if( remoteBusAnn != null ) {
            for(Class next : remoteBusAnn.value()) {
                if (next.getAnnotation(Local.class) != null) {
                    AnnotationProcessorException fatalException =
                            new AnnotationProcessorException(localStrings.getLocalString(
                                    "enterprise.deployment.annotation.handlers.invalidbusinessinterface",
                                    "The interface {0} cannot be both a local and a remote business interface.",
                                    new Object[]{next.getName()}));
                    fatalException.setFatal(true);
                    throw fatalException;
                }
                clientInterfaces.add(next);
                remoteBusIntfs.add(next);
            }
            emptyRemoteBusAnn = remoteBusIntfs.isEmpty();
        }

        Local localBusAnn = (Local) ejbClass.getAnnotation(Local.class);
        if( localBusAnn != null ) {
            for(Class next : localBusAnn.value()) {
                if (next.getAnnotation(Remote.class) != null) {
                    AnnotationProcessorException fatalException =
                            new AnnotationProcessorException(localStrings.getLocalString(
                                    "enterprise.deployment.annotation.handlers.invalidbusinessinterface",
                                    "The interface {0} cannot be both a local and a remote business interface.",
                                    new Object[]{next.getName()}));
                    fatalException.setFatal(true);
                    throw fatalException;
                }
                clientInterfaces.add(next);
                localBusIntfs.add(next);
            }
            emptyLocalBusAnn = localBusIntfs.isEmpty();
        }

        List<Class> imlementingInterfaces = new ArrayList<>();
        List<Class> implementedDesignatedInterfaces = new ArrayList<>();
        for(Class next : ejbClass.getInterfaces()) {
            if( !excludedFromImplementsClause(next) ) {
                if( next.getAnnotation(Local.class) != null || next.getAnnotation(Remote.class) != null ) {
                    implementedDesignatedInterfaces.add(next);
                }
                imlementingInterfaces.add(next);
            }
        }

        LocalBean localBeanAnn = (LocalBean) ejbClass.getAnnotation(LocalBean.class);
        if( localBeanAnn != null ) {
            ejbDesc.setLocalBean(true);
        }

        // total number of local/remote business interfaces declared
        // outside of the implements clause plus implemented designated interfaces
        int designatedInterfaceCount =
            remoteBusIntfs.size() + localBusIntfs.size() +
            ejbDesc.getRemoteBusinessClassNames().size() +
            ejbDesc.getLocalBusinessClassNames().size() +
            implementedDesignatedInterfaces.size();

        for(Class next : imlementingInterfaces) {
            String nextIntfName = next.getName();

            if (remoteBusIntfs.contains(next) || localBusIntfs.contains(next)
                    || ejbDesc.getRemoteBusinessClassNames().contains(nextIntfName)
                    || ejbDesc.getLocalBusinessClassNames().contains(nextIntfName)) {
                // Interface has already been identified as a Remote/Local
                // business interface, so ignore.
                continue;
            }

            boolean isLocal = next.isAnnotationPresent(Local.class);
            boolean isRemote = next.isAnnotationPresent(Remote.class);

            if (isLocal || isRemote) {
                if (isLocal) {
                    localBusIntfs.add(next);
                }
                if (isRemote) {
                    remoteBusIntfs.add(next);
                }
                clientInterfaces.add(next);
                continue;
            }

            if (designatedInterfaceCount == 0 && !ejbDesc.isLocalBean()) {
                // If there's an empty @Remote annotation on the class
                // it's treated as a remote business interface.
                // If there's an empty @Local annotation on the class or if
                // the bean class is annotated with neither the @Local nor the
                // @Remote annotation, it's treated as a local business interface.
                if (emptyLocalBusAnn || emptyRemoteBusAnn) {
                    if (emptyRemoteBusAnn) {
                        remoteBusIntfs.add(next);
                    }
                    if (emptyLocalBusAnn) {
                        localBusIntfs.add(next);
                    }
                } else {
                    localBusIntfs.add(next);
                }
                clientInterfaces.add(next);
            }
        }

        for (Class next : clientInterfaces) {
            if (remoteBusIntfs.contains(next) && localBusIntfs.contains(next)) {
                AnnotationProcessorException fatalException =
                        new AnnotationProcessorException(localStrings.getLocalString(
                                "enterprise.deployment.annotation.handlers.invalidbusinessinterface",
                                "The interface {0} cannot be both a local and a remote business interface.",
                                new Object[]{next.getName()}));
                fatalException.setFatal(true);
                throw fatalException;
            }
        }

        if (localBusIntfs.size() > 0) {
            for(Class next : localBusIntfs) {
                ejbDesc.addLocalBusinessClassName(next.getName());
            }
        }

        if (remoteBusIntfs.size() > 0) {
            for(Class next : remoteBusIntfs) {
                ejbDesc.addRemoteBusinessClassName(next.getName());
            }
        }


        // Do Adapted @Home / Adapted @LocalHome processing here too since
        // they are logically part of the structural @Stateless/@Stateful info.
        RemoteHome remoteHomeAnn = (RemoteHome)
            ejbClass.getAnnotation(RemoteHome.class);

        if( remoteHomeAnn != null ) {
            Class remoteHome = remoteHomeAnn.value();
            Class remoteIntf = getComponentIntfFromHome(remoteHome);
            if( EJBHome.class.isAssignableFrom(remoteHome) &&
                (remoteIntf != null) ) {

                clientInterfaces.add(remoteHome);
                ejbDesc.setHomeClassName(remoteHome.getName());
                ejbDesc.setRemoteClassName(remoteIntf.getName());

            } else {
                log(Level.SEVERE, ainfo,
                    localStrings.getLocalString(
                    "enterprise.deployment.annotation.handlers.invalidremotehome",
                    "Encountered invalid @RemoteHome interface {0}.",
                    new Object[] { remoteHome }));
                return getDefaultFailedResult();
            }
        }

        LocalHome localHomeAnn = (LocalHome)
            ejbClass.getAnnotation(LocalHome.class);

        if( localHomeAnn != null ) {
            Class localHome = localHomeAnn.value();
            Class localIntf = getComponentIntfFromHome(localHome);
            if( EJBLocalHome.class.isAssignableFrom(localHome) &&
                (localIntf != null) ) {

                clientInterfaces.add(localHome);
                ejbDesc.setLocalHomeClassName(localHome.getName());
                ejbDesc.setLocalClassName(localIntf.getName());

            } else {
                log(Level.SEVERE, ainfo,
                    localStrings.getLocalString(
                    "enterprise.deployment.annotation.handlers.invalidlocalhome",
                    "Encountered invalid @LocalHome interface {0}.",
                    new Object[] { localHome }));
                return getDefaultFailedResult();
            }
        }

        // Web Service API might not be available so do a check before looking
        // for @WebService on bean class
        boolean canDoWebServiceAnnCheck = false;
        try {
            canDoWebServiceAnnCheck = (provider.getType("jakarta.jws.WebService") != null);

        } catch(Exception e) {
            log(Level.FINE, ainfo, e.getMessage());
        }

        if( (!ejbDesc.isLocalBean()) &&
            (clientInterfaces.size() == 0) &&
            !ejbDesc.hasWebServiceEndpointInterface() &&
            ( !canDoWebServiceAnnCheck ||
              (ejbClass.getAnnotation(jakarta.jws.WebService.class) == null) ) ) {
            ejbDesc.setLocalBean(true);
        }

        //If this is a no-Interface local EJB, set all classes for this bean
        if (ejbDesc.isLocalBean()) {
            addNoInterfaceLocalBeanClasses(ejbDesc, ejbClass);
        }


        return getDefaultProcessedResult();
    }

    private void addNoInterfaceLocalBeanClasses(EjbDescriptor ejbDesc, Class ejbClass) {
        Class nextClass = ejbClass;
        //The session bean's no-interface view can be accessed via the bean class
        //and all its super classes
        while ((nextClass != Object.class) && (nextClass != null)) {
            ejbDesc.addNoInterfaceLocalBeanClass(nextClass.getName());
            nextClass = nextClass.getSuperclass();
        }
    }
    private Class getComponentIntfFromHome(Class homeIntf) {

        Class componentIntf = null;

        Method[] methods = homeIntf.getMethods();
        for (Method m : methods) {
            if (m.getName().startsWith("create")) {
                componentIntf = m.getReturnType();
                break;
            }
        }

        return componentIntf;
    }

    protected boolean excludedFromImplementsClause(Class intf) {
        return ( (intf == java.io.Serializable.class) ||
                 (intf == java.io.Externalizable.class) ||
                 ( (intf.getPackage() != null) &&
                   intf.getPackage().getName().equals("jakarta.ejb")) );
    }


    protected void doDescriptionProcessing(String description, EjbDescriptor ejbDescriptor) {
        // Since there are multiple descriptions allowed in the deployment
        // descriptor, there are no overriding issues here.  If the
        // component-defining annotation contains a description, it will
        // always be added to the list of descriptions for the bean.
        if (description != null && !description.isEmpty()) {
            ejbDescriptor.setDescription(description);
        }
    }


    protected void doMappedNameProcessing(String mappedName, EjbDescriptor ejbDesc) {
        // Set mappedName() if a value has been given in the annotation and
        // it hasn't already been set on the descriptor via the .xml.
        if( ejbDesc.getMappedName().isEmpty() ) {
            if( !mappedName.isEmpty() ) {
                ejbDesc.setMappedName(mappedName);
            }
        }
    }
}
