/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import com.sun.common.util.logging.LoggingConfigImpl;
import com.sun.enterprise.config.serverbeans.customvalidators.NotDuplicateTargetName;
import com.sun.enterprise.config.serverbeans.customvalidators.NotTargetKeyword;
import com.sun.enterprise.config.util.ServerHelper;

import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.api.admin.config.ConfigExtension;
import org.glassfish.api.admin.config.Container;
import org.glassfish.api.admin.config.Named;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.admin.config.PropertyDesc;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.config.support.datatypes.Port;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.quality.ToDo;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigExtensionMethod;
import org.jvnet.hk2.config.ConfigView;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import static org.glassfish.config.support.Constants.NAME_SERVER_REGEX;
import static org.glassfish.hk2.utilities.BuilderHelper.createConstantDescriptor;
import static org.glassfish.hk2.utilities.ServiceLocatorUtilities.addOneDescriptor;
import static org.jvnet.hk2.config.ConfigSupport.apply;
import static org.jvnet.hk2.config.ConfigSupport.getImpl;

/**
 * The configuration defines the configuration of a server instance that can be shared by other server instances.
 * The availability-service and are SE/EE only
 */
@Configured
@NotDuplicateTargetName(message = "{config.duplicate.name}", payload = Config.class)
public interface Config extends Named, PropertyBag, SystemPropertyBag, Payload, ConfigLoader, ConfigBeanProxy, RefContainer {
    /**
     * Name of the configured object
     *
     * @return name of the configured object
     * FIXME: should set 'key=true'. See bugs 6039, 6040
     */
    @NotNull
    @NotTargetKeyword(message = "{config.reserved.name}", payload = Config.class)
    @Pattern(regexp = NAME_SERVER_REGEX, message = "{config.invalid.name}", payload = Config.class)
    @Override
    String getName();

    @Override
    void setName(String configName) throws PropertyVetoException;

    /**
     * Gets the value of the {@code dynamicReconfigurationEnabled} property.
     *
     * <p>When set to {@code true} then any changes to the system (e.g. applications deployed, resources created)
     * will be automatically applied to the affected servers without a restart being required. When set
     * to {@code false} such changes will only be picked up by the affected servers when each server restarts.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getDynamicReconfigurationEnabled();

    /**
     * Sets the value of the {@code dynamicReconfigurationEnabled} property.
     *
     * @param reconfigurationEnabled allowed object is {@link String}
     */
    void setDynamicReconfigurationEnabled(String reconfigurationEnabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code networkConfig} property.
     *
     * @return possible object is {@link NetworkConfig}
     */
    @Element(required = true)
    NetworkConfig getNetworkConfig();

    /**
     * Sets the value of the {@code networkConfig} property.
     *
     * @param networkConfig allowed object is {@link NetworkConfig}
     */
    void setNetworkConfig(NetworkConfig networkConfig) throws PropertyVetoException;

    /**
     * Gets the value of the {@code httpService} property.
     *
     * @return possible object is {@link HttpService}
     */
    @Element(required = true)
    HttpService getHttpService();

    /**
     * Sets the value of the {@code httpService} property.
     *
     * @param httpService allowed object is {@link HttpService}
     */
    void setHttpService(HttpService httpService) throws PropertyVetoException;

    /**
     * Gets the value of the {@code adminService} property.
     *
     * @return possible object is {@link AdminService}
     */
    @Element(required = true)
    AdminService getAdminService();

    /**
     * Sets the value of the {@code adminService} property.
     *
     * @param adminService allowed object is {@link AdminService}
     */
    void setAdminService(AdminService adminService) throws PropertyVetoException;

    /**
     * Gets the value of the {@code logService} property.
     *
     * @return possible object is {@link LogService}
     */
    @Element(required = true)
    LogService getLogService();

    /**
     * Sets the value of the {@code logService} property.
     *
     * @param logService allowed object is {@link LogService}
     */
    void setLogService(LogService logService) throws PropertyVetoException;

    /**
     * Gets the value of the {@code securityService} property.
     *
     * @return possible object is {@link SecurityService}
     */
    @Element(required = true)
    SecurityService getSecurityService();

    /**
     * Sets the value of the {@code securityService} property.
     *
     * @param securityService allowed object is {@link SecurityService}
     */
    void setSecurityService(SecurityService securityService) throws PropertyVetoException;

    /**
     * Gets the value of the {@code monitoringService} property.
     *
     * @return possible object is {@link MonitoringService}
     */
    @Element()
    @NotNull
    MonitoringService getMonitoringService();

    /**
     * Sets the value of the {@code monitoringService} property.
     *
     * @param monitoringService allowed object is {@link MonitoringService}
     */
    void setMonitoringService(MonitoringService monitoringService) throws PropertyVetoException;

    /**
     * Gets the value of the {@code diagnosticService} property.
     *
     * @return possible object is {@link DiagnosticService}
     */
    @Element
    DiagnosticService getDiagnosticService();

    /**
     * Sets the value of the {@code diagnosticService} property.
     *
     * @param diagnosticService allowed object is {@link DiagnosticService}
     */
    void setDiagnosticService(DiagnosticService diagnosticService) throws PropertyVetoException;

    /**
     * Gets the value of the {@code javaConfig} property.
     *
     * @return possible object is {@link JavaConfig}
     */
    @Element(required = true)
    JavaConfig getJavaConfig();

    /**
     * Sets the value of the {@code javaConfig} property.
     *
     * @param javaConfig allowed object is {@link JavaConfig}
     */
    void setJavaConfig(JavaConfig javaConfig) throws PropertyVetoException;

    /**
     * Gets the value of the {@code availabilityService} property.
     *
     * @return possible object is {@link AvailabilityService}
     */
    @Element
    @NotNull
    AvailabilityService getAvailabilityService();

    /**
     * Sets the value of the {@code availabilityService} property.
     *
     * @param availabilityService allowed object is {@link AvailabilityService}
     */
    void setAvailabilityService(AvailabilityService availabilityService) throws PropertyVetoException;

    /**
     * Gets the value of the {@code threadPools} property.
     *
     * @return possible object is {@link ThreadPools}
     */
    @Element(required = true)
    ThreadPools getThreadPools();

    /**
     * Sets the value of the {@code threadPools} property.
     *
     * @param threadPools allowed object is {@link ThreadPools}
     */
    void setThreadPools(ThreadPools threadPools) throws PropertyVetoException;

    /**
     * Gets the value of the {@code groupManagementService} property.
     *
     * @return possible object is {@link GroupManagementService}
     */
    @Element
    @NotNull
    GroupManagementService getGroupManagementService();

    /**
     * Sets the value of the {@code groupManagementService} property.
     *
     * @param groupManagementService allowed object is {@link GroupManagementService}
     */
    void setGroupManagementService(GroupManagementService groupManagementService) throws PropertyVetoException;

    /**
     * Gets the value of the {@code systemProperty} property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore
     * any modification you make to the returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the {@code systemProperty} property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     * getSystemProperty().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link SystemProperty }
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Any more legal system properties?")
    @PropertiesDesc(systemProperties = true, props = {
            @PropertyDesc(name = "HTTP_LISTENER_PORT", defaultValue = "8080", dataType = Port.class),
            @PropertyDesc(name = "HTTP_SSL_LISTENER_PORT", defaultValue = "1043", dataType = Port.class),
            @PropertyDesc(name = "HTTP_ADMIN_LISTENER_PORT", defaultValue = "4848", dataType = Port.class),
            @PropertyDesc(name = "IIOP_LISTENER_PORT", defaultValue = "3700", dataType = Port.class),
            @PropertyDesc(name = "IIOP_SSL_LISTENER_PORT", defaultValue = "1060", dataType = Port.class),
            @PropertyDesc(name = "IIOP_SSL_MUTUALAUTH_PORT", defaultValue = "1061", dataType = Port.class),
            @PropertyDesc(name = "JMX_SYSTEM_CONNECTOR_PORT", defaultValue = "8686", dataType = Port.class)
    })
    @Element
    @Override
    List<SystemProperty> getSystemProperty();

    // Default methods for accessing the logging.properties file

    default Map<String, String> getLoggingProperties() {
        LoggingConfigImpl loggingConfig = getLoggingConfig();

        Map<String, String> loggingProperties = new HashMap<>();
        try {
            loggingProperties = loggingConfig.getLoggingProperties();
        } catch (IOException ignored) {
        }
        return loggingProperties;
    }

    default String setLoggingProperty(String property, String value) {
        LoggingConfigImpl loggingConfig = getLoggingConfig();

        String loggingProperty = null;
        try {
            loggingProperty = loggingConfig.setLoggingProperty(property, value);
        } catch (IOException ignored) {
        }
        return loggingProperty;
    }

    default Map<String, String> updateLoggingProperties(Map<String, String> properties) {
        LoggingConfigImpl loggingConfig = getLoggingConfig();

        Map<String, String> loggingProperties = new HashMap<>();
        try {
            loggingProperties = loggingConfig.updateLoggingProperties(properties);
        } catch (IOException ignored) {
        }
        return loggingProperties;
    }

    private LoggingConfigImpl getLoggingConfig() {
        ConfigBean configBean = (ConfigBean) ((ConfigView) Proxy.getInvocationHandler(this)).getMasterView();
        ServerEnvironmentImpl env = configBean.getHabitat().getService(ServerEnvironmentImpl.class);
        LoggingConfigImpl loggingConfig = new LoggingConfigImpl();
        loggingConfig.setupConfigDir(env.getConfigDirPath(), env.getLibPath());
        return loggingConfig;
    }

    default NetworkListener getAdminListener() {
        return ServerHelper.getAdminListener(this);
    }

    /**
     * Return an extension configuration given the extension type.
     *
     * @param type type of the requested extension configuration
     * @param <T> interface subclassing the {@link ConfigExtension} type
     * @return a configuration proxy of type {@code T} or {@code null} if there is no such configuration with that type.
     */
    @ConfigExtensionMethod
    <T extends ConfigExtension> T getExtensionByType(Class<T> type);

    /**
     * Add name as an index key for this {@link Config} and for the objects that are directly referenced
     * by this {@link Config}. This includes all the {@link Config} extensions.
     *
     * @param habitat {@link ServiceLocator} that contains this config
     * @param name {@code name} to use to identify the objects
     */
    default void addIndex(ServiceLocator habitat, String name) {
        addOneDescriptor(habitat, createConstantDescriptor(this, name, Config.class));

        // directly referenced objects
        ConfigBeanProxy[] directRef = {
                getAdminService(),
                getAvailabilityService(),
                getDiagnosticService(),
                getHttpService(),
                getJavaConfig(),
                getLogService(),
                getNetworkConfig(),
                getSecurityService(),
                getThreadPools(),
                getMonitoringService()
        };

        for (ConfigBeanProxy proxy : directRef) {
            if (proxy != null) {
                addOneDescriptor(habitat, createConstantDescriptor(proxy, name, getImpl(proxy).getProxyType()));
            }
        }

        // containers
        for (Container container : getContainers()) {
            addOneDescriptor(habitat, createConstantDescriptor(container, name, getImpl(container).getProxyType()));
        }
    }

    /**
     * @param configBeanType The config bean type we want to check whether the configuration exists for it or not.
     * @param <P> Type that extends the {@link ConfigBeanProxy} which is the type of class we accept as parameter
     * @return {@code true} if configuration for the type exists in the target area of {@code domain.xml} and {@code false} if not.
     */
    default <P extends ConfigExtension> boolean checkIfExtensionExists(Class<P> configBeanType) {
        for (ConfigExtension extension : getExtensions()) {
            try {
                configBeanType.cast(extension);
                return true;
            } catch (Exception ignored) {
                // ignore, not the right type.
            }
        }
        return false;
    }

    default ResourceRef getResourceRef(SimpleJndiName refName) {
        for (ResourceRef ref : getResourceRef()) {
            if (ref.getRef().equals(refName.toString())) {
                return ref;
            }
        }
        return null;
    }

    default boolean isResourceRefExists(SimpleJndiName refName) {
        return getResourceRef(refName) != null;
    }

    default void createResourceRef(String enabled, SimpleJndiName refName) throws TransactionFailure {
        apply(param -> {
            ResourceRef newResourceRef = param.createChild(ResourceRef.class);
            newResourceRef.setEnabled(enabled);
            newResourceRef.setRef(refName.toString());
            param.getResourceRef().add(newResourceRef);
            return newResourceRef;
        }, this);
    }

    default void deleteResourceRef(SimpleJndiName refName) throws TransactionFailure {
        final ResourceRef ref = getResourceRef(refName);
        if (ref != null) {
            apply(param -> param.getResourceRef().remove(ref), this);
        }
    }

    default boolean isDas() {
        try {
            String type = getAdminService().getType();

            if (type != null && (type.equals("das") || type.equals("das-and-server"))) {
                return true;
            }
        } catch (Exception ignored) {
            // fall through
        }
        return false;
    }

    /**
     * Properties as per {@link PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    @Override
    List<Property> getProperty();

    /**
     * Get the configuration for other types of containers.
     *
     * @return list of containers configuration
     */
    @Element("*")
    List<Container> getContainers();

    @Element("*")
    List<ConfigExtension> getExtensions();
}
