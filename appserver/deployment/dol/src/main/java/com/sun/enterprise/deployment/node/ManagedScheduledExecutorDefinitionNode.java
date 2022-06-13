
package com.sun.enterprise.deployment.node;

import java.util.Map;

import org.w3c.dom.Node;

import com.sun.enterprise.deployment.ManagedScheduledExecutorDefinitionDescriptor;
import com.sun.enterprise.deployment.xml.TagNames;

public class ManagedScheduledExecutorDefinitionNode extends DeploymentDescriptorNode<ManagedScheduledExecutorDefinitionDescriptor> {

    public final static XMLElement tag = new XMLElement(TagNames.MANAGED_SCHEDULED_EXECUTOR);

    ManagedScheduledExecutorDefinitionDescriptor descriptor = null;

    public ManagedScheduledExecutorDefinitionNode() {
        registerElementHandler(new XMLElement(TagNames.RESOURCE_PROPERTY), ResourcePropertyNode.class,
                "addManagedScheduledExecutorDefinitionDescriptor");
    }

    @Override
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(TagNames.MANAGED_SCHEDULED_EXECUTOR_NAME, "setName");
        table.put(TagNames.MANAGED_SCHEDULED_EXECUTOR_MAX_ASYNC, "setMaxAsync");
        table.put(TagNames.MANAGED_SCHEDULED_EXECUTOR_CONTEXT_SERVICE_REF, "setContext");
        table.put(TagNames.MANAGED_SCHEDULED_EXECUTOR_HUNG_TASK_THRESHOLD, "setHungTaskThreshold");
        return table;
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName,
                                ManagedScheduledExecutorDefinitionDescriptor managedScheduledExecutorDefinitionDescriptor) {
        Node node = appendChild(parent, nodeName);
        appendTextChild(node, TagNames.MANAGED_SCHEDULED_EXECUTOR_NAME,
                managedScheduledExecutorDefinitionDescriptor.getName());
        appendTextChild(node, TagNames.MANAGED_SCHEDULED_EXECUTOR_MAX_ASYNC,
                managedScheduledExecutorDefinitionDescriptor.getMaxAsync());
        appendTextChild(node, TagNames.MANAGED_SCHEDULED_EXECUTOR_CONTEXT_SERVICE_REF,
                managedScheduledExecutorDefinitionDescriptor.getContext());
        appendTextChild(node, TagNames.MANAGED_SCHEDULED_EXECUTOR_HUNG_TASK_THRESHOLD,
                String.valueOf(managedScheduledExecutorDefinitionDescriptor.getHungTaskThreshold()));
        ResourcePropertyNode propertyNode = new ResourcePropertyNode();
        propertyNode.writeDescriptor(node, managedScheduledExecutorDefinitionDescriptor);
        return node;
    }

    @Override
    public ManagedScheduledExecutorDefinitionDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new ManagedScheduledExecutorDefinitionDescriptor();
        }
        return descriptor;
    }
}