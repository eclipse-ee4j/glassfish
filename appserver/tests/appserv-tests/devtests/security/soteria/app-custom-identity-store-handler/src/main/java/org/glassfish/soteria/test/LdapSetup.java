/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.soteria.test;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldif.LDIFReader;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * Starts up the embedded Unboundid LDAP server on port 33389 and loads a test directory
 * into it containing the same caller- and roles names as the Database and Embedded idenity
 * stores are using.
 * 
 * @author Arjan Tijms
 *
 */
@Startup
@Singleton
public class LdapSetup {
    
    private InMemoryDirectoryServer directoryServer;

    @PostConstruct
    public void init() {
        try {
            InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=net");
            config.setListenerConfigs(
                new InMemoryListenerConfig("myListener", null, 33389, null, null, null));

            directoryServer = new InMemoryDirectoryServer(config);
            
            directoryServer.importFromLDIF(true, 
                new LDIFReader(this.getClass().getResourceAsStream("/test.ldif")));

            directoryServer.startListening();
        } catch (LDAPException e) {
            throw new IllegalStateException(e);
        }
    }
    
    @PreDestroy
    public void destroy() {
        directoryServer.shutDown(true);
    }
    
}
