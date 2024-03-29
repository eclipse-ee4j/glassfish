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

package org.glassfish.ejb.deployment.annotation.handlers;

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.annotation.context.EjbContext;
import com.sun.enterprise.deployment.annotation.handlers.PostProcessor;
import com.sun.enterprise.deployment.util.TypeUtil;

import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible for handling the jakarta.ejb.Lock.
 *
 * @author Mahesh Kannan
 * @author Marina Vatkina
 */
@Service
@AnnotationHandlerFor(Lock.class)
public class LockHandler extends AbstractAttributeHandler implements PostProcessor<EjbContext> {

    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo, EjbContext[] ejbContexts)
        throws AnnotationProcessorException {
        Lock lockAnn = (Lock) ainfo.getAnnotation();

        for (EjbContext ejbContext : ejbContexts) {
            if (ejbContext.getDescriptor() instanceof EjbSessionDescriptor) {
                EjbSessionDescriptor singletonDesc = (EjbSessionDescriptor) ejbContext.getDescriptor();

                if (!singletonDesc.isSingleton()) {
                    throw new AnnotationProcessorException("@Lock is only permitted for singleton session beans");
                }

                if (ElementType.TYPE.equals(ainfo.getElementType())) {
                    // Delay processing Class-level default until after methods are processed
                    ejbContext.addPostProcessInfo(ainfo, this);
                } else {
                    Method annMethod = (Method) ainfo.getAnnotatedElement();

                    // Only assign lock type if the method hasn't already been processed.
                    // This correctly ignores overridden superclass methods and
                    // applies the correct .xml overriding semantics.
                    if (!matchesExistingReadOrWriteLockMethod(annMethod, singletonDesc)) {
                        MethodDescriptor newMethodDesc = new MethodDescriptor(annMethod);
                        if (lockAnn.value() == LockType.WRITE) {
                            singletonDesc.addWriteLockMethod(newMethodDesc);
                        } else {
                            singletonDesc.addReadLockMethod(newMethodDesc);
                        }
                    }
                }
            }
        }

        return getDefaultProcessedResult();
    }


    /**
     * @return an array of annotation types this annotation handler would
     *         require to be processed (if present) before it processes it's own
     *         annotation type.
     */
    @Override
    public Class<? extends Annotation>[] getTypeDependencies() {
        return new Class[]{Singleton.class, ConcurrencyManagement.class};
    }

    @Override
    protected boolean supportTypeInheritance() {
        return true;
    }

    /**
     * Set the default value (from class type annotation) on all
     * methods that don't have a value.
     */
    @Override
    public void postProcessAnnotation(AnnotationInfo ainfo, EjbContext ejbContext)
            throws AnnotationProcessorException {

        EjbSessionDescriptor ejbDesc = (EjbSessionDescriptor) ejbContext.getDescriptor();
        Class<?> classAn = (Class<?>) ainfo.getAnnotatedElement();
        Lock lockAnn = (Lock) ainfo.getAnnotation();

        // At this point, all method-level specific annotations have been processed.
        // For non-private methods, find the ones from the EjbContext's
        // component definition view that are declared on this class. This will correctly
        // eliminate any overridden methods and provide the most-derived version of each.
        // Use the Class's declared methods list to get the private methods.

        List<Method> toProcess = new ArrayList<>();
        for (Method m : ejbContext.getComponentDefinitionMethods()) {
            if (classAn.equals(m.getDeclaringClass())) {
                toProcess.add(m);
            }
        }
        for (Method m : classAn.getDeclaredMethods()) {
            if (Modifier.isPrivate(m.getModifiers())) {
                toProcess.add(m);
            }
        }

        for (Method m : toProcess) {

            // If the method is declared on the same class as the TYPE-level default
            // and it hasn't already been assigned lock information from the deployment
            // descriptor, set it.
            if (!matchesExistingReadOrWriteLockMethod(m, ejbDesc)) {
                MethodDescriptor newMethodDesc = new MethodDescriptor(m);
                if (lockAnn.value() == LockType.WRITE) {
                    ejbDesc.addWriteLockMethod(newMethodDesc);
                } else {
                    ejbDesc.addReadLockMethod(newMethodDesc);
                }
            }

        }
    }


    private boolean matchesExistingReadOrWriteLockMethod(Method methodToMatch, EjbSessionDescriptor desc) {
        List<MethodDescriptor> lockMethods = desc.getReadAndWriteLockMethods();
        boolean match = false;
        for (MethodDescriptor next : lockMethods) {
            Method m = next.getMethod(desc);
            if (m.getDeclaringClass().equals(methodToMatch.getDeclaringClass())
                && TypeUtil.sameMethodSignature(m, methodToMatch)) {
                match = true;
                break;
            }
        }

        return match;
    }

}
