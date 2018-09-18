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

package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.MessageSecurityConfig;
import com.sun.appserv.management.config.ProviderConfig;
import com.sun.appserv.management.config.SecurityServiceConfig;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.util.HashMap;
import java.util.Map;


/**
 */
public final class MessageSecurityConfigTest
        extends AMXTestBase {
    public MessageSecurityConfigTest() {
    }

    private static SecurityServiceConfig
    getDefaultSecurityServiceConfig(final DomainRoot domainRoot) {
        final ConfigConfig config = ConfigConfigTest.ensureDefaultInstance(domainRoot);
        final SecurityServiceConfig ss = config.getSecurityServiceConfig();
        assert (ss != null);
        return ss;
    }

    private static MessageSecurityConfig
    create(
            final DomainRoot domainRoot,
            final String authLayer) {
        final SecurityServiceConfig ss = getDefaultSecurityServiceConfig(domainRoot);

        final Map<String, String> optional = new HashMap<String, String>();
        final MessageSecurityConfig msc = ss.createMessageSecurityConfig(authLayer,
                                                                         "ClientProvider", ProviderConfig.PROVIDER_TYPE_CLIENT,
                                                                         "com.sun.xml.wss.provider.ClientSecurityAuthModul", optional);

        msc.createProviderConfig("ServerProvider",
                                 ProviderConfig.PROVIDER_TYPE_SERVER, "com.sun.xml.wss.provider.ServerSecurityAuthModule", optional);

        msc.createProviderConfig("DummyProvider1",
                                 ProviderConfig.PROVIDER_TYPE_SERVER, "AMX.TEST.DummySecurityAuthModule", optional);

        msc.createProviderConfig("DummyProvider2",
                                 ProviderConfig.PROVIDER_TYPE_SERVER, "AMX.TEST.DummySecurityAuthModule", optional);

        msc.removeProviderConfig("DummyProvider1");
        msc.removeProviderConfig("DummyProvider2");

        return msc;
    }

    static private final String AUTH_TYPE = MessageSecurityConfig.AUTH_LAYER_HTTP_SERVLET;

    /**
     Note: this can't be tested except by making a new one, and the names are predefined, so
     if it already exists, it must be deleted first.
     */
    public void
    testCreateRemove() {
        final SecurityServiceConfig ss = getDefaultSecurityServiceConfig(getDomainRoot());
        final Map<String, MessageSecurityConfig> messageSecurityConfigs = ss.getMessageSecurityConfigMap();
        MessageSecurityConfig msc = messageSecurityConfigs.get(AUTH_TYPE);

        if (msc != null) {
            ss.removeMessageSecurityConfig(AUTH_TYPE);
            msc = null;
        }

        msc = create(getDomainRoot(), AUTH_TYPE);

        ss.removeMessageSecurityConfig(AUTH_TYPE);
    }

}



























