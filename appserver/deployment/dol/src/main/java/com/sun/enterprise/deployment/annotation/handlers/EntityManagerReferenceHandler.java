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

import com.sun.enterprise.deployment.EntityManagerReferenceDescriptor;
import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.deployment.MetadataSource;
import com.sun.enterprise.deployment.annotation.context.AppClientContext;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import org.glassfish.apf.*;
import org.jvnet.hk2.annotations.Service;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceProperty;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Level;

/**
 * This handler is responsible for handling the
 * jakarta.persistence.PersistenceUnit annotation.
 *
 */
@Service
@AnnotationHandlerFor(PersistenceContext.class)
public class EntityManagerReferenceHandler
    extends AbstractResourceHandler {

    public EntityManagerReferenceHandler() {
    }

    /**
     * Process a particular annotation which type is the same as the
     * one returned by @see getAnnotationType(). All information
     * pertinent to the annotation and its context is encapsulated
     * in the passed AnnotationInfo instance.
     *
     * @param ainfo the annotation information
     * @param rcContexts an array of ResourceContainerContext
     * @return HandlerProcessingResult
     */
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            ResourceContainerContext[] rcContexts)
            throws AnnotationProcessorException {

        AnnotatedElementHandler aeHandler =
            ainfo.getProcessingContext().getHandler();
        if (aeHandler instanceof AppClientContext) {
            // application client does not support @PersistenceContext
            String msg = localStrings.getLocalString(
                "enterprise.deployment.annotation.handlers.invalidaehandler",
                "Invalid annotation symbol found for this type of class.");
            log(Level.WARNING, ainfo, msg);
            return getDefaultProcessedResult();
        }
        PersistenceContext emRefAn = (PersistenceContext)ainfo.getAnnotation();
        return processEmRef(ainfo, rcContexts, emRefAn);
    }


    /**
     * Process a particular annotation which type is the same as the
     * one returned by @see getAnnotationType(). All information
     * pertinent to the annotation and its context is encapsulated
     * in the passed AnnotationInfo instance.
     *
     */
    protected HandlerProcessingResult processEmRef(AnnotationInfo ainfo,
            ResourceContainerContext[] rcContexts, PersistenceContext emRefAn)
            throws AnnotationProcessorException {
        EntityManagerReferenceDescriptor emRefs[] = null;

        if (ElementType.FIELD.equals(ainfo.getElementType())) {
            Field f = (Field)ainfo.getAnnotatedElement();
            String targetClassName = f.getDeclaringClass().getName();

            String logicalName = emRefAn.name();

            // applying with default
            if (logicalName.equals("")) {
                logicalName = targetClassName + "/" + f.getName();
            }

            emRefs = getEmReferenceDescriptors(logicalName, rcContexts);

            InjectionTarget target = new InjectionTarget();
            target.setFieldName(f.getName());
            target.setClassName(targetClassName);
            target.setMetadataSource(MetadataSource.ANNOTATION);

            for (EntityManagerReferenceDescriptor emRef : emRefs) {

                emRef.addInjectionTarget(target);

                if (emRef.getName().length() == 0) { // a new one
                    processNewEmRefAnnotation(emRef, logicalName, emRefAn);
                }
            }
        } else if (ElementType.METHOD.equals(ainfo.getElementType())) {

            Method m = (Method)ainfo.getAnnotatedElement();
            String targetClassName = m.getDeclaringClass().getName();

            String logicalName = emRefAn.name();
            if( logicalName.equals("") ) {
                // Derive javabean property name.
                String propertyName =
                    getInjectionMethodPropertyName(m, ainfo);

                // prefixing with fully qualified type name
                logicalName = targetClassName + "/" + propertyName;
            }

            validateInjectionMethod(m, ainfo);

            emRefs = getEmReferenceDescriptors(logicalName, rcContexts);

            InjectionTarget target = new InjectionTarget();
            target.setMethodName(m.getName());
            target.setClassName(targetClassName);
            target.setMetadataSource(MetadataSource.ANNOTATION);

            for (EntityManagerReferenceDescriptor emRef : emRefs) {

                emRef.addInjectionTarget(target);

                if (emRef.getName().length() == 0) { // a new one

                    processNewEmRefAnnotation(emRef, logicalName, emRefAn);

                }
            }
        } else if( ElementType.TYPE.equals(ainfo.getElementType()) ) {
            // name() is required for TYPE-level usage
            String logicalName = emRefAn.name();

            if( "".equals(logicalName) ) {
                log(Level.SEVERE, ainfo,
                    localStrings.getLocalString(
                    "enterprise.deployment.annotation.handlers.nonametypelevel",
                    "TYPE-Level annotation symbol on class must specify name."));
                return getDefaultFailedResult();
            }

            emRefs = getEmReferenceDescriptors(logicalName, rcContexts);
            for (EntityManagerReferenceDescriptor emRef : emRefs) {
                if (emRef.getName().length() == 0) { // a new one

                    processNewEmRefAnnotation(emRef, logicalName, emRefAn);

                }
            }
        }

        return getDefaultProcessedResult();
    }

    /**
     * Return EntityManagerReferenceDescriptors with given name
     * if exists or a new one without name being set.
     */
    private EntityManagerReferenceDescriptor[]
        getEmReferenceDescriptors(String logicalName,
                                   ResourceContainerContext[] rcContexts) {

        EntityManagerReferenceDescriptor emRefs[] =
                new EntityManagerReferenceDescriptor[rcContexts.length];
        for (int i = 0; i < rcContexts.length; i++) {
            EntityManagerReferenceDescriptor emRef =
                (EntityManagerReferenceDescriptor)rcContexts[i].
                    getEntityManagerReference(logicalName);
            if (emRef == null) {
                emRef = new EntityManagerReferenceDescriptor();
                rcContexts[i].addEntityManagerReferenceDescriptor
                    (emRef);
            }
            emRefs[i] = emRef;
        }

        return emRefs;
    }

    private void processNewEmRefAnnotation
        (EntityManagerReferenceDescriptor emRef,
         String logicalName, PersistenceContext annotation) {

        emRef.setName(logicalName);

        if( !(annotation.unitName().equals("")) ) {
            emRef.setUnitName(annotation.unitName());
        }

        emRef.setPersistenceContextType(annotation.type());
        emRef.setSynchronizationType(annotation.synchronization());

        // Add each property from annotation to descriptor, unless
        // it has been overridden within the .xml.
        Map existingProperties = emRef.getProperties();

        for(PersistenceProperty next : annotation.properties()) {
            if( !existingProperties.containsKey(next.name()) ) {
                emRef.addProperty(next.name(), next.value());
            }
        }

    }

}
