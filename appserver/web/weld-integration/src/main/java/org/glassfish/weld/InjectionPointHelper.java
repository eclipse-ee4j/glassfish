/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.weld;

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.InjectionCapable;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.WebBundleDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import javax.naming.NamingException;

import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.hk2.api.ServiceLocator;

import static java.util.Locale.ENGLISH;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT_ENV;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_GLOBAL;

public class InjectionPointHelper {

    private final ServiceLocator services;
    private final ComponentEnvManager compEnvManager;
    private final GlassfishNamingManager namingManager;

    public InjectionPointHelper(ServiceLocator serviceLocator) {
        services = serviceLocator;
        compEnvManager = services.getService(ComponentEnvManager.class);
        namingManager = services.getService(GlassfishNamingManager.class);
    }

    public Object resolveInjectionPoint(Member member, Application app) throws NamingException {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null.");
        }

        if (app == null) {
            throw new IllegalArgumentException("Application cannot be null.");
        }

        Field field = null;
        Method method = null;
        Annotation[] annotations;

        if (member instanceof Field) {
            field = (Field) member;
            annotations = field.getDeclaredAnnotations();
        } else if (member instanceof Method) {
            method = (Method) member;
            annotations = method.getDeclaredAnnotations();
        } else {
            throw new IllegalArgumentException("Member must be Field or Method");
        }

        Annotation envAnnotation = getEnvAnnotation(annotations);

        if (envAnnotation == null) {
            throw new IllegalArgumentException("No Jakarta EE env dependency annotation found on " + member);
        }

        String envAnnotationName = null;
        try {
            envAnnotationName = (String) envAnnotation.annotationType().getDeclaredMethod("name").invoke(envAnnotation);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid annotation : must have name() attribute " + envAnnotation.toString(), e);
        }

        String envDependencyName = envAnnotationName;
        Class<?> declaringClass = member.getDeclaringClass();

        if (envAnnotationName == null || envAnnotationName.equals("")) {
            if (field != null) {
                envDependencyName = declaringClass.getName() + "/" + field.getName();
            } else {
                envDependencyName = declaringClass.getName() + "/" + getInjectionMethodPropertyName(method);
            }
        }

        if (envAnnotationName != null && envAnnotationName.startsWith(JNDI_CTX_JAVA_GLOBAL)) {
            return namingManager.getInitialContext().lookup(envAnnotationName);
        }
        BundleDescriptor matchingBundle = null;

        for (BundleDescriptor bundle : app.getBundleDescriptors()) {

            if (bundle instanceof EjbBundleDescriptor || bundle instanceof WebBundleDescriptor) {

                JndiNameEnvironment jndiEnv = (JndiNameEnvironment) bundle;

                // TODO normalize for java:comp/env/ prefix
                for (InjectionCapable next : jndiEnv.getInjectableResourcesByClass(declaringClass.getName())) {
                    if (next.getComponentEnvName().toString().equals(envDependencyName)) {
                        matchingBundle = bundle;
                        break;
                    }
                }
            }

            if (matchingBundle != null) {
                break;
            }
        }

        if (matchingBundle == null) {
            throw new IllegalArgumentException(
                    "Cannot find matching env dependency for " + member + " in Application " + app.getAppName());
        }

        String componentId = compEnvManager.getComponentEnvId((JndiNameEnvironment) matchingBundle);
        SimpleJndiName lookupName = new SimpleJndiName(envDependencyName.startsWith(JNDI_CTX_JAVA) ? envDependencyName
            : JNDI_CTX_JAVA_COMPONENT_ENV + envDependencyName);
        return namingManager.lookup(componentId, lookupName);
    }

    private String getInjectionMethodPropertyName(Method method) {
        String methodName = method.getName();

        if (methodName.length() <= 3 || !methodName.startsWith("set")) {
            throw new IllegalArgumentException("Illegal env dependency setter name" + method.getName());
        }

        return methodName.substring(3, 4).toLowerCase(ENGLISH) + methodName.substring(4);
    }

    private Annotation getEnvAnnotation(Annotation[] annotations) {
        Annotation envAnnotation = null;

        for (Annotation next : annotations) {
            String className = next.annotationType().getName();
            if (className.equals("jakarta.ejb.EJB") ||
               className.equals("jakarta.annotation.Resource") ||
               className.equals("jakarta.persistence.PersistenceContext") ||
               className.equals("jakarta.persistence.PersistenceUnit") ||
               className.equals("jakarta.xml.ws.WebServiceRef")) {
                envAnnotation = next;
                break;
            }
        }

        return envAnnotation;

    }
}
