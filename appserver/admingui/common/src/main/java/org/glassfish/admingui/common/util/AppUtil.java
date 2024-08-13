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

package org.glassfish.admingui.common.util;

import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


/**
 *
 * @author anilam
 */
public class AppUtil {

    public static List<String> getSnifferListOfModule(String appName, String moduleName){
        Map subMap = RestUtil.restRequest(
            GuiUtil.getSessionValue("REST_URL")+"/applications/application/" + appName + "/module/" + moduleName + "/engine", null, "GET", null, false);
        final Map dataMap = (Map) subMap.get("data");
        List sniffersList = new ArrayList();
        if (dataMap != null){
            final Map extraProperties = (Map)(dataMap).get("extraProperties");
            if (extraProperties != null){
                final Map<String, Object> childResourcesMap = (Map) extraProperties.get("childResources");
                if (childResourcesMap != null){
                    //List<String> sniffers =  new ArrayList( childResourcesMap.keySet());
                    for (String oneSniffer: childResourcesMap.keySet()){
                        if (sniffersHide.contains(oneSniffer) )
                            continue;
                        sniffersList.add(oneSniffer);
                    }
                    Collections.sort(sniffersList);
                    return sniffersList;
                }
            }
        }
        return sniffersList;
    }

    public static boolean isApplicationEnabled(String appName,  String target){
        String prefix = (String) GuiUtil.getSessionValue("REST_URL");
        List clusters = TargetUtil.getClusters();
        List standalone = TargetUtil.getStandaloneInstances();
        standalone.add("server");
        Map attrs = null;
        String endpoint="";
        if (clusters.contains(target)){
            endpoint = prefix + "/clusters/cluster/" + target + "/application-ref/" + appName;
            attrs = RestUtil.getAttributesMap(prefix + endpoint);
        }else{
            endpoint = prefix+"/servers/server/" + target + "/application-ref/" + appName;
            attrs = RestUtil.getAttributesMap(endpoint);
        }
        return Boolean.parseBoolean((String) attrs.get("enabled"));
    }

    static public Map getWsEndpointMap(String appName, String moduleName, List snifferList){
        Map wsAppMap = new HashMap();
        try{
            String encodedAppName = URLEncoder.encode(appName, "UTF-8");
            String encodedModuleName = URLEncoder.encode(moduleName, "UTF-8");
            String prefix = GuiUtil.getSessionValue("REST_URL") + "/applications/application/" + encodedAppName;
            if (snifferList.contains("webservices")){
                Map wsAttrMap = new HashMap();
                //wsAttrMap.put("applicationname", encodedAppName);
                wsAttrMap.put("modulename", encodedModuleName);
                Map wsMap = RestUtil.restRequest(prefix+"/list-webservices", wsAttrMap, "GET", null, false);
                Map extraProps = (Map)((Map)wsMap.get("data")).get("extraProperties");
                if (extraProps != null){
                    wsAppMap = (Map) extraProps.get(appName);
                }
            }
        }catch(Exception ex){
            GuiUtil.getLogger().info(GuiUtil.getCommonMessage("log.error.wsException") + ex.getLocalizedMessage());
            if (GuiUtil.getLogger().isLoggable(Level.FINE)){
                ex.printStackTrace();
            }
        }
        return wsAppMap;
    }

    static public Map getEndpointDetails(Map wsEndpointMap, String moduleName, String componentName){
        if (wsEndpointMap == null){
            return null;
        }
        Map modMap = (Map) wsEndpointMap.get(moduleName);
        if (modMap == null){
            return null;
        }
        return (Map) modMap.get(componentName);
    }

    static public void manageAppTarget(String applicationName, String targetName, boolean add, String enabled, List clusterList, List standaloneList, HandlerContext handlerCtx){
        List clusters = (clusterList == null) ? TargetUtil.getClusters() : clusterList;
        String clusterEndpoint = GuiUtil.getSessionValue("REST_URL")+"/clusters/cluster/";
        String serverEndpoint = GuiUtil.getSessionValue("REST_URL")+"/servers/server/";
        String endpoint ;
        Map attrs = new HashMap();

        if (clusters.contains(targetName)){
            endpoint = clusterEndpoint + targetName + "/application-ref" ;
        }else{
            endpoint = serverEndpoint + targetName + "/application-ref" ;
        }
        if (add){
            attrs.put("id", applicationName);
            if (enabled != null){
                attrs.put("enabled", enabled);
            }
        }else{
            endpoint = endpoint + "/" + applicationName;
        }
        attrs.put("target", targetName);
        RestUtil.restRequest(endpoint, attrs, (add)? "POST" : "DELETE", handlerCtx, false);
    }

    static public Boolean doesAppContainsResources(String appName){
        return RestUtil.doesProxyExist(GuiUtil.getSessionValue("REST_URL") + "/applications/application/" + appName + "/resources");
    }

    static public String getAppScopedResType(String resName, String type){
        int index = appResTypes.indexOf(resName);
        if (index != -1){
            if (type.equals("display")) {
                return appResTypesToDisplay.get(index);
            } else if (type.equals("edit")) {
                return appResTypesEdit.get(index);
            }
        }
        return null;
    }

    static final public List sniffersHide = new ArrayList();
    static {
        sniffersHide.add("security");
    }
    static final public List<String> appResTypes = new ArrayList<String>();
    static {
        appResTypes.add("<JdbcResource>");
        appResTypes.add("<ConnectorResource>");
        appResTypes.add("<ExternalJndiResource>");
        appResTypes.add("<CustomResource>");
        appResTypes.add("<AdminObjectResource>");
        appResTypes.add("<MailResource>");
        appResTypes.add("<JdbcConnectionPool>");
        appResTypes.add("<ConnectorConnectionPool>");
        appResTypes.add("<ResourceAdapterConfig>");
        appResTypes.add("<WorkSecurityMap>");
    }
    static final public List<String> appResTypesToDisplay = new ArrayList<String>();
    static {
        appResTypesToDisplay.add(GuiUtil.getMessage("tree.jdbcResources"));
        appResTypesToDisplay.add(GuiUtil.getMessage("tree.connectorResources"));
        appResTypesToDisplay.add(GuiUtil.getMessage("tree.externalResources"));
        appResTypesToDisplay.add(GuiUtil.getMessage("tree.customResources"));
        appResTypesToDisplay.add(GuiUtil.getMessage("tree.adminObjectResources"));
        appResTypesToDisplay.add(GuiUtil.getMessage("tree.javaMailSessions"));
        appResTypesToDisplay.add(GuiUtil.getMessage("common.JdbcConnectionPools"));
        appResTypesToDisplay.add(GuiUtil.getMessage("tree.connectorConnectionPools"));
        appResTypesToDisplay.add(GuiUtil.getMessage("tree.resourceAdapterConfigs"));
        appResTypesToDisplay.add(GuiUtil.getMessage("tree.workSecurityMaps"));
    }
    static final public List<String> appResTypesEdit = new ArrayList<String>();
    static {
        appResTypesEdit.add("jdbc/jdbcResourceEdit.jsf?name=");
        appResTypesEdit.add("jca/connectorResourceEdit.jsf?name=");
        appResTypesEdit.add("full/externalResourceEdit.jsf?name=");
        appResTypesEdit.add("full/customResourceEdit.jsf?name=");
        appResTypesEdit.add("jca/adminObjectEdit.jsf?name=");
        appResTypesEdit.add("full/mailResourceEdit.jsf?name=");
        appResTypesEdit.add("jdbc/jdbcConnectionPoolEdit.jsf?name=");
        appResTypesEdit.add("jca/connectorConnectionPoolEdit.jsf?name=");
        appResTypesEdit.add("jca/resourceAdapterConfigEdit.jsf?name=");
        appResTypesEdit.add("jca/workSecurityMapEdit.jsf?mapName=");
    }
}

