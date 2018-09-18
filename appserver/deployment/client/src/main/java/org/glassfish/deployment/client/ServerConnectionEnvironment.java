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

package org.glassfish.deployment.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * This class defines the additional environment needed to
 * connect to a particular application server deployment
 * backend.  For now, the properties supported are specific
 * to jmx https connector.  We will extend it to support
 * jmx rmi connector when the jmx implementation is ready.
 * <br>
 * Environment supported in this class are defined by the
 * JMX connectors.  
 * @see also com.sun.enterprise.admin.jmx.remote.DefaultConfiguration
 * <br>
 * For example, to set a client trust manager, the key of env shall
 * be DefaultConfiguration.TRUST_MANAGER_PROPERTY_NAME.
 *
 * @author Qingqing Ouyang
 */
public final class ServerConnectionEnvironment extends HashMap {
    
    /** 
     * Creates a new instance of ServerConnectionEnvironment
     */
    public ServerConnectionEnvironment() {
        super();
    }
    
    /** 
     * Creates a new instance of ServerConnectionEnvironment
     */
    public ServerConnectionEnvironment(Map env) {
        super(env);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("ServerConnectionEnvironments: \n");
        Iterator entries = entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            buf.append("Key = " + entry.getKey());
            buf.append("    ");
            buf.append("Value = " + entry.getValue());
            buf.append(";\n");
        }

        return buf.toString();
    }
}
