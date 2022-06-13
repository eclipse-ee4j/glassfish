package org.glassfish.concurrent.runtime.deployment.annotation.handlers;

import static org.glassfish.deployment.common.JavaEEResourceType.MANAGED_EXECUTOR_DEFINITION_DESCRIPTOR;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.config.support.TranslatedConfigView;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.deployment.ManagedExecutorDefinitionDescriptor;
import com.sun.enterprise.deployment.MetadataSource;
import com.sun.enterprise.deployment.ResourceDescriptor;
import com.sun.enterprise.deployment.annotation.context.ResourceContainerContext;
import com.sun.enterprise.deployment.annotation.handlers.AbstractResourceHandler;

import jakarta.enterprise.concurrent.ManagedExecutorDefinition;

@Service
@AnnotationHandlerFor(ManagedExecutorDefinition.class)
public class ManagedExecutorDefinitionHandler extends AbstractResourceHandler {

    private static final Logger logger = Logger.getLogger(ManagedExecutorDefinitionHandler.class.getName());

    @Override
    protected HandlerProcessingResult processAnnotation(AnnotationInfo annotationInfo, ResourceContainerContext[] resourceContainerContexts) throws AnnotationProcessorException {
        logger.log(Level.INFO, "Entering ManagedExecutorDefinitionHandler.processAnnotation");
        ManagedExecutorDefinition managedExecutorDefinition = (ManagedExecutorDefinition) annotationInfo.getAnnotation();
        return processAnnotation(managedExecutorDefinition, resourceContainerContexts);
    }

    protected HandlerProcessingResult processAnnotation(ManagedExecutorDefinition managedExecutorDefinition, ResourceContainerContext[] contexts) {
        logger.log(Level.INFO, "Registering ManagedExecutorService from annotation config");

        for (ResourceContainerContext context : contexts) {
            Set<ResourceDescriptor> resourceDescriptors = context.getResourceDescriptors(MANAGED_EXECUTOR_DEFINITION_DESCRIPTOR);
            ManagedExecutorDefinitionDescriptor medes = createDescriptor(managedExecutorDefinition);
            if (descriptorAlreadyPresent(resourceDescriptors, medes)) {
                merge(resourceDescriptors, managedExecutorDefinition);
            } else {
                resourceDescriptors.add(medes);
            }
        }

        return getDefaultProcessedResult();
    }

    public ManagedExecutorDefinitionDescriptor createDescriptor(ManagedExecutorDefinition managedExecutorDefinition) {
        ManagedExecutorDefinitionDescriptor medd = new ManagedExecutorDefinitionDescriptor();
        medd.setName(TranslatedConfigView.expandValue(managedExecutorDefinition.name()));
        medd.setContext(TranslatedConfigView.expandValue(managedExecutorDefinition.context()));

        if (managedExecutorDefinition.hungTaskThreshold() < 0) {
            medd.setHungAfterSeconds(0);
        } else {
            medd.setHungAfterSeconds(managedExecutorDefinition.hungTaskThreshold());
        }

        if (managedExecutorDefinition.maxAsync() < 0) {
            medd.setMaximumPoolSize(Integer.MAX_VALUE);
        } else {
            medd.setMaximumPoolSize(managedExecutorDefinition.maxAsync());
        }

        medd.setMetadataSource(MetadataSource.ANNOTATION);
        return medd;
    }

    private boolean descriptorAlreadyPresent(final Set<ResourceDescriptor> resourceDescriptors, final ManagedExecutorDefinitionDescriptor medd) {
        Optional<ResourceDescriptor> optResourceDescriptor = resourceDescriptors.stream().filter(d -> d.equals(medd)).findAny();
        return optResourceDescriptor.isPresent();
    }

    private void merge(Set<ResourceDescriptor> resourceDescriptors, ManagedExecutorDefinition med) {
        for (ResourceDescriptor resource : resourceDescriptors) {
            ManagedExecutorDefinitionDescriptor descriptor = (ManagedExecutorDefinitionDescriptor) resource;
            if (descriptor.getName().equals(med.name())) {

                if (descriptor.getHungAfterSeconds() == -1 && med.hungTaskThreshold() != -1) {
                    descriptor.setHungAfterSeconds(med.hungTaskThreshold());
                }

                if (descriptor.getMaximumPoolSize() == -1 && med.maxAsync() != -1) {
                    descriptor.setMaximumPoolSize(med.maxAsync());
                }

                if (descriptor.getContext() == null && med.context() != null && !med.context().isBlank()) {
                    descriptor.setContext(TranslatedConfigView.expandValue(med.context()));
                }
            }
        }
    }
}