/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * InstanceHandler.java
 *
 * Created on August 10, 2006, 2:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/**
 *
 * @author anilam
 */
package org.glassfish.admingui.common.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.RestUtil;

public class InstanceHandler {

    /** Creates a new instance of InstanceHandler */
    public InstanceHandler() {
    }

    @Handler(id="getJvmOptionsValues",
        input={
            @HandlerInput(name="endpoint",   type=String.class, required=true),
            @HandlerInput(name="attrs", type=Map.class, required=false)
        },
        output={
            @HandlerOutput(name="result", type=java.util.List.class)})
    public static void getJvmOptionsValues(HandlerContext handlerCtx) {
        try{
            ArrayList<String> list = getJvmOptions(handlerCtx);
            handlerCtx.setOutputValue("result", GuiUtil.convertArrayToListOfMap(list.toArray(), "value"));
        }catch (Exception ex){
            handlerCtx.setOutputValue("result", new HashMap());
            GuiUtil.getLogger().info(GuiUtil.getCommonMessage("log.error.getJvmOptionsValues") + ex.getLocalizedMessage());
            if (GuiUtil.getLogger().isLoggable(Level.FINE)){
                ex.printStackTrace();
            }
        }
    }

     public static ArrayList getJvmOptions(HandlerContext handlerCtx) {
        ArrayList<String> list;
        String endpoint = (String) handlerCtx.getInputValue("endpoint");
        if (!endpoint.endsWith(".json"))
            endpoint = endpoint + ".json";
        Map<String, Object> attrs = (Map<String, Object>) handlerCtx.getInputValue("attrs");
        Map result = (HashMap) RestUtil.restRequest(endpoint, attrs, "get", handlerCtx, false).get("data");
        list = (ArrayList<String>) ((Map<String, Object>) result.get("extraProperties")).get("leafList");
        if (list == null)
            list = new ArrayList<String>();
        return list;
    }

   @Handler(id="saveJvmOptionValues",
        input={
            @HandlerInput(name="endpoint",   type=String.class, required=true),
            @HandlerInput(name="target",   type=String.class, required=true),
            @HandlerInput(name="attrs", type=Map.class, required=false),
            @HandlerInput(name="profiler", type=String.class, required=true),
            @HandlerInput(name="options",   type=List.class),
            @HandlerInput(name="deleteProfileEndpoint",   type=String.class),
            @HandlerInput(name="origList",   type=List.class)
            } )
   public static void saveJvmOptionValues(HandlerContext handlerCtx) {
        String endpoint = (String) handlerCtx.getInputValue("endpoint");
        String target = (String) handlerCtx.getInputValue("target");
        try {
            List<Map> options = (List<Map>) handlerCtx.getInputValue("options");
            ArrayList<String> newList = new ArrayList();
            for (Map oneRow : options) {
                newList.add((String) oneRow.get(PROPERTY_VALUE));
            }
            ArrayList<String> oldList = getJvmOptions(handlerCtx);
            if (newList.equals(oldList)) {
                // if old list is same as new list, return without saving anything
                return;
            }
            Map<String, Object> payload = new HashMap<String, Object>();
            payload.put("profiler", (String)handlerCtx.getInputValue("profiler"));
            prepareJvmOptionPayload(payload, target, options);
            RestUtil.restRequest(endpoint, payload, "POST", handlerCtx, false, true);
        } catch (Exception ex) {
            //If this is called during create profile, we want to delete the profile which was created, and stay at the same
            //place for user to fix the jvm options.
            String deleteProfileEndpoint = (String) handlerCtx.getInputValue("deleteProfileEndpoint");
            if (!GuiUtil.isEmpty(deleteProfileEndpoint)){
                Map attrMap = new HashMap();
                attrMap.put("target", (String) handlerCtx.getInputValue("target"));
                RestUtil.restRequest(deleteProfileEndpoint, attrMap, "DELETE", handlerCtx, false, false);
            }

            //If the origList is not empty,  we want to restore it. Since POST remove all options first and then add it back. As a
            //result, all previous existing option is gone.
            List<Map> origList = (List<Map>) handlerCtx.getInputValue("origList");
            Map<String, Object> payload1 = new HashMap<String, Object>();
            if (endpoint.contains("profiler")) {
                payload1.put("profiler", "true");
            }
            if ( (origList != null) && origList.size()>0){
                prepareJvmOptionPayload(payload1, target, origList);
                RestUtil.restRequest(endpoint, payload1, "POST", handlerCtx, false, false);
            }
            GuiUtil.handleException(handlerCtx, ex);
        }
    }

    private static void prepareJvmOptionPayload(Map payload, String target, List<Map> options){
        payload.put("target", target);
        for (Map oneRow : options) {
            String str = (String) oneRow.get(PROPERTY_VALUE);
            String str1 = UtilHandlers.escapePropertyValue(str);         //refer to GLASSFISH-19069
            ArrayList kv = getKeyValuePair(str1);
            payload.put((String)kv.get(0), kv.get(1));
        }
    }

    public static ArrayList getKeyValuePair(String str) {
        ArrayList list = new ArrayList(2);
        int index = str.indexOf("=");
        String key = "";
        String value = "";
        if (index != -1) {
            key = str.substring(0,str.indexOf("="));
            value = str.substring(str.indexOf("=")+1,str.length());
        } else {
            key = str;
        }
        if (key.startsWith("-XX:"))
            key = "\"" + key + "\"";
        list.add(0, key);
        list.add(1, value);
        return list;
    }

    private static final String PROPERTY_VALUE = "value";
}


