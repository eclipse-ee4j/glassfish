/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.uberjar.builder.installroot;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author bhavanishankar@dev.java.net
 */

public class InstallRootBuilder implements BundleActivator {

    private static final Logger logger = Logger.getLogger("embedded-glassfish");
    private static String resourceroot = "glassfish7/glassfish/";

    public void start(BundleContext context) throws Exception {
        String installRoot = context.getProperty("com.sun.aas.installRoot");
        buildInstallRoot(context.getBundle(), installRoot);
    }

    public void stop(BundleContext context) throws Exception {
        logger.fine("InstallRootBuilder stopped");
    }

    public void buildInstallRoot(Bundle bundle, String installRoot) throws Exception {
        List<String> resources = getResources(bundle, resourceroot + "lib/");
        for (String resource : resources) {
            InstallRootBuilderUtil.copy(bundle.getResource(resource).openConnection().getInputStream(),
                    installRoot, resource.substring(resourceroot.length()));
        }
    }

    private List<String> getResources(Bundle b, String... subpaths) {
        List<String> resources = new ArrayList();
        if (subpaths == null || subpaths.length == 0) {
            subpaths = new String[]{"/"};
        }
        for (String subpath : subpaths) {
            for (Enumeration e = b.getEntryPaths(subpath); e != null && e.hasMoreElements();) {
                String entryPath = (String) e.nextElement();
                if (entryPath.endsWith("/")) {
                    resources.addAll(getResources(b, entryPath));
                } else {
                    resources.add(entryPath);
                }
            }
        }
        return resources;
    }

}
