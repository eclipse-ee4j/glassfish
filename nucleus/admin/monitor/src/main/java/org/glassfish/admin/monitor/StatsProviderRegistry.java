/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.monitor;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.glassfish.flashlight.client.ProbeClientMethodHandle;
import org.glassfish.gmbal.ManagedObjectManager;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderInfo;

public class StatsProviderRegistry {
    private Map<String, List<StatsProviderRegistryElement>> configToRegistryElementMap = new HashMap();
    private Map<Object, StatsProviderRegistryElement> statsProviderToRegistryElementMap = new HashMap();
    private boolean isAMXReady = false;
    private boolean isMBeanEnabled = true;

    static final String[] defaultConfigLevels = new String[] { "LOW", "HIGH" };
    public static final Map<String, Integer> configLevelsMap = new ConcurrentHashMap();

    public StatsProviderRegistry(MonitoringRuntimeDataRegistry mrdr) {
        for (int i = 0; i < defaultConfigLevels.length; i++) {
            configLevelsMap.put(defaultConfigLevels[i].toUpperCase(Locale.ENGLISH), i);
        }
    }

    public void registerStatsProvider(StatsProviderInfo spInfo) {
        String configLevelStr = spInfo.getConfigLevel();

        if (configLevelStr == null) {
            // Pick the highest in the configLevels
            spInfo.setConfigLevel(defaultConfigLevels[defaultConfigLevels.length - 1]);
        }

        StatsProviderRegistryElement spre = new StatsProviderRegistryElement(spInfo);
        initialize(spre, spInfo.getConfigElement(), spInfo.getStatsProvider());
    }

    private void initialize(StatsProviderRegistryElement spre, String configStr, Object statsProvider) {
        // add a mapping from config to StatsProviderRegistryElement, so you can easily
        // retrieve all stats element for enable/disable functionality
        if (configToRegistryElementMap.containsKey(configStr)) {
            List<StatsProviderRegistryElement> spreList = configToRegistryElementMap.get(configStr);
            spreList.add(spre);
        } else {
            List<StatsProviderRegistryElement> spreList = new ArrayList();
            spreList.add(spre);
            configToRegistryElementMap.put(configStr, spreList);
        }
        // add a mapping from StatsProvider to StatsProviderRegistryElement
        // would make it easy for you when unregistering
        statsProviderToRegistryElementMap.put(statsProvider, spre);
    }

    public void unregisterStatsProvider(Object statsProvider) throws Exception {

        StatsProviderRegistryElement spre = statsProviderToRegistryElementMap.get(statsProvider);
        // Remove the entry of statsProviderRegistryElement from configToRegistryElementMap
        List<StatsProviderRegistryElement> spreList = configToRegistryElementMap.get(spre.getConfigStr());
        if (spreList != null) {
            spreList.remove(spre);
            if (spreList.isEmpty())
                configToRegistryElementMap.remove(spre.getConfigStr());
        }

        // Remove the entry of statsProvider from the statsProviderToRegistryElementMap
        statsProviderToRegistryElementMap.remove(statsProvider);

        // Remove the reference to statsProvider in spre (so it gets picked up by GC)
        spre.setStatsProvider(null);

    }

    StatsProviderRegistryElement getStatsProviderRegistryElement(Object statsProvider) {
        return statsProviderToRegistryElementMap.get(statsProvider);
    }

    List<StatsProviderRegistryElement> getStatsProviderRegistryElement(String configElement) {
        return (this.configToRegistryElementMap.get(configElement));
    }

    Collection<StatsProviderRegistryElement> getSpreList() {
        return statsProviderToRegistryElementMap.values();
    }

    Collection<String> getConfigElementList() {
        return this.configToRegistryElementMap.keySet();
    }

    void setAMXReady(boolean ready) {
        this.isAMXReady = ready;
    }

    boolean isAMXReady() {
        return this.isAMXReady;
    }

    void setMBeanEnabled(boolean enabled) {
        this.isMBeanEnabled = enabled;
    }

    boolean isMBeanEnabled() {
        return this.isMBeanEnabled;
    }

    static class StatsProviderRegistryElement {
        String configStr;
        PluginPoint pp;
        String subTreePath;
        String parentTreeNodePath = null;
        String invokerId;
        List<String> childTreeNodeNames = null;
        Collection<ProbeClientMethodHandle> handles = null;
        Object statsProvider;
        String mbeanName = null;
        ManagedObjectManager mom = null;
        Method resetMethod = null;
        boolean isEnabled = false;
        int configLevel;

        public StatsProviderRegistryElement(StatsProviderInfo spInfo) {

            this.configStr = spInfo.getConfigElement();
            this.pp = spInfo.getPluginPoint();
            this.subTreePath = spInfo.getSubTreeRoot();
            this.invokerId = spInfo.getInvokerId();
            this.statsProvider = spInfo.getStatsProvider();
            this.mbeanName = spInfo.getSubTreeRoot();
            String configLevelStr = spInfo.getConfigLevel();

            configLevel = StatsProviderRegistry.configLevelsMap.get(configLevelStr.toUpperCase(Locale.ENGLISH));
        }

        public String getConfigStr() {
            return configStr;
        }

        public PluginPoint getPluginPoint() {
            return pp;
        }

        public String getSubTreePath() {
            return subTreePath;
        }

        String getInvokerId() {
            return invokerId;
        }

        public List<String> getChildTreeNodeNames() {
            return childTreeNodeNames;
        }

        public String getParentTreeNodePath() {
            return parentTreeNodePath;
        }

        public Collection<ProbeClientMethodHandle> getHandles() {
            return handles;
        }

        public Object getStatsProvider() {
            return statsProvider;
        }

        public void setStatsProvider(Object statsProvider) {
            this.statsProvider = statsProvider;
        }

        public String getMBeanName() {
            return mbeanName;
        }

        public ManagedObjectManager getManagedObjectManager() {
            return mom;
        }

        public void setManagedObjectManager(ManagedObjectManager mom) {
            this.mom = mom;
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public void setEnabled(boolean enabled) {
            isEnabled = enabled;
        }

        public boolean isEnableAllowed(String userConfigLevelStr) {
            Integer userConfigLevel = StatsProviderRegistry.configLevelsMap.get(userConfigLevelStr.toUpperCase(Locale.ENGLISH));
            if ((userConfigLevel != null) && (userConfigLevel >= configLevel))
                return true;
            return false;
        }

        public void setParentTreeNodePath(String completePathName) {
            this.parentTreeNodePath = completePathName;
        }

        public void setChildNodeNames(List<String> childNodeNames) {
            this.childTreeNodeNames = childNodeNames;
        }

        public void setHandles(Collection<ProbeClientMethodHandle> handles) {
            this.handles = handles;
        }

        void setResetMethod(Method method) {
            this.resetMethod = method;
        }

        Method getResetMethod() {
            return this.resetMethod;
        }

        public String toString() {
            String str = "    configStr = " + configStr + "\n" + "    statsProvider = " + statsProvider.getClass().getName() + "\n"
                    + "    PluginPoint = " + pp + "\n" + "    handles = " + ((handles == null) ? "null" : "not null") + "\n"
                    + "    parentTreeNodePath = " + parentTreeNodePath;
            return str;
        }
    }
}
