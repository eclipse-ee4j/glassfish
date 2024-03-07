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

package com.sun.ejb.monitoring.stats;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.external.probe.provider.PluginPoint;
import com.sun.ejb.containers.EjbContainerUtilImpl;

/**
 * Utility class for Ejb monitoring.
 *
 * @author Marina Vatkina
 */
public class EjbMonitoringUtils {

    private static final Logger _logger = EjbContainerUtilImpl.getLogger();

    static final String NODE = "/";
    static final String SEP = "-";
    static final String APPLICATION_NODE = "applications" + NODE;
    static final String EJB_MONITORING_NODE = "ejb-container";
    static final String METHOD_NODE = NODE + "bean-methods" + NODE;


    static String registerComponent(String appName, String moduleName,
                String beanName, Object listener, String invokerId) {
        String beanSubTreeNode = getBeanNode(appName, moduleName, beanName);
        try {
            StatsProviderManager.register(EJB_MONITORING_NODE,
                    PluginPoint.APPLICATIONS, beanSubTreeNode, listener, null, invokerId);
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, "[**EjbMonitoringUtils**] Could not register listener for "
                    + getDetailedLoggingName(appName, moduleName, beanName), ex);

            return null;
        }

        return beanSubTreeNode;
    }

    static String registerSubComponent(String appName, String moduleName,
            String beanName, String subNode, Object listener, String invokerId) {
        String subTreeNode = getBeanNode(appName, moduleName, beanName) + NODE + subNode;
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("SUB-NODE NAME: " + subTreeNode);
        }
        try {
             StatsProviderManager.register(EJB_MONITORING_NODE,
                    PluginPoint.APPLICATIONS, subTreeNode, listener, null, invokerId);
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, "[**EjbMonitoringUtils**] Could not register subnode ["
                    + subNode + "] listener for " + getDetailedLoggingName(appName, moduleName, beanName), ex);

            return null;
        }

        return subTreeNode;
    }

    static String registerMethod(String parentNode, String mname, Object listener, String invokerId) {
        String subTreeNode = parentNode + METHOD_NODE + mname;
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("METHOD NODE NAME: " + subTreeNode);
        }
        try {
            StatsProviderManager.register(EJB_MONITORING_NODE,
                    PluginPoint.APPLICATIONS, subTreeNode, listener, null, invokerId);
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, "[**EjbMonitoringUtils**] Could not register method "
                    + "listener for " + subTreeNode, ex);
            return null;
        }

        return subTreeNode;
    }



    public static String stringify(Method m) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("==> Converting method to String: " + m);
        }
        StringBuffer sb = new StringBuffer();
        sb.append(m.getName());
        Class[] args = m.getParameterTypes();
        for (Class c : args) {
            sb.append(SEP).append(c.getName().replaceAll("_", "\\."));
        }
        String result = sb.toString().replaceAll("\\.", "\\\\.");
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("==> Converted method String: " + result);
        }
        return result;
    }

    static String getBeanNode(String appName, String moduleName, String beanName) {
        StringBuffer sb = new StringBuffer();
        /** sb.append(APPLICATION_NODE); **/

        if (appName != null) {
            sb.append(appName).append(NODE);
        }
        sb.append(moduleName).append(NODE).append(beanName);

        String beanSubTreeNode = sb.toString().replaceAll("\\.", "\\\\.").
               replaceAll("_jar", "\\\\.jar").replaceAll("_war", "\\\\.war");

        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("BEAN NODE NAME: " + beanSubTreeNode);
        }
        return beanSubTreeNode;
    }

    public static String getInvokerId(String appName, String modName, String beanName) {
        if (appName == null) {
            return "_" + modName + "_" + beanName;
        }

        return "_" + appName + "_" + modName + "_" + beanName;
    }


    public static String getDetailedLoggingName(String appName, String modName, String beanName) {
        if (appName == null) {
            return "modName=" + modName + "; beanName=" + beanName;
        }

        return "appName=" + appName + "; modName=" + modName + "; beanName=" + beanName;
    }

    public static String getLoggingName(String appName, String modName, String beanName) {
        if (appName == null) {
            return modName + ":" + beanName;
        }

        return appName + ":" + modName + ":" + beanName;
    }

}
