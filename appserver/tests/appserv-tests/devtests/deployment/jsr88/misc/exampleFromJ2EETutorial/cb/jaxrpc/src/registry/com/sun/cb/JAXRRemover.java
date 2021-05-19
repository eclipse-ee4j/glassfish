/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.cb;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;
import java.net.*;
import java.security.*;
import java.util.*;
import javax.naming.*;

/**
 * The JAXRRemover class consists of a makeConnection method,
 * a createOrgKey method, and an executeRemove method. It
 * finds and deletes the organization that the OrgPublisher
 * class created.
 */
public class JAXRRemover {

    Connection connection = null;
    RegistryService rs = null;

    public JAXRRemover() {}

    /**
     * Establishes a connection to a registry.
     *
     * @param queryUrl        the URL of the query registry
     * @param publishUrl        the URL of the publish registry
     */
    public void makeConnection(String queryUrl,
        String publishUrl) {

        Context           context = null;
        ConnectionFactory factory = null;
        /*
         * Define connection configuration properties.
         * To delete, you need both the query URL and the
         * publish URL.
         */
        Properties props = new Properties();
        props.setProperty("javax.xml.registry.queryManagerURL",
            queryUrl);
        props.setProperty("javax.xml.registry.lifeCycleManagerURL",
            publishUrl);

        try {
            // Create the connection, passing it the
            // configuration properties
            context = new InitialContext();
            factory = (ConnectionFactory)
                context.lookup("java:comp/env/eis/JAXR");
            factory.setProperties(props);
            connection = factory.createConnection();
            System.out.println("Created connection to registry");
        } catch (Exception e) {
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.close();
                } catch (JAXRException je) {}
            }
        }
    }

    /**
     * Searches for the organization created by the JAXRPublish
     * program, verifying it by checking that the key strings
     * match.
     *
     * @param keyStr        the key of the published organization
     *
     * @return        the key of the organization found
     */
    public javax.xml.registry.infomodel.Key createOrgKey(String keyStr) {

        BusinessLifeCycleManager blcm = null;
        javax.xml.registry.infomodel.Key orgKey = null;

        try {
            rs = connection.getRegistryService();
            blcm = rs.getBusinessLifeCycleManager();
            System.out.println("Got registry service and " +
                "life cycle manager");

            orgKey = blcm.createKey(keyStr);
        } catch (Exception e) {
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.close();
                } catch (JAXRException je) {
                    System.err.println("Connection close failed");
                }
            }
        }
        return orgKey;
    }

    /**
     * Removes the organization with the specified key value.
     *
     * @param key        the Key of the organization
     */
    public void executeRemove(javax.xml.registry.infomodel.Key key,
        String username, String password) {

        BusinessLifeCycleManager blcm = null;

        try {
            blcm = rs.getBusinessLifeCycleManager();

            // Get authorization from the registry
            PasswordAuthentication passwdAuth =
                new PasswordAuthentication(username,
                    password.toCharArray());

            Set creds = new HashSet();
            creds.add(passwdAuth);
            connection.setCredentials(creds);
            System.out.println("Established security credentials");

            String id = key.getId();
            System.out.println("Deleting organization with id " + id);
            Collection keys = new ArrayList();
            keys.add(key);
            BulkResponse response = blcm.deleteOrganizations(keys);
            Collection exceptions = response.getExceptions();
            if (exceptions == null) {
                System.out.println("Organization deleted");
                Collection retKeys = response.getCollection();
                Iterator keyIter = retKeys.iterator();
                javax.xml.registry.infomodel.Key orgKey = null;
                if (keyIter.hasNext()) {
                    orgKey = (javax.xml.registry.infomodel.Key) keyIter.next();
                    id = orgKey.getId();
                    System.out.println("Organization key was " + id);
                }
            } else {
                Iterator excIter = exceptions.iterator();
                Exception exception = null;
                while (excIter.hasNext()) {
                    exception = (Exception) excIter.next();
                    System.err.println("Exception on delete: " +
                    exception.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally  {
            // At end, close connection to registry
            if (connection != null) {
                try {
                    connection.close();
                } catch (JAXRException je) {
                    System.err.println("Connection close failed");
                }
            }
        }
    }
}
