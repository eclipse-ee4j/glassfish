/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * Thup.java
 *
 * Created on September 18, 2004, 11:53 PM
 */

package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Properties;

/**
 * An abbreviation for PortHostUserPassword
 * It's annoying to pass it around all over the place as individual items,
 * so I created this trivial class.
 * @author  bnevins
 */
class Phup
{
    Phup(int port, String host, String user, String password)
    {
        this.port = port;
        this.host = host;
        this.user = user;
        this.password = password;
    }

    ///////////////////////////////////////////////////////////////////////////

    Phup(String port, String host, String user, String password) throws DeploymentTestsException
    {
        this.port = string2int(port);
        this.host = host;
        this.user = user;
        this.password = password;
    }

    ///////////////////////////////////////////////////////////////////////////

    Phup(Properties props) throws DeploymentTestsException
    {
        user        = props.getProperty("user");
        password    = props.getProperty("password");
        host        = props.getProperty("host");

        if(user == null || password == null || host == null)
            throw new DeploymentTestsException("Can't find user and/or password and/or host in Properties file.");

        // string2int validates...
        port = string2int(props.getProperty("port"));
    }

    //////////////////////////////////////////////////////////////////////////

    private static int string2int(String s) throws DeploymentTestsException
    {
        try
        {
            int i = Integer.parseInt(s);

            if(i <= 0 || i > 65535)
                throw new NumberFormatException();

            return i;
        }
        catch(NumberFormatException nfe)
        {
            throw new DeploymentTestsException("Bad port number: " + s);
        }
    }

    //////////////////////////////////////////////////////////////////////////

    // note that these have default scope...
    String user;
    String password;
    String host;
    int    port;
}
