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

package org.glassfish.appclient.client.acc;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.appclient.client.acc.config.ClientContainer;
import org.glassfish.appclient.client.acc.config.Property;
import org.glassfish.appclient.client.acc.config.Security;
import org.glassfish.appclient.client.acc.config.TargetServer;
import org.glassfish.embeddable.client.UserError;
import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;

/**
 * Encapsulates the logic for deciding what TargetServer objects to use for
 * a given command invocation.
 *
 * @author tjquinn
 */
public class TargetServerHelper {

    private static int DEFAULT_ENDPOINT_PORT = Integer.parseInt(GlassFishORBHelper.DEFAULT_ORB_INIT_PORT);
    private static final String SSL_PROPERTY_NAME = "ssl";


    /**
     * Returns the target endpoint(s) to use for bootstrapping to the server-
     * side ORB(s).
     * <p>
     * The user can specify the set of targets in multiple ways.  If the user
     * uses the -targetserver command-line option, then that setting overrides
     * all other derivations of the target server list.  In the absence of
     * the -targetserver option, the appclient command composes a list of
     * endpoints based on (first) the target-server element(s) from the ACC
     * configuration file and adds to that list, if present, any setting from
     * a client-container/property setting of GlassFishORBManager.IIOP_ENDPOINTS_PROPERTY.
     *
     * @param clientContainer the client container JAXB fragment
     * @return target server(s) to use for bootstrapping
     */
    public static TargetServer[] targetServers(
            final ClientContainer clientContainer,
            final String commandOptionValue) throws UserError {

        /*
         * Precedence:
         * 1. command-line -targetserver option
         * 2. config file target-server elements plus any config file top-level property setting
         */
        List<TargetServer> result =
             (commandOptionValue != null)
                ? selectEndpointsFromCommandLine(commandOptionValue)
                : selectEndpointsFromConfig(clientContainer);
        return result.toArray(new TargetServer[result.size()]);
    }

    private static List<TargetServer> selectEndpointsFromConfig(ClientContainer cc) {
        /*
         * Scan the property elements in the configuration for useful information.
         */
        /*
         * If the configuration specifies the "ssl" property then add a
         * child security element to each TargetServer created for the
         * target-server elements.
         */
        boolean isGlobalSSL = false;
//        String endpointPropertySetting = null;

        for (Property p : cc.getProperty()) {
            /*
             * Per Ken S., commented out for now.
             */
/*            if (p.getName().equals(GlassFishORBManager.IIOP_ENDPOINTS_PROPERTY)) {
                endpointPropertySetting = p.getValue();
             } else {
 */
                if (p.getName().equals(SSL_PROPERTY_NAME)) {
                    isGlobalSSL = Boolean.parseBoolean(p.getValue());
                }
//            }
        }

        List<TargetServer> endpoints = new ArrayList<TargetServer>();

        /*
         * Start the target list with those specified in the configuration.
         */
        endpoints.addAll(cc.getTargetServer());

//        /*
//         * Add a TargetServer for each host:port expression in the configuration's
//         * endpoints property, if present.
//         */
//        if (endpointPropertySetting != null) {
//            endpoints.addAll(endpointsFromListList(endpointPropertySetting));
//        }

        /*
         * For all target servers assembled so far, if one does not have a
         * user-specified security child but the global SSL property was set
         * then add a security child.
         */
        for (TargetServer ts : endpoints) {
            /*
             * If the user selected SSL by setting that property in the
             * config then add a security child, because the AppClientContainer
             * expects each TargetServer object to declare its own
             * security needs explicitly.
             *
             * Note that if the user already defined a security element for the
             * target server we don't override it.
             */
            if (isGlobalSSL && ts.getSecurity() == null) {
                Security sec = new Security();
                ts.setSecurity(sec);
            }
        }


        return endpoints;
    }

    private static List<TargetServer> selectEndpointsFromCommandLine(final String serverText) {
        return endpointsFromListList(serverText);
    }

    private static final String COMMA = ",";
    private static final String PLUS = "+";

    private static List<TargetServer> endpointsFromListList(final String endpointsText) {
        List<TargetServer> result = new ArrayList<TargetServer>();
        /*
         * Permit either comma or plus sign as the list separator.  The command
         * parsing on Windows treats a comma as an argument separator.  Users can
         * include a comma-list inside double quotes on Windows to overcome this
         * but allowing + also might help a little.
         */
        String delimiter;
        if (endpointsText.contains(PLUS)) {
            delimiter = PLUS;
        } else {
            delimiter = COMMA;
        }

        for (String endpointText : endpointsText.split(delimiter)) {
            result.add(endpointFromHostColonPort(endpointText));
        }
        return result;
    }

    private static TargetServer endpointFromHostColonPort(
            final String endpointText) {
        final int colon = endpointText.indexOf(':');
        final String host;
        final int port;
        if (colon == -1) {
            host = endpointText;
            port = DEFAULT_ENDPOINT_PORT;
        } else {
            host = endpointText.substring(0, colon);
            port = Integer.parseInt(endpointText.substring(colon+1));
        }
        return new TargetServer(host, port);
    }

}
