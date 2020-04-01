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

package org.glassfish.ejb.deployment.annotation.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBs;

import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible for handling the javax.ejb.EJBs attribute
 *
 */
@Service
@AnnotationHandlerFor(EJBs.class)
public class EJBsHandler extends EJBHandler {
    
    public EJBsHandler() {
    }

    protected HandlerProcessingResult processAnnotation(AnnotationInfo ainfo,
            ResourceContainerContext[] rcContexts)
            throws AnnotationProcessorException {

        EJBs ejbsAnnotation = (EJBs) ainfo.getAnnotation();
        
        EJB[] ejbAnnotations = ejbsAnnotation.value();

        if(ejbAnnotations.length == 0) {
            String localizedMsg = localStrings.getLocalString(
                    "enterprise.deployment.annotation.handlers.emptyEJBs",
                    "No @EJB elements in @EJBs on " + ainfo.getAnnotatedElement(),
                    new Object[]{ejbsAnnotation, ainfo.getAnnotatedElement()});
            logger.log(Level.WARNING, localizedMsg);
        }

        List<HandlerProcessingResult> results = new ArrayList<HandlerProcessingResult>();

        for(EJB ejb : ejbAnnotations) {
            results.add(processEJB(ainfo, rcContexts, ejb));
        }

        return getOverallProcessingResult(results);
    }

}
