/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.enterprise.config.serverbeans;

import com.sun.enterprise.config.serverbeans.customvalidators.ContextRootCheck;

import jakarta.validation.constraints.NotNull;

import java.beans.PropertyVetoException;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import org.glassfish.api.admin.RestRedirect;
import org.glassfish.api.admin.RestRedirects;
import org.glassfish.api.admin.config.ApplicationName;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import static java.util.logging.Logger.getAnonymousLogger;

@Configured
@RestRedirects({
        @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "undeploy"),
        @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "redeploy")
})
@ContextRootCheck(message = "{contextroot.duplicate}", payload = Application.class)
public interface Application extends ApplicationName, PropertyBag {

    String APP_LOCATION_PROP_NAME = "appLocation";

    String DEPLOYMENT_PLAN_LOCATION_PROP_NAME = "deploymentPlanLocation";

    String ARCHIVE_TYPE_PROP_NAME = "archiveType";

    String ALT_DD_LOCATION_PROP_NAME = "altDDLocation";

    String RUNTIME_ALT_DD_LOCATION_PROP_NAME = "runtimeAltDDLocation";

    /**
     * Gets the value of the {@code contextRoot} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getContextRoot();

    /**
     * Sets the value of the {@code contextRoot} property.
     *
     * @param contextRoot allowed object is {@link String}
     */
    void setContextRoot(String contextRoot) throws PropertyVetoException;

    /**
     * Gets the value of the {@code location} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getLocation();

    /**
     * Sets the value of the {@code location} property.
     *
     * @param location allowed object is {@link String}
     */
    void setLocation(String location) throws PropertyVetoException;

    /**
     * Gets the value of the {@code objectType} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(required = true)
    @NotNull
    String getObjectType();

    /**
     * Sets the value of the {@code objectType} property.
     *
     * @param objectType allowed object is {@link String}
     */
    void setObjectType(String objectType) throws PropertyVetoException;

    /**
     * Gets the value of the {@code enabled} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getEnabled();

    /**
     * Sets the value of the {@code enabled} property.
     *
     * @param enabled allowed object is {@link String}
     */
    void setEnabled(String enabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code libraries} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getLibraries();

    /**
     * Sets the value of the {@code libraries} property.
     *
     * @param libraries allowed object is {@link String}
     */
    void setLibraries(String libraries) throws PropertyVetoException;

    /**
     * Gets the value of the {@code availabilityEnabled} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getAvailabilityEnabled();

    /**
     * Sets the value of the {@code availabilityEnabled} property.
     *
     * @param availabilityEnabled allowed object is {@link String}
     */
    void setAvailabilityEnabled(String availabilityEnabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code asyncReplication} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getAsyncReplication();

    /**
     * Sets the value of the {@code asyncReplication} property.
     *
     * @param asyncReplication allowed object is {@link String}
     */
    void setAsyncReplication(String asyncReplication) throws PropertyVetoException;

    /**
     * Gets the value of the {@code directoryDeployed} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getDirectoryDeployed();

    /**
     * Sets the value of the {@code directoryDeployed} property.
     *
     * @param directoryDeployed allowed object is {@link String}
     */
    void setDirectoryDeployed(String directoryDeployed) throws PropertyVetoException;

    /**
     * Gets the value of the {@code description} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getDescription();

    /**
     * Sets the value of the {@code description} property.
     *
     * @param description allowed object is {@link String}
     */
    void setDescription(String description) throws PropertyVetoException;

    /**
     * Gets the value of the {@code deploymentOrder} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "100", dataType = Integer.class)
    String getDeploymentOrder();

    /**
     * Sets the value of the {@code deploymentOrder} property.
     *
     * @param deploymentOrder allowed object is {@link String}
     */
    void setDeploymentOrder(String deploymentOrder) throws PropertyVetoException;

    @Element
    List<Module> getModule();

    /**
     * Gets the value of the {@code engine} property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the {@code engine} property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     * getEngine().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link Engine }
     */
    @Element
    List<Engine> getEngine();

    @Element
    Resources getResources();

    void setResources(Resources resources) throws PropertyVetoException;

    @Element
    AppTenants getAppTenants();

    void setAppTenants(AppTenants appTenants);

    @Element("*")
    List<ApplicationExtension> getExtensions();

    /**
     * Get an extension of the specified type. If there is more than one, it is
     * undefined as to which one is returned.
     */
    default <T extends ApplicationExtension> T getExtensionByType(Class<T> type) {
        for (ApplicationExtension extension : getExtensions()) {
            try {
                return type.cast(extension);
            } catch (Exception e) {
                // ignore, not the right type.
            }
        }
        return null;
    }

    /**
     * Get all extensions of the specified type.  If there are none, and empty
     * list is returned.
     */
    default <T extends ApplicationExtension> List<T> getExtensionsByType(Class<T> type) {
        ArrayList<T> extensions = new ArrayList<>();
        for (ApplicationExtension extension : getExtensions()) {
            try {
                extensions.add(type.cast(extension));
            } catch (Exception e) {
                // ignore, not the right type.
            }
        }
        return extensions;
    }

    default Module getModule(String moduleName) {
        for (Module module : getModule()) {
            if (module.getName().equals(moduleName)) {
                return module;
            }
        }
        return null;
    }

    default Properties getDeployProperties() {
        Properties deploymentProps = new Properties();
        for (Property property : getProperty()) {
            String propertyValue = property.getValue();
            if (propertyValue != null) {
                deploymentProps.put(property.getName(), propertyValue);
            }
        }

        String objectType = getObjectType();
        if (objectType != null) {
            deploymentProps.setProperty(ServerTags.OBJECT_TYPE, objectType);
        }

        String contextRoot = getContextRoot();
        if (contextRoot != null) {
            deploymentProps.setProperty(ServerTags.CONTEXT_ROOT, contextRoot);
        }

        String directoryDeployed = getDirectoryDeployed();
        if (directoryDeployed != null) {
            deploymentProps.setProperty(ServerTags.DIRECTORY_DEPLOYED, directoryDeployed);
        }

        return deploymentProps;
    }

    default DeployCommandParameters getDeployParameters(ApplicationRef appRef) {
        URI uri = null;
        try {
            uri = new URI(getLocation());
        } catch (URISyntaxException e) {
            getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
        }

        if (uri == null) {
            return null;
        }

        DeployCommandParameters deploymentParams = new DeployCommandParameters(new File(uri));
        deploymentParams.name = getName();
        deploymentParams.description = getDescription();
        if (Boolean.parseBoolean(getEnabled()) && appRef != null && Boolean.parseBoolean(appRef.getEnabled())) {
            deploymentParams.enabled = Boolean.TRUE;
        } else {
            deploymentParams.enabled = Boolean.FALSE;
        }
        deploymentParams.contextroot = getContextRoot();
        deploymentParams.libraries = getLibraries();
        deploymentParams.availabilityenabled = Boolean.parseBoolean(getAvailabilityEnabled());
        deploymentParams.asyncreplication = Boolean.parseBoolean(getAsyncReplication());
        if (appRef != null) {
            deploymentParams.lbenabled = appRef.getLbEnabled();
            deploymentParams.virtualservers = appRef.getVirtualServers();
        }
        deploymentParams.deploymentorder = Integer.valueOf(getDeploymentOrder());
        for (Property property : getProperty()) {
            String propertyName = property.getName();
            switch (propertyName) {
                case ARCHIVE_TYPE_PROP_NAME:
                    deploymentParams.type = property.getValue();
                    break;
                case ALT_DD_LOCATION_PROP_NAME:
                    URI altDDUri = null;
                    try {
                        altDDUri = new URI(property.getValue());
                    } catch (URISyntaxException e) {
                        getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
                    }
                    if (altDDUri != null) {
                        deploymentParams.altdd = new File(altDDUri);
                    }
                    break;
                case RUNTIME_ALT_DD_LOCATION_PROP_NAME:
                    URI runtimeAltDDUri = null;
                    try {
                        runtimeAltDDUri = new URI(property.getValue());
                    } catch (URISyntaxException e) {
                        getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
                    }
                    if (runtimeAltDDUri != null) {
                        deploymentParams.runtimealtdd = new File(runtimeAltDDUri);
                    }
                    break;
                default:
                    break;
            }
        }
        return deploymentParams;
    }

    default Map<String, Properties> getModulePropertiesMap() {
        Map<String, Properties> modulePropertiesMap = new HashMap<>();
        for (Module module : getModule()) {
            List<Property> properties = module.getProperty();
            if (properties != null) {
                Properties moduleProps = new Properties();
                for (Property property : properties) {
                    String propertyValue = property.getValue();
                    if (propertyValue != null) {
                        moduleProps.put(property.getName(), propertyValue);
                    }
                }
                modulePropertiesMap.put(module.getName(), moduleProps);
            }
        }
        return modulePropertiesMap;
    }

    default boolean isStandaloneModule() {
        return !Boolean.parseBoolean(getDeployProperties().getProperty(ServerTags.IS_COMPOSITE));
    }

    default boolean containsSnifferType(String snifferType) {
        // first add application level engines
        List<Engine> engines = new ArrayList<>(getEngine());

        // now add module level engines
        for (Module module : getModule()) {
            engines.addAll(module.getEngines());
        }

        for (Engine engine : engines) {
            if (engine.getSniffer().equals(snifferType)) {
                return true;
            }
        }
        return false;
    }

    default boolean isLifecycleModule() {
        return Boolean.parseBoolean(getDeployProperties().getProperty(ServerTags.IS_LIFECYCLE));
    }

    default File application() {
        return fileForProp(APP_LOCATION_PROP_NAME);
    }

    default File deploymentPlan() {
        return fileForProp(DEPLOYMENT_PLAN_LOCATION_PROP_NAME);
    }

    default String archiveType() {
        for (Property property : getProperty()) {
            if (property.getName().equals(ARCHIVE_TYPE_PROP_NAME)) {
                return property.getValue();
            }
        }
        return null;
    }

    private File fileForProp(final String propName) {
        for (Property property : getProperty()) {
            if (property.getName().equals(propName)) {
                return new File(URI.create(property.getValue()));
            }
        }
        return null;
    }

    /**
     * Properties as per {@link PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    @Override
    List<Property> getProperty();
}
