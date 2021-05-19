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
import java.util.ResourceBundle;
import java.io.*;

public class OrgPublisher {

    public static void main(String[] args) {

        String queryURL = URLHelper.getQueryURL();
        String publishURL = URLHelper.getPublishURL();
        String endpoint = URLHelper.getEndpointURL();

        ResourceBundle registryBundle =
           ResourceBundle.getBundle("com.sun.cb.CoffeeRegistry");

        String username =
            registryBundle.getString("registry.username");
        String password =
            registryBundle.getString("registry.password");
        String keyFile = registryBundle.getString("key.file");

        JAXRPublisher publisher = new JAXRPublisher();
        publisher.makeConnection(queryURL, publishURL);
        String key = publisher.executePublish(username,
            password, endpoint);
        try {
            FileWriter out = new FileWriter(keyFile);
            out.write(key);
            out.flush();
            out.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

}
