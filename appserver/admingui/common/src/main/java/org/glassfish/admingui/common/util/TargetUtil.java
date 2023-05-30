/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.admingui.common.handlers.RestUtilHandlers;

import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;

/**
 *
 * @author anilam
 */
public class TargetUtil {

    public static boolean isCluster(String name){
        if (GuiUtil.isEmpty(name)){
            return false;
        }
        return getClusters().contains(name);
    }

    public static boolean isInstance(String name){
        if (GuiUtil.isEmpty(name)){
            return false;
        }
        return getInstances().contains(name);
    }

    public static List getStandaloneInstances(){
        List<String> result = new ArrayList<String>();
        String endpoint = GuiUtil.getSessionValue("REST_URL") + "/list-instances" ;
        Map attrsMap = new HashMap();
        attrsMap.put("standaloneonly", "true");
        attrsMap.put("nostatus", "true");
        try{
            Map responseMap = RestUtil.restRequest( endpoint , attrsMap, "get" , null, false);
            Map  dataMap = (Map) responseMap.get("data");
            Map<String, Object>  extraProps = (Map<String, Object>) dataMap.get("extraProperties");
            if (extraProps == null){
                return result;
            }
            List<Map<String, String>> props = (List<Map<String, String>>) extraProps.get("instanceList");
            if (props == null){
                return result;
            }
            result = RestUtilHandlers.getListFromMapKey(props);
        }catch (Exception ex){
            GuiUtil.getLogger().severe("Error in getStandaloneInstances ; \nendpoint = " +endpoint + ", attrsMap=" + attrsMap);
        }

        return result;
    }

    public static List getClusters(){
        List clusters = new ArrayList();
        try{
            clusters.addAll(RestUtil.getChildMap(GuiUtil.getSessionValue("REST_URL") + "/clusters/cluster").keySet());
        }catch (Exception ex){
            GuiUtil.getLogger().info(GuiUtil.getCommonMessage("log.error.getClusters") + ex.getLocalizedMessage());
            if (GuiUtil.getLogger().isLoggable(Level.FINE)){
                ex.printStackTrace();
            }
        }
        return clusters;
    }

    public static List getInstances(){
        List instances = new ArrayList();
        try{
            instances.addAll(RestUtil.getChildMap(GuiUtil.getSessionValue("REST_URL") + "/servers/server").keySet());
        }catch (Exception ex){
            GuiUtil.getLogger().info(GuiUtil.getCommonMessage("log.error.getInstances") + ex.getLocalizedMessage());
            if (GuiUtil.getLogger().isLoggable(Level.FINE)){
                ex.printStackTrace();
            }
        }
        return instances;
    }

    public static List getConfigs(){
        List config = new ArrayList();
        try{
            config.addAll(RestUtil.getChildMap(GuiUtil.getSessionValue("REST_URL") + "/configs/config").keySet());
        }catch (Exception ex){
            GuiUtil.getLogger().info(GuiUtil.getCommonMessage("log.error.getClusters") + ex.getLocalizedMessage());
            if (GuiUtil.getLogger().isLoggable(Level.FINE)){
                ex.printStackTrace();
            }
        }
        return config;
    }

    public static List getClusteredInstances(String cluster) {
        List instances = new ArrayList();
        try {
            instances.addAll(RestUtil.getChildMap(GuiUtil.getSessionValue("REST_URL") + "/clusters/cluster/" + cluster + "/server-ref").keySet());
        } catch (Exception ex) {
            GuiUtil.getLogger().severe(ex.getMessage());
        }
        return instances;
    }

    public static String getTargetEndpoint(String target){
        try{
            String encodedName = URLEncoder.encode(target, "UTF-8");
            String endpoint = (String)GuiUtil.getSessionValue("REST_URL");
            if (target.equals("server")){
                endpoint = endpoint + "/servers/server/server";
            }else{
                List clusters = TargetUtil.getClusters();
                if (clusters.contains(target)){
                    endpoint = endpoint + "/clusters/cluster/" + encodedName;
                }else{
                    endpoint = endpoint + "/servers/server/" + encodedName;
                }
            }
            return endpoint;
        }catch(Exception ex){
            GuiUtil.getLogger().info(GuiUtil.getCommonMessage("log.error.getTargetEndpoint") + ex.getLocalizedMessage());
            if (GuiUtil.getLogger().isLoggable(Level.FINE)){
                ex.printStackTrace();
            }
            return "";
        }
    }

    public static String getConfigName(String target) {
        String endpoint = getTargetEndpoint(target);
        return (String)RestUtil.getAttributesMap(endpoint).get("configRef");
    }

    public static Collection<String> getHostNames(String target) {
        Set<String> hostNames = new HashSet();
        hostNames.toArray();
        List clusters = TargetUtil.getClusters();
        List<String> instances = new ArrayList();
        if (clusters.contains(target)){
             instances = getClusteredInstances(target);
        } else {
            instances.add(target);
        }

        for (String instance : instances) {
            String hostName = null;
            String ep = (String)GuiUtil.getSessionValue("REST_URL") + "/servers/server/" + instance;
            String node =
                    (String)RestUtil.getAttributesMap(ep).get("nodeRef");
            if (node != null) {
                ep = (String)GuiUtil.getSessionValue("REST_URL") + "/nodes/node/" + node;
                hostName =  (String)RestUtil.getAttributesMap(ep).get("nodeHost");
            }
            if (hostName == null)
                hostName = "localhost";
            hostNames.add(hostName);
        }
        return hostNames;
    }
}
