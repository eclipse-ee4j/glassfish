package com.sun.enterprise.deployment.node;

import java.util.Map;

import org.w3c.dom.Node;

import com.sun.enterprise.deployment.ManagedThreadFactoryDefinitionDescriptor;
import com.sun.enterprise.deployment.xml.TagNames;

public class ManagedThreadFactoryDefinitionNode extends DeploymentDescriptorNode<ManagedThreadFactoryDefinitionDescriptor> {

    public final static XMLElement tag = new XMLElement(TagNames.MANAGED_THREAD_FACTORY);

    ManagedThreadFactoryDefinitionDescriptor descriptor = null;

    public ManagedThreadFactoryDefinitionNode() {
        registerElementHandler(new XMLElement(TagNames.RESOURCE_PROPERTY),
                ResourcePropertyNode.class, "addManagedThreadFactoryPropertyDescriptor");
    }

    @Override
    protected Map getDispatchTable() {
        Map table = super.getDispatchTable();
        table.put(TagNames.MANAGED_THREAD_FACTORY_NAME, "setName");
        table.put(TagNames.MANAGED_THREAD_FACTORY_CONTEXT_SERVICE_REF, "setContext");
        table.put(TagNames.MANAGED_THREAD_FACTORY_PRIORITY, "setPriority");
        return table;
    }

    @Override
    public Node writeDescriptor(Node parent, String nodeName,
                                ManagedThreadFactoryDefinitionDescriptor managedThreadFactoryDefinitionDescriptor) {
        Node node = appendChild(parent, nodeName);
        appendTextChild(node, TagNames.MANAGED_EXECUTOR_NAME, managedThreadFactoryDefinitionDescriptor.getName());
        appendTextChild(node, TagNames.MANAGED_THREAD_FACTORY_CONTEXT_SERVICE_REF, managedThreadFactoryDefinitionDescriptor.getContext());
        appendTextChild(node, TagNames.MANAGED_THREAD_FACTORY_PRIORITY, String.valueOf(managedThreadFactoryDefinitionDescriptor.getPriority()));
        ResourcePropertyNode propertyNode = new ResourcePropertyNode();
        propertyNode.writeDescriptor(node, managedThreadFactoryDefinitionDescriptor);
        return node;
    }

    @Override
    public ManagedThreadFactoryDefinitionDescriptor getDescriptor() {
        if (descriptor == null) {
            descriptor = new ManagedThreadFactoryDefinitionDescriptor();
        }
        return descriptor;
    }
}