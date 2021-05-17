/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;
import jakarta.enterprise.util.AnnotationLiteral;

import test.fwk.FrameworkService;
import test.fwk.SomeFwkServiceImpl;
import test.fwk.SomeFwkServiceInterface;

/**
 * A portable extension that supports injection of custom framework
 * services into Beans.
 *
 * @author Sivakumar Thyagarajan
 */
public class ServiceFrameworkExtension implements Extension{

    public static boolean beforeBeanDiscoveryCalled = false;
    public static boolean afterBeanDiscoveryCalled = false;
    public static boolean afterProcessBeanCalled = false;
    public static boolean processAnnotatedTypeCalled = false;

    /*
     * A map of Framework Service Types to be injected and additional metadata
     * about the FrameworkService to be injected.
     */
    private HashMap<Type, Set<FrameworkService>> frameworkServicesToBeInjected
                                = new HashMap<Type, Set<FrameworkService>>();
    private static final boolean DEBUG_ENABLED = false;


    //Observers for container lifecycle events
    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery bdd){
        debug("beforeBeanDiscovery" + bdd);
        beforeBeanDiscoveryCalled = true;
    }

    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat){
        debug("Process annotated type" + pat.getAnnotatedType().getBaseType());
        processAnnotatedTypeCalled = true;
    }

    /**
     * Observer for <code>ProcessInjectionTarget</code> events. This event is
     * fired for every Java EE component class supporting injection that may be
     * instantiated by the container at runtime. Injections points of every
     * discovered enabled Java EE component is checked to see if there is a
     * request for injection of a framework service.
     */
    void afterProcessInjectionTarget(@Observes ProcessInjectionTarget<?> pb){
        debug("AfterProcessInjectionTarget" + pb.getAnnotatedType().getBaseType());
        Set<InjectionPoint> ips = pb.getInjectionTarget().getInjectionPoints();
        discoverServiceInjectionPoints(ips);
    }

    /**
     * Observer for <code>ProcessInjectionTarget</code> events. This event is
     * fired fire an event for each enabled bean, interceptor or decorator
     * deployed in a bean archive, before registering the Bean object.
     * Injections points of every discovered enabled Java EE component is
     * checked to see if there is a request for injection of a framework
     * service.
     */
    void afterProcessBean(@Observes ProcessBean pb){
        afterProcessBeanCalled = true;
        debug("afterProcessBean - " + pb.getAnnotated().getBaseType());
        Set<InjectionPoint> ips = pb.getBean().getInjectionPoints();
        discoverServiceInjectionPoints(ips);
    }

    /*
     * Discover injection points where the framework service is requested
     * through the <code>FrameworkService</code> qualifier and a map is
     * populated for all framework services that have been requested.
     */
    private void discoverServiceInjectionPoints(Set<InjectionPoint> ips) {
        for (Iterator<InjectionPoint> iterator = ips.iterator();
                                                    iterator.hasNext();) {
            InjectionPoint injectionPoint = iterator.next();
            Set<Annotation> qualifs = injectionPoint.getQualifiers();
            for (Iterator<Annotation> qualifIter = qualifs.iterator();
                                                    qualifIter.hasNext();) {
                Annotation annotation = qualifIter.next();
                if (annotation.annotationType().equals(FrameworkService.class)){
                    printDebugForInjectionPoint(injectionPoint);
                    //Keep track of service-type and its attributes
                    System.out.println("---- Injection requested for " +
                            "framework service type " + injectionPoint.getType()
                            + " and annotated with dynamic="
                            + injectionPoint.getAnnotated()
                                    .getAnnotation(FrameworkService.class)
                                    .dynamic()
                            + ", serviceCriteria="
                            + injectionPoint.getAnnotated()
                                    .getAnnotation(FrameworkService.class)
                                    .serviceCriteria());
                    //Add to list of framework services to be injected
                    Type key = injectionPoint.getType();
                    FrameworkService value = injectionPoint.getAnnotated()
                    .getAnnotation(FrameworkService.class);
                    if (!frameworkServicesToBeInjected.containsKey(key)){
                        frameworkServicesToBeInjected.put(key, new HashSet<FrameworkService>());
                    }
                    frameworkServicesToBeInjected.get(key).add(value);
                    System.out.println(frameworkServicesToBeInjected.size());

                }
            }
        }
    }

    /**
     * Observer for <code>AfterBeanDiscovery</code> events. This
     * observer method is used to register <code>Bean</code>s for the framework
     * services that have been requested to be injected.
     */
    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd){
        afterBeanDiscoveryCalled = true;
        debug("After Bean Discovery");
        for (Iterator<Type> iterator = this.frameworkServicesToBeInjected.keySet().iterator();
                                                iterator.hasNext();) {
            Type type =  iterator.next();
            //If the injection point's type is not a Class or Interface, we
            //don't know how to handle this.
            if (!(type instanceof Class)) {
                System.out.println("Unknown type:" + type);
                abd.addDefinitionError(new UnsupportedOperationException(
                        "Injection target type " + type + "not supported"));
                break; //abort deployment
            }
            //Add the Bean representing the framework service so that it
            //is available for injection
            addBean(abd, type, this.frameworkServicesToBeInjected.get(type));
        }
    }

    /*
     * Add a <code>Bean</code> for the framework service requested. Instantiate
     * or discover the bean from the framework service registry,
     * and return a reference to the service if a dynamic reference is requested.
     */
    private void addBean(AfterBeanDiscovery abd, final Type type,
            final Set<FrameworkService> frameworkServices) {
        for (Iterator<FrameworkService> iterator = frameworkServices.iterator(); iterator
                .hasNext();) {
            final FrameworkService frameworkService = iterator.next();
            System.out.println(" --- Adding a framework service BEAN " + type + " for " + frameworkService);
            abd.addBean(new FrameworkServiceBean(type, frameworkService));
        }
    }


    private final class FrameworkServiceBean implements Bean {
        private final Type type;
        private final FrameworkService frameworkService;

        private FrameworkServiceBean(Type type,
                FrameworkService frameworkService) {
            this.type = type;
            this.frameworkService = frameworkService;
        }

        @Override
        public Object create(CreationalContext arg0) {
            //get the service from the service registry
            return FrameworkServiceFactory.getService(type, frameworkService);
        }

        @Override
        public void destroy(Object instance,
                CreationalContext creationalContext) {
          System.out.println("destroy::" + instance);
          //unget the service reference
          FrameworkServiceFactory.ungetService(instance);
        }

        @Override
        public Class getBeanClass() {
            return (Class)type;
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
          return Collections.emptySet();
        }

        @Override
        public String getName() {
            return type + "_dynamic_" + frameworkService.dynamic()
                    + "_criteria_" + frameworkService.serviceCriteria()
                    + "_waitTimeout" + frameworkService.waitTimeout();
        }

        @Override
        public Set<Annotation> getQualifiers() {
            Set<Annotation> s = new HashSet<Annotation>();
            s.add(new AnnotationLiteral<Default>() {});
            s.add(new AnnotationLiteral<Any>() {});
            //Add the appropriate parameters to the FrameworkService qualifier
            //as requested in the injection point
            s.add(new FrameworkServiceQualifierType(frameworkService));
            return s;
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
            Set<Type> s = new HashSet<Type>();
            s.add(type);
            s.add(Object.class);
            return s;
        }

        @Override
        public boolean isAlternative() {
            return false;
        }

        @Override
        public boolean isNullable() {
            return false;
        }
    }

    /*
     * Represents an annotation type instance of FrameworkService
     * with parameters equal to those specified in the injection point
     */
    private final class FrameworkServiceQualifierType
    extends AnnotationLiteral<FrameworkService> implements FrameworkService {
        private String serviceCriteria = "";
        private boolean dynamic = false;
        private int waitTimeout = -1;

        public FrameworkServiceQualifierType(FrameworkService frameworkService){
            this.serviceCriteria = frameworkService.serviceCriteria();
            this.dynamic = frameworkService.dynamic();
            this.waitTimeout  = frameworkService.waitTimeout();
        }
        @Override
        public String serviceCriteria(){
            return this.serviceCriteria;
        }

        @Override
        public boolean dynamic() {
            return this.dynamic;
        }

        @Override
        public int waitTimeout() {
            return this.waitTimeout;
        }
    }

    private void debug(String string) {
        if(DEBUG_ENABLED)
            System.out.println("MyExtension:: " + string);

    }

    private void printDebugForInjectionPoint(InjectionPoint injectionPoint) {
        if (DEBUG_ENABLED) {
            System.out.println("@@@@@@@ INJECTION-POINT: Annotation:"
                    + injectionPoint.getAnnotated()); // annotatedfield
            System.out.print(" ,Bean:" + injectionPoint.getBean());// bean
            System.out.print(" ,Class:" + injectionPoint.getClass()); // r untime
                                                           // class?
            System.out.print(" ,Member:" + injectionPoint.getMember());// Field
            System.out.print(" ,Qualifiers:" + injectionPoint.getQualifiers());// qualifiers
            System.out.print(" ,Type:" + injectionPoint.getType());// type of
                                                         // injection
                                                         // point
        }
    }

}
