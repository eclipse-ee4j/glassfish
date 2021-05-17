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

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.logging.Level;

import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.RestUtil;


public class LoggingHandlers {

    /** Creates a new instance of InstanceHandler */
    public LoggingHandlers() {
    }


    @Handler(id = "getLoggerLevels",
    input = {
        @HandlerInput(name = "loggerLevels", type = Map.class, required = true)},
    output = {
        @HandlerOutput(name = "loggerList", type = List.class)
    })
    public static void getLoggerLevels(HandlerContext handlerCtx) {

        Map<String, String> loggerLevels = (Map) handlerCtx.getInputValue("loggerLevels");
        List result = new ArrayList();
        if (loggerLevels != null)    {
            for(Map.Entry<String,String> e : loggerLevels.entrySet()){
                Map oneRow = new HashMap();
                    oneRow.put("loggerName", e.getKey());
                    oneRow.put("level", e.getValue());
                    oneRow.put("selected", false);
                    result.add(oneRow);
            }
        }
        handlerCtx.setOutputValue("loggerList",  result);
     }


    @Handler(id = "changeLoggerLevels",
    input = {
        @HandlerInput(name = "newLogLevel", type = String.class, required = true),
        @HandlerInput(name = "allRows", type = List.class, required = true)},
    output = {
        @HandlerOutput(name = "newList", type = List.class)})
    public static void changeLoggerLevels(HandlerContext handlerCtx) {
        String newLogLevel = (String) handlerCtx.getInputValue("newLogLevel");
        List obj = (List) handlerCtx.getInputValue("allRows");
        List<Map> allRows = (List) obj;
        if (GuiUtil.isEmpty(newLogLevel)){
            handlerCtx.setOutputValue("newList",  allRows);
            return;
        }
        for(Map oneRow : allRows){
            boolean selected = (Boolean) oneRow.get("selected");
            if (selected){
                oneRow.put("level", newLogLevel);
                oneRow.put("selected", false);
            }
        }
        handlerCtx.setOutputValue("newList",  allRows);
     }



    @Handler(id = "updateLoggerLevels",
    input = {
        @HandlerInput(name = "allRows", type = List.class, required = true),
        @HandlerInput(name = "config", type = String.class, required = true)})
    public static void updateLoggerLevels(HandlerContext handlerCtx) {
        List<Map<String,Object>> allRows = (List<Map<String,Object>>) handlerCtx.getInputValue("allRows");
        String config = (String)handlerCtx.getInputValue("config");
        Map<String, Object> props = new HashMap();
        try{
            StringBuilder sb = new StringBuilder();
            String sep = "";
            for(Map<String, Object> oneRow : allRows){
                if ( !GuiUtil.isEmpty((String) oneRow.get("loggerName"))){
                    sb.append(sep).append(oneRow.get("loggerName")).append("=").append(oneRow.get("level"));
                    sep=":";
                }
            }
            props.put("id", sb.toString());
            props.put("target", config);
            RestUtil.restRequest((String)GuiUtil.getSessionValue("REST_URL") + "/set-log-levels.json",
                    props, "POST", null, false, true);
            // after saving logger levels remove the deleted loggers
            deleteLoggers(allRows, config);
        }catch(Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
            if (GuiUtil.getLogger().isLoggable(Level.FINE)){
                ex.printStackTrace();
            }
        }

     }

    public static void deleteLoggers(List<Map<String, Object>> allRows, String configName) {
        ArrayList<String> newLoggers = new ArrayList<String>();
        HashMap attrs = new HashMap();
        attrs.put("target", configName);
        Map result = RestUtil.restRequest((String)GuiUtil.getSessionValue("REST_URL") + "/list-log-levels.json",
                    attrs, "GET", null, false);
        List<String> oldLoggers = (List<String>)((HashMap)((HashMap) result.get("data")).get("extraProperties")).get("loggers");
        for(Map<String, Object> oneRow : allRows){
            newLoggers.add((String)oneRow.get("loggerName"));
        }
        // delete the removed loggers
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (String logger : oldLoggers) {
            if (!newLoggers.contains(logger)) {
                sb.append(sep).append(logger);
                sep=":";
            }
        }
        if (sb.length() > 0){
            attrs = new HashMap();
            attrs.put("id", sb.toString());
            attrs.put("target", configName);
            RestUtil.restRequest((String)GuiUtil.getSessionValue("REST_URL") + "/delete-log-levels", attrs, "POST", null, false);
        }
    }

    @Handler(id = "saveLoggingAttributes",
    input = {
        @HandlerInput(name = "attrs", type = Map.class, required=true),
        @HandlerInput(name = "config", type = String.class, required=true)
    })

    public static void saveLoggingAttributes(HandlerContext handlerCtx) {
        Map<String,Object> attrs = (Map<String,Object>) handlerCtx.getInputValue("attrs");
        String config = (String)handlerCtx.getInputValue("config");
        Map<String, Object> props = new HashMap();
        try{
            for (Map.Entry<String, Object> e : attrs.entrySet()) {
                String key=e.getKey();
                if ((key.equals("com.sun.enterprise.server.logging.SyslogHandler.useSystemLogging")||
                      key.equals("com.sun.enterprise.server.logging.GFFileHandler.logtoConsole") ||
                      key.equals("com.sun.enterprise.server.logging.GFFileHandler.multiLineMode") ||
                     key.equals("com.sun.enterprise.server.logging.GFFileHandler.rotationOnDateChange" ))
                        && (e.getValue() == null)) {
                    attrs.put(key, "false");
                }
                props.put("id", key + "='" + attrs.get(key) + "'");
                props.put("target", config);
                RestUtil.restRequest((String)GuiUtil.getSessionValue("REST_URL") + "/set-log-attributes",
                    props, "POST", null, false, true);
            }
        }catch (Exception ex){
            GuiUtil.handleException(handlerCtx, ex);
            if (GuiUtil.getLogger().isLoggable(Level.FINE)){
                ex.printStackTrace();
            }
        }
     }

}


