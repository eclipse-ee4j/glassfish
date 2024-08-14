/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.apache.naming.resources;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Dictionary;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * This class is responsible for adding {@code DirContextURLStreamHandler}
 * to OSGi service registry.
 *
 * As much as we would have liked it to be both an activator as well as a Startup service, we can't.
 * In embedded mode, this Startup service would fail to load, so we have separated the startup servuce part to
 * {@link WebNamingStartup} class. That Startup service ensures that this bundle gets activated
 * during server startup and the activator ensures that we register a jndi protocol handler.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class DirContextURLStreamHandlerService
        extends AbstractURLStreamHandlerService
        implements BundleActivator {

    // We have to extend DirContextURLStreamHandler so that we
    // can make openConnection and toExternalForm available as
    // public methods.
    private static class DelegatingDirContextURLStreamHandler
            extends DirContextURLStreamHandler{
        @Override
        public URLConnection openConnection(URL u) throws IOException {
            return super.openConnection(u);
        }

        @Override
        public String toExternalForm(URL u) {
            return super.toExternalForm(u);
        }
    }

    public URLConnection openConnection(URL u) throws IOException {
        return new DelegatingDirContextURLStreamHandler().openConnection(u);
    }

    @Override
    public String toExternalForm(URL u) {
        return new DelegatingDirContextURLStreamHandler().toExternalForm(u);
    }

    public void start(BundleContext context) throws Exception {
        Dictionary p = new Properties();
        p.put(URLConstants.URL_HANDLER_PROTOCOL,
                new String[]{"jndi"});
        context.registerService(
                URLStreamHandlerService.class.getName(),
                this,
                p);
    }

    public void stop(BundleContext context) throws Exception {
    }
}
