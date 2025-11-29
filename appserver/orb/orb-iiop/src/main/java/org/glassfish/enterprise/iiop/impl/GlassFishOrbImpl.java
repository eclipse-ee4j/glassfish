/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.enterprise.iiop.impl;

import com.sun.corba.ee.impl.naming.cosnaming.TransientNameService;
import com.sun.corba.ee.impl.orb.ORBImpl;
import com.sun.corba.ee.impl.osgi.loader.OSGIListener;
import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.folb.GroupInfoService;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.enterprise.module.HK2Module;

import java.lang.System.Logger;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.glassfish.external.amx.AMXGlassfish;
import org.omg.CORBA.ORBPackage.InvalidName;

import static com.sun.corba.ee.spi.misc.ORBConstants.FOLB_SERVER_GROUP_INFO_SERVICE;
import static java.lang.System.Logger.Level.DEBUG;

class GlassFishOrbImpl extends ORBImpl {

    private static final Logger LOG = System.getLogger(GlassFishOrbImpl.class.getName());

    private final String description;

    public GlassFishOrbImpl(
        HK2Module corbaOrbOsgiModule,
        GroupInfoService clusterGroupInfo,
        Properties orbInitProperties,
        String[] args) {

        this.description = super.toString() + "[corbaOrbModule=" + corbaOrbOsgiModule + ", clusterGroupInfo="
            + clusterGroupInfo + ", orbInitProperties=" + orbInitProperties + ", args=" + Arrays.toString(args) + "]";
        if (corbaOrbOsgiModule != null) {
            corbaOrbOsgiModule.start();
        }
        orbInitProperties.setProperty(ORBConstants.DISABLE_ORBD_INIT_PROPERTY, "true");
        if (corbaOrbOsgiModule != null) {
            classNameResolver(
                makeCompositeClassNameResolver(OSGIListener.classNameResolver(), ORB.defaultClassNameResolver()));
            classCodeBaseHandler(OSGIListener.classCodeBaseHandler());
        }
        setRootParentObjectName(AMXGlassfish.DEFAULT.serverMonForDAS());

        setParameters(args, orbInitProperties);

        new TransientNameService(this);

        // Done to indicate this is a server and needs to create listen ports.
        try {
            resolve_initial_references("RootPOA");
        } catch (InvalidName e) {
            throw new IllegalStateException("RootPOA not found", e);
        }

        if (clusterGroupInfo == null) {
            return;
        }

        try {
            register_initial_reference(FOLB_SERVER_GROUP_INFO_SERVICE, (org.omg.CORBA.Object) clusterGroupInfo);
            LOG.log(DEBUG, "Naming registration complete: {0}", clusterGroupInfo);

            // Just for logging
            GroupInfoService gisRef = (GroupInfoService) resolve_initial_references(FOLB_SERVER_GROUP_INFO_SERVICE);
            List<ClusterInstanceInfo> clusterInstances = gisRef.getClusterInstanceInfo(null);
            LOG.log(DEBUG, "Results from getClusterInstanceInfo:");
            if (clusterInstances != null) {
                for (ClusterInstanceInfo instance : clusterInstances) {
                    LOG.log(DEBUG, instance);
                }
            }
        } catch (InvalidName e) {
            throw new IllegalStateException("Registering GroupInfoService failed: {0}", e);
        }
    }

    @Override
    public String toString() {
        return description;
    }
}
