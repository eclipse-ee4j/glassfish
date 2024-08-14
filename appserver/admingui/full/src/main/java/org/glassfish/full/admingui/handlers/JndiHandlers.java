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
 * ConnectorHandlers.java
 *
 * Created on Sept 1, 2006, 8:32 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
/**
 *
 */
package org.glassfish.full.admingui.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JndiHandlers {

    /** Creates a new instance of ConnectorsHandler */
    public JndiHandlers() {
    }



    @Handler(id="gf.getJndiResourceForCreate",
    input={
        @HandlerInput(name="resources", type=Map.class)},
    output={
        @HandlerOutput(name="result", type=List.class),
        @HandlerOutput(name="attrMap", type=Map.class),
        @HandlerOutput(name="classnameOption", type=String.class),
        @HandlerOutput(name="factoryMap", type=String.class)})
    public static void getJndiResourceForCreate(HandlerContext handlerCtx) {

        Map<String, String> emap = (Map<String, String>) handlerCtx.getInputValue("resources");
        List result = new ArrayList();
        result.addAll(emap.keySet());
        String factoryMap = getFactoryMap(emap);

        handlerCtx.setOutputValue("result",result);
        handlerCtx.setOutputValue("classnameOption", "predefine");
        Map attrMap = new HashMap();
        attrMap.put("predefinedClassname", Boolean.TRUE);
        handlerCtx.setOutputValue("attrMap", attrMap);
        handlerCtx.setOutputValue("factoryMap", factoryMap);
    }

    @Handler(id="gf.getJndiResourceAttrForEdit",
    input={
        @HandlerInput(name="resType", type=String.class),
        @HandlerInput(name="resources", type=Map.class)},
    output={
        @HandlerOutput(name="attrMap",      type=Map.class),
        @HandlerOutput(name="classnameOption",      type=String.class),
        @HandlerOutput(name="result",      type=List.class),
        @HandlerOutput(name="factoryMap", type=String.class)})

    public static void getJndiResourceAttrForEdit(HandlerContext handlerCtx) {
        List result = new ArrayList();
        Map<String, String> emap = (Map<String, String>) handlerCtx.getInputValue("resources");
        result.addAll(emap.keySet());
        String factoryMap = getFactoryMap(emap);
        handlerCtx.setOutputValue("factoryMap", factoryMap);

        String resType = (String) handlerCtx.getInputValue("resType");
        Map attrMap = new HashMap();
        if (emap.containsKey(resType)) {
            handlerCtx.setOutputValue("classnameOption", "predefine");
            attrMap.put("predefinedClassname", Boolean.TRUE);
            attrMap.put("classname", resType);

        } else {
            //Custom realm class
            handlerCtx.setOutputValue("classnameOption", "input");
            attrMap.put("predefinedClassname", Boolean.FALSE);
            attrMap.put("classnameInput", resType);

        }
        handlerCtx.setOutputValue("result", result);
        handlerCtx.setOutputValue("attrMap", attrMap);
    }

    private static String getFactoryMap(Map<String,String> emap){
        String factMap = null;
        StringBuilder factoryMap = new StringBuilder();
        if(emap != null) {
            String sep = "";
            for(Map.Entry<String,String> e : emap.entrySet()){
                    factoryMap.append(sep)
                            .append("\"")
                            .append(e.getKey())
                            .append("\": '")
                            .append(e.getValue())
                            .append("'");
                    sep = ",";
            }
        }
        factMap = "{" + factoryMap.toString() + "}";
        return factMap;
    }

        @Handler(id="updateJndiResourceAttrs",
        input={
                @HandlerInput(name="classnameOption",   type=String.class),
                @HandlerInput(name="attrMap",      type=Map.class)},
        output={
            @HandlerOutput(name="resType", type=String.class)
            } )
        public static void updateJndiResourceAttrs(HandlerContext handlerCtx){
            String option = (String) handlerCtx.getInputValue("classnameOption");
            Map<String,String> attrMap = (Map)handlerCtx.getInputValue("attrMap");
            String restype ;
            if (option.equals("predefine")) {
                restype = attrMap.get("classname");
            } else {
                restype = attrMap.get("classnameInput");
            }
            handlerCtx.setOutputValue("resType", restype);
        }
}
