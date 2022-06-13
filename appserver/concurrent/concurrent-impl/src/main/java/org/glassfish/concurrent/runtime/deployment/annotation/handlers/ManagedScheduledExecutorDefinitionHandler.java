
package org.glassfish.concurrent.runtime.deployment.annotation.handlers;

import static java.util.logging.Level.INFO;
import static org.glassfish.deployment.common.JavaEEResourceType.MANAGED_SCHEDULED_EXECUTOR_DEFINITION_DESCRIPTOR;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.config.support.TranslatedConfigView;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.deployment.ManagedScheduledExecutorDefinitionDescriptor;
import com.sun.enterprise.deployment.MetadataSource;
import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractResourceHandler;

import jakarta.enterprise.concurrent.ManagedScheduledExecutorDefinition;

@Service
@AnnotationHandlerFor(ManagedScheduledExecutorDefinition.class)
public class ManagedScheduledExecutorDefinitionHandler extends AbstractResourceHandler {

    private static final Logger logger = Logger.getLogger(ManagedScheduledExecutorDefinitionHandler.class.getName());

    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo annotationInfo, ResourceContainerContext[] resourceContainerContexts) throws AnnotationProcessorException {
        logger.log(INFO, "Entering ManagedScheduledExecutorDefinitionHandler.processAnnotation");

        ManagedScheduledExecutorDefinition managedScheduledExecutorDefinition =
                (ManagedScheduledExecutorDefinition) annotationInfo.getAnnotation();

        return processAnnotation(managedScheduledExecutorDefinition, resourceContainerContexts);
    }

    protected HandlerProcessingResult processAnnotation(ManagedScheduledExecutorDefinition managedScheduledExecutorDefinition, ResourceContainerContext[] contexts) {
        logger.log(INFO, "Registering ManagedScheduledExecutorDefinitionHandler from annotation config");

        for (ResourceContainerContext context : contexts) {
            Set<ResourceDescriptor> resourceDescriptors = context.getResourceDescriptors(MANAGED_SCHEDULED_EXECUTOR_DEFINITION_DESCRIPTOR);
            ManagedScheduledExecutorDefinitionDescriptor msedd = createDescriptor(managedScheduledExecutorDefinition);
            if (descriptorAlreadyPresent(resourceDescriptors, msedd)) {
                merge(resourceDescriptors, managedScheduledExecutorDefinition);
            } else {
                resourceDescriptors.add(msedd);
            }
        }
        return getDefaultProcessedResult();
    }

    public ManagedScheduledExecutorDefinitionDescriptor createDescriptor(ManagedScheduledExecutorDefinition managedScheduledExecutorDefinition) {
        ManagedScheduledExecutorDefinitionDescriptor msedd = new ManagedScheduledExecutorDefinitionDescriptor();
        msedd.setName(TranslatedConfigView.expandValue(managedScheduledExecutorDefinition.name()));
        msedd.setContext(TranslatedConfigView.expandValue(managedScheduledExecutorDefinition.context()));

        if (managedScheduledExecutorDefinition.hungTaskThreshold() < 0) {
            msedd.setHungTaskThreshold(0);
        } else {
            msedd.setHungTaskThreshold(managedScheduledExecutorDefinition.hungTaskThreshold());
        }

        if (managedScheduledExecutorDefinition.maxAsync() < 0) {
            msedd.setMaxAsync(Integer.MAX_VALUE);
        } else {
            msedd.setMaxAsync(managedScheduledExecutorDefinition.maxAsync());
        }

        msedd.setMetadataSource(MetadataSource.ANNOTATION);
        return msedd;
    }

    private boolean descriptorAlreadyPresent(Set<ResourceDescriptor> resourceDescriptors, ManagedScheduledExecutorDefinitionDescriptor msedd) {
        Optional<ResourceDescriptor> optResourceDescriptor = resourceDescriptors.stream().filter(d -> d.equals(msedd)).findAny();
        return optResourceDescriptor.isPresent();
    }

    private void merge(Set<ResourceDescriptor> resourceDescriptors, ManagedScheduledExecutorDefinition msed) {
        for (ResourceDescriptor resource : resourceDescriptors) {
            ManagedScheduledExecutorDefinitionDescriptor descriptor = (ManagedScheduledExecutorDefinitionDescriptor) resource;
            if (descriptor.getName().equals(msed.name())) {

                if (descriptor.getHungTaskThreshold() == -1 && msed.hungTaskThreshold() != -1) {
                    descriptor.setHungTaskThreshold(msed.hungTaskThreshold());
                }

                if (descriptor.getMaxAsync() == -1) {
                    descriptor.setMaxAsync(msed.maxAsync());
                }

                if (descriptor.getContext() == null && msed.context() != null && !msed.context().isBlank()) {
                    descriptor.setContext(TranslatedConfigView.expandValue(msed.context()));
                }
            }
        }
    }
}