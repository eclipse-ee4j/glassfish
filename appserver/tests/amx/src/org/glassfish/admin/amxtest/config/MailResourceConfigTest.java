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

/*
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/config/MailResourceConfigTest.java,v 1.9 2007/05/05 05:23:55 tcfujii Exp $
* $Revision: 1.9 $
* $Date: 2007/05/05 05:23:55 $
*/
package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.MailResourceConfig;
import com.sun.appserv.management.config.ResourceConfigKeys;
import com.sun.appserv.management.config.ResourceRefConfig;
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.helper.RefHelper;
import com.sun.appserv.management.util.misc.MapUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


import com.sun.appserv.management.helper.AttributeResolverHelper;

/**
 */
public final class MailResourceConfigTest
        extends ResourceConfigTestBase {
    private static final String MAIL_RESOURCE_HOST = "localhost";
    private static final String MAIL_RESOURCE_USER = "someone";
    private static final String MAIL_RESOURCE_FROM = "someone@somewhere.com";

    private static final Map<String, String> OPTIONS = Collections.unmodifiableMap(MapUtil.newMap(
            new String[]{ResourceConfigKeys.ENABLED_KEY, "false"}));

    public MailResourceConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getDomainConfig());
        }
    }

    public static String
    getDefaultInstanceName() {
        return getDefaultInstanceName("MailResourceConfig");
    }

    /**
     synchronized because multiple instances are created, and we've chosen to remove/add
     this resource multiple times for some specific tests.
     */
    public static synchronized MailResourceConfig
    ensureDefaultInstance(final DomainConfig dc) {
        MailResourceConfig result = dc.getResourcesConfig().getMailResourceConfigMap().get(getDefaultInstanceName());

        /*
        if ( result != null )
        {
            System.out.println( "ensureDefaultInstance(): removing: " +
                JMXUtil.toString( Util.getExtra(result).getObjectName() ) );
            dc.removeMailResourceConfig( result.getName() );
            result  = null;
        }
        */

        if (result == null) {
            result = createInstance(dc, getDefaultInstanceName(),
                                    MAIL_RESOURCE_HOST, MAIL_RESOURCE_USER, MAIL_RESOURCE_FROM, OPTIONS);
            assert ! AttributeResolverHelper.resolveBoolean( result, "Enabled" );

            final StandaloneServerConfig serverConfig = dc.getServersConfig().getStandaloneServerConfigMap().get("server");

            final Map<String, String> options = new HashMap<String, String>();
            options.put(ResourceConfigKeys.ENABLED_KEY, "false");
            final ResourceRefConfig ref = serverConfig.createResourceRefConfig(result.getName(), options);
            assert ! AttributeResolverHelper.resolveBoolean( ref, "Enabled" );

            RefHelper.removeAllRefsTo(result, false);
        }

        return result;
    }

    public static MailResourceConfig
    createInstance(
            final DomainConfig ss,
            final String name,
            final String host,
            final String user,
            final String from,
            Map<String, String> optional) {
        return ss.getResourcesConfig().createMailResourceConfig(name, host, user, from, optional);
    }

    protected String
    getProgenyTestName() {
        return ("jndi/MailResourceConfigMgrTest");
    }

    protected Container
    getProgenyContainer() {
        return getDomainConfig();
    }

    protected String
    getProgenyJ2EEType() {
        return XTypes.MAIL_RESOURCE_CONFIG;
    }


    protected void
    removeProgeny(final String name) {
        getDomainConfig().getResourcesConfig().removeMailResourceConfig(name);
    }

    protected final AMXConfig
    createProgeny(
            final String name,
            final Map<String, String> options) {
        final MailResourceConfig config =
                getDomainConfig().getResourcesConfig().createMailResourceConfig(name,
                                                           MAIL_RESOURCE_HOST,
                                                           MAIL_RESOURCE_USER,
                                                           MAIL_RESOURCE_FROM,
                                                           options);
        assert (config != null);
        return (config);
    }

}


