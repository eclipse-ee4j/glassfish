package com.sun.enterprise.deployment.node;

import java.util.Map;

import org.w3c.dom.Node;

import com.sun.enterprise.deployment.ManagedExecutorDefinitionDescriptor;
import com.sun.enterprise.deployment.xml.TagNames;

public class ManagedExecutorDefinitionNode extends DeploymentDescriptorNode<ManagedExecutorDefinitionDescriptor> {

    public final static XMLElement tag = new XMLElement(TagNames.MANAGED_EXECUTOR);

    ManagedExecutorDefinitionDescriptor descriptor = null;

    public ManagedExecutorDefinitionNode() {
        registerElementHandler(new XMLElement(TagNames.RESOURCE_PROPERTY), ResourcePropertyNode.class,
                "addManagedExecutorPropertyDescriptor");
    }

    @Override
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(TagNames.MANAGED_EXECUTOR_NAME, "setName");
        table.put(TagNames.MANAGED_EXECUTOR_MAX_ASYNC, "setMaximumPoolSize");
        table.put(TagNames.MANAGED_EXECUTOR_HUNG_TASK_THRESHOLD, "setHungAfterSeconds");
        table.put(TagNames.MANAGED_EXECUTOR_CONTEXT_SERVICE_REF, "setContext");
        return table;
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName, ManagedExecutorDefinitionDescriptor managedExecutorDefinitionDescriptor) {
        Node node = appendChild(parent, nodeName);
        appendTextChild(node, TagNames.MANAGED_EXECUTOR_NAME, managedExecutorDefinitionDescriptor.getName());
        appendTextChild(node, TagNames.MANAGED_EXECUTOR_MAX_ASYNC, String.valueOf(managedExecutorDefinitionDescriptor.getMaximumPoolSize()));
        appendTextChild(node, TagNames.MANAGED_EXECUTOR_HUNG_TASK_THRESHOLD, String.valueOf(managedExecutorDefinitionDescriptor.getHungAfterSeconds()));
        appendTextChild(node, TagNames.MANAGED_EXECUTOR_CONTEXT_SERVICE_REF, managedExecutorDefinitionDescriptor.getContext());
        ResourcePropertyNode propertyNode = new ResourcePropertyNode();
        propertyNode.writeDescriptor(node, managedExecutorDefinitionDescriptor);
        return node;
    }

    @Override
    public ManagedExecutorDefinitionDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new ManagedExecutorDefinitionDescriptor();
        }
        return descriptor;
    }

}