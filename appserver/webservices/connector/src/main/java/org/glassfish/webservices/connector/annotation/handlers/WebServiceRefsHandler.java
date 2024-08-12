/*
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

package org.glassfish.webservices.connector.annotation.handlers;

import jakarta.xml.ws.WebServiceRef;
import jakarta.xml.ws.WebServiceRefs;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.jvnet.hk2.annotations.Service;

/**
 * This handler is responsible for handling the WebServiceRefs annotation
 *
 */
@Service
@AnnotationHandlerFor(WebServiceRefs.class)
public class WebServiceRefsHandler extends WebServiceRefHandler {

    public WebServiceRefsHandler() {
    }

    public HandlerProcessingResult processAnnotation(AnnotationInfo ainfo)
            throws AnnotationProcessorException {

        WebServiceRefs wsRefsAnnotation = (WebServiceRefs) ainfo.getAnnotation();

        WebServiceRef[] wsRefAnnotations = wsRefsAnnotation.value();
        List<HandlerProcessingResult> results = new ArrayList<HandlerProcessingResult>();

        for(WebServiceRef wsRef : wsRefAnnotations) {
            results.add(processAWsRef(ainfo, wsRef));
        }
        HandlerProcessingResult finalResult = null;
        for (HandlerProcessingResult result : results) {
            if (finalResult == null ||
                    (result.getOverallResult().compareTo(
                    finalResult.getOverallResult()) > 0)) {
                finalResult = result;
            }
        }
        return finalResult;
    }
}
