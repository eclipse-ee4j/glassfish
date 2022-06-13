package com.sun.enterprise.deployment;

import static org.glassfish.deployment.common.JavaEEResourceType.MANAGED_THREADFACTORY_DEFINITION_DESCRIPTOR;

import java.util.Properties;

public class ManagedThreadFactoryDefinitionDescriptor extends ResourceDescriptor {

    private static final long serialVersionUID = 1L;
    private static final String JAVA_URL = "java:";
    private static final String JAVA_COMP_URL = "java:comp/";

    private String name;
    private String context;
    private int priority = Thread.NORM_PRIORITY;
    private Properties properties = new Properties();

    public ManagedThreadFactoryDefinitionDescriptor() {
        super.setResourceType(MANAGED_THREADFACTORY_DEFINITION_DESCRIPTOR);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ManagedThreadFactoryDefinitionDescriptor) {
            ManagedThreadFactoryDefinitionDescriptor ref = (ManagedThreadFactoryDefinitionDescriptor) obj;
            return this.getName().equals(getURLName(ref.getName()));
        }
        return false;
    }

    public static String getURLName(String thisName) {
        if (!thisName.contains(JAVA_URL)) {
            thisName = JAVA_COMP_URL + thisName;
        }
        return thisName;
    }

    public void addManagedThreadFactoryPropertyDescriptor(ResourcePropertyDescriptor propertyDescriptor) {
        properties.put(propertyDescriptor.getName(), propertyDescriptor.getValue());
    }
}