/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb30.hello.session3;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Stateless;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConnectionFactoryDefinition;
import jakarta.jms.JMSConnectionFactoryDefinitions;
import javax.naming.InitialContext;

@JMSConnectionFactoryDefinitions(
        value = {
                @JMSConnectionFactoryDefinition(
                        description = "global-scope resource defined by @JMSConnectionFactoryDefinition",
                        name = "java:global/env/HelloEJB_ModByDD_JMSConnectionFactory",
                        interfaceName = "jakarta.jms.ConnectionFactory",
                        resourceAdapter = "jmsra",
                        user = "admin",
                        password = "admin",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=LocalTransaction"},
                        minPoolSize = 0
                ),

                @JMSConnectionFactoryDefinition(
                        description = "global-scope resource defined by @JMSConnectionFactoryDefinition",
                        name = "java:global/env/HelloEJB_Annotation_JMSConnectionFactory",
                        interfaceName = "jakarta.jms.ConnectionFactory",
                        resourceAdapter = "jmsra",
                        user = "admin",
                        password = "admin",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=XATransaction" },
                        minPoolSize = 0
                ),

                @JMSConnectionFactoryDefinition(
                        description = "application-scope resource defined by @JMSConnectionFactoryDefinition",
                        name = "java:app/env/HelloEJB_Annotation_JMSConnectionFactory",
                        interfaceName = "jakarta.jms.ConnectionFactory",
                        resourceAdapter = "jmsra",
                        user = "admin",
                        password = "admin",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=XATransaction"},
                        minPoolSize = 0
                ),

                @JMSConnectionFactoryDefinition(
                        description = "module-scope resource defined by @JMSConnectionFactoryDefinition",
                        name = "java:module/env/HelloEJB_Annotation_JMSConnectionFactory",
                        interfaceName = "jakarta.jms.ConnectionFactory",
                        resourceAdapter = "jmsra",
                        user = "admin",
                        password = "admin",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=XATransaction"},
                        minPoolSize = 0
                ),

                @JMSConnectionFactoryDefinition(
                        description = "component-scope resource defined by @JMSConnectionFactoryDefinition",
                        name = "java:comp/env/HelloEJB_Annotation_JMSConnectionFactory",
                        interfaceName = "jakarta.jms.ConnectionFactory",
                        resourceAdapter = "jmsra",
                        user = "admin",
                        password = "admin",
                        properties = {"org.glassfish.connector-connection-pool.transaction-support=XATransaction"},
                        minPoolSize = 0
                )
        }
)

@Stateless
public class HelloEJB implements Hello {

    private Collection<Connection> connections = null;

    @PostConstruct
    public void postConstruction() {
        connections = new ArrayList<Connection>();
        System.out.println("In HelloEJB::postConstruction()");
    }

    @PreDestroy
    public void closeConnections() {
        for (Connection c : connections) {
            try {
                c.close();
            } catch (Exception e) {
            }
        }
        connections.clear();
        connections = null;
    }

    public void hello() {
        // JMSConnectionFactory-Definition through Annotation
        lookupJMSConnectionFactory("java:global/env/Appclient_ModByDD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:global/env/Appclient_Annotation_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:app/env/Appclient_Annotation_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:module/env/Appclient_Annotation_JMSConnectionFactory", false);
        lookupJMSConnectionFactory("java:comp/env/Appclient_Annotation_JMSConnectionFactory", false);

        lookupJMSConnectionFactory("java:global/env/Servlet_ModByDD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:global/env/Servlet_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:app/env/Servlet_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:module/env/Servlet_JMSConnectionFactory", false);
        lookupJMSConnectionFactory("java:comp/env/Servlet_JMSConnectionFactory", false);

        lookupJMSConnectionFactory("java:global/env/HelloStatefulEJB_ModByDD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:global/env/HelloStatefulEJB_Annotation_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:app/env/HelloStatefulEJB_Annotation_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:module/env/HelloStatefulEJB_Annotation_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:comp/env/HelloStatefulEJB_Annotation_JMSConnectionFactory", false);

        lookupJMSConnectionFactory("java:global/env/HelloEJB_ModByDD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:global/env/HelloEJB_Annotation_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:app/env/HelloEJB_Annotation_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:module/env/HelloEJB_Annotation_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:comp/env/HelloEJB_Annotation_JMSConnectionFactory", true);

        // JMSConnectionFactory-Definition through DD
        lookupJMSConnectionFactory("java:global/env/Application_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:app/env/Application_DD_JMSConnectionFactory", true);

        lookupJMSConnectionFactory("java:global/env/Appclient_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:app/env/Appclient_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:module/env/Appclient_DD_JMSConnectionFactory", false);
        lookupJMSConnectionFactory("java:comp/env/Appclient_DD_JMSConnectionFactory", false);

        lookupJMSConnectionFactory("java:global/env/Web_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:app/env/Web_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:module/env/Web_DD_JMSConnectionFactory", false);
        lookupJMSConnectionFactory("java:comp/env/Web_DD_JMSConnectionFactory", false);

        lookupJMSConnectionFactory("java:global/env/HelloStatefulEJB_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:app/env/HelloStatefulEJB_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:module/env/HelloStatefulEJB_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:comp/env/HelloStatefulEJB_DD_JMSConnectionFactory", false);

        lookupJMSConnectionFactory("java:global/env/HelloEJB_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:app/env/HelloEJB_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:module/env/HelloEJB_DD_JMSConnectionFactory", true);
        lookupJMSConnectionFactory("java:comp/env/HelloEJB_DD_JMSConnectionFactory", true);

        System.out.println("Stateless EJB lookup jms-connection-factory-definitions successfully!");
    }

    private void lookupJMSConnectionFactory(String jndiName, boolean expectSuccess) {
        Connection c = null;
        try {
            System.out.println("Stateless EJB lookup jms connection factory: " + jndiName);
            InitialContext ic = new InitialContext();
            ConnectionFactory cf = (ConnectionFactory) ic.lookup(jndiName);
            c = cf.createConnection();
            connections.add(c);
            System.out.println("Stateless EJB can access jms connection factory: " + jndiName);
        } catch (Exception e) {
            if (expectSuccess) {
                throw new RuntimeException("Stateless EJB failed to access jms connection factory: " + jndiName, e);
            }
            System.out.println("Stateless EJB cannot access jms connection factory: " + jndiName);
            return;
        }
        if (!expectSuccess) {
            throw new RuntimeException("Stateless EJB should not run into here.");
        }
    }
}
