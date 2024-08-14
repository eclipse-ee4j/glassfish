/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.extras.osgicontainer;

import java.io.File;

import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.OpsParams;
import org.jvnet.hk2.annotations.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * OSGi deployer, takes care of loading and cleaning modules from the OSGi runtime.
 *
 * @author Jerome Dochez
 * @author Sanjeeb Sahoo
 */
@Service
public class OSGiDeployer implements Deployer<OSGiContainer, OSGiDeployedBundle> {

    public OSGiDeployedBundle load(OSGiContainer container, DeploymentContext context) {
        return new OSGiDeployedBundle(getApplicationBundle(context, true));
    }

    public void unload(OSGiDeployedBundle appContainer, DeploymentContext context) {
    }

    public void clean(DeploymentContext context) {
        try {
            OpsParams params = context.getCommandParameters(OpsParams.class);
            // we should clean for both undeployment and the failed deployment
            if (params.origin.isUndeploy() || params.origin.isDeploy()) {
                Bundle bundle = getApplicationBundle(context);
                bundle.uninstall();
                getPA().refreshPackages(new Bundle[]{bundle});
                System.out.println("Uninstalled " + bundle);
            }
        } catch (BundleException e) {
            throw new RuntimeException(e);
        }
    }

    private PackageAdmin getPA() {
        final BundleContext context = getBundleContext();
        return (PackageAdmin) context.getService(context.getServiceReference(PackageAdmin.class.getName()));
    }

    public MetaData getMetaData() {
        return null;
    }

    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return null;
    }

    public boolean prepare(DeploymentContext context) {
        File file = context.getSourceDir();
        OpsParams params = context.getCommandParameters(OpsParams.class);
        if (params.origin.isDeploy()) {
            assert(file.isDirectory());
            installBundle(makeBundleLocation(file));
        }
        return true;
    }

    private Bundle installBundle(final String location) {
        try {
            Bundle bundle = getBundleContext().installBundle(location);
            System.out.println("Installed " + bundle + " from " + bundle.getLocation());
            return bundle;
        } catch (BundleException e) {
            throw new RuntimeException(e);
        }
    }

    private BundleContext getBundleContext() {
        return BundleReference.class.cast(getClass().getClassLoader()).getBundle().getBundleContext();
    }

    private Bundle getApplicationBundle(DeploymentContext context, boolean reinstallIfAbsent) {
        String location = makeBundleLocation(context.getSourceDir());
        for(Bundle b : getBundleContext().getBundles()) {
            if (location.equals(b.getLocation())) {
                return b;
            }
        }
        if (reinstallIfAbsent) {
            System.out.println("Bundle does not exist, so reinstalling from " + location);
            return installBundle(location);
        }
        throw new RuntimeException("Unable to determine bundle corresponding to application location " + context.getSourceDir());
    }

    private Bundle getApplicationBundle(DeploymentContext context) {
        return getApplicationBundle(context, false);
    }

    private String makeBundleLocation(File file) {
        return "reference:" + file.toURI();
    }

}
