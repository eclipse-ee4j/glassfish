/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.annotation.context.EjbContext;
import com.sun.enterprise.deployment.annotation.handlers.PostProcessor;
import com.sun.enterprise.deployment.util.TypeUtil;

import jakarta.ejb.MessageDriven;
import jakarta.ejb.Singleton;
import jakarta.ejb.Stateful;
import jakarta.ejb.Stateless;
import jakarta.ejb.Timeout;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ejb.TransactionManagement;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Set;
import java.util.logging.Level;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.ejb.deployment.descriptor.ContainerTransaction;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible for handling the jakarta.ejb.TransactionAttribute.
 *
 * @author Shing Wai Chan
 */
@Service
@AnnotationHandlerFor(TransactionAttribute.class)
public class TransactionAttributeHandler extends AbstractAttributeHandler implements PostProcessor<EjbContext> {

    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo, EjbContext[] ejbContexts)
        throws AnnotationProcessorException {
        TransactionAttribute taAn = (TransactionAttribute) ainfo.getAnnotation();

        for (EjbContext ejbContext : ejbContexts) {
            EjbDescriptor ejbDesc = (EjbDescriptor) ejbContext.getDescriptor();
            ContainerTransaction containerTransaction = getContainerTransaction(taAn.value());

            if (ElementType.TYPE.equals(ainfo.getElementType())) {
                ejbContext.addPostProcessInfo(ainfo, this);
                continue;
            }

            Method annMethod = (Method) ainfo.getAnnotatedElement();
            Set<MethodDescriptor> txBusMethods = ejbDesc.getTxBusinessMethodDescriptors();
            for (MethodDescriptor md : txBusMethods) {
                Method method = md.getMethod(ejbDesc);
                if (TypeUtil.sameMethodSignature(method, annMethod) && ejbDesc.getContainerTransactionFor(md) == null) {
                    // override by xml
                    ejbDesc.setContainerTransactionFor(md, containerTransaction);
                }
            }

            if (!(ejbDesc instanceof EjbSessionDescriptor)) {
                continue;
            }
            EjbSessionDescriptor sd = (EjbSessionDescriptor) ejbDesc;
            if (!sd.isStateful() && !sd.isSingleton()) {
                continue;
            }

            ClassLoader loader = ejbDesc.getEjbBundleDescriptor().getClassLoader();
            Set<LifecycleCallbackDescriptor> lcds = ejbDesc.getLifecycleCallbackDescriptors();
            for (LifecycleCallbackDescriptor lcd : lcds) {
                if (!lcd.getLifecycleCallbackClass().equals(ejbDesc.getEjbClassName())
                    || !lcd.getLifecycleCallbackMethod().equals(annMethod.getName())) {
                    continue;
                }
                try {
                    Method m = lcd.getLifecycleCallbackMethodObject(loader);
                    MethodDescriptor md = new MethodDescriptor(m, MethodDescriptor.LIFECYCLE_CALLBACK);
                    if (!TypeUtil.sameMethodSignature(m, annMethod) || ejbDesc.getContainerTransactionFor(md) != null) {
                        continue;
                    }
                    // stateful lifecycle callback txn attr type EJB spec
                    if (sd.isStateful() && containerTransaction != null) {
                        String txAttr = containerTransaction.getTransactionAttribute();
                        if (txAttr != null && !txAttr.equals(ContainerTransaction.REQUIRES_NEW)
                            && !txAttr.equals(ContainerTransaction.NOT_SUPPORTED)) {
                            logger.log(Level.WARNING,
                                MessageFormat.format(
                                    "Stateful session bean {0} lifecycle callback method {1} has transaction "
                                        + "attribute {2} with container-managed transaction demarcation. "
                                        + "The transaction attribute should be either REQUIRES_NEW or NOT_SUPPORTED",
                                    sd.getName(), m.getName(), txAttr));
                        }
                    }
                    // override by xml
                    ejbDesc.setContainerTransactionFor(md, containerTransaction);
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "Found matching callback method " + ejbDesc.getEjbClassName() + "<>" + md
                            + " : " + containerTransaction);
                    }
                } catch (Exception e) {
                    logger.log(Level.FINE, "Transaction attribute for a lifecycle callback annotation processing error", e);
                }
            } // inner for
        } // outer for

        return getDefaultProcessedResult();
    }


    private ContainerTransaction getContainerTransaction(TransactionAttributeType taType) {
        switch(taType) {
            case MANDATORY:
                return new ContainerTransaction(
                    ContainerTransaction.MANDATORY,
                    ContainerTransaction.MANDATORY);
            case REQUIRED:
                return new ContainerTransaction(
                    ContainerTransaction.REQUIRED,
                    ContainerTransaction.REQUIRED);
            case REQUIRES_NEW:
                return new ContainerTransaction(
                    ContainerTransaction.REQUIRES_NEW,
                    ContainerTransaction.REQUIRES_NEW);
            case SUPPORTS:
                return new ContainerTransaction(
                    ContainerTransaction.SUPPORTS,
                    ContainerTransaction.SUPPORTS);
            case NOT_SUPPORTED:
                return new ContainerTransaction(
                    ContainerTransaction.NOT_SUPPORTED,
                    ContainerTransaction.NOT_SUPPORTED);
            default:
                return new ContainerTransaction(
                    ContainerTransaction.NEVER,
                    ContainerTransaction.NEVER);
        }
    }


    /**
     * @return an array of annotation types this annotation handler would
     * require to be processed (if present) before it processes it's own
     * annotation type.
     */
    @Override
    public Class<? extends Annotation>[] getTypeDependencies() {
        return new Class[] {
            MessageDriven.class, Stateful.class, Stateless.class, Singleton.class,
                Timeout.class, TransactionManagement.class};
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
    public void postProcessAnnotation(AnnotationInfo ainfo, EjbContext ejbContext) throws AnnotationProcessorException {
        EjbDescriptor ejbDesc = (EjbDescriptor) ejbContext.getDescriptor();
        TransactionAttribute taAn = (TransactionAttribute) ainfo.getAnnotation();
        ContainerTransaction containerTransaction = getContainerTransaction(taAn.value());
        Class<?> classAn = (Class<?>) ainfo.getAnnotatedElement();

        Set<MethodDescriptor> txBusMethods = ejbDesc.getTxBusinessMethodDescriptors();
        for (MethodDescriptor md : txBusMethods) {
            // override by xml
            if (classAn.equals(ejbContext.getDeclaringClass(md)) && ejbDesc.getContainerTransactionFor(md) == null) {
                ejbDesc.setContainerTransactionFor(md, containerTransaction);
            }
        }
    }
}
