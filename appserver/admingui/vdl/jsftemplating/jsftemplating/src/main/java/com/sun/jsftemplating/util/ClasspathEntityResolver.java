/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import org.xml.sax.InputSource;
import org.xml.sax.ext.EntityResolver2;

/**
 * <p>
 * This <code>EntityResolver</code> looks for files that are included as <code>SYSTEM</code> entities in the java
 * class-path. If the file is not found in the class path the resolver returns null, allowing default mechanism to
 * search for the file on the file system.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class ClasspathEntityResolver implements EntityResolver2 {

    /**
     * <p>
     * This method implements the old-style resolveEntity method. This is not recommended as it does not provide the baseURI
     * and may fail when it shouldn't.
     * </p>
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) {
        return resolveEntity("", publicId, "", systemId);
    }

    /**
     * <p>
     * This method returns null.
     * </p>
     */
    @Override
    public InputSource getExternalSubset(String name, String baseURI) {
        return null;
    }

    /**
     *
     */
    @Override
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) {
        if (systemId != null) {
            if (baseURI != null) {
                if (systemId.startsWith(baseURI)) {
                    systemId = systemId.substring(baseURI.length());
                }
            }
            int idx = systemId.indexOf(':');
            if (idx != -1) {
                // remove "file:/", "jndi:/", etc
                systemId = systemId.substring(idx + 1);
            }

            // Remove any extra leading /'s
            while (systemId.startsWith("/")) {
                // String of leading '/'
                systemId = systemId.substring(1);
            }

            // We should first check to see if it is in the context root,
            // avoid using Servlet API's so this code can work outside a
            // Servlet container.
            InputStream stream = null;
            URL url = null;
            if (FACES_CONTEXT != null) {
                try {
                    // The following will work w/ ServletContext/PortletContext
                    // Get the FacesContext...
                    Method meth = FACES_CONTEXT.getMethod("getCurrentInstance", (Class[]) null);
                    Object ctx = meth.invoke((Object) null, (Object[]) null);

                    if (ctx != null) {
                        // Get the ExternalContext...
                        meth = ctx.getClass().getMethod("getExternalContext", (Class[]) null);
                        ctx = meth.invoke(ctx, (Object[]) null);

                        // Get actual underlying external context...
                        meth = ctx.getClass().getMethod("getContext", (Class[]) null);
                        ctx = meth.invoke(ctx, (Object[]) null);

                        // Get the resource using the ServletContext/PortletContext
                        meth = ctx.getClass().getMethod("getResource", STRING_ARG);
                        // The path must start w/ a '/'
                        url = (URL) meth.invoke(ctx, "/" + systemId);
                        if (url != null) {
                            stream = url.openStream();
                        }
                    }
                } catch (NoSuchMethodException ex) {
                    throw new RuntimeException(ex);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                } catch (InvocationTargetException ex) {
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    // Ignore... we will check other places
                }
            }

            // First check to see if we can load this via a file:/// path,
            // even though this may work via the default... depending on how
            // the uri is constructed, it may not be able to locate it
            // correctly this way. We will give higher priority to
            // file:/// than finding it in the ClassPath.
            if (stream == null) {
                try {
                    stream = new URL(baseURI + "/" + systemId).openStream();
                } catch (IOException ex) {
                    // Ignore... we will check the ClassPath
                }

                if (stream == null) {
                    // Ok, we tried... now check the Classpath...
                    // Get the ClassLoader
                    ClassLoader loader = Util.getClassLoader(systemId);

                    // Attempt to find the resource via the ClassPath
                    stream = loader.getResourceAsStream(systemId);
                    if (stream == null) {
                        // Try adding a '/'
                        stream = loader.getResourceAsStream("/" + systemId);
                        if (stream == null) {
                            // Try in the META-INF directory
                            stream = loader.getResourceAsStream("META-INF/" + systemId);
                        }
                    }
                }
            }

            if (stream != null) {
                // Found, return an InputSource to the resource
                return new InputSource(stream);
            }
            if (LogUtil.configEnabled(LOGGER_NAME)) {
                LogUtil.config(LOGGER_NAME, "Unable to resolve entity." + "\n\tsystemId: '" + systemId + "'\n\tbaseURI: '" + baseURI
                        + "'\n\tpublicId: '" + publicId + "'\n\tname: '" + name + "'");
            }
        }

        // use the default behaviour
        return null;
    }

    public static final String LOGGER_NAME = "javax.enterpise.system.tools.admin.guiframework";

    private static final Class[] STRING_ARG = new Class[] { String.class };
    private static final Class FACES_CONTEXT = Util.noExceptionLoadClass("jakarta.faces.context.FacesContext");
}
