/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jdbcruntime;

import javax.naming.NamingException;
import javax.sql.DataSource;
import org.glassfish.api.naming.DefaultResourceProxy;
import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.api.naming.NamespacePrefixes;
import org.jvnet.hk2.annotations.Service;

/**
 * Naming Object Proxy to handle the Default Data Source.
 * Maps to a pre-configured data source, when binding for
 * a datasource reference is absent in the @Resource annotation.
 *
 * @author Shalini M
 */
@Service
@NamespacePrefixes({DefaultDataSource.DEFAULT_DATASOURCE})
public class DefaultDataSource implements NamedNamingObjectProxy, DefaultResourceProxy {

    static final String DEFAULT_DATASOURCE = "java:comp/DefaultDataSource";
    static final String DEFAULT_DATASOURCE_PHYS = "jdbc/__default";
    private DataSource dataSource;

    @Override
    public Object handle(String name) throws NamingException {
        if(dataSource == null) {
            javax.naming.Context ctx = new javax.naming.InitialContext();
            // cache the datasource to avoid JNDI lookup overheads
            dataSource = (DataSource)ctx.lookup(DEFAULT_DATASOURCE_PHYS);
        }
        return dataSource;
    }

    @Override
    public String getPhysicalName() {
        return DEFAULT_DATASOURCE_PHYS;
    }

    @Override
    public String getLogicalName() {
        return DEFAULT_DATASOURCE;
    }
}
