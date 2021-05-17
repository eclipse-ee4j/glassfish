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

package com.sun.enterprise.web;

import java.util.Map;

/**
 * Specifies behavior that the requester of an ad hoc path must implement.
 * <p>
 * The web container will call back to these methods when it needs to create an
 * instance of the servlet to support the path.
 *
 * @author Tim Quinn
 */
public interface AdHocServletInfo {

    /**
     * Returns the class type of the servlet that should be created to process
     * requests.  Note that the class must represent a subclass of HttpServlet.
     *
     * @return The servlet class
     */
    public Class getServletClass();

    /**
     * Returns the name of the servlet that the container should assign when it
     * adds a servlet to a web module.
     *
     * @return The servlet name
     */
    public String getServletName();

    /**
     * Returns a Map containing name and value pairs to be used in preparing
     * the init params in the servlet's ServletConfig object.
     *
     * @return Map containing the servlet init parameters
     */
    public Map<String,String> getServletInitParams();

}
