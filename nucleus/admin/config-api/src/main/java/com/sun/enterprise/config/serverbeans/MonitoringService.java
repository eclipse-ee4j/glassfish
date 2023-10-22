/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.validation.constraints.NotNull;

import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.glassfish.api.admin.config.ConfigExtension;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.monitoring.ContainerMonitoring;
import org.glassfish.api.monitoring.MonitoringItem;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.Transaction;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

@Configured
public interface MonitoringService extends ConfigExtension, PropertyBag {

    /**
     * Gets the value of the {@code moduleMonitoringLevels} property.
     *
     * @return possible object is {@link ModuleMonitoringLevels}
     */
    @Element
    @NotNull
    ModuleMonitoringLevels getModuleMonitoringLevels();

    /**
     * Sets the value of the {@code moduleMonitoringLevels} property.
     *
     * @param moduleMonitoringLevels allowed object is {@link ModuleMonitoringLevels}
     */
    void setModuleMonitoringLevels(ModuleMonitoringLevels moduleMonitoringLevels) throws PropertyVetoException;

    /**
     * Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();

    /**
     * Gets the value of the {@code mbean-enabled} attribute.
     *
     * <p>This {@code boolean} attribute determines whether monitoring mbeans are
     * enabled or disabled. When disabled, all monitoring activity will be disabled.
     *
     * @return present monitoring activity status
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getMbeanEnabled();

    /**
     * Sets the value of the {@code mbean-enabled} attribute.
     *
     * @param mbeanEnabled allowed object is a {@link String}
     *
     */
    void setMbeanEnabled(String mbeanEnabled) throws PropertyVetoException;

    // TODO: Ref: Issue # 8706. Sreeni to work with GmBal team and provide a
    // final resolution on where the above mbean-enabled flag would reside.
    // With the addition of the new attribute below, the above attribute becomes
    // misplaced. The attribute monitoring-enabled actually enables/disables the
    // monitoring infrastructure (POJOs) while mbean-enabled enables/disables
    // one type of veneer viz the mbean-layer

    /**
     * Gets the value of the {@code monitoring-enabled} attribute.
     *
     * <p>This {@code boolean} attribute determines whether monitoring mebans are
     * enabled or disabled. When disabled, all monitoring activity will be disabled.
     *
     * @return present monitoring activity status
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getMonitoringEnabled();

    /**
     * Sets the value of the {@code monitoring-enabled} attribute.
     *
     * @param monitoringEnabled allowed object is {@link String}
     */
    void setMonitoringEnabled(String monitoringEnabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code dtrace-enabled} attribute.
     *
     * @return present dtrace status
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getDtraceEnabled();

    /**
     * Sets the value of the {@code dtrace-enabled} attribute.
     *
     * @param dtraceEnabled allowed object is {@link String}.
     */
    void setDtraceEnabled(String dtraceEnabled) throws PropertyVetoException;

    /**
     * Get the monitoring configuration for containers that used the default
     * {@link ContainerMonitoring}.
     *
     * @return list of container monitoring configurations (default)
     */
    @Element
    List<ContainerMonitoring> getContainerMonitoring();

    /**
     * Get the monitoring configuration for other types of containers that used
     * custom monitoring configuration.
     *
     * @return list of container monitoring configurations
     */
    @Element("*")
    List<MonitoringItem> getMonitoringItems();

    /**
     * Return the monitoring configuration for a container by the provided name,
     * assuming the named container used the default {@link ContainerMonitoring}
     * to express its monitoring configuration.
     *
     * @param name name of the container to return the configuration for
     * @return the container configuration or null if not found
     */
    default ContainerMonitoring getContainerMonitoring(String name) {
        for (ContainerMonitoring monitoring : getContainerMonitoring()) {
            if (monitoring.getName().equals(name)) {
                return monitoring;
            }
        }
        return null;
    }

    default String getMonitoringLevel(String moduleName) {
        // It is possible that the given module name might exist as
        // attribute of module-monitoring-levels or
        // as container-monitoring element provided for extensibility.

        // Order of precedence is to first check module-monitoring-levels
        // then container-monitoring.

        // strip - part from name
        String name = moduleName.replaceAll("-", "");

        Method getter = GetterCache.methods.get(name.toLowerCase());
        if (getter != null) {
            try {
                return  (String) getter.invoke(getModuleMonitoringLevels());
            } catch (IllegalAccessException | InvocationTargetException e) {
                Logger.getAnonymousLogger().log(Level.WARNING, e.getMessage(), e);
            }
        }

        // container-monitoring
        for (ContainerMonitoring monitoring : getContainerMonitoring()) {
            if (monitoring.getName().equals(moduleName)) {
                return monitoring.getLevel();
            }
        }

        return null;
    }

    default boolean setMonitoringLevel(String moduleName, String level) throws TransactionFailure, PropertyVetoException {
        // It is possible that the given module name might exist as
        // attribute of module-monitoring-levels or
        // as container-monitoring element provided for extensibility.

        // Order of precedence is to first check module-monitoring-levels
        // then container-monitoring.

        // module-monitoring-levels

        // strip - part from name
        String name = moduleName.replaceAll("-", "");

        Method setter = SetterCache.methods.get(name.toLowerCase());
        if (setter != null) {
            try {
                Transaction tx = Transaction.getTransaction(this);
                if (tx == null) {
                    throw new TransactionFailure(
                        LocalStringHolder.localStrings.getLocalString("noTransaction", "Internal Error - Cannot obtain transaction object"));
                }
                ModuleMonitoringLevels monitoringLevels = tx.enroll(getModuleMonitoringLevels());
                setter.invoke(monitoringLevels, level);
                return true;
            } catch (IllegalAccessException | InvocationTargetException e) {
                Logger.getAnonymousLogger().log(Level.WARNING, e.getMessage(), e);
            }
        }

        // container-monitoring
        for (ContainerMonitoring monitoring : getContainerMonitoring()) {
            if (monitoring.getName().equals(moduleName)) {
                monitoring.setLevel(level);
                return true;
            }
        }

        return false;
    }

    default boolean isAnyModuleOn() {
        ModuleMonitoringLevels monitoringLevels = getModuleMonitoringLevels();
        for (Method getter : GetterCache.methods.values()) {
            try {
                String level = (String) getter.invoke(monitoringLevels);
                if (!"OFF".equals(level)) {
                    return true;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                Logger.getAnonymousLogger().log(Level.WARNING, e.getMessage(), e);
            }
        }

        for (ContainerMonitoring monitoring : getContainerMonitoring()) {
            if (!"OFF".equals(monitoring.getLevel())) {
                return true;
            }
        }

        return false;
    }

    final class GetterCache {
        // We need to use reflection to compare the given name with the
        // getters of ModuleMonitoringLevel.
        // For performance, the method names are cached when this is run first time.
        private static final Map<String, Method> methods;

        static {
            Map<String, Method> getMethods = new HashMap<>();
            for (Method method : ModuleMonitoringLevels.class.getDeclaredMethods()) {
                String methodName = method.getName();
                if (methodName.startsWith("get") && method.getReturnType().equals(String.class)) {
                    getMethods.put(methodName.substring(3).toLowerCase(), method);
                }
            }
            methods = Collections.unmodifiableMap(getMethods);
        }

        private GetterCache() {
            throw new AssertionError();
        }
    }

    final class SetterCache {
        // We need to use reflection to compare the given name with the
        // getters of ModuleMonitoringLevel.
        // For performance, the method names are cached when this is run first time.
        private static final Map<String, Method> methods;

        static {
            Map<String, Method> setMethods = new HashMap<>();
            for (Method method : ModuleMonitoringLevels.class.getDeclaredMethods()) {
                String methodName = method.getName();
                if (methodName.startsWith("set")) {
                    setMethods.put(methodName.substring(3).toLowerCase(), method);
                }
            }
            methods = Collections.unmodifiableMap(setMethods);
        }

        private SetterCache() {
            throw new AssertionError();
        }
    }

    final class LocalStringHolder {

        private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(MonitoringService.class);

        private LocalStringHolder() {
            throw new AssertionError();
        }
    }
}
