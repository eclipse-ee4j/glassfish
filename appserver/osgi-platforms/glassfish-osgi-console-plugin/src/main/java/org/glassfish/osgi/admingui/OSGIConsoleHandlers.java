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

package org.glassfish.osgi.admingui;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import org.glassfish.internal.api.Globals;

/**
 * handler to get instance's host and port information which is used to
 * form the http url for osgi-console.
 *
 * @author mohit
 */
public class OSGIConsoleHandlers {

    private static String http_port = "HTTP_LISTENER_PORT";
    private static String consolePath = "/osgi/system/console/bundles";

    @Handler(id = "getConsoleUrl",
    input = {
        @HandlerInput(name = "instanceName", type = String.class, required = true)},
    output = {
        @HandlerOutput(name = "consoleUrl", type = String.class)})
    public static void getConsoleUrl(HandlerContext handlerCtx) {
        String instanceName = (String) handlerCtx.getInputValue("instanceName");

        Domain domain = Globals.get(Domain.class);
        Server server = domain.getServerNamed(instanceName);

        String port = null;
        SystemProperty httpPort = server.getSystemProperty(http_port);
        if(httpPort != null) {
            port = httpPort.getValue();
        } else {
            //if port is not set as system property, get it from config
            Config cfg = server.getConfig();
            SystemProperty httpConfigPort = cfg.getSystemProperty(http_port);
            if(httpConfigPort != null){
                port = httpConfigPort.getValue();
            }
        }

        if(port == null) {
            throw new RuntimeException("Not able to get HTTP_LISTENER_PORT " +
                    "for instance : " + instanceName);
        }

        Node node = domain.getNodeNamed(server.getNodeRef());
        String host = node.getNodeHost();
        String consoleUrl = "http://" + host + ":" + port + consolePath;

        handlerCtx.setOutputValue("consoleUrl", consoleUrl);
    }

}
