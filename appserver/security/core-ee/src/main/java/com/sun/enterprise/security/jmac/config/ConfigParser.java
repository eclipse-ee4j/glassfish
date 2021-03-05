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

package com.sun.enterprise.security.jmac.config;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * AuthConfigImpl relies on a ConfigParser to read the module configuration.
 *
 * <p>
 * The ConfigParser is expected to parse that information into the HashMap described below.
 *
 * @version %I%, %G%
 */
public interface ConfigParser {

    /**
     * Initialize the parser. Passing null as argument means the parser is to find configuration object as necessary.
     */
    public void initialize(Object config) throws IOException;

    /**
     * Get the module configuration information. The information is returned as a HashMap.
     *
     * <p>
     * The key is an intercept:
     * <ul>
     * <li>SOAP
     * <li>HttpServlet
     * </ul>
     *
     * <p>
     * The value is a AuthConfigImpl.InterceptEntry, which contains:
     * <ul>
     * <li>default provider ID
     * <li>default type (client or server)
     * <li>HashMap, where key = provider ID value = BaseAuthConfigImpl.IDEntry
     * </ul>
     *
     * <p>
     * An IDEntry contains:
     * <ul>
     * <li>type (client or server)
     * <li>moduleClassName
     * <li>default requestPolicy
     * <li>default responsePolicy
     * <li>options
     * <li>
     * </ul>
     */
    public Map getConfigMap();

    /**
     * Get the name of layers with default set in domain.xml.
     */
    public Set<String> getLayersWithDefault();
}
