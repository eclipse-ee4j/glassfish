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

import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.HTTPListenerConfig;
import com.sun.appserv.management.config.HTTPListenerConfigKeys;
import com.sun.appserv.management.config.HTTPServiceConfig;
import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.config.SSLConfig;
import static com.sun.appserv.management.config.SSLConfigKeys.*;
import com.sun.appserv.management.util.misc.MapUtil;
import com.sun.appserv.management.util.misc.TypeCast;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 */
public final class HTTPListenerConfigTest
        extends ConfigMgrTestBase {
    static final String ADDRESS = "0.0.0.0";
    static final String DEF_VIRTUAL_SERVER = "server";
    static final String SERVER_NAME = "localhost";

    // !!! deliberately use old, incorrect form; it should still succeed
    static final Map<String, Object> OPTIONAL = new HashMap<String, Object>();

    static {
        OPTIONAL.put(PropertiesAccess.PROPERTY_PREFIX + "xyz", "abc");
        OPTIONAL.put(HTTPListenerConfigKeys.ENABLED_KEY, Boolean.FALSE);
        OPTIONAL.put(HTTPListenerConfigKeys.ACCEPTOR_THREADS_KEY, new Integer(4));
        //OPTIONAL.put( HTTPListenerConfigKeys.BLOCKING_ENABLED_KEY, "false" );
        //OPTIONAL.put( HTTPListenerConfigKeys.REDIRECT_PORT_KEY, "9081" );
        OPTIONAL.put(HTTPListenerConfigKeys.XPOWERED_BY_KEY, Boolean.TRUE);
        //OPTIONAL.put( HTTPListenerConfigKeys.FAMILY_KEY, HTTPListenerConfigFamilyValues.INET );
    }

    public HTTPListenerConfigTest() {
    }


    HTTPServiceConfig
    getHTTPServiceConfig() {
        return (getConfigConfig().getHTTPServiceConfig());
    }

    public void
    testGetHTTPListeners() {
        final HTTPServiceConfig httpService =
                getConfigConfig().getHTTPServiceConfig();

        final Map<String, HTTPListenerConfig> proxies = httpService.getHTTPListenerConfigMap();

        for (final String listenerName : proxies.keySet()) {
            final HTTPListenerConfig listener = (HTTPListenerConfig)
                    proxies.get(listenerName);

            listener.getEnabled();
        }
    }

    protected String
    getProgenyTestName() {
        return ("HTTPListenerConfigMgrTest-test-listener");
    }

    protected Container
    getProgenyContainer() {
        return getHTTPService();
    }

    protected String
    getProgenyJ2EEType() {
        return XTypes.HTTP_LISTENER_CONFIG;
    }


    protected void
    removeProgeny(final String name) {
        getHTTPService().removeHTTPListenerConfig(name);
    }

    protected final AMXConfig
    createProgeny(
            final String name,
            final Map<String, String> options) {
        // this is incorrect code-on purpose-to test backward compatibility with Maps
        // that aren't of type <String,String>
        final Map<String, String> optional = TypeCast.asMap(OPTIONAL);
        assert (!MapUtil.isAllStrings(optional));

        final Map<String, String> allOptions = MapUtil.newMap(options, optional);
        assert (!MapUtil.isAllStrings(allOptions));

        final int port = 31000 + (name.hashCode() % 31000);

        final HTTPListenerConfig config =
                getHTTPService().createHTTPListenerConfig(name,
                                                          ADDRESS, port, DEF_VIRTUAL_SERVER, SERVER_NAME, allOptions);

        return (config);
    }

    protected final HTTPServiceConfig
    getHTTPService() {
        return getConfigConfig().getHTTPServiceConfig();
    }


    public void
    testCreateSSL()
            throws Exception {
        final Map<String, String> options =
                Collections.unmodifiableMap(MapUtil.newMap(
                        new String[]
                                {
                                        CLIENT_AUTH_ENABLED_KEY, "false",
                                        SSL_2_ENABLED_KEY, "true",
                                        SSL_3_ENABLED_KEY, "true",
                                        SSL_2_CIPHERS_KEY, "+rc4,-rc4export,-rc2,-rc2export,+idea,+des,+desede3",
                                        SSL3_TLS_CIPHERS_KEY,
                                        "+rsa_rc4_128_md5,+rsa3des_sha,+rsa_des_sha,-rsa_rc4_40_md5" +
                                                "-rsa_rc2_40_md5,-rsa_null_md5,-rsa_des_56_sha,-rsa_rc4_56_sha",
                                        TLS_ENABLED_KEY, "true",
                                        TLS_ROLLBACK_ENABLED_KEY, "true",
                                }
                ));

        if (!checkNotOffline("testCreateSSL")) {
            return;
        }

        final String NAME = "HTTPListenerConfigMgr-listener-for-testCreateSSL";

        try {
            removeEx(NAME);
            final HTTPListenerConfig newListener =
                    (HTTPListenerConfig) createProgeny(NAME, null);
            assert newListener != null;

            // verify that the new listener is present
            final Map<String, HTTPListenerConfig> listeners =
                    getHTTPService().getHTTPListenerConfigMap();
            final HTTPListenerConfig listener = listeners.get(NAME);
            assert listener != null;
            assert listener == newListener;

            final String CERT_NICKNAME = NAME + "Cert";

            final SSLConfig ssl = listener.createSSLConfig(CERT_NICKNAME, options);
            assert ssl != null;
            assert ssl.getCertNickname().equals(CERT_NICKNAME);

            listener.removeSSLConfig();
        }
        finally {
            remove(NAME);
        }
    }
}


