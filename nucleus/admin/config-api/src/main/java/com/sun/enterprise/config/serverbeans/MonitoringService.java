/*
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
import org.glassfish.api.monitoring.ContainerMonitoring;
import org.glassfish.api.monitoring.MonitoringItem;
import org.jvnet.hk2.config.*;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;

import jakarta.validation.constraints.NotNull;
import org.glassfish.api.admin.config.ConfigExtension;

/* @XmlType(name = "", propOrder = {
   "moduleMonitoringLevels",
   "property"
}) */

@Configured
public interface MonitoringService extends ConfigExtension, PropertyBag {

    /**
     * Gets the value of the moduleMonitoringLevels property.
     *
     * @return possible object is {@link ModuleMonitoringLevels }
     */
    @Element
    @NotNull
    public ModuleMonitoringLevels getModuleMonitoringLevels();

    /**
     * Sets the value of the moduleMonitoringLevels property.
     *
     * @param value allowed object is {@link ModuleMonitoringLevels }
     */
    public void setModuleMonitoringLevels(ModuleMonitoringLevels value) throws PropertyVetoException;

    /**
     * Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();

    /**
     * Gets the value of the mbean-enabled attribute. This boolean attribute determines whether monitoring mbeans are
     * enabled or disabled. When disabled, all montioring activity will be disabled
     *
     * @return present monitoring activity status
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    public String getMbeanEnabled();

    /**
     * Sets the value of the mbean-enabled attribute.
     *
     * @param value allowed object is a String
     *
     */
    public void setMbeanEnabled(String value) throws PropertyVetoException;

    // TODO: Ref: Issue # 8706. Sreeni to work with GmBal team and provide a
    // final resolution on where the above mbean-enabled flag would reside.
    // With the addition of the new attribute below, the above attribute becomes
    // misplaced. The attribute monitoring-enabled actually enables/disables the
    // monitoring infrastructure (POJOs) while mbean-enabled enables/disables
    // one type of veneer viz the mbean-layer

    /**
     * Gets the value of the monitoring-enabled attribute. This boolean attribute determines whether monitoring mebans are
     * enabled or disabled. When disabled, all montioring activity will be disabled
     *
     * @return present monitoring activity status
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    public String getMonitoringEnabled();

    /**
     * Sets the value of the monitoring-enabled attribute.
     *
     * @param value allowed object is String
     */
    public void setMonitoringEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the dtrace-enabled attribute.
     *
     * @return present dtrace status
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    public String getDtraceEnabled();

    /**
     * Sets the value of the dtrace-enabled attribute.
     *
     * @param value allowed object is String
     */
    public void setDtraceEnabled(String value) throws PropertyVetoException;

    /**
     * Get the monitoring configuration for containers that used the default ContainerMonitoring.
     *
     * @return list of container monitoring configurations (default)
     */
    @Element
    List<ContainerMonitoring> getContainerMonitoring();

    /**
     * Get the monitoring configuration for other types of containers that used custom monitoring configuration.
     *
     * @return list of container monitoring configurations
     */
    @Element("*")
    List<MonitoringItem> getMonitoringItems();

    /**
     * Return the monitoring configuration for a container by the provided name, assuming the named container used the
     * default ContainerMonitoring to express its monitoring configuration.
     *
     * @param name name of the container to return the configuration for
     * @return the container configuration or null if not found
     */
    @DuckTyped
    ContainerMonitoring getContainerMonitoring(String name);

    @DuckTyped
    String getMonitoringLevel(String name);

    @DuckTyped
    void setMonitoringLevel(String name, String level);

    @DuckTyped
    boolean isAnyModuleOn();

    public class Duck {
        public static ContainerMonitoring getContainerMonitoring(MonitoringService ms, String name) {
            for (ContainerMonitoring cm : ms.getContainerMonitoring()) {
                if (cm.getName().equals(name)) {
                    return cm;
                }
            }
            return null;
        }

        private final static List<String> getMethods = new ArrayList<String>();

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

        public static String getMonitoringLevel(MonitoringService ms, String name) {

            String level = null;

            // It is possible that the given module name might exist as
            // attribute of module-monitoring-levels or
            // as container-monitoring element provided for extensibility.

            // Order of precedence is to first check module-monitoring-levels
            // then container-monitoring.

            // module-monitoring-levels
            populateGetMethods();

            // strip - part from name
            String rName = name.replaceAll("-", "");

            Iterator<String> itr = getMethods.iterator();
            while (itr.hasNext()) {
                String methodName = itr.next();
                if (rName.equalsIgnoreCase(methodName.substring(3))) {
                    try {
                        Method mthd = ModuleMonitoringLevels.class.getMethod(methodName, (Class[]) null);
                        level = (String) mthd.invoke(ms.getModuleMonitoringLevels(), (Object[]) null);
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
            for (ContainerMonitoring cm : ms.getContainerMonitoring()) {
                if (cm.getName().equals(name)) {
                    return cm.getLevel();
                }
            }

            return null;
        }

        public static boolean isAnyModuleOn(MonitoringService ms) {
            boolean rv = false;
            populateGetMethods();
            ModuleMonitoringLevels mml = ms.getModuleMonitoringLevels();
            for (String methodName : getMethods) {
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
            for (ContainerMonitoring cm : ms.getContainerMonitoring()) {
                rv = rv || !"OFF".equals(cm.getLevel());
            }
            return rv;
        }

        private final static List<String> setMethods = new ArrayList<String>();

        public static boolean setMonitoringLevel(MonitoringService ms, String name, String level)
                throws PropertyVetoException, TransactionFailure {

            // It is possible that the given module name might exist as
            // attribute of module-monitoring-levels or
            // as container-monitoring element provided for extensibility.

            // Order of precedence is to first check module-monitoring-levels
            // then container-monitoring.

            // module-monitoring-levels
            // We need to use reflection to comapre the given name with the
            // getters of ModuleMonitoringLevel.
            // For performance, the method names are cached when this is run first time.

            boolean isLevelUpdated = false;

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

            // strip - part from name
            String rName = name.replaceAll("-", "");

            Iterator<String> itr = setMethods.iterator();

            while (itr.hasNext()) {
                String methodName = itr.next();
                if (rName.equalsIgnoreCase(methodName.substring(3))) {
                    try {
                        Method mthd = ModuleMonitoringLevels.class.getMethod(methodName, new Class[] { java.lang.String.class });
                        Transaction tx = Transaction.getTransaction(ms);
                        if (tx == null) {
                            throw new TransactionFailure(
                                    localStrings.getLocalString("noTransaction", "Internal Error - Cannot obtain transaction object"));
                        }
                        ModuleMonitoringLevels mml = tx.enroll(ms.getModuleMonitoringLevels());
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
                for (ContainerMonitoring cm : ms.getContainerMonitoring()) {
                    if (cm.getName().equals(name)) {
                        cm.setLevel(level);
                        isLevelUpdated = true;
                    }
                }
            }
            return isLevelUpdated;

        }
    }

    final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(MonitoringService.class);
}
