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
import java.util.ResourceBundle;

public final class URLHelper {

    public static final String getSaajURL() {

        ResourceBundle registryBundle =
           ResourceBundle.getBundle("com.sun.cb.CoffeeBreak");

        String saajURL = registryBundle.getString("saaj.url");


        return saajURL;

    } // getDate

    public static final String getEndpointURL() {

        ResourceBundle registryBundle =
           ResourceBundle.getBundle("com.sun.cb.CoffeeBreak");

        String endPointURL = registryBundle.getString("endpoint.url");


        return endPointURL;

    } // getDate

    public static final String getQueryURL() {

        ResourceBundle registryBundle =
           ResourceBundle.getBundle("com.sun.cb.CoffeeBreak");

        String queryURL = registryBundle.getString("query.url");


        return queryURL;

    } // getDate

    public static final String getPublishURL() {

        ResourceBundle registryBundle =
           ResourceBundle.getBundle("com.sun.cb.CoffeeBreak");

        String publishURL = registryBundle.getString("publish.url");

        return publishURL;

    } // getDate


} // class
