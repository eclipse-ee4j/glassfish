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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.web.admin.monitor;

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author PRASHANTH ABBAGANI
 */
@Service(name = "web")
@Singleton
public class WebStatsProviderBootstrap implements PostConstruct {

    private static final String NODE_SEPARATOR = "/";

    @Inject
    private Domain domain;

    // Map of apps and its StatsProvider list
    private ConcurrentMap<String, ConcurrentMap<String, Queue<Object>>> vsNameToStatsProviderMap =
            new ConcurrentHashMap<String, ConcurrentMap<String, Queue<Object>>>();
    private Queue<Object> webContainerStatsProviderQueue = new ConcurrentLinkedQueue<Object>();
    private AtomicBoolean isWebStatsProvidersRegistered = new AtomicBoolean(false);

    public WebStatsProviderBootstrap() {
    }

    public void postConstruct(){
        //Register the Web stats providers
        registerWebStatsProviders();
    }

    private synchronized void registerWebStatsProviders() {
        if (isWebStatsProvidersRegistered.get()) {
            return;
        }

        JspStatsProvider jsp = new JspStatsProvider(null, null);
        RequestStatsProvider wsp = new RequestStatsProvider(null, null);
        ServletStatsProvider svsp = new ServletStatsProvider(null, null);
        SessionStatsProvider sssp = new SessionStatsProvider(null, null);
        StatsProviderManager.register("web-container", PluginPoint.SERVER,
            "web/jsp", jsp);
        StatsProviderManager.register("web-container", PluginPoint.SERVER,
            "web/request", wsp);
        StatsProviderManager.register("web-container", PluginPoint.SERVER,
            "web/servlet", svsp);
        StatsProviderManager.register("web-container", PluginPoint.SERVER,
            "web/session", sssp);
        webContainerStatsProviderQueue.add(jsp);
        webContainerStatsProviderQueue.add(wsp);
        webContainerStatsProviderQueue.add(svsp);
        webContainerStatsProviderQueue.add(sssp);

        isWebStatsProvidersRegistered.set(true);
    }

    public void registerApplicationStatsProviders(String monitoringName,
            String vsName, List<String> servletNames) {

        // try register again as it may be unregistered
        registerWebStatsProviders();

        //create stats providers for each virtual server 'vsName'
        String node = getNodeString(monitoringName, vsName);
        ConcurrentMap<String, Queue<Object>> statsProviderMap = vsNameToStatsProviderMap.get(vsName);
        Queue<Object> statspList = null;
        if (statsProviderMap == null) {
            statsProviderMap = new ConcurrentHashMap<String, Queue<Object>>();
            ConcurrentMap<String, Queue<Object>> anotherMap =
                    vsNameToStatsProviderMap.putIfAbsent(vsName, statsProviderMap);
            if (anotherMap != null) {
                statsProviderMap = anotherMap;
            }
        } else {
            statspList = statsProviderMap.get(monitoringName);
        }
        if (statspList == null) {
            statspList = new ConcurrentLinkedQueue<Object>();
            Queue<Object> anotherQueue = statsProviderMap.putIfAbsent(monitoringName, statspList);
            if (anotherQueue != null) {
                statspList = anotherQueue;
            }
        }

        JspStatsProvider jspStatsProvider =
                new JspStatsProvider(monitoringName, vsName);
        StatsProviderManager.register(
                "web-container", PluginPoint.APPLICATIONS, node,
                jspStatsProvider);
        statspList.add(jspStatsProvider);
        ServletStatsProvider servletStatsProvider =
                new ServletStatsProvider(monitoringName, vsName);
        StatsProviderManager.register(
                "web-container", PluginPoint.APPLICATIONS, node,
                servletStatsProvider);
        statspList.add(servletStatsProvider);
        SessionStatsProvider sessionStatsProvider =
                new SessionStatsProvider(monitoringName, vsName);
        StatsProviderManager.register(
                "web-container", PluginPoint.APPLICATIONS, node,
                sessionStatsProvider);
        statspList.add(sessionStatsProvider);
        RequestStatsProvider websp =
                new RequestStatsProvider(monitoringName, vsName);
        StatsProviderManager.register(
                "web-container", PluginPoint.APPLICATIONS, node,
                websp);

        for (String servletName : servletNames) {
             ServletInstanceStatsProvider servletInstanceStatsProvider =
                 new ServletInstanceStatsProvider(servletName,
                     monitoringName, vsName, servletStatsProvider);
             StatsProviderManager.register(
                     "web-container", PluginPoint.APPLICATIONS,
                     getNodeString(monitoringName, vsName, servletName),
                     servletInstanceStatsProvider);
             statspList.add(servletInstanceStatsProvider);
        }

        statspList.add(websp);
    }

    public void unregisterApplicationStatsProviders(String monitoringName,
            String vsName) {

        Map<String, Queue<Object>> statsProviderMap = vsNameToStatsProviderMap.get(vsName);
        // remove stats providers for a given monitoringName and vs
        Queue<Object> statsProviders = statsProviderMap.remove(monitoringName);
        for (Object statsProvider : statsProviders) {
            StatsProviderManager.unregister(statsProvider);
        }

        if (statsProviderMap.isEmpty()) {
            vsNameToStatsProviderMap.remove(vsName);
        }

        // remove web stats provider if it is empty (for all vs)
        if (vsNameToStatsProviderMap.isEmpty()) {
            for (Object statsProvider : webContainerStatsProviderQueue) {
                StatsProviderManager.unregister(statsProvider);
            }
            webContainerStatsProviderQueue.clear();
            isWebStatsProvidersRegistered.set(false);
        }
    }

    private String getNodeString(String moduleName, String... others) {
        StringBuilder sb = new StringBuilder(moduleName);
        for (String other: others) {
            sb.append(NODE_SEPARATOR).append(other);
        }
        return sb.toString();
    }
}
