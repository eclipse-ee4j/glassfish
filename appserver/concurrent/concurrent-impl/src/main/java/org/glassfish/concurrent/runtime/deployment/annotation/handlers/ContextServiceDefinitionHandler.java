package org.glassfish.concurrent.runtime.deployment.annotation.handlers;

import static org.glassfish.deployment.common.JavaEEResourceType.CONTEXT_SERVICE_DEFINITION_DESCRIPTOR;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.apf.AnnotationHandlerFor;
import org.glassfish.apf.AnnotationInfo;
import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.apf.HandlerProcessingResult;
import org.glassfish.config.support.TranslatedConfigView;
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
 * @author Petr Aubrecht &lt;aubrecht@asoftware.cz&gt;
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
        logger.log(Level.INFO, "Creating custom context service by annotation");
        ContextServiceDefinitionDescriptor csdd = createDescriptor(contextServiceDefinition);

        // add to resource contexts
        for (ResourceContainerContext context : resourceContainerContexts) {
            Set<ResourceDescriptor> csddes = context.getResourceDescriptors(CONTEXT_SERVICE_DEFINITION_DESCRIPTOR);
            csddes.add(csdd);
        }
    }

    public ContextServiceDefinitionDescriptor createDescriptor(ContextServiceDefinition contectServiceDefinition) {
        Set<String> unusedContexts = collectUnusedContexts(contectServiceDefinition);

        ContextServiceDefinitionDescriptor csdd = new ContextServiceDefinitionDescriptor();
        csdd.setDescription("Context Service Definition");
        csdd.setName(TranslatedConfigView.expandValue(contectServiceDefinition.name()));
        csdd.setPropagated(evaluateContexts(contectServiceDefinition.propagated(), unusedContexts));
        csdd.setCleared(evaluateContexts(contectServiceDefinition.cleared(), unusedContexts));
        csdd.setUnchanged(evaluateContexts(contectServiceDefinition.unchanged(), unusedContexts));
        return csdd;
    }

    private Set<String> evaluateContexts(String[] sourceContexts, Set<String> unusedContexts) {
        Set<String> contexts = new HashSet<>();
        for (String context : sourceContexts) {
            if (ContextServiceDefinition.ALL_REMAINING.equals(context)) {
                contexts.addAll(unusedContexts);
                contexts.add(ContextServiceDefinition.ALL_REMAINING); // keep remaining for custom context providers
            } else {
                contexts.add(context);
            }
        }
        return contexts;
    }

    private Set<String> collectUnusedContexts(ContextServiceDefinition csdd) {
        Map<String, String> usedContexts = new HashMap<>();
        for (String context : csdd.propagated()) {
            usedContexts.put(context, "propagated");
        }
        for (String context : csdd.cleared()) {
            String previous = usedContexts.put(context, "cleared");
            if (previous != null) {
                throw new RuntimeException("Duplicate context " + context + " in " + previous + " and cleared context attributes in ContextServiceDefinition annotation!");
            }
        }
        for (String context : csdd.unchanged()) {
            String previous = usedContexts.put(context, "unchanged");
            if (previous != null) {
                throw new RuntimeException("Duplicate context " + context + " in " + previous + " and unchanged context attributes in ContextServiceDefinition annotation!");
            }
        }
        Set<String> allStandardContexts = new HashSet(Set.of(
                ContextServiceDefinition.APPLICATION,
                ContextServiceDefinition.SECURITY,
                ContextServiceDefinition.TRANSACTION));
        allStandardContexts.removeAll(usedContexts.keySet());
        return allStandardContexts;
    }
}