/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config.dom;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * <p>
 * A {@link Protocol} which redirects an HTTP(S) request to a different location
 * using HTTP 302 redirection semantics.
 * </p>
 */
@Configured
public interface HttpRedirect extends ConfigBeanProxy, PropertyBag {
    int PORT = -1;
    boolean SECURE = false;

    /**
     * @return the network port the request should be redirected to.  If no
     *         value was specified, the default of <code>-1</code> will be returned
     *         which signifies a redirection to the same port the current request
     *         was made on
     */
    @Attribute(defaultValue = "" + PORT, dataType = Integer.class)
    @Range(min=-1, max=65535)
    String getPort();

    @SuppressWarnings({"UnusedDeclaration"})
    void setPort(String port);

    /**
     * @return <code>true</code> will redirect the request using <code>HTTPS</code>
     *         where as a value of <code>false</code> will use <code>HTTP</code>
     */
    @Attribute(defaultValue = "" + SECURE, dataType = Boolean.class)
    String getSecure();

    @SuppressWarnings({"UnusedDeclaration"})
    void setSecure(String value);

}
