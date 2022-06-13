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

import jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition;

@Service
@AnnotationHandlerFor(ManagedThreadFactoryDefinition.List.class)
public class ManagedThreadFactoryDefinitionListHandler extends AbstractResourceHandler {

    public static final Logger logger = Logger.getLogger(ManagedThreadFactoryDefinitionListHandler.class.getName());

    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo annotationInfo, ResourceContainerContext[] resourceContainerContexts) throws AnnotationProcessorException {
        logger.log(INFO, "Entering ManagedThreadFactoryDefinitionListHandler.processAnnotation");

        ManagedThreadFactoryDefinition.List managedThreadFactoryDList =
                (ManagedThreadFactoryDefinition.List) annotationInfo.getAnnotation();

        ManagedThreadFactoryDefinition[] definitions = managedThreadFactoryDList.value();
        if (definitions != null) {
            for (ManagedThreadFactoryDefinition definition : definitions) {
                ManagedThreadFactoryDefinitionHandler mtfdh = new ManagedThreadFactoryDefinitionHandler();
                mtfdh.processAnnotation(definition, resourceContainerContexts);
            }
        }

        return null;
    }
}