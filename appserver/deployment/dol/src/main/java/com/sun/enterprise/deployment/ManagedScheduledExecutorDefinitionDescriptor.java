package com.sun.enterprise.deployment;

import static org.glassfish.deployment.common.JavaEEResourceType.MANAGED_SCHEDULED_EXECUTOR_DEFINITION_DESCRIPTOR;

import java.util.Properties;

public class ManagedScheduledExecutorDefinitionDescriptor extends ResourceDescriptor {

    private static final long serialVersionUID = 1L;
    private static final String JAVA_URL = "java:";
    private static final String JAVA_COMP_URL = "java:comp/";

    private String name;
    private String context;
    private long hungTaskThreshold;
    private int maxAsync = Integer.MAX_VALUE;

    private Properties properties = new Properties();

    public ManagedScheduledExecutorDefinitionDescriptor() {
        super.setResourceType(MANAGED_SCHEDULED_EXECUTOR_DEFINITION_DESCRIPTOR);
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

    public long getHungTaskThreshold() {
        return hungTaskThreshold;
    }

    public void setHungTaskThreshold(long hungTaskThreshold) {
        this.hungTaskThreshold = hungTaskThreshold;
    }

    public int getMaxAsync() {
        return maxAsync;
    }

    public void setMaxAsync(int maxAsync) {
        this.maxAsync = maxAsync;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public static String getURLName(String thisName) {
        if (!thisName.contains(JAVA_URL)) {
            thisName = JAVA_COMP_URL + thisName;
        }
        return thisName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ManagedScheduledExecutorDefinitionDescriptor) {
            ManagedScheduledExecutorDefinitionDescriptor ref =
                    (ManagedScheduledExecutorDefinitionDescriptor) obj;
            return this.getName().equals(getURLName(ref.getName()));
        }
        return false;
    }

    public void addManagedScheduledExecutorDefinitionDescriptor(ResourcePropertyDescriptor propertyDescriptor) {
        properties.put(propertyDescriptor.getName(), propertyDescriptor.getValue());
    }
}