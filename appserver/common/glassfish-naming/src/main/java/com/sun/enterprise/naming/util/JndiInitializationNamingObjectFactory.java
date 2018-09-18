/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.naming.util;

import com.sun.enterprise.naming.spi.NamingObjectFactory;
import org.glassfish.api.naming.NamingObjectProxy;
import org.jvnet.hk2.annotations.Service;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.*;

/**
 * <p>A naming object factory that is used by resource-references
 * of type JDBC/JMS/Connector/Mail so that Application Client
 * Container based lookup of these references will result in
 * the server returning the "Reference" instead of the actual
 * object since the actual object will not work in the application
 * client container.</p>
 * <p> By virtue of implementing NamingObjectProxy.InitializationNamingObjectProxy,
 * above requirement of returning "Reference" is achieved. Refer other
 * implementations like org.glassfish.resourcebase.resources.api.ResourceProxy and
 * org.glassfish.javaee.services.CommonResourceProxy</p>
 *
 */
@Service
public class JndiInitializationNamingObjectFactory implements NamingObjectFactory,
        NamingObjectProxy.InitializationNamingObjectProxy, Serializable{

    private String name;
    private String jndiName;
    private boolean cacheResult;

    private transient JndiNamingObjectFactory jndiNamingObjectFactory ;

    public JndiInitializationNamingObjectFactory() {
        //need a no-org constructor since it's serializable.
    }

    public JndiInitializationNamingObjectFactory(String name, String jndiName, boolean cacheResult) {
        this.name = name;
        this.jndiName = jndiName;
        this.cacheResult = cacheResult;
        //couldn't make JndiInitializationNamingObjectFactory simply extend JndiNaminObjectFactory
        //since serialization/de-serialization requires no-arg constructor for super classes too.
        jndiNamingObjectFactory = new JndiNamingObjectFactory(name, jndiName, cacheResult);
    }

    /**
     * @inheritDoc
     */
    public boolean isCreateResultCacheable() {
        return getJndiNamingObjectFactory().isCreateResultCacheable();
    }

    /**
     * re-construct JndiNamingObjectFactory in case it is null (due to de-serialization)
     * @return JndiNamingObjectFactory
     */
    private JndiNamingObjectFactory getJndiNamingObjectFactory() {

        if(jndiNamingObjectFactory == null){
            jndiNamingObjectFactory = new JndiNamingObjectFactory(name, jndiName, cacheResult);
        }
        return jndiNamingObjectFactory;
    }

    /**
     * @inheritDoc
     */
    public Object create(Context ic) throws NamingException {
        return  getJndiNamingObjectFactory().create(ic);
    }
}
