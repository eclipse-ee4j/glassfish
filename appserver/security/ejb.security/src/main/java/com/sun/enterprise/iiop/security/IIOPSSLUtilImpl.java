/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.iiop.security;

import com.sun.corba.ee.spi.transport.SocketInfo;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.security.ssl.J2EEKeyManager;
import com.sun.enterprise.security.ssl.SSLUtils;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.security.SecureRandom;
import java.util.List;
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;

import org.glassfish.enterprise.iiop.api.GlassFishORBLocator;
import org.glassfish.enterprise.iiop.api.IIOPSSLUtil;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Service;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;
import static org.glassfish.security.common.SharedSecureRandom.SECURE_RANDOM;

/**
 * @author Kumar
 */
@Service
@Singleton
public class IIOPSSLUtilImpl implements IIOPSSLUtil {
    private static final Logger LOG = LogDomains.getLogger(IIOPSSLUtilImpl.class, LogDomains.SECURITY_LOGGER, false);

    @Inject
    private SSLUtils sslUtils;

    @Override
    public KeyManager[] getKeyManagers(String alias) {
        try {
            if (alias != null && !sslUtils.isTokenKeyAlias(alias)) {
                LOG.log(WARNING,
                    "IIOP1004: Key alias {0} not found in keystore. Returning no key managers, SSL will not be supported.",
                    alias);
                return new KeyManager[0];
            }
            KeyManager[] mgrs = sslUtils.getKeyManagers();
            if (alias != null && mgrs != null && mgrs.length > 0) {
                KeyManager[] newMgrs = new KeyManager[mgrs.length];
                for (int i = 0; i < mgrs.length; i++) {
                    LOG.log(FINE, "Setting J2EEKeyManager for alias {0}", alias);
                    newMgrs[i] = new J2EEKeyManager((X509KeyManager) mgrs[i], alias);
                }
                mgrs = newMgrs;
            }
            return mgrs;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TrustManager[] getTrustManagers() {
        try {
            return sslUtils.getTrustManagers();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SecureRandom getInitializedSecureRandom() {
        return SECURE_RANDOM;
    }

    @Override
    public List<SocketInfo> getSSLPortsAsSocketInfo(com.sun.corba.ee.spi.ior.IOR ior) {
        SecurityMechanismSelector selector = Lookups.getSecurityMechanismSelector();
        return selector.getSSLSocketInfo(ior);
    }

    @Override
    public TaggedComponent createSSLTaggedComponent(IORInfo iorInfo, List<com.sun.corba.ee.spi.folb.SocketInfo> socketInfos) {
        int sslMutualAuthPort = -1;
        try {
            if (iorInfo instanceof com.sun.corba.ee.spi.legacy.interceptor.IORInfoExt extInfo) {
                sslMutualAuthPort = extInfo.getServerPort("SSL_MUTUALAUTH");
            }
        } catch (com.sun.corba.ee.spi.legacy.interceptor.UnknownType ute) {
            LOG.log(FINE, "UnknownType exception", ute);
        }

        LOG.log(FINE, "sslMutualAuthPort: {0}", sslMutualAuthPort);

        // Cannot be an injected field, leads to a cyclic dependency, makes HK2 angry.
        final GlassFishORBLocator orbLocator = Globals.get(GlassFishORBLocator.class);
        EjbDescriptor desc = orbLocator.getEjbDescriptor(iorInfo);
        if (desc == null) {
            return null;
        }
        CSIV2TaggedComponentInfo ctc = new CSIV2TaggedComponentInfo(sslMutualAuthPort, orbLocator.getORB());
        return ctc.createSecurityTaggedComponent(socketInfos, desc);
    }
}
