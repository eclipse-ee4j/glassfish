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

package org.glassfish.apf;

import java.util.Map;
import java.lang.reflect.AnnotatedElement;

/**
 * This interface encapsulates all AnnotatedElements processing result by
 * the AnnotationProcessor tool. Each AnnotatedElement can have multiple
 * annotations, therefore the AnnotationProcessor tool result is defined
 * by the list of AnnotatedElements it processed and for each AnnotatedElement,
 * it defines each Annotation processed and its processing result.
 *
 * @author Jerome Dochez
 */
public interface ProcessingResult {

    /**
     * @return a Map indexed by all AnnotatedElements processed and each
     * AnnotatedElement HandlerProcessingResult as values.
     */
    public Map<AnnotatedElement,HandlerProcessingResult> getResults();

    /**
     * @return the overall processing result, which is usually the most
     * severe ResultType for all the annotated elements processing
     */
    public ResultType getOverallResult();
}
