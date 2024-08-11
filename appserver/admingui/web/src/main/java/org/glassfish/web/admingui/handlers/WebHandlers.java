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

package org.glassfish.web.admingui.handlers;


import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.admingui.common.util.GuiUtil;

/**
 *
 * @author Anissa Lam
 */
public class WebHandlers {

    @Handler(id="changeNetworkListenersInVS",
    input={
        @HandlerInput(name = "vsAttrs", type = Map.class, required = true),
        @HandlerInput(name = "listenerName", type = String.class, required = true),
        @HandlerInput(name = "addFlag", type = Boolean.class, required = true)},
        output={
            @HandlerOutput(name="result", type=Map.class)})
    public static void changeNetworkListenersInVS(HandlerContext handlerCtx){
        //get the virtual server and add this network listener to it.
        Map vsAttrs = (HashMap) handlerCtx.getInputValue("vsAttrs");
        String listenerName = (String) handlerCtx.getInputValue("listenerName");
        Boolean addFlag = (Boolean) handlerCtx.getInputValue("addFlag");
        String nwListeners = (String)vsAttrs.get("networkListeners");
        List<String> listeners = GuiUtil.parseStringList(nwListeners, ",");
        if (addFlag.equals(Boolean.TRUE)){
            if (! listeners.contains(listenerName)){
                listeners.add(listenerName);
            }
        }else {
            if (listeners.contains(listenerName)){
                listeners.remove(listenerName);
            }
        }
        String ll = GuiUtil.listToString(listeners, ",");
        vsAttrs.put("networkListeners", ll);
        handlerCtx.setOutputValue("result", vsAttrs);
    }
}
