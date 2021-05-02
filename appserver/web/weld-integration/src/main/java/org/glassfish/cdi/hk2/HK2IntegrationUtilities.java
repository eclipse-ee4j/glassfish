/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.cdi.hk2;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashSet;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.InjecteeImpl;
import org.glassfish.hk2.utilities.NamedImpl;

import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Named;

/**
 * Integration utilities
 *
 * @author jwells
 *
 */
public class HK2IntegrationUtilities {
    private final static String APP_SL_NAME = "java:app/hk2/ServiceLocator";

    /**
     * This method returns the proper ApplicationServiceLocator to use for CDI integration
     *
     * @return The application service loctor (will not return null)
     * @throws AssertionError if no ServiceLocator can be found
     */
    public static ServiceLocator getApplicationServiceLocator() {
        try {
            return (ServiceLocator) new InitialContext().lookup(APP_SL_NAME);
        } catch (NamingException ne) {
            return null;
        }
    }

    private static Set<Annotation> getHK2Qualifiers(InjectionPoint injectionPoint) {
        Set<Annotation> setQualifiers = injectionPoint.getQualifiers();

        Set<Annotation> hk2Qualifiers = new HashSet<>();

        for (Annotation annotation : setQualifiers) {
            if (annotation.annotationType().equals(Default.class)) {
                continue;
            }

            if (annotation.annotationType().equals(Named.class)) {
                Named named = (Named) annotation;
                if ("".equals(named.value())) {
                    Annotated annotated = injectionPoint.getAnnotated();
                    if (annotated instanceof AnnotatedField) {
                        AnnotatedField<?> annotatedField = (AnnotatedField<?>) annotated;

                        annotation = new NamedImpl(annotatedField.getJavaMember().getName());
                    }

                }

            }

            hk2Qualifiers.add(annotation);
        }

        return hk2Qualifiers;
    }

    public static Injectee convertInjectionPointToInjectee(InjectionPoint injectionPoint) {
        InjecteeImpl injectee = new InjecteeImpl(injectionPoint.getType());

        injectee.setRequiredQualifiers(getHK2Qualifiers(injectionPoint));
        injectee.setParent((AnnotatedElement) injectionPoint.getMember()); // Also sets InjecteeClass

        Annotated annotated = injectionPoint.getAnnotated();
        if (annotated instanceof AnnotatedField) {
            injectee.setPosition(-1);
        } else {
            AnnotatedParameter<?> annotatedParameter = (AnnotatedParameter<?>) annotated;
            injectee.setPosition(annotatedParameter.getPosition());
        }

        return injectee;
    }

}
