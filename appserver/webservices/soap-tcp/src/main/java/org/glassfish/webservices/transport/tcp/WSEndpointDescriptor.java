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

package org.glassfish.webservices.transport.tcp;

import com.sun.enterprise.deployment.WebServiceEndpoint;

import javax.xml.namespace.QName;

/**
 * @author Alexey Stashok
 */
public class WSEndpointDescriptor {

    final private QName wsServiceName;
    final private String uri;
    final private String contextRoot;
    final private String urlPattern;
    final private boolean isEJB;
    final private WebServiceEndpoint wsServiceEndpoint;

    // Full address to endpoint
    final private String requestURL;

    public WSEndpointDescriptor(final WebServiceEndpoint wsServiceDescriptor, final String contextRoot,
            final String urlPattern, final String requestURL) {
        this.wsServiceName = wsServiceDescriptor.getServiceName();
        this.uri = wsServiceDescriptor.getEndpointAddressUri();
        this.isEJB = wsServiceDescriptor.implementedByEjbComponent();
        this.wsServiceEndpoint = wsServiceDescriptor;
        this.contextRoot = contextRoot;
        this.urlPattern = urlPattern;
        this.requestURL = requestURL;
    }

    public QName getWSServiceName() {
        return wsServiceName;
    }

    public String getURI() {
        return uri;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public WebServiceEndpoint getWSServiceEndpoint() {
        return wsServiceEndpoint;
    }

    public boolean isEJB() {
        return isEJB;
    }
}
