/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
        String level = null;

        // It is possible that the given module name might exist as
        // attribute of module-monitoring-levels or
        // as container-monitoring element provided for extensibility.

        // Order of precedence is to first check module-monitoring-levels
        // then container-monitoring.

        // module-monitoring-levels
        Util.populateGetMethods();

        // strip - part from name
        String name = moduleName.replaceAll("-", "");

        Iterator<String> itr = Util.getMethods.iterator();
        while (itr.hasNext()) {
            String methodName = itr.next();
            if (name.equalsIgnoreCase(methodName.substring(3))) {
                try {
                    Method mthd = ModuleMonitoringLevels.class.getMethod(methodName, (Class[]) null);
                    level = (String) mthd.invoke(getModuleMonitoringLevels(), (Object[]) null);
                } catch (NoSuchMethodException nsme) {
                    Logger.getAnonymousLogger().log(Level.WARNING, nsme.getMessage(), nsme);
                } catch (IllegalAccessException ile) {
                    Logger.getAnonymousLogger().log(Level.WARNING, ile.getMessage(), ile);
                } catch (java.lang.reflect.InvocationTargetException ite) {
                    Logger.getAnonymousLogger().log(Level.WARNING, ite.getMessage(), ite);
                }
                break;
            }
        }

        if (level != null)
            return level;

        // container-monitoring
        for (ContainerMonitoring cm : getContainerMonitoring()) {
            if (cm.getName().equals(moduleName)) {
                return cm.getLevel();
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
        boolean isLevelUpdated = false;

        Util.populateSetMethods();

        // strip - part from name
        String name = moduleName.replaceAll("-", "");

        Iterator<String> itr = Util.setMethods.iterator();

        while (itr.hasNext()) {
            String methodName = itr.next();
            if (name.equalsIgnoreCase(methodName.substring(3))) {
                try {
                    Method mthd = ModuleMonitoringLevels.class.getMethod(methodName, new Class[] { java.lang.String.class });
                    Transaction tx = Transaction.getTransaction(this);
                    if (tx == null) {
                        throw new TransactionFailure(
                                Util.localStrings.getLocalString("noTransaction", "Internal Error - Cannot obtain transaction object"));
                    }
                    ModuleMonitoringLevels mml = tx.enroll(getModuleMonitoringLevels());
                    mthd.invoke(mml, level);
                    isLevelUpdated = true;
                } catch (NoSuchMethodException nsme) {
                    Logger.getAnonymousLogger().log(Level.WARNING, nsme.getMessage(), nsme);
                } catch (IllegalAccessException ile) {
                    Logger.getAnonymousLogger().log(Level.WARNING, ile.getMessage(), ile);
                } catch (java.lang.reflect.InvocationTargetException ite) {
                    Logger.getAnonymousLogger().log(Level.WARNING, ite.getMessage(), ite);
                }
                break;
            }
        }

        if (!isLevelUpdated) {
            // container-monitoring
            for (ContainerMonitoring cm : getContainerMonitoring()) {
                if (cm.getName().equals(name)) {
                    cm.setLevel(level);
                    isLevelUpdated = true;
                }
            }
        }
        return isLevelUpdated;
    }

    default boolean isAnyModuleOn() {
        boolean rv = false;
        Util.populateGetMethods();
        ModuleMonitoringLevels mml = getModuleMonitoringLevels();
        for (String methodName : Util.getMethods) {
            try {
                Method mthd = ModuleMonitoringLevels.class.getMethod(methodName, (Class[]) null);
                String level = (String) mthd.invoke(mml, (Object[]) null);
                rv = rv || !"OFF".equals(level);
            } catch (NoSuchMethodException nsme) {
                Logger.getAnonymousLogger().log(Level.WARNING, nsme.getMessage(), nsme);
            } catch (IllegalAccessException ile) {
                Logger.getAnonymousLogger().log(Level.WARNING, ile.getMessage(), ile);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                Logger.getAnonymousLogger().log(Level.WARNING, ite.getMessage(), ite);
            }
        }
        for (ContainerMonitoring cm : getContainerMonitoring()) {
            rv = rv || !"OFF".equals(cm.getLevel());
        }
        return rv;
    }

    class Util {

        private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(MonitoringService.class);

        private static final List<String> getMethods = new ArrayList<>();

        private static final List<String> setMethods = new ArrayList<>();

        private static void populateGetMethods() {
            // We need to use reflection to compare the given name with the
            // getters of ModuleMonitoringLevel.
            // For performance, the method names are cached when this is run first time.
            synchronized (getMethods) {
                if (getMethods.isEmpty()) {
                    for (Method method : ModuleMonitoringLevels.class.getDeclaredMethods()) {
                        // If it is a getter store it in the list
                        String str = method.getName();
                        if (str.startsWith("get") && method.getReturnType().equals(String.class)) {
                            getMethods.add(str);
                        }
                    }
                }
            }
        }

        private static void populateSetMethods() {
            // We need to use reflection to compare the given name with the
            // getters of ModuleMonitoringLevel.
            // For performance, the method names are cached when this is run first time.
            synchronized (setMethods) {
                if (setMethods.isEmpty()) {
                    for (Method method : ModuleMonitoringLevels.class.getDeclaredMethods()) {
                        // If it is a setter store it in the list
                        String str = method.getName();
                        if (str.startsWith("set")) {
                            setMethods.add(str);
                        }
                    }
                }
            }
        }
    }
}
