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

package org.glassfish.web.valve;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.jvnet.hk2.annotations.Contract;

/**
 * Contract interface for registering ServletContainerInterceptor
 * to the Web Container. It can be inherited by anyone who want to
 * extend the Web Container.
 *
 * @author Jeremy_Lv
 */
@Contract
public interface ServletContainerInterceptor {

    /**
     * User can set some useful informations before
     * invoking the Servlet application
     * @param req
     * @param res
     */
    void preInvoke(Request req, Response res);

    /**
     * User can remove some useful informations after
     * invoking the Servlet application
     * @param req
     * @param res
     */
    void postInvoke(Request req, Response res);
}
