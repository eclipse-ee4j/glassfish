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

package org.glassfish.apf.impl;

import java.util.Map;
import java.util.HashMap;
import java.lang.annotation.Annotation;

import org.glassfish.apf.ResultType;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.apf.AnnotationHandler;

/**
 *
 * @author dochez
 */
public class HandlerProcessingResultImpl implements HandlerProcessingResult {

    Map<Class<? extends Annotation>,ResultType> results;
    ResultType overallResult = ResultType.UNPROCESSED;

    /**
     * Creates a new instance of HandlerProcessingResultImpl
     */
    public HandlerProcessingResultImpl(Map<Class<? extends Annotation>, ResultType> results) {
        this.results = results;
    }

    public HandlerProcessingResultImpl() {
        results = new HashMap<Class<? extends Annotation>, ResultType>();
    }

    public static HandlerProcessingResultImpl getDefaultResult(Class<? extends Annotation> annotationType, ResultType result) {

        HandlerProcessingResultImpl impl = new HandlerProcessingResultImpl();
        impl.results.put(annotationType, result);
        impl.overallResult = result;
        return impl;
    }

    public Map<Class<? extends Annotation>,ResultType> processedAnnotations() {
        return results;
    }

    public void addResult(Class<? extends Annotation> annotationType, ResultType result) {
        if (result.compareTo(overallResult)>0) {
            overallResult = result;
        }
        results.put(annotationType, result);
    }

    public void addAll(HandlerProcessingResult result) {
         if (result == null) {
             return;
         }
         if (result.getOverallResult().compareTo(overallResult)>0) {
            overallResult = result.getOverallResult();
        }
        results.putAll(result.processedAnnotations());
    }

    public ResultType getOverallResult(){
        return overallResult;
    }

}
