/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.deployment.ConnectorConfigProperty;
import com.sun.enterprise.deployment.ConnectorDescriptor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * This is Resource Adapter configuration parser. It parses the
 * ra.xml file for the Resources adapter javabean properties
 *
 * @author Srikanth P
 */
public class ResourceAdapterConfigParserImpl implements ConnectorConfigParser {

    //private final static Logger _logger = LogDomains.getLogger(ResourceAdapterConfigParserImpl.class, LogDomains.RSR_LOGGER);

    /**
     * Default constructor.
     */
    public ResourceAdapterConfigParserImpl() {
    }

    /**
     * Parses the ra.xml for the Resource Adapter javabean properties.
     * Here the second parameter connectionDefName is not used and can
     * be null or any value.
     * <p/>
     * It throws ConnectorRuntimeException if module dir is null or
     * corresponing rar is not deployed i.e invalid moduleDir parameter.
     *
     * @param desc ConnectorDescriptor pertaining to rar.
     * @param Not  used. Can be null or any value,
     * @return Javabean properties with the propety names and values
     *         of propeties. The property values will be the values
     *         mentioned in ra.xml if present. Otherwise it will be the
     *         default values obtained by introspecting the javabean.
     *         In both the case if no value is present, empty String is
     *         returned as the value.
     * @throws ConnectorRuntimeException if moduleDir is null .
     *                                   If corresponding rar is not deployed i.e moduleDir is invalid.
     */
    @Override
    public Properties getJavaBeanProps(ConnectorDescriptor desc,
                                       String connectionDefName, String rarName) throws ConnectorRuntimeException {

        if (desc == null) {
            throw new ConnectorRuntimeException("Invalid arguments");
        }

        /* ddVals           -> Properties present in ra.xml
        *  introspectedVals -> All properties with values
        *                                 obtained by introspection of resource
        *                                  adapter javabean
        *  mergedVals       -> merged props of raConfigPros and
        *                                 allraConfigPropsWithDefVals
        */

        Set ddVals = desc.getConfigProperties();
        Properties mergedVals = null;
        String className = desc.getResourceAdapterClass();
        Properties introspectedVals = null;
        if (className != null && className.length() != 0) {
            introspectedVals = configParserUtil.introspectJavaBean(
                    className, ddVals, false, rarName);
            mergedVals = configParserUtil.mergeProps(ddVals, introspectedVals);
        }
        return mergedVals;
    }

    @Override
    public List<String> getConfidentialProperties(ConnectorDescriptor desc, String rarName, String... keyFields)
            throws ConnectorRuntimeException {

        if (desc == null || rarName == null) {
            throw new ConnectorRuntimeException("Invalid arguments");
        }

        List<String> confidentialProperties = new ArrayList<>();
        Set configProperties = desc.getConfigProperties();
        if(configProperties != null){
            Iterator iterator = configProperties.iterator();
            while(iterator.hasNext()){
                ConnectorConfigProperty ccp = (ConnectorConfigProperty)iterator.next();
                if(ccp.isConfidential()){
                    confidentialProperties.add(ccp.getName());
                }
            }
        }
        return confidentialProperties;
    }
}
