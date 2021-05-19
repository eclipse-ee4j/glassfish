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

package com.sun.enterprise.iiop.security;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.security.ssl.J2EEKeyManager;
import com.sun.enterprise.security.ssl.SSLUtils;
import com.sun.logging.LogDomains;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.inject.Inject;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;
import org.glassfish.enterprise.iiop.api.IIOPSSLUtil;
import org.glassfish.internal.api.SharedSecureRandom;

import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Singleton;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;

/**
 *
 * @author Kumar
 */
@Service
@Singleton
public class IIOPSSLUtilImpl implements IIOPSSLUtil {
    @Inject
    private SSLUtils sslUtils;

    private GlassFishORBHelper orbHelper;

    private static final Logger _logger;
    static {
        _logger = LogDomains.getLogger(IIOPSSLUtilImpl.class, LogDomains.SECURITY_LOGGER);
    }
    private Object appClientSSL;

    public Object getAppClientSSL() {
        return this.appClientSSL;
    }

    public void setAppClientSSL(Object ssl) {
        this.appClientSSL = ssl;
    }

    public KeyManager[] getKeyManagers(String alias) {
        KeyManager[] mgrs = null;
        try {
            if (alias != null && !sslUtils.isTokenKeyAlias(alias)) {
                throw new IllegalStateException(getFormatMessage("iiop.cannot_find_keyalias", new Object[] { alias }));
            }

            mgrs = sslUtils.getKeyManagers();
            if (alias != null && mgrs != null && mgrs.length > 0) {
                KeyManager[] newMgrs = new KeyManager[mgrs.length];
                for (int i = 0; i < mgrs.length; i++) {
                    if (_logger.isLoggable(Level.FINE)) {
                        StringBuffer msg = new StringBuffer("Setting J2EEKeyManager for ");
                        msg.append(" alias : " + alias);
                        _logger.log(Level.FINE, msg.toString());
                    }
                    newMgrs[i] = new J2EEKeyManager((X509KeyManager) mgrs[i], alias);
                }
                mgrs = newMgrs;
            }
        } catch (Exception e) {
            // TODO: log here
            throw new RuntimeException(e);
        }
        return mgrs;
    }

    public TrustManager[] getTrustManagers() {
        try {
            return sslUtils.getTrustManagers();
        } catch (Exception e) {
            // TODO: log here
            throw new RuntimeException(e);
        }
    }

    /**
     * This API get the format string from resource bundle of _logger.
     *
     * @param key the key of the message
     * @param params the parameter array of Object
     * @return the format String for _logger
     */
    private String getFormatMessage(String key, Object[] params) {
        return MessageFormat.format(_logger.getResourceBundle().getString(key), params);
    }

    public SecureRandom getInitializedSecureRandom() {
        return SharedSecureRandom.get();
    }

    @Override
    public Object getSSLPortsAsSocketInfo(Object ior) {
        SecurityMechanismSelector selector = Lookups.getSecurityMechanismSelector();
        return selector.getSSLSocketInfo(ior);
    }

    public TaggedComponent createSSLTaggedComponent(IORInfo iorInfo, Object sInfos) {
        List<com.sun.corba.ee.spi.folb.SocketInfo> socketInfos = (List<com.sun.corba.ee.spi.folb.SocketInfo>) sInfos;
        orbHelper = Lookups.getGlassFishORBHelper();
        TaggedComponent result = null;
        org.omg.CORBA.ORB orb = orbHelper.getORB();
        int sslMutualAuthPort = -1;
        try {
            if (iorInfo instanceof com.sun.corba.ee.spi.legacy.interceptor.IORInfoExt) {
                sslMutualAuthPort = ((com.sun.corba.ee.spi.legacy.interceptor.IORInfoExt) iorInfo).getServerPort("SSL_MUTUALAUTH");
            }
        } catch (com.sun.corba.ee.spi.legacy.interceptor.UnknownType ute) {
            _logger.log(Level.FINE, ".isnert: UnknownType exception", ute);
        }

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, ".insert: sslMutualAuthPort: " + sslMutualAuthPort);
        }

        CSIV2TaggedComponentInfo ctc = new CSIV2TaggedComponentInfo(orb, sslMutualAuthPort);
        EjbDescriptor desc = ctc.getEjbDescriptor(iorInfo);
        if (desc != null) {
            result = ctc.createSecurityTaggedComponent(socketInfos, desc);
        }
        return result;
    }

}
