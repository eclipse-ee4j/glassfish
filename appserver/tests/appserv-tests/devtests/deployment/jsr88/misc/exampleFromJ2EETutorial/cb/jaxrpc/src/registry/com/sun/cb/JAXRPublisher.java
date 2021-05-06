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
 * The JAXRPublisher class consists of a makeConnection
 * method and an executePublish method. The makeConnection
 * method establishes a connection to the Registry Server.
 * The executePublish method creates an organization and
 * publishes it to the registry.
 */
public class JAXRPublisher {

    Connection connection = null;

    public JAXRPublisher() {}

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
         * To publish, you need both the query URL and the
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
     * Creates an organization, its classification, and its
     * services, and saves it to the registry.
     */
    public String executePublish(String username,
        String password, String endpoint) {

        String id = null;
        RegistryService rs = null;
        BusinessLifeCycleManager blcm = null;
        BusinessQueryManager bqm = null;

        try {
            rs = connection.getRegistryService();
            blcm = rs.getBusinessLifeCycleManager();
            bqm = rs.getBusinessQueryManager();
            System.out.println("Got registry service, query " +
                "manager, and life cycle manager");

            // Get authorization from the registry
            PasswordAuthentication passwdAuth =
                new PasswordAuthentication(username,
                    password.toCharArray());

            Set creds = new HashSet();
            creds.add(passwdAuth);
            connection.setCredentials(creds);
            System.out.println("Established security credentials");

            // Get hardcoded strings from a ResourceBundle
            ResourceBundle bundle =
                ResourceBundle.getBundle("com.sun.cb.CoffeeRegistry");

            // Create organization name and description
            Organization org =
                blcm.createOrganization(bundle.getString("org.name"));
            InternationalString s =
                blcm.createInternationalString
                (bundle.getString("org.description"));
            org.setDescription(s);

            // Create primary contact, set name
            User primaryContact = blcm.createUser();
            PersonName pName =
                blcm.createPersonName(bundle.getString("person.name"));
            primaryContact.setPersonName(pName);

            // Set primary contact phone number
            TelephoneNumber tNum = blcm.createTelephoneNumber();
            tNum.setNumber(bundle.getString("phone.number"));
            Collection phoneNums = new ArrayList();
            phoneNums.add(tNum);
            primaryContact.setTelephoneNumbers(phoneNums);

            // Set primary contact email address
            EmailAddress emailAddress =
                blcm.createEmailAddress(bundle.getString("email.address"));
            Collection emailAddresses = new ArrayList();
            emailAddresses.add(emailAddress);
            primaryContact.setEmailAddresses(emailAddresses);

            // Set primary contact for organization
            org.setPrimaryContact(primaryContact);

            // Set classification scheme to NAICS
            ClassificationScheme cScheme =
                bqm.findClassificationSchemeByName
                    (null, bundle.getString("classification.scheme"));

            // Create and add classification
            Classification classification = (Classification)
                blcm.createClassification(cScheme,
                    bundle.getString("classification.name"),
                    bundle.getString("classification.value"));
            Collection classifications = new ArrayList();
            classifications.add(classification);
            org.addClassifications(classifications);

            // Create services and service
            Collection services = new ArrayList();
            Service service =
                blcm.createService(bundle.getString("service.name"));
            InternationalString is =
                blcm.createInternationalString
                (bundle.getString("service.description"));
            service.setDescription(is);

            // Create service bindings
            Collection serviceBindings = new ArrayList();
            ServiceBinding binding = blcm.createServiceBinding();
            is = blcm.createInternationalString
                (bundle.getString("service.binding"));
            binding.setDescription(is);
                                           binding.setValidateURI(false);
            binding.setAccessURI(endpoint);
            serviceBindings.add(binding);

            // Add service bindings to service
            service.addServiceBindings(serviceBindings);

            // Add service to services, then add services to organization
            services.add(service);
            org.addServices(services);

            // Add organization and submit to registry
            // Retrieve key if successful
            Collection orgs = new ArrayList();
            orgs.add(org);
            BulkResponse response = blcm.saveOrganizations(orgs);
            Collection exceptions = response.getExceptions();
            if (exceptions == null) {
                System.out.println("Organization saved");

                Collection keys = response.getCollection();
                Iterator keyIter = keys.iterator();
                if (keyIter.hasNext()) {
                    javax.xml.registry.infomodel.Key orgKey =
                        (javax.xml.registry.infomodel.Key) keyIter.next();
                    id = orgKey.getId();
                    System.out.println("Organization key is " + id);
                }
            } else {
                Iterator excIter = exceptions.iterator();
                Exception exception = null;
                while (excIter.hasNext()) {
                    exception = (Exception) excIter.next();
                    System.err.println("Exception on save: " +
                        exception.toString());
                }
            }
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
    return id;
    }
}
