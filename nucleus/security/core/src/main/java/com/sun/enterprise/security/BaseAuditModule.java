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

/*
 * AuditModule.java
 *
 * Created on July 27, 2003, 11:32 PM
 */

package com.sun.enterprise.security;

import java.util.Properties;
/**
 * Base class that should be extended by all classes that wish to provide their
 * own Audit support.
 * @author  Harpreet Singh
 * @version
 */
public abstract class BaseAuditModule {
    protected Properties props = null;
    /**
     * Method is invoked at server startup, during AuditModule initialization.
     * If method returns without any exception then S1AS assumes that the module
     * is ready to serve any requests.
     * @param props the properties for the AuditModule. These properties are
     * defined in the domain.xml
     */
    public void init(Properties props) {
        this.props = props;
    }

    /**
     * Invoked post authentication request for a user in a given realm
     * @param user username for whom the authentication request was made
     * @param realm the realm name under which the user is authenticated.
     * @param success the status of the authentication
     */
    public void authentication(String user, String realm, boolean success) {
    }

    /**
     * Invoked upon completion of the server startup
     */
    public void serverStarted() {
    }

    /**
     * Invoked upon completion of the server shutdown
     */
    public void serverShutdown() {
    }

}
