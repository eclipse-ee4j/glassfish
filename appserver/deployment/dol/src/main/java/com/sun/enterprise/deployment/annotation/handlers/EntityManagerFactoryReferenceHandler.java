/*
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

package com.sun.enterprise.deployment.annotation.handlers;

import com.sun.enterprise.deployment.EntityManagerFactoryReferenceDescriptor;
import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.MetadataSource;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.jvnet.hk2.annotations.Service;

import jakarta.persistence.PersistenceUnit;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * This handler is responsible for handling the
 * jakarta.persistence.PersistenceUnit annotation.
 *
 */
@Service
@AnnotationHandlerFor(PersistenceUnit.class)
public class EntityManagerFactoryReferenceHandler
    extends AbstractResourceHandler {

    public EntityManagerFactoryReferenceHandler() {
    }

    /**
     * Process a particular annotation which type is the same as the
     * one returned by @see getAnnotationType(). All information
     * pertinent to the annotation and its context is encapsulated
     * in the passed AnnotationInfo instance.
     *
     * @param ainfo the annotation information
     * @param rcContexts an array of ResourceContainerContext
     * @param HandlerProcessingResult
     */
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            ResourceContainerContext[] rcContexts)
            throws AnnotationProcessorException {

        PersistenceUnit emfRefAn = (PersistenceUnit)ainfo.getAnnotation();
        return processEmfRef(ainfo, rcContexts, emfRefAn);
    }


    /**
     * Process a particular annotation which type is the same as the
     * one returned by @see getAnnotationType(). All information
     * pertinent to the annotation and its context is encapsulated
     * in the passed AnnotationInfo instance.
     *
     */
    protected HandlerProcessingResult processEmfRef(AnnotationInfo ainfo,
            ResourceContainerContext[] rcContexts, PersistenceUnit emfRefAn)
            throws AnnotationProcessorException {
        EntityManagerFactoryReferenceDescriptor emfRefs[] = null;

        if (ElementType.FIELD.equals(ainfo.getElementType())) {
            Field f = (Field)ainfo.getAnnotatedElement();
            String targetClassName = f.getDeclaringClass().getName();

            String logicalName = emfRefAn.name();

            // applying with default
            if (logicalName.equals("")) {
                logicalName = targetClassName + "/" + f.getName();
            }

            emfRefs = getEmfReferenceDescriptors(logicalName, rcContexts);

            InjectionTarget target = new InjectionTarget();
            target.setFieldName(f.getName());
            target.setClassName(targetClassName);
            target.setMetadataSource(MetadataSource.ANNOTATION);

            for (EntityManagerFactoryReferenceDescriptor emfRef : emfRefs) {
                emfRef.addInjectionTarget(target);

                if (emfRef.getName().length() == 0) { // a new one
                    processNewEmfRefAnnotation(emfRef, logicalName, emfRefAn);
                }
            }
        } else if (ElementType.METHOD.equals(ainfo.getElementType())) {

            Method m = (Method)ainfo.getAnnotatedElement();
            String targetClassName = m.getDeclaringClass().getName();

            String logicalName = emfRefAn.name();
            if( logicalName.equals("") ) {
                // Derive javabean property name.
                String propertyName =
                    getInjectionMethodPropertyName(m, ainfo);

                // prefixing with fully qualified type name
                logicalName = targetClassName + "/" + propertyName;
            }

            validateInjectionMethod(m, ainfo);

            emfRefs = getEmfReferenceDescriptors(logicalName, rcContexts);

            InjectionTarget target = new InjectionTarget();
            target.setMethodName(m.getName());
            target.setClassName(targetClassName);
            target.setMetadataSource(MetadataSource.ANNOTATION);

            for (EntityManagerFactoryReferenceDescriptor emfRef : emfRefs) {

                emfRef.addInjectionTarget(target);

                if (emfRef.getName().length() == 0) { // a new one

                    processNewEmfRefAnnotation(emfRef, logicalName, emfRefAn);

                }
            }
        } else if( ElementType.TYPE.equals(ainfo.getElementType()) ) {
            // name() is required for TYPE-level usage
            String logicalName = emfRefAn.name();

            if( "".equals(logicalName) ) {
                log(Level.SEVERE, ainfo,
                    localStrings.getLocalString(
                    "enterprise.deployment.annotation.handlers.nonametypelevel",
                    "TYPE-Level annotation symbol on class must specify name."));
                return getDefaultFailedResult();
            }

            emfRefs = getEmfReferenceDescriptors(logicalName, rcContexts);
            for (EntityManagerFactoryReferenceDescriptor emfRef : emfRefs) {
                if (emfRef.getName().length() == 0) { // a new one

                    processNewEmfRefAnnotation(emfRef, logicalName, emfRefAn);

                }
            }
        }

        return getDefaultProcessedResult();
    }

    /**
     * Return EntityManagerFactoryReferenceDescriptors with given name
     * if exists or a new one without name being set.
     */
    private EntityManagerFactoryReferenceDescriptor[]
        getEmfReferenceDescriptors(String logicalName,
                                   ResourceContainerContext[] rcContexts) {

        EntityManagerFactoryReferenceDescriptor emfRefs[] =
                new EntityManagerFactoryReferenceDescriptor[rcContexts.length];
        for (int i = 0; i < rcContexts.length; i++) {
            EntityManagerFactoryReferenceDescriptor emfRef =
                (EntityManagerFactoryReferenceDescriptor)rcContexts[i].
                    getEntityManagerFactoryReference(logicalName);
            if (emfRef == null) {
                emfRef = new EntityManagerFactoryReferenceDescriptor();
                rcContexts[i].addEntityManagerFactoryReferenceDescriptor
                    (emfRef);
            }
            emfRefs[i] = emfRef;
        }

        return emfRefs;
    }

    private void processNewEmfRefAnnotation
        (EntityManagerFactoryReferenceDescriptor emfRef,
         String logicalName, PersistenceUnit annotation) {

        emfRef.setName(logicalName);

        if( !(annotation.unitName().equals("")) ) {
            emfRef.setUnitName(annotation.unitName());
        }

    }

}
