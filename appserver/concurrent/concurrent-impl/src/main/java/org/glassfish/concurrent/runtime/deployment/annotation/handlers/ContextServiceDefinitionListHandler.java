/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.glassfish.concurrent.runtime.deployment.annotation.handlers;

import static java.util.logging.Level.INFO;

import java.util.logging.Logger;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractResourceHandler;

import jakarta.enterprise.concurrent.ContextServiceDefinition;

/**
 * Handler for list of @ContextServiceDefinition annotations.
 *
 * @author Petr Aubrecht &lt;aubrecht@asoftware.cz&gt;
 */
@Service
@AnnotationHandlerFor(ContextServiceDefinition.List.class)
public class ContextServiceDefinitionListHandler extends AbstractResourceHandler {
    private static final Logger logger = Logger.getLogger(ContextServiceDefinitionListHandler.class.getName());

    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo annotationInfo, ResourceContainerContext[] resourceContainerContexts) throws AnnotationProcessorException {
        logger.log(INFO, "Entering ContextServiceDefinitionListHandler.processAnnotation");
        ContextServiceDefinition.List contextServiceListDefinition = (ContextServiceDefinition.List) annotationInfo.getAnnotation();

        ContextServiceDefinition[] definitions = contextServiceListDefinition.value();
        if (definitions != null) {
            for (ContextServiceDefinition definition : definitions) {
                ContextServiceDefinitionHandler handler = new ContextServiceDefinitionHandler();
                handler.processSingleAnnotation(definition, resourceContainerContexts);
            }
        }

        return getDefaultProcessedResult();
    }
}