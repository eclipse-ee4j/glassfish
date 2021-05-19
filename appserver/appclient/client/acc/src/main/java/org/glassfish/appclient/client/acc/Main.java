/*
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

package org.glassfish.appclient.client.acc;


/**
 * This is the main program invoked from the command line.
 *
 * It processes the command line arguments to prepare the Map of options, then
 * passes the Map to an instance of the embedded ACC.
 */
public class Main {

    private static final String CLIENT = "-client";
    private static final String NAME = "-name";
    private static final String MAIN_CLASS = "-mainclass";
    private static final String TEXT_AUTH = "-textauth";
    private static final String XML_PATH = "-xml";
    private static final String ACC_CONFIG_XML = "-configxml";
    private static final String DEFAULT_CLIENT_CONTAINER_XML = "sun-acc.xml";
    // duplicated in com.sun.enterprise.jauth.ConfigXMLParser
    private static final String SUNACC_XML_URL = "sun-acc.xml.url";
    private static final String NO_APP_INVOKE = "-noappinvoke";
    //Added for allow user to pass user name and password through command line.
    private static final String USER = "-user";
    private static final String PASSWORD = "-password";
    private static final String PASSWORD_FILE = "-passwordfile";
    private static final String LOGIN_NAME = "j2eelogin.name";
    private static final String LOGIN_PASSWORD = "j2eelogin.password";
    private static final String DASH = "-";

    private static final String lineSep = System.getProperty("line.separator");

}
