/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.apf.test.example;

import java.lang.annotation.Annotation;

import org.glassfish.apf.AnnotationHandler;
import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.jvnet.hk2.annotations.Service;

/**
 * @author jwells
 */
@Service
@AnnotationHandlerFor(ClassB.class)
public class HandlerForClassB implements AnnotationHandler {

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        throw new AssertionError("not called");
    }

    @Override
    public HandlerProcessingResult processAnnotation(AnnotationInfo element)
            throws AnnotationProcessorException {
        throw new AssertionError("not called");
    }

    @Override
    public Class<? extends Annotation>[] getTypeDependencies() {
        throw new AssertionError("not called");
    }

}
