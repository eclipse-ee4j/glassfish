/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.cdi.CDILoggerInfo;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.weld.WeldDeployer;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.web.WebModule;
import com.sun.faces.spi.FacesConfigResourceProvider;

import jakarta.servlet.ServletContext;

/**
 * This provider returns the Web Beans faces-config.xml to the JSF runtime. It will only return the configuraion file
 * for Web Beans deployments.
 */
public class WeldFacesConfigProvider implements FacesConfigResourceProvider {

    private static final String HABITAT_ATTRIBUTE = "org.glassfish.servlet.habitat";
    private InvocationManager invokeMgr;

    private Logger logger = Logger.getLogger(WeldFacesConfigProvider.class.getName());

    private static final String SERVICES_FACES_CONFIG = "META-INF/services/faces-config.xml";

    @Override
    public Collection<URI> getResources(ServletContext context) {

        ServiceLocator defaultServices = (ServiceLocator) context.getAttribute(HABITAT_ATTRIBUTE);
        invokeMgr = defaultServices.getService(InvocationManager.class);
        ComponentInvocation inv = invokeMgr.getCurrentInvocation();
        WebModule webModule = (WebModule) inv.getContainer();
        WebBundleDescriptor wdesc = webModule.getWebBundleDescriptor();

        List<URI> list = new ArrayList<>(1);

        if (!wdesc.hasExtensionProperty(WeldDeployer.WELD_EXTENSION)) {
            return list;
        }

        // Don't use Util.getCurrentLoader().  This config resource should
        // be available from the same classloader that loaded this instance.
        // Doing so allows us to be more OSGi friendly.
        ClassLoader loader = this.getClass().getClassLoader();
        URL resource = loader.getResource(SERVICES_FACES_CONFIG);
        if (resource != null) {
            try {
                list.add(resource.toURI());
            } catch (URISyntaxException ex) {
                if (logger.isLoggable(Level.SEVERE)) {
                    logger.log(Level.SEVERE, CDILoggerInfo.SEVERE_ERROR_CREATING_URI_FOR_FACES_CONFIG_XML,
                            new Object[] { resource.toExternalForm(), ex });
                }
            }
        }
        return list;
    }

}
