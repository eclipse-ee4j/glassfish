/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.annotation.context;

import com.sun.enterprise.deployment.ConnectorDescriptor;

import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;

import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.ProcessingContext;
import org.glassfish.apf.context.AnnotationContext;

public class RarBundleContext extends AnnotationContext {

    private final ConnectorDescriptor desc;

    public RarBundleContext(ConnectorDescriptor desc) {
        this.desc = desc;
    }


    public ConnectorDescriptor getDescriptor() {
        return desc;
    }


    @Override
    public void setProcessingContext(ProcessingContext processingContext) {
        super.setProcessingContext(processingContext);
    }


    @Override
    public ProcessingContext getProcessingContext() {
        return super.getProcessingContext();
    }


    @Override
    public void startElement(ElementType type, AnnotatedElement element) throws AnnotationProcessorException {
        getProcessingContext().pushHandler(this);
    }


    @Override
    public void endElement(ElementType type, AnnotatedElement element) throws AnnotationProcessorException {
        getProcessingContext().popHandler();
    }
}
