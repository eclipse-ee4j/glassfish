package org.glassfish.concurrent.runtime.deployment.annotation.handlers;

import static java.util.logging.Level.INFO;
import static org.glassfish.deployment.common.JavaEEResourceType.MANAGED_THREADFACTORY_DEFINITION_DESCRIPTOR;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.config.support.TranslatedConfigView;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.deployment.ManagedThreadFactoryDefinitionDescriptor;
import com.sun.enterprise.deployment.MetadataSource;
import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractResourceHandler;

import jakarta.enterprise.concurrent.ManagedThreadFactoryDefinition;

@Service
@AnnotationHandlerFor(ManagedThreadFactoryDefinition.class)
public class ManagedThreadFactoryDefinitionHandler extends AbstractResourceHandler {

    private static final Logger logger = Logger.getLogger(ManagedThreadFactoryDefinitionHandler.class.getName());

    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo annotationInfo, ResourceContainerContext[] resourceContainerContexts) throws AnnotationProcessorException {
        logger.log(INFO, "Entering ManagedThreadFactoryDefinitionHandler.processAnnotation");

        ManagedThreadFactoryDefinition managedThreadFactoryDefinition = (ManagedThreadFactoryDefinition) annotationInfo.getAnnotation();

        return processAnnotation(managedThreadFactoryDefinition, resourceContainerContexts);
    }

    protected HandlerProcessingResult processAnnotation(ManagedThreadFactoryDefinition managedThreadFactoryDefinition, ResourceContainerContext[] contexts) {
        logger.log(INFO, "Registering ManagedThreadFactory from annotation config");

        for (ResourceContainerContext context : contexts) {
            Set<ResourceDescriptor> resourceDescriptors = context.getResourceDescriptors(MANAGED_THREADFACTORY_DEFINITION_DESCRIPTOR);
            ManagedThreadFactoryDefinitionDescriptor mtfdd = createDescriptor(managedThreadFactoryDefinition);
            if (descriptorAlreadyPresent(resourceDescriptors, mtfdd)) {
                merge(resourceDescriptors, managedThreadFactoryDefinition);
            } else {
                resourceDescriptors.add(mtfdd);
            }
        }
        return getDefaultProcessedResult();
    }

    public ManagedThreadFactoryDefinitionDescriptor createDescriptor(ManagedThreadFactoryDefinition managedThreadFactoryDefinition) {
        ManagedThreadFactoryDefinitionDescriptor mtfdd = new ManagedThreadFactoryDefinitionDescriptor();
        mtfdd.setMetadataSource(MetadataSource.ANNOTATION);
        mtfdd.setName(TranslatedConfigView.expandValue(managedThreadFactoryDefinition.name()));
        mtfdd.setContext(TranslatedConfigView.expandValue(managedThreadFactoryDefinition.context()));
        if (managedThreadFactoryDefinition.priority() <= 0) {
            mtfdd.setPriority(Thread.NORM_PRIORITY);
        } else {
            mtfdd.setPriority(managedThreadFactoryDefinition.priority());
        }

        return mtfdd;
    }

    private boolean descriptorAlreadyPresent(final Set<ResourceDescriptor> resourceDescriptors, final ManagedThreadFactoryDefinitionDescriptor mtfdd) {
        Optional<ResourceDescriptor> optResourceDescriptor = resourceDescriptors.stream().filter(d -> d.equals(mtfdd)).findAny();
        return optResourceDescriptor.isPresent();
    }

    private void merge(Set<ResourceDescriptor> resourceDescriptors, ManagedThreadFactoryDefinition mtfdd) {
        for (ResourceDescriptor resource : resourceDescriptors) {
            ManagedThreadFactoryDefinitionDescriptor descriptor = (ManagedThreadFactoryDefinitionDescriptor) resource;
            if (descriptor.getName().equals(mtfdd.name())) {

                if (descriptor.getPriority() == -1 && mtfdd.priority() != -1) {
                    descriptor.setPriority(mtfdd.priority());
                }

                if (descriptor.getContext() == null && mtfdd.context() != null && !mtfdd.context().isBlank()) {
                    descriptor.setContext(TranslatedConfigView.expandValue(mtfdd.context()));
                }
            }
        }
    }
}