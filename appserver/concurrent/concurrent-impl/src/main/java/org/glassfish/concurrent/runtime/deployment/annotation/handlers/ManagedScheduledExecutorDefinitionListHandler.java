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

import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;

@Service
@AnnotationHandlerFor(ManagedScheduledExecutorDefinition.List.class)
public class ManagedScheduledExecutorDefinitionListHandler extends AbstractResourceHandler {

    public static final Logger logger = Logger.getLogger(ManagedScheduledExecutorDefinitionListHandler.class.getName());

    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo annotationInfo, ResourceContainerContext[] resourceContainerContexts) throws AnnotationProcessorException {
        logger.log(INFO, "Entering ManagedScheduledExecutorDefinitionListHandler.processAnnotation");

        ManagedScheduledExecutorDefinition.List managedScheduleExecutorDefinitionList =
            (ManagedScheduledExecutorDefinition.List) annotationInfo.getAnnotation();

        ManagedScheduledExecutorDefinition[] definitions = managedScheduleExecutorDefinitionList.value();
        if (definitions != null) {
            for (ManagedScheduledExecutorDefinition definition : definitions) {
                ManagedScheduledExecutorDefinitionHandler handler = new ManagedScheduledExecutorDefinitionHandler();
                handler.processAnnotation(definition, resourceContainerContexts);
            }
        }

        return getDefaultProcessedResult();
    }
}