/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.weld.jsf;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.web.WebModule;
import com.sun.faces.spi.FacesConfigResourceProvider;

import jakarta.servlet.ServletContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.cdi.CDILoggerInfo;
import org.glassfish.hk2.api.ServiceLocator;

import static java.util.logging.Level.SEVERE;
import static org.glassfish.cdi.CDILoggerInfo.SEVERE_ERROR_CREATING_URI_FOR_FACES_CONFIG_XML;
import static org.glassfish.weld.WeldDeployer.WELD_EXTENSION;

/**
 * This provider returns the Web Beans faces-config.xml to the Faces runtime. It will only return the configuration file
 * for Web Beans deployments.
 */
public class WeldFacesConfigProvider implements FacesConfigResourceProvider {

    private static final String HABITAT_ATTRIBUTE = "org.glassfish.servlet.habitat";
    private static final String SERVICES_FACES_CONFIG = "META-INF/services/faces-config.xml";

    private Logger logger = CDILoggerInfo.getLogger();

    @Override
    public Collection<URI> getResources(ServletContext context) {
        List<URI> list = new ArrayList<>(1);

        if (!getWebBundleDescriptor(context).hasExtensionProperty(WELD_EXTENSION)) {
            return list;
        }

        // Don't use Util.getCurrentLoader(). This config resource should
        // be available from the same classloader that loaded this instance.
        // Doing so allows us to be more OSGi friendly.
        URL resource = getClass().getClassLoader().getResource(SERVICES_FACES_CONFIG);
        if (resource != null) {
            try {
                list.add(resource.toURI());
            } catch (URISyntaxException ex) {
                if (logger.isLoggable(SEVERE)) {
                    logger.log(SEVERE, SEVERE_ERROR_CREATING_URI_FOR_FACES_CONFIG_XML,
                            new Object[] { resource.toExternalForm(), ex });
                }
            }
        }

        return list;
    }

    WebBundleDescriptor getWebBundleDescriptor(ServletContext context) {
        return ((WebModule) ((ServiceLocator)
            context.getAttribute(HABITAT_ATTRIBUTE))
                   .getService(InvocationManager.class)
                   .getCurrentInvocation()
                   .getContainer())
                   .getWebBundleDescriptor();
    }

}
