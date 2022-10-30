/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors.jms.system;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.enterprise.connectors.ConnectorRuntime;

import jakarta.jms.ConnectionFactory;

import javax.naming.NamingException;

import org.glassfish.api.naming.DefaultResourceProxy;
import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.api.naming.NamespacePrefixes;
import org.glassfish.api.naming.SimpleJndiName;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT;

/**
 * Naming Object Proxy to handle the Default JMS Connection Factory.
 * Maps to a pre-configured jms connectionFactory, when binding for
 * a jms connectionFactory reference is absent in the @Resource annotation.
 *
 * @author David Zhao
 */
@Service
@NamespacePrefixes({DefaultJMSConnectionFactory.DEFAULT_CF})
public class DefaultJMSConnectionFactory implements NamedNamingObjectProxy, DefaultResourceProxy {
    static final String DEFAULT_CF = JNDI_CTX_JAVA_COMPONENT + "DefaultJMSConnectionFactory";
    static final String DEFAULT_CF_PHYS = "jms/__defaultConnectionFactory";
    private ConnectionFactory connectionFactory;
    private ConnectionFactory connectionFactoryPM;

    @Override
    public Object handle(String name) throws NamingException {
        ConnectionFactory cachedCF = null;
        boolean isCFPM = false;
        if (name != null && name.endsWith(ConnectorConstants.PM_JNDI_SUFFIX)) {
            cachedCF = connectionFactoryPM;
            isCFPM = true;
        } else {
            cachedCF = connectionFactory;
        }
        if(cachedCF == null) {
            javax.naming.Context ctx = new javax.naming.InitialContext();
            if (isCFPM) {
                ConnectorRuntime connectorRuntime = ConnectorRuntime.getRuntime();
                cachedCF = (ConnectionFactory) connectorRuntime.lookupPMResource(SimpleJndiName.of(DEFAULT_CF_PHYS), false);
                connectionFactoryPM = cachedCF;
            } else {
                cachedCF = (ConnectionFactory) ctx.lookup(DEFAULT_CF_PHYS);
                connectionFactory = cachedCF;
            }
        }
        return cachedCF;
    }

    @Override
    public String getPhysicalName() {
        return DEFAULT_CF_PHYS;
    }

    @Override
    public String getLogicalName() {
        return DEFAULT_CF;
    }
}
