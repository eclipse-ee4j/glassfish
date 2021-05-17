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

import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.jvnet.hk2.annotations.Service;

import jakarta.persistence.PersistenceUnit;
import jakarta.persistence.PersistenceUnits;
import java.util.ArrayList;
import java.util.List;

/**
 * This handler is responsible for handling the
 * jakarta.persistence.PersistenceUnits annotation.
 *
 */
@Service
@AnnotationHandlerFor(PersistenceUnits.class)
public class EntityManagerFactoryReferencesHandler
    extends EntityManagerFactoryReferenceHandler {

    public EntityManagerFactoryReferencesHandler() {
    }

    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            ResourceContainerContext[] rcContexts)
            throws AnnotationProcessorException {

        PersistenceUnits annotation = (PersistenceUnits) ainfo.getAnnotation();

        PersistenceUnit[] emfRefAnnotations = annotation.value();
        List<HandlerProcessingResult> results = new ArrayList<HandlerProcessingResult>();

        for(PersistenceUnit emfRef : emfRefAnnotations) {
            results.add(processEmfRef(ainfo, rcContexts, emfRef));
        }

        return getOverallProcessingResult(results);
    }

}
