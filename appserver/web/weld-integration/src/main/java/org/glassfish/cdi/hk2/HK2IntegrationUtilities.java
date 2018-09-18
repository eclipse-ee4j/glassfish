/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.InjecteeImpl;
import org.glassfish.hk2.utilities.NamedImpl;

/**
 * Integration utilities
 * 
 * @author jwells
 *
 */
public class HK2IntegrationUtilities {
    private final static String APP_SL_NAME = "java:app/hk2/ServiceLocator";
    
    /**
     * This method returns the proper ApplicationServiceLocator
     * to use for CDI integration
     * 
     * @return The application service loctor (will not return null)
     * @throws AssertionError if no ServiceLocator can be found
     */
    public static ServiceLocator getApplicationServiceLocator() {
        try {
            Context ic = new InitialContext();
            
            return (ServiceLocator) ic.lookup(APP_SL_NAME);
        }
        catch (NamingException ne) {
            return null;
        }
    }
    
    private static Set<Annotation> getHK2Qualifiers(InjectionPoint injectionPoint) {
        Set<Annotation> setQualifiers = injectionPoint.getQualifiers();
        
        Set<Annotation> retVal = new HashSet<Annotation>();
        
        for (Annotation anno : setQualifiers) {
            if (anno.annotationType().equals(Default.class)) continue;
            
            if (anno.annotationType().equals(Named.class)) {
                Named named = (Named) anno;
                if ("".equals(named.value())) {
                    Annotated annotated = injectionPoint.getAnnotated();
                    if (annotated instanceof AnnotatedField) {
                        AnnotatedField<?> annotatedField = (AnnotatedField<?>) annotated;
                        
                        Field field = annotatedField.getJavaMember();
                        anno = new NamedImpl(field.getName());
                    }
                    
                }
                
            }
            
            retVal.add(anno);
        }
        
        return retVal;
    }
    
    public static Injectee convertInjectionPointToInjectee(InjectionPoint injectionPoint) {
        InjecteeImpl retVal = new InjecteeImpl(injectionPoint.getType());
        
        retVal.setRequiredQualifiers(getHK2Qualifiers(injectionPoint));
        retVal.setParent((AnnotatedElement) injectionPoint.getMember());  // Also sets InjecteeClass
        
        Annotated annotated = injectionPoint.getAnnotated();
        if (annotated instanceof AnnotatedField) {
            retVal.setPosition(-1);
        }
        else {
            AnnotatedParameter<?> annotatedParameter = (AnnotatedParameter<?>) annotated;
            retVal.setPosition(annotatedParameter.getPosition());
        }
        
        return retVal;
    }

}
