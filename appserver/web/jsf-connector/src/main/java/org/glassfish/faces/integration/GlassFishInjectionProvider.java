/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.faces.integration;

import com.sun.enterprise.container.common.spi.CDIService;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.container.common.spi.util.InjectionException;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.InjectionInfo;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.web.Constants;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.spi.AnnotationScanner;
import com.sun.faces.spi.DiscoverableInjectionProvider;
import com.sun.faces.spi.HighAvailabilityEnabler;
import com.sun.faces.spi.InjectionProviderException;
import com.sun.faces.util.FacesLogger;

import jakarta.servlet.ServletContext;

import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.classmodel.reflect.AnnotationModel;
import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.hk2.classmodel.reflect.Types;

import static java.security.AccessController.doPrivileged;
import static java.util.logging.Level.FINE;
import static org.glassfish.api.invocation.ComponentInvocation.ComponentInvocationType.SERVLET_INVOCATION;

/**
 * <p>
 * This <code>InjectionProvider</code> is specific to the GlassFish/SJSAS 9.x PE/EE application servers.
 * </p>
 */
public class GlassFishInjectionProvider extends DiscoverableInjectionProvider implements AnnotationScanner, HighAvailabilityEnabler {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();
    private static final String SERVICELOCATOR_ATTRIBUTE = "org.glassfish.servlet.habitat";

    private ComponentEnvManager componentEnvManager;
    private InjectionManager injectionManager;
    private InvocationManager invocationManager;
    private CDIService cdiService;

    /**
     * <p>
     * Constructs a new <code>GlassFishInjectionProvider</code> instance.
     * </p>
     *
     * @param servletContext
     */
    public GlassFishInjectionProvider(ServletContext servletContext) {
        ServiceLocator defaultServices = (ServiceLocator) servletContext.getAttribute(SERVICELOCATOR_ATTRIBUTE);

        componentEnvManager = defaultServices.getService(ComponentEnvManager.class);
        invocationManager = defaultServices.getService(InvocationManager.class);
        injectionManager = defaultServices.getService(InjectionManager.class);
        cdiService = defaultServices.getService(CDIService.class);
    }

    @Override
    public Map<String, List<ScannedAnnotation>> getAnnotatedClassesInCurrentModule(ServletContext servletContext) throws InjectionProviderException {

        DeploymentContext dc = (DeploymentContext) servletContext.getAttribute(Constants.DEPLOYMENT_CONTEXT_ATTRIBUTE);
        Types types = dc.getTransientAppMetaData(Types.class.getName(), Types.class);
        Collection<Type> allTypes = types.getAllTypes();
        Collection<AnnotationModel> annotations = null;
        Map<String, List<ScannedAnnotation>> classesByAnnotation = new HashMap<String, List<ScannedAnnotation>>();
        List<ScannedAnnotation> classesWithThisAnnotation = null;
        for (final Type cur : allTypes) {
            annotations = cur.getAnnotations();
            ScannedAnnotation toAdd = null;
            for (AnnotationModel curAnnotation : annotations) {
                String curAnnotationName = curAnnotation.getType().getName();
                if (null == (classesWithThisAnnotation = classesByAnnotation.get(curAnnotationName))) {
                    classesWithThisAnnotation = new ArrayList<ScannedAnnotation>();
                    classesByAnnotation.put(curAnnotationName, classesWithThisAnnotation);
                }
                toAdd = new ScannedAnnotation() {

                    @Override
                    public boolean equals(Object obj) {
                        boolean result = false;
                        if (obj instanceof ScannedAnnotation) {
                            String otherName = ((ScannedAnnotation) obj).getFullyQualifiedClassName();
                            if (null != otherName) {
                                result = cur.getName().equals(otherName);
                            }
                        }

                        return result;
                    }

                    @Override
                    public int hashCode() {
                        String str = getFullyQualifiedClassName();
                        Collection<URI> obj = getDefiningURIs();
                        int result = str != null ? str.hashCode() : 0;
                        result = 31 * result + (obj != null ? obj.hashCode() : 0);
                        return result;
                    }

                    @Override
                    public String getFullyQualifiedClassName() {
                        return cur.getName();
                    }

                    @Override
                    public Collection<URI> getDefiningURIs() {
                        return cur.getDefiningURIs();
                    }

                };
                if (!classesWithThisAnnotation.contains(toAdd)) {
                    classesWithThisAnnotation.add(toAdd);
                }
            }
        }

        return classesByAnnotation;
    }

    /**
     * <p>
     * The implementation of this method must perform the following steps:
     * <ul>
     * <li>Inject the supported resources per the Servlet 2.5 specification into the provided object</li>
     * </ul>
     * </p>
     *
     * @param managedBean the target managed bean
     */
    @Override
    public void inject(Object managedBean) throws InjectionProviderException {
        try {
            injectionManager.injectInstance(managedBean, getNamingEnvironment(), false);

            if (cdiService.isCurrentModuleCDIEnabled()) {
                cdiService.injectManagedObject(managedBean, getBundle());

            }

        } catch (InjectionException ie) {
            throw new InjectionProviderException(ie);
        }
    }

    /**
     * <p>
     * The implemenation of this method must invoke any method marked with the <code>@PreDestroy</code> annotation (per the
     * Common Annotations Specification).
     *
     * @param managedBean the target managed bean
     */
    @Override
    public void invokePreDestroy(Object managedBean) throws InjectionProviderException {
        try {
            injectionManager.invokeInstancePreDestroy(managedBean);
        } catch (InjectionException ie) {
            throw new InjectionProviderException(ie);
        }
    }

    /**
     * <p>
     * The implemenation of this method must invoke any method marked with the <code>@PostConstruct</code> annotation (per
     * the Common Annotations Specification).
     *
     * @param managedBean the target managed bean
     *
     * @throws com.sun.faces.spi.InjectionProviderException if an error occurs when invoking the method annotated by the
     * <code>@PostConstruct</code> annotation
     */
    @Override
    public void invokePostConstruct(Object managedBean) throws InjectionProviderException {
        try {
            this.invokePostConstruct(managedBean, getNamingEnvironment());
        } catch (InjectionException ie) {
            throw new InjectionProviderException(ie);
        }

    }

    // --------------------------------------------------------- Private Methods

    /**
     * <p>
     * This is based off of code in <code>InjectionManagerImpl</code>.
     * </p>
     *
     * @return <code>JndiNameEnvironment</code>
     * @throws InjectionException if we're unable to obtain the <code>JndiNameEnvironment</code>
     */
    private JndiNameEnvironment getNamingEnvironment() throws InjectionException {
        ComponentInvocation currentInvocation = invocationManager.getCurrentInvocation();

        if (currentInvocation == null) {
            throw new InjectionException("null invocation context");
        }

        if (currentInvocation.getInvocationType() != SERVLET_INVOCATION) {
            throw new InjectionException("Wrong invocation type");
        }


        JndiNameEnvironment componentEnv = (JndiNameEnvironment) currentInvocation.jndiEnvironment;

        if (componentEnv != null) {
            return componentEnv;
        }

        throw new InjectionException("No descriptor registered for " + " current invocation : " + currentInvocation);
    }

    /**
     * <p>
     * This is based off of code in <code>InjectionManagerImpl</code>.
     * </p>
     *
     * @param instance managed bean instance
     * @param envDescriptor JNDI environment
     * @throws InjectionException if an error occurs
     */
    private void invokePostConstruct(Object instance, JndiNameEnvironment envDescriptor) throws InjectionException {
        LinkedList<Method> postConstructMethods = new LinkedList<Method>();

        Class<? extends Object> nextClass = instance.getClass();

        // Process each class in the inheritance hierarchy, starting with
        // the most derived class and ignoring java.lang.Object.
        while ((!Object.class.equals(nextClass)) && (nextClass != null)) {

            InjectionInfo injInfo = envDescriptor.getInjectionInfoByClass(nextClass);

            if (injInfo.getPostConstructMethodName() != null) {

                Method postConstructMethod = getPostConstructMethod(injInfo, nextClass);

                // Invoke the preDestroy methods starting from
                // the least-derived class downward.
                postConstructMethods.addFirst(postConstructMethod);
            }

            nextClass = nextClass.getSuperclass();
        }

        for (Method postConstructMethod : postConstructMethods) {
            invokeLifecycleMethod(postConstructMethod, instance);
        }

    }

    /**
     * <p>
     * This is based off of code in <code>InjectionManagerImpl</code>.
     * </p>
     *
     * @param injectionInfo InjectionInfo
     * @param resourceClass target class
     * @return a Method marked with the @PostConstruct annotation
     * @throws InjectionException if an error occurs
     */
    private Method getPostConstructMethod(InjectionInfo injectionInfo, Class<? extends Object> resourceClass) throws InjectionException {
        Method postConstructMethod = injectionInfo.getPostConstructMethod();

        if (postConstructMethod == null) {
            String postConstructMethodName = injectionInfo.getPostConstructMethodName();

            // Check for the method within the resourceClass only.
            // This does not include super-classses.
            for (Method next : resourceClass.getDeclaredMethods()) {
                // InjectionManager only handles injection into PostConstruct
                // methods with no arguments.
                if (next.getName().equals(postConstructMethodName) && (next.getParameterTypes().length == 0)) {
                    postConstructMethod = next;
                    injectionInfo.setPostConstructMethod(postConstructMethod);
                    break;
                }
            }
        }

        if (postConstructMethod == null) {
            throw new InjectionException("InjectionManager exception. PostConstruct method " + injectionInfo.getPostConstructMethodName()
                    + " could not be found in class " + injectionInfo.getClassName());
        }

        return postConstructMethod;
    }

    /**
     * <p>
     * This is based off of code in <code>InjectionManagerImpl</code>.
     * </p>
     *
     * @param lifecycleMethod the method to invoke
     * @param instance the instanced to invoke the method against
     * @throws InjectionException if an error occurs
     */
    private void invokeLifecycleMethod(final Method lifecycleMethod, final Object instance) throws InjectionException {
        try {

            if (LOGGER.isLoggable(FINE)) {
                LOGGER.fine("Calling lifecycle method " + lifecycleMethod + " on class " + lifecycleMethod.getDeclaringClass());
            }

            // Wrap actual value insertion in doPrivileged to
            // allow for private/protected field access.
            doPrivileged(new java.security.PrivilegedExceptionAction<Object>() {
                @Override
                public java.lang.Object run() throws Exception {
                    if (!lifecycleMethod.trySetAccessible()) {
                        throw new InaccessibleObjectException("Unable to make accessible: " + lifecycleMethod);
                    }
                    lifecycleMethod.invoke(instance);
                    return null;
                }
            });
        } catch (Exception t) {
            String msg = "Exception attempting invoke lifecycle " + " method " + lifecycleMethod;
            LOGGER.log(FINE, msg, t);

            InjectionException ie = new InjectionException(msg);
            Throwable cause = (t instanceof InvocationTargetException) ? t.getCause() : t;
            ie.initCause(cause);
            throw ie;

        }

        return;
    }

    private BundleDescriptor getBundle() {

        JndiNameEnvironment env = componentEnvManager.getCurrentJndiNameEnvironment();

        BundleDescriptor bundle = null;

        if (env instanceof BundleDescriptor) {
            bundle = (BundleDescriptor) env;
        }

        if (bundle == null) {
            throw new IllegalStateException("Invalid context for managed bean creation");
        }

        return bundle;
    }

    /**
     * Method to test with HA has been enabled. If so, then set the JSF context param
     * com.sun.faces.enableAgressiveSessionDirtying to true
     *
     * @param ctx
     */
    @Override
    public void enableHighAvailability(ServletContext ctx) {
        // look at the following values for the web app
        // 1> has <distributable /> in the web.xml
        // 2> Was deployed with --availabilityenabled --target <clustername>
        WebConfiguration config = WebConfiguration.getInstance(ctx);
        if (!config.isSet(WebConfiguration.BooleanWebContextInitParameter.EnableDistributable )) {
            Object isDistributableObj = ctx.getAttribute(Constants.IS_DISTRIBUTABLE_ATTRIBUTE);
            Object enableHAObj = ctx.getAttribute(Constants.ENABLE_HA_ATTRIBUTE);
            if (isDistributableObj instanceof Boolean && enableHAObj instanceof Boolean) {
                boolean isDistributable = ((Boolean) isDistributableObj).booleanValue();
                boolean enableHA = ((Boolean) enableHAObj).booleanValue();

                if (LOGGER.isLoggable(FINE)) {
                    LOGGER.log(FINE, "isDistributable = {0} enableHA = {1}", new Object[] { isDistributable, enableHA });
                }
                if (isDistributable && enableHA) {
                    LOGGER.fine("setting the EnableAgressiveSessionDirtying to true");
                    config.overrideContextInitParameter(WebConfiguration.BooleanWebContextInitParameter.EnableDistributable,
                            Boolean.TRUE);
                }
            }
        }
    }
}
