/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.PersistenceProperty;
import jakarta.persistence.SynchronizationType;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;

import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;

/**
 * This handler is responsible for handling the custom
 * qualifier annotation used in place of the
 * jakarta.persistence.PersistenceContext annotation.
 */
public class EntityManagerReferenceQualifiedHandler extends EntityManagerReferenceHandler {
    private Class<? extends Annotation> qualifierType;
    private PersistenceContext effectivePC;

    static public class EffectivePC extends AnnotationLiteral<PersistenceContext> implements PersistenceContext {
        String unitName;
        EffectivePC(String unitName) {
            this.unitName = unitName;
        }
        @Override
        public String name() {
            return "";
        }

        public String unitName() {
            return unitName;
        }

        @Override
        public PersistenceContextType type() {
            return PersistenceContextType.TRANSACTION;
        }

        @Override
        public SynchronizationType synchronization() {
            return SynchronizationType.SYNCHRONIZED;
        }

        public PersistenceProperty[] properties() {
            return new PersistenceProperty[0];
        }
    }
    public EntityManagerReferenceQualifiedHandler(Class<? extends Annotation> qualifierType, String unitName) {
        this.qualifierType = qualifierType;
        this.effectivePC = new EffectivePC(unitName);
    }

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return qualifierType;
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
    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo, ResourceContainerContext[] rcContexts)
        throws AnnotationProcessorException {
        log(Level.INFO, ainfo, "Processing "+ainfo.getAnnotation()+" annotation, effectivePC=" + effectivePC);
        return processEmRef(ainfo, rcContexts, effectivePC);
    }

    protected HandlerProcessingResult processEmRef(AnnotationInfo ainfo, ResourceContainerContext[] rcContexts) throws AnnotationProcessorException {
        EntityManagerReferenceDescriptor emRefs[] = null;

        if (ElementType.FIELD.equals(ainfo.getElementType())) {
            Field f = (Field) ainfo.getAnnotatedElement();
            String targetClassName = f.getDeclaringClass().getName();
            String logicalName = effectivePC.name();

            // applying with default
            if (logicalName.isEmpty()) {
                logicalName = targetClassName + "/" + f.getName();
            }

            emRefs = getEmReferenceDescriptors(logicalName, rcContexts);
            InjectionTarget target = new InjectionTarget();
            target.setFieldName(f.getName());
            target.setClassName(targetClassName);
            target.setMetadataSource(MetadataSource.ANNOTATION);
            for (EntityManagerReferenceDescriptor emRef : emRefs) {
                emRef.addInjectionTarget(target);
                if (emRef.getName().isEmpty()) {
                    // a new one
                    processNewEmRefAnnotation(emRef, logicalName, effectivePC);
                }
            }
        } else if (ElementType.METHOD.equals(ainfo.getElementType())) {
            Method m = (Method) ainfo.getAnnotatedElement();
            String targetClassName = m.getDeclaringClass().getName();
            String logicalName = effectivePC.name();
            if (logicalName.isEmpty()) {
                // Derive javabean property name.
                String propertyName = getInjectionMethodPropertyName(m, ainfo);

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
                if (emRef.getName().isEmpty()) {
                    // a new one
                    processNewEmRefAnnotation(emRef, logicalName, effectivePC);
                }
            }
        } else if (ElementType.TYPE.equals(ainfo.getElementType())) {
            // name() is required for TYPE-level usage
            String logicalName = effectivePC.name();

            if (logicalName.isEmpty()) {
                log(Level.SEVERE, ainfo,
                        I18N.getLocalString("enterprise.deployment.annotation.handlers.nonametypelevel",
                                "TYPE-Level annotation symbol on class must specify name."));
                return getDefaultFailedResult();
            }

            emRefs = getEmReferenceDescriptors(logicalName, rcContexts);
            for (EntityManagerReferenceDescriptor emRef : emRefs) {
                if (emRef.getName().isEmpty()) {
                    // a new one

                    processNewEmRefAnnotation(emRef, logicalName, effectivePC);

                }
            }
        }

        return getDefaultProcessedResult();
    }

}
