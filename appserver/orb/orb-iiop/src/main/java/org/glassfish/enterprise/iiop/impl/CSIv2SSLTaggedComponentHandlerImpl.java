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

package org.glassfish.enterprise.iiop.impl;

import com.sun.corba.ee.impl.folb.CSIv2SSLTaggedComponentHandler;
import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.orb.DataCollector;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBConfigurator;
import com.sun.corba.ee.spi.transport.SocketInfo;
import com.sun.logging.LogDomains;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.enterprise.iiop.api.IIOPSSLUtil;
import org.glassfish.internal.api.Globals;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;

/**
 * @author Harold Carr, 2005
 */
public class CSIv2SSLTaggedComponentHandlerImpl extends org.omg.CORBA.LocalObject
    implements CSIv2SSLTaggedComponentHandler, ORBConfigurator {

    private static final Logger _logger = LogDomains.getLogger(
        CSIv2SSLTaggedComponentHandlerImpl.class, LogDomains.CORBA_LOGGER);

    private final String baseMsg = CSIv2SSLTaggedComponentHandlerImpl.class.getName();

    private ORB orb;

    ////////////////////////////////////////////////////
    //
    // CSIv2SSLTaggedComponentHandler
    //

    @Override
    public TaggedComponent insert(IORInfo iorInfo, List<ClusterInstanceInfo> clusterInstanceInfo) {
        try {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "{0}.insert->:", baseMsg);
            }

            List<com.sun.corba.ee.spi.folb.SocketInfo> socketInfos = new ArrayList<>();
            for (ClusterInstanceInfo clInstInfo : clusterInstanceInfo) {
                for (com.sun.corba.ee.spi.folb.SocketInfo sinfo : clInstInfo.endpoints()) {
                    if (sinfo.type().equals("SSL") || sinfo.type().equals("SSL_MUTUALAUTH")) {
                        socketInfos.add(sinfo);
                    }
                }
            }
            IIOPSSLUtil sslUtil = null;
            if (Globals.getDefaultHabitat() != null) {
                sslUtil = Globals.getDefaultHabitat().getService(IIOPSSLUtil.class);
                return sslUtil.createSSLTaggedComponent(iorInfo, socketInfos);
            } else {
                return null;
            }

        } finally {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "{0}.insert<-: {1}", new Object[] {baseMsg, null});
            }
        }
    }


    @Override
    public List<SocketInfo> extract(IOR ior) {
        List<SocketInfo> socketInfo = null;
        try {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "{0}.extract->:", baseMsg);
            }

            // IIOPProfileTemplate iiopProfileTemplate =
            // (IIOPProfileTemplate)ior.getProfile().getTaggedProfileTemplate();
            // IIOPAddress primary = iiopProfileTemplate.getPrimaryAddress() ;
            // String host = primary.getHost().toLowerCase(Locale.ENGLISH);

            IIOPSSLUtil sslUtil = null;
            if (Globals.getDefaultHabitat() != null) {
                sslUtil = Globals.getDefaultHabitat().getService(IIOPSSLUtil.class);
                socketInfo = (List<SocketInfo>) sslUtil.getSSLPortsAsSocketInfo(ior);
            }

            if (socketInfo == null) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "{0}.extract: did not find SSL SocketInfo", baseMsg);
                }
            } else {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "{0}.extract: found SSL socketInfo", baseMsg);
                }
            }
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "{0}.extract: Connection Context", baseMsg);
            }
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "Exception getting SocketInfo", ex);
        } finally {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "{0}.extract<-: {1}", new Object[] {baseMsg, socketInfo});
            }
        }
        return socketInfo;
    }

    ////////////////////////////////////////////////////
    //
    // ORBConfigurator
    //

    @Override
    public void configure(DataCollector collector, ORB orb) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, ".configure->:");
        }

        this.orb = orb;
        try {
            orb.register_initial_reference(ORBConstants.CSI_V2_SSL_TAGGED_COMPONENT_HANDLER, this);
        } catch (InvalidName e) {
            _logger.log(Level.WARNING, ".configure: ", e);
        }

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, ".configure<-:");
        }
    }
}
