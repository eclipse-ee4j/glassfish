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
 * Class.java
 *
 * Created on November 11, 2003, 1:45 PM
 */

package org.glassfish.config.support;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemProperty;

import java.util.List;

/**
 * Utility for getting the value of a system-property of an instance, particularly for an instance that is not the
 * current running instance. The current running instance automatically has tokens in the config resolved. The value
 * returned is the value of the system property which has the highest precedence. The system-property defined at higher
 * precedence levels overrides system-property defined at lower precedence levels. The order of precedence from highest
 * to lowest is 1. server 2. cluster 3. config 4. domain
 *
 * @author kebbs
 * @author Jennifer Chou
 */
public class PropertyResolver {

    private Domain _domain = null;
    private Cluster _cluster = null;
    private Server _server = null;
    private Config _config = null;

    public PropertyResolver(Domain domain, String instanceName) {
        _domain = domain;
        _server = _domain.getServerNamed(instanceName);
        if (_server != null) {
            _config = _domain.getConfigNamed(_server.getConfigRef());
        } else {
            _config = _domain.getConfigNamed(instanceName);
        }
        _cluster = _domain.getClusterForInstance(instanceName);
    }

    /**
     * Given a propery name, return its corresponding value in the specified SystemProperty array. Return null if the
     * property is not found.
     */
    private String getPropertyValue(String propName, List<SystemProperty> props) {
        String propVal = null;
        for (SystemProperty prop : props) {
            if (prop.getName().equals(propName)) {
                return prop.getValue();
            }
        }
        return propVal;
    }

    /**
     * Given a property name, return its corresponding value as defined in the domain, configuration, cluster, or server
     * element. Return null if the property is not found. Property values at the server override those at the configuration
     * which override those at the domain level. Does not check if the property is available in java.lang.System. This
     * restriction is to prevent incorrect values being returned when trying to retrieve properties for instances other than
     * the currently running server (such as DAS). In this case, we don't want to incorrectly return the DAS
     * java.lang.System property.
     */
    public String getPropertyValue(String propName) {
        if (propName.startsWith("${") && propName.endsWith("}")) {
            propName = propName.substring(2, propName.lastIndexOf("}"));
        }
        String propVal = null;
        //First look for a server instance property matching the propName
        if (_server != null) {
            propVal = getPropertyValue(propName, _server.getSystemProperty());
        }
        if (propVal == null) {
            if (_cluster != null) {
                //If not found in the server instance, look for the propName in the
                //cluster
                propVal = getPropertyValue(propName, _cluster.getSystemProperty());
            }
            if (propVal == null) {
                if (_config != null) {
                    //If not found in the server instance or cluster, look for the
                    //propName in the config
                    propVal = getPropertyValue(propName, _config.getSystemProperty());
                    if (propVal == null) {
                        if (_domain != null) {
                            //Finally if the property is not found in the server, cluster,
                            //or configuration, look for the propName in the domain
                            propVal = getPropertyValue(propName, _domain.getSystemProperty());
                        }
                    }
                }
            }
        }

        return propVal;
    }
}
