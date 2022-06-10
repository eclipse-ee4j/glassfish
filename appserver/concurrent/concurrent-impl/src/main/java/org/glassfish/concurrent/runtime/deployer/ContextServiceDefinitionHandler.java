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
package org.glassfish.concurrent.runtime.deployer;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.concurrent.runtime.ConcurrentRuntime;
import org.glassfish.config.support.TranslatedConfigView;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.deployment.ContextServiceDefinitionDescriptor;
import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractResourceHandler;

import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.inject.Inject;

/**
 * Handler for @ContextServiceDefinition.
 *
 * @author Petr Aubrecht <aubrecht@asoftware.cz>
 */
@Service
@AnnotationHandlerFor(ContextServiceDefinition.class)
public class ContextServiceDefinitionHandler extends AbstractResourceHandler {

    private static final Logger logger = Logger.getLogger(ContextServiceDefinitionHandler.class.getName());

    @Inject
    private Domain domain;

    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo annotationInfo,
            ResourceContainerContext[] resourceContainerContexts)
            throws AnnotationProcessorException {
        logger.log(Level.INFO, "Entering ContextServiceDefinitionHandler.processAnnotation");
        ContextServiceDefinition contextServiceDefinition = (ContextServiceDefinition) annotationInfo.getAnnotation();

        processSingleAnnotation(contextServiceDefinition, resourceContainerContexts);

        return getDefaultProcessedResult();
    }

    public void processSingleAnnotation(ContextServiceDefinition contextServiceDefinition, ResourceContainerContext[] resourceContainerContexts) {
        //        AnnotatedElement annotatedElement = annotationInfo.getAnnotatedElement();
        //        logger.log(Level.INFO, "Trying to create custom context service by annotation");
//        String allContexts = Stream.of(ConcurrentRuntime.CONTEXT_INFO_CLASSLOADER,
//                ConcurrentRuntime.CONTEXT_INFO_JNDI, ConcurrentRuntime.CONTEXT_INFO_SECURITY,
//                ConcurrentRuntime.CONTEXT_INFO_WORKAREA).collect(Collectors.joining(", "));
        String allContexts = null;
        ContextServiceConfig contextServiceConfig = new ContextServiceConfig(contextServiceDefinition.name(),
                allContexts,
                "true");
        ConcurrentRuntime concurrentRuntime = ConcurrentRuntime.getRuntime();
        // create a context service
        //        ContextServiceImpl managedExecutorServiceImpl =
        concurrentRuntime.getContextService(null, contextServiceConfig);

        // add to contexts
        ContextServiceDefinitionDescriptor cdd = createDescriptor(contextServiceDefinition);
        for (ResourceContainerContext context : resourceContainerContexts) {
            Set<ResourceDescriptor> csddes = context.getResourceDescriptors(JavaEEResourceType.CONTEXT_SERVICE_DEFINITION_DESCRIPTOR);
            csddes.add(cdd);
        }
    }

    public ContextServiceDefinitionDescriptor createDescriptor(ContextServiceDefinition contectServiceDefinition) {
        ContextServiceDefinitionDescriptor csdd = new ContextServiceDefinitionDescriptor();
        csdd.setDescription("Context Service Definition");
        csdd.setName((String)TranslatedConfigView.getTranslatedValue(contectServiceDefinition.name()));
//        csdd.setPropagated(Stream.of(contectServiceDefinition.propagated()).map(x -> TranslatedConfigView.expandValue(x)).toArray(String[]::new));
//        csdd.setCleared(Stream.of(contectServiceDefinition.cleared()).map(x -> TranslatedConfigView.expandValue(x)).toArray(String[]::new));
//        csdd.setUnchanged(Stream.of(contectServiceDefinition.unchanged()).map(x -> TranslatedConfigView.expandValue(x)).toArray(String[]::new));
        return csdd;
    }
}