/*
 * Copyright (c) 1997, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.impl.j2ee;

import com.sun.enterprise.config.serverbeans.Server;

import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.impl.config.ConfigBeanRegistry;
import org.glassfish.admin.amx.impl.util.Issues;
import org.glassfish.admin.amx.impl.util.ObjectNameBuilder;
import org.glassfish.admin.amx.j2ee.J2EEDomain;
import org.glassfish.admin.amx.j2ee.J2EEManagedObject;
import org.glassfish.admin.amx.j2ee.J2EETypes;

/**
 * Base implementation for the J2EEDomain for DAS and non-DAS server instances.
 */
public class J2EEDomainImpl extends J2EEManagedObjectImplBase {

    public static final Class<? extends J2EEManagedObject> INTF = J2EEDomain.class;

    private String serverName = "server";

    public J2EEDomainImpl(final ObjectName parentObjectName, final Metadata meta) {
        super(parentObjectName, meta, INTF);
        Issues.getAMXIssues().notDone("J2EEDomainImpl needs to account for DAS/non-DAS");
    }

    /**
     * JSR 77 impl
     *
     * @return String representation of the ObjectName
     */
    public String[] getservers() {

        return getChildrenAsStrings(J2EETypes.J2EE_SERVER);
    }

    @Override
    protected String getExtraObjectNameProps(final MBeanServer server, final ObjectName nameIn) {
        // SPECIAL CASE per JSR 77 spec:
        // add the 'name' property even though this is a singleton
        String props = super.getExtraObjectNameProps(server, nameIn);
        final String nameProp = Util.makeNameProp(nameIn.getDomain());
        props = Util.concatenateProps(props, nameProp);

        return props;
    }

    @Override
    protected void
    registerChildren() {

        final ObjectNameBuilder builder = getObjectNames();

        final MetadataImpl meta = defaultChildMetadata();
        List<Server> servers = getDomain().getServers().getServer();
        for (Server server : servers) {

            meta.setCorrespondingConfig(ConfigBeanRegistry.getInstance().getObjectNameForProxy(server));
            final DASJ2EEServerImpl impl = new DASJ2EEServerImpl(getObjectName(), meta);
            ObjectName serverObjectName = builder.buildChildObjectName(J2EETypes.J2EE_SERVER, server.getName());
            registerChild(impl, serverObjectName);
        }
        //ImplUtil.getLogger().info( "Registered J2EEDomain as " + getObjectName() + " with J2EEServer of " + serverObjectName);
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}







