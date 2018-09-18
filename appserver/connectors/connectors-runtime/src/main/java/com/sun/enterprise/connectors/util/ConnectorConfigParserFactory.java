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

package com.sun.enterprise.connectors.util;

/**
 * This is a factory class for the connector configuration parser classes.
 *
 * @author Srikanth P
 */
public class ConnectorConfigParserFactory {


    /**
     * Returns a specific connector configuration parser class based on the
     * type of configurations to parse.
     *
     * @param type Parser class type.
     * @return parser class
     */
    public static ConnectorConfigParser getParser(String type) {

        if (type == null) {
            return null;
        }
        if (type.equals(ConnectorConfigParser.AOR)) {
            return new AdminObjectConfigParserImpl();
        } else if (type.equals(ConnectorConfigParser.MCF)) {
            return new MCFConfigParserImpl();
        } else if (type.equals(ConnectorConfigParser.RA)) {
            return new ResourceAdapterConfigParserImpl();
        } else if (type.equals(ConnectorConfigParser.MSL)) {
            return new MessageListenerConfigParserImpl();
        } else {
            return null;
        }
    }
}
