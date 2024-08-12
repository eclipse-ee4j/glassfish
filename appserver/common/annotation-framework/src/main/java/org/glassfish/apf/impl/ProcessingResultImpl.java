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

import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.apf.ProcessingResult;
import org.glassfish.apf.ResultType;

/**
 * Implementation of the ProcessingResult interface
 *
 * @author Jerome Dochez
 */
public class ProcessingResultImpl implements ProcessingResult {

    Map<AnnotatedElement, HandlerProcessingResult> results;
    ResultType overallResult = ResultType.UNPROCESSED;

    /** Creates a new instance of ProcessingResultImpl */
    public ProcessingResultImpl() {
        results = new HashMap<AnnotatedElement, HandlerProcessingResult>();
    }

    public void add(ProcessingResult pr) {

        Map<AnnotatedElement, HandlerProcessingResult> results = pr.getResults();
        for (Map.Entry<AnnotatedElement, HandlerProcessingResult> element : results.entrySet()) {
            add(element.getKey(), element.getValue());
        }
    }

    public void add(AnnotatedElement element, HandlerProcessingResult elementResult) {

        if (elementResult.getOverallResult().compareTo(overallResult)>0) {
            overallResult = elementResult.getOverallResult();
        }
        if (results.containsKey(element)) {
            HandlerProcessingResultImpl previousResult = (HandlerProcessingResultImpl) results.get(element);
            previousResult.addAll(elementResult);
        } else {
            if (elementResult instanceof HandlerProcessingResultImpl) {
                results.put(element, elementResult);
            } else {
                HandlerProcessingResultImpl result = new HandlerProcessingResultImpl();
                result.addAll(elementResult);
                results.put(element, result);
            }
        }
    }

    public Map<AnnotatedElement,HandlerProcessingResult> getResults() {
        return results;
    }

    public ResultType getOverallResult(){
        return overallResult;
    }
}
