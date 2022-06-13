package com.sun.enterprise.deployment;

import static org.glassfish.deployment.common.JavaEEResourceType.CONTEXT_SERVICE_DEFINITION_DESCRIPTOR;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Deployment information for context service.
 *
 * @author Petr Aubrecht &lt;aubrecht@asoftware.cz&gt;
 */
public class ContextServiceDefinitionDescriptor extends ResourceDescriptor {

    private static final long serialVersionUID = 1L;
    private String name;
    private Set<String> cleared;
    private Set<String> propagated;
    private Set<String> unchanged;
    private java.util.Properties properties = new java.util.Properties();

    public ContextServiceDefinitionDescriptor() {
        super.setResourceType(CONTEXT_SERVICE_DEFINITION_DESCRIPTOR);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ContextServiceDefinitionDescriptor other = (ContextServiceDefinitionDescriptor) obj;
        return Objects.equals(this.name, other.name);
    }

    @Override
    public String toString() {
        return "ContextServiceDefinitionDescriptor{" + "name=" + name + ", cleared=" + cleared + ", propagated=" + propagated + ", unchanged=" + unchanged + '}';
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getCleared() {
        return cleared;
    }

    public void setCleared(Set<String> cleared) {
        this.cleared = cleared;
    }

    public void addCleared(String clearedItem) {
        if (cleared == null) {
            cleared = new HashSet<>();
        }
        this.cleared.add(clearedItem);
    }

    public Set<String> getPropagated() {
        return propagated;
    }

    public void setPropagated(Set<String> propagated) {
        this.propagated = propagated;
    }

    public void addPropagated(String propagatedItem) {
        if (propagated == null) {
            propagated = new HashSet<>();
        }
        this.propagated.add(propagatedItem);
    }

    public Set<String> getUnchanged() {
        return unchanged;
    }

    public void setUnchanged(Set<String> unchanged) {
        this.unchanged = unchanged;
    }

    public void addUnchanged(String unchangedItem) {
        if (unchanged == null) {
            unchanged = new HashSet<>();
        }
        this.unchanged.add(unchangedItem);
    }

    public java.util.Properties getProperties() {
        return properties;
    }

    public void setProperties(java.util.Properties properties) {
        this.properties = properties;
    }

    public void addContextServiceExecutorDescriptor(ResourcePropertyDescriptor propertyDescriptor) {
        properties.put(propertyDescriptor.getName(), propertyDescriptor.getValue());
    }
}