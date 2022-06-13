package com.sun.enterprise.deployment;

import static org.glassfish.deployment.common.JavaEEResourceType.MANAGED_EXECUTOR_DEFINITION_DESCRIPTOR;

import java.util.Properties;

public class ManagedExecutorDefinitionDescriptor extends ResourceDescriptor {

    private static final long serialVersionUID = 1L;
    private static final String JAVA_URL = "java:";
    private static final String JAVA_COMP_URL = "java:comp/";

    private String name;
    private int maximumPoolSize = Integer.MAX_VALUE;
    private long hungAfterSeconds;
    private String context;
    private Properties properties = new Properties();

    public ManagedExecutorDefinitionDescriptor() {
        super.setResourceType(MANAGED_EXECUTOR_DEFINITION_DESCRIPTOR);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public long getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public long getHungAfterSeconds() {
        return hungAfterSeconds;
    }

    public void setHungAfterSeconds(long hungAfterSeconds) {
        this.hungAfterSeconds = hungAfterSeconds;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return (String) properties.get(key);
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ManagedExecutorDefinitionDescriptor) {
            ManagedExecutorDefinitionDescriptor ref = (ManagedExecutorDefinitionDescriptor) obj;
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

    public void addManagedExecutorPropertyDescriptor(ResourcePropertyDescriptor propertyDescriptor) {
        properties.put(propertyDescriptor.getName(), propertyDescriptor.getValue());
    }
}