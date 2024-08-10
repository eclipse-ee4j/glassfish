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

import java.util.Set;

import javax.management.ObjectName;

import org.glassfish.admin.amx.impl.util.Issues;
import org.glassfish.admin.amx.j2ee.J2EEServer;
import org.glassfish.admin.amx.util.SetUtil;

import static org.glassfish.admin.amx.j2ee.J2EETypes.JAVA_MAIL_RESOURCE;
import static org.glassfish.admin.amx.j2ee.J2EETypes.JCA_RESOURCE;
import static org.glassfish.admin.amx.j2ee.J2EETypes.JDBC_RESOURCE;
import static org.glassfish.admin.amx.j2ee.J2EETypes.JMS_RESOURCE;
import static org.glassfish.admin.amx.j2ee.J2EETypes.JNDI_RESOURCE;
import static org.glassfish.admin.amx.j2ee.J2EETypes.JTA_RESOURCE;
import static org.glassfish.admin.amx.j2ee.J2EETypes.JVM;
import static org.glassfish.admin.amx.j2ee.J2EETypes.RMI_IIOP_RESOURCE;
import static org.glassfish.admin.amx.j2ee.J2EETypes.URL_RESOURCE;

/**
JSR 77 extension representing an Appserver standalone server (non-clustered).

Note that this class has a subclass:  DASJ2EEServerImpl.
 */
public class J2EEServerImpl extends J2EELogicalServerImplBase
{
    public static final Class<J2EEServer> INTF = J2EEServer.class;

    private volatile RegistrationSupport mRegistrationSupport = null;

    public J2EEServerImpl(final ObjectName parentObjectName, final Metadata meta)
    {
        super(parentObjectName, meta, INTF);
    }
    /* The vendor information for this server. */

    private static final String serverVendor = "Oracle Corporation";

    public String[] getjavaVMs()
    {
        final ObjectName child = child(JVM);

        return child == null ? new String[0] : new String[]
                {
                    child.toString()
                };
    }

    public static final Set<String> J2EE_RESOURCE_TYPES = SetUtil.newUnmodifiableStringSet(
        JDBC_RESOURCE,
        JAVA_MAIL_RESOURCE,
        JCA_RESOURCE,
        JMS_RESOURCE,
        JNDI_RESOURCE,
        JTA_RESOURCE,
        RMI_IIOP_RESOURCE,
        URL_RESOURCE
    );

    public String[] getresources()
    {
        return getChildrenAsStrings( J2EE_RESOURCE_TYPES );
    }

    public String getserverVersion()
    {
        Issues.getAMXIssues().notDone("How to get the server version");
        return "Glassfish V3.1";
    }

    public String getserverVendor()
    {
        return serverVendor;
    }

    public String getjvm()
    {
        return "" + getAncestorByType(JVM);
    }

    @Override
    protected void registerChildren()
    {
        super.registerChildren();

        final J2EEServer selfProxy = getSelf(J2EEServer.class);
        mRegistrationSupport = new RegistrationSupport( selfProxy );
        mRegistrationSupport.start();
    }

    @Override
    protected void unregisterChildren()
    {
        if (mRegistrationSupport != null) {
            mRegistrationSupport.cleanup();
        }
        super.unregisterChildren();
    }

}





















