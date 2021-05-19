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
import java.lang.annotation.Annotation;

/**
 * This interface defines the result of an annotation processing
 * returned by an annotation handler. Annotation handlers are supposed
 * to handle ONE annotation at a time. However, this simplistic
 * approach does not always work as annotations on a element can have
 * interdependencies.
 *
 * This could be resolved trough sophisticated caching in the context
 * for instance but it is usually easier to notify the processor that
 * a particular annotation handler has processed more than one annotation
 * (which shouldn't be reprocessed subsequently...)
 *
 * @author Jerome Dochez
 */
public interface HandlerProcessingResult {

    /**
     * Returns a map of annotation types processed by this handler as keys
     * and a ResultType as a value to communicate the result of the annotation
     * processing for each processed annotation types.
     * @return the map of processed annoation types and result.
     */
    public Map<Class<? extends Annotation>,ResultType> processedAnnotations();

    /**
     * @return the overall result for this handler processing. Since handlers
     * can process more than one annotation, this overall result will contain
     * the most severe error the haandler encountered while processing the
     * annotations.
     */
     public ResultType getOverallResult();
}
