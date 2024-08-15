/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * JmsResourceHandler.java
 *
 * Created on January 9, 2013, 2:32 PM
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

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.RestUtil;

public class JmsResourceHandler {

    public JmsResourceHandler() {
    }

    /**
     * <p> This handler return the list of JMS Resources to be displayed in the table.
     */
    @Handler(id = "getJmsResourcesInfo",
    input = {
        @HandlerInput(name = "resourcesList", type = List.class),
        @HandlerInput(name = "isConnectionFactory", type = Boolean.class)},
    output = {
        @HandlerOutput(name = "result", type = java.util.List.class)
    })
    public static void getJmsResourcesInfo(HandlerContext handlerCtx) {

        List<Map<String, Object>> resourcesList = (List) handlerCtx.getInputValue("resourcesList");
        Boolean isConnectionFactory = (Boolean) handlerCtx.getInputValue("isConnectionFactory");
        String prefix = isConnectionFactory ? GuiUtil.getSessionValue("REST_URL") + "/resources/connector-resource/" :
                GuiUtil.getSessionValue("REST_URL") + "/resources/admin-object-resource/";
        try{
            for(Map<String, Object> one : resourcesList){
                String encodedName = URLEncoder.encode((String) one.get("name"), "UTF-8");
                String endpoint = prefix + encodedName;
                Map attrs = (Map) RestUtil.getAttributesMap(endpoint);
                String desc = null;
                if (isConnectionFactory){
                    String poolName = URLEncoder.encode((String)attrs.get("poolName"), "UTF-8");
                    String e1 = (String) GuiUtil.getSessionValue("REST_URL") + "/resources/connector-connection-pool/" + poolName;
                    Map poolAttrs = (Map) RestUtil.getAttributesMap(e1);
                    one.put("resType", (String) poolAttrs.get("connectionDefinitionName"));
                    String lname = (String) one.get("logical-jndi-name");
                    one.put("logicalJndiName", (lname==null)? "" : lname);
                    one.put("encodedPoolName", poolName);
                    one.put("objectType", (String) attrs.get("objectType"));
                    desc = (String)poolAttrs.get("description");
                }else{
                    one.put("resType", (String) attrs.get("resType"));
                    desc = (String)attrs.get("description");
                }
                one.put("selected", false);
                one.put("enabled", (String) attrs.get("enabled"));
                one.put("encodedName", encodedName);
                one.put("description", (desc == null)? "" : desc);
            }
        }catch(Exception ex){
             GuiUtil.getLogger().info(GuiUtil.getCommonMessage("log.error.getJMSResources") + ex.getLocalizedMessage());
            if (GuiUtil.getLogger().isLoggable(Level.FINE)){
                ex.printStackTrace();
            }
        }
        handlerCtx.setOutputValue("result", resourcesList);
    }


}
