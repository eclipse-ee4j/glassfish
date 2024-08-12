/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deployment.annotation.context.EjbContext;

import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;

import java.lang.annotation.Annotation;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.deployment.EjbDescriptor.BEAN_TRANSACTION_TYPE;
import static com.sun.enterprise.deployment.EjbDescriptor.CONTAINER_TRANSACTION_TYPE;

/**
 * This handler is responsible for handling the jakarta.ejb.TransactionManagement.
 *
 * @author Shing Wai Chan
 */
@Service
@AnnotationHandlerFor(TransactionManagement.class)
public class TransactionManagementHandler extends AbstractAttributeHandler {

    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo, EjbContext[] ejbContexts)
        throws AnnotationProcessorException {
        TransactionManagement tmAn = (TransactionManagement) ainfo.getAnnotation();
        String tmType = TransactionManagementType.CONTAINER.equals(tmAn.value())
            ? CONTAINER_TRANSACTION_TYPE
            : BEAN_TRANSACTION_TYPE;

        for (EjbContext ejbContext : ejbContexts) {
            EjbDescriptor ejbDesc = (EjbDescriptor) ejbContext.getDescriptor();
            // override by xml
            if (ejbDesc.getTransactionType() == null) {
                ejbDesc.setTransactionType(tmType);
            }
        }

        return getDefaultProcessedResult();
    }


    @Override
    public Class<? extends Annotation>[] getTypeDependencies() {
        return getEjbAnnotationTypes();
    }


    @Override
    protected boolean supportTypeInheritance() {
        return true;
    }
}
