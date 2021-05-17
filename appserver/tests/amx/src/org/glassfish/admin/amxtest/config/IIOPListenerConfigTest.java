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
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/config/IIOPListenerConfigTest.java,v 1.6 2007/05/05 05:23:54 tcfujii Exp $
* $Revision: 1.6 $
* $Date: 2007/05/05 05:23:54 $
*/
package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.IIOPListenerConfig;
import com.sun.appserv.management.config.IIOPListenerConfigKeys;
import com.sun.appserv.management.config.IIOPServiceConfig;
import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.config.SSLConfig;
import com.sun.appserv.management.util.misc.MapUtil;

import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Map;

/**
 */
public final class IIOPListenerConfigTest
        extends ConfigMgrTestBase {
    static final String ADDRESS = "0.0.0.0";
    static final Map<String, String> OPTIONAL = new HashMap<String, String>();

    static {
        OPTIONAL.put(PropertiesAccess.PROPERTY_PREFIX + "xyz", "abc");
        OPTIONAL.put(IIOPListenerConfigKeys.ENABLED_KEY, "false");
        OPTIONAL.put(IIOPListenerConfigKeys.SECURITY_ENABLED_KEY, "true");
    }

    public IIOPListenerConfigTest() {
    }

    protected Container
    getProgenyContainer() {
        return getIIOPService();
    }

    protected String
    getProgenyJ2EEType() {
        return XTypes.IIOP_LISTENER_CONFIG;
    }


    protected void
    removeProgeny(final String name) {
        getIIOPService().removeIIOPListenerConfig(name);
    }


    protected final ObjectName
    create(String name) {
        return Util.getObjectName(createProgeny(name, null));
    }

    protected final AMXConfig
    createProgeny(
            final String name,
            final Map<String, String> options) {
        final Map<String, String> allOptions = MapUtil.newMap(options, OPTIONAL);

        final int port = (name.hashCode() % 32000) + 32000;
        allOptions.put(IIOPListenerConfigKeys.PORT_KEY, "" + port);

        return getIIOPService().createIIOPListenerConfig(name, ADDRESS, allOptions);
    }

    protected final IIOPServiceConfig
    getIIOPService() {
        return getConfigConfig().getIIOPServiceConfig();
    }

    public void
    testCreateSSL()
            throws Exception {
        if (!checkNotOffline("testCreateSSL")) {
            return;
        }

        final String NAME = "IIOPListenerConfigMgr-testCreateSSL";

        removeEx(NAME);

        final IIOPListenerConfig newListener =
                (IIOPListenerConfig) createProgeny(NAME, null);

        try {
            final Map<String, IIOPListenerConfig> listeners =
                    getIIOPService().getIIOPListenerConfigMap();

            final IIOPListenerConfig listener =
                    (IIOPListenerConfig) listeners.get(NAME);
            assert listener != null;
            assert listener == newListener;

            final String CERT_NICKNAME = NAME + "Cert";

            final SSLConfig ssl = listener.createSSLConfig(CERT_NICKNAME, null);
            assert ssl != null;
            assert ssl.getCertNickname().equals(CERT_NICKNAME);

            listener.removeSSLConfig();
        }
        finally {
            remove(NAME);
        }
    }
/*
        public void
    testCreateSSLClientConfig()
        throws Exception
    {
        final Set<IIOPServiceConfig> s = getQueryMgr().getJ2EETypeProxies("X-IIOPServiceConfig");
        assert s.size() >= 0;
        IIOPServiceConfig iiopService = (IIOPServiceConfig)s.iterator().next();
        assert iiopService != null;
        Map sslParams = new HashMap();
        sslParams.put("CertNickname", "mycert");
        final ObjectName on = iiopService.createIIOPSSLClientConfig(sslParams);
        assert on != null && on.equals(iiopService.getIIOPSSLClientConfigObjectName());
        IIOPSSLClientConfig sslClientConfig = iiopService.getIIOPSSLClientConfig();
        assert sslClientConfig != null;
    }
 */
}


