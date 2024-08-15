/*
 * Copyright (c) 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.annotation.handlers;

import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.annotation.context.EjbBundleContext;
import com.sun.enterprise.deployment.annotation.context.EjbContext;
import com.sun.enterprise.deployment.annotation.context.EjbInterceptorContext;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.annotation.context.WebBundleContext;
import com.sun.enterprise.deployment.annotation.context.WebComponentContext;
import com.sun.enterprise.deployment.annotation.context.WebComponentsContext;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.Interceptors;

import java.lang.System.Logger;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

import org.glassfish.deployment.common.RootDeploymentDescriptor;

import static java.lang.System.Logger.Level.DEBUG;

// This class was extracted from several *DefinitionHandler classes where it was copy pasted.
public final class ResourceAnnotationControl {
    private static final Logger LOG = System.getLogger(ResourceAnnotationControl.class.getName());
    private final String annotationName;

    public ResourceAnnotationControl(Class<?> annotationClass) {
        this.annotationName = annotationClass.getSimpleName();
    }

    /**
     * To take care of the case where an ejb is provided in a .war and
     * annotation processor will process this class twice (once for ejb and
     * once for web-bundle-context, which is a bug).<br>
     * This method helps to overcome the issue, partially.<br>
     * Checks whether both the annotated class and the context are either ejb or web.
     *
     * @param annotatedClass annotated-class
     * @param ejbClass indicates whether the class is an ejb-class
     * @param warClass indicates whether the class is an web-class
     * @param context resource-container-context
     * @return boolean indicates whether the annotation can be processed.
     */
    public boolean canProcessAnnotation(Class<?> annotatedClass, boolean ejbClass, boolean warClass,
        ResourceContainerContext context) {
        LOG.log(DEBUG, "canProcessAnnotation(annotatedClass={0}, ejbClass={1}, warClass={2}, context.class={3})",
            annotatedClass, ejbClass, warClass, context.getClass());
        if (ejbClass) {
            if (!(context instanceof EjbBundleContext
                || context instanceof EjbContext
                || context instanceof EjbInterceptorContext)) {
                LOG.log(DEBUG, "Ignoring @{0} annotation processing as the class is an EJB class"
                    + " and context {1} is not one of supported contexts.", annotationName, context.getClass());
                return false;
            }
        } else if (context instanceof EjbBundleContext) {
            EjbBundleContext ejbContext = (EjbBundleContext) context;
            EjbBundleDescriptor ejbBundleDescriptor = ejbContext.getDescriptor();
            EjbDescriptor[] ejbDescriptors = ejbBundleDescriptor.getEjbByClassName(annotatedClass.getName());
            if (ejbDescriptors.length == 0) {
                LOG.log(DEBUG, "Ignoring @{0} annotation processing as the {1} is not an EJB class"
                    + " and the context is EJBContext", annotationName, annotatedClass);
                return false;
            }
        } else if (warClass) {
            if (!(context instanceof WebBundleContext
                || context instanceof WebComponentsContext
                || context instanceof WebComponentContext )) {
                LOG.log(DEBUG, "Ignoring @{0} annotation processing as the class is a Web class"
                    + " and context is not one of WebContext", annotationName);
                return false;
            }
        } else if (context instanceof WebBundleContext) {
            WebBundleContext webBundleContext = (WebBundleContext) context;
            WebBundleDescriptor webBundleDescriptor = webBundleContext.getDescriptor();
            Collection<RootDeploymentDescriptor> extDesc = webBundleDescriptor.getExtensionsDescriptors();
            for (RootDeploymentDescriptor desc : extDesc) {
                if (desc instanceof EjbBundleDescriptor) {
                    EjbBundleDescriptor ejbBundleDesc = (EjbBundleDescriptor) desc;
                    EjbDescriptor[] ejbDescs = ejbBundleDesc.getEjbByClassName(annotatedClass.getName());
                    if (ejbDescs.length > 0) {
                        LOG.log(DEBUG, "Ignoring @{0} annotation processing as the {1} is not a Web class"
                            + " and the context is WebContext", annotationName, annotatedClass);
                        return false;
                    } else if (ejbBundleDesc.getInterceptorByClassName(annotatedClass.getName()) != null) {
                        LOG.log(DEBUG, "Ignoring @{0} annotation processing as the {1} is not a Web class"
                            + " and the context is WebContext", annotationName, annotatedClass);
                        return false;
                    } else {
                        Method[] methods = annotatedClass.getDeclaredMethods();
                        for (Method method : methods) {
                            Annotation[] annotations = method.getAnnotations();
                            for (Annotation annotation : annotations) {
                                if (annotation.annotationType().equals(AroundInvoke.class)
                                    || annotation.annotationType().equals(AroundTimeout.class)
                                    || annotation.annotationType().equals(Interceptors.class)) {
                                    LOG.log(DEBUG, "Ignoring @{0} annotation processing as the {1} is not a Web class"
                                        + " or an interceptor and the context is WebContext",
                                        annotationName, annotatedClass);
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
