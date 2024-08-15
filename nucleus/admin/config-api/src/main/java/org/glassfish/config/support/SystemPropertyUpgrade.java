/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.config.support;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.serverbeans.SystemPropertyBag;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import static com.sun.enterprise.config.util.PortConstants.ADMIN;
import static com.sun.enterprise.config.util.PortConstants.DEBUG;
import static com.sun.enterprise.config.util.PortConstants.DEFAULT_JAVA_DEBUGGER_PORT;
import static com.sun.enterprise.config.util.PortConstants.DEFAULT_OSGI_SHELL_TELNET_PORT;
import static com.sun.enterprise.config.util.PortConstants.HTTP;
import static com.sun.enterprise.config.util.PortConstants.OSGI;

/**
 * Upgrade service to add the new 3.1 system properties to the config elements (except DAS config, server-config) in
 * existing domain.xml: <system-property name="ASADMIN_LISTENER_PORT" value="24848"></system-property>
 * <system-property name="OSGI_SHELL_TELNET_PORT" value="26666"></system-property>
 * <system-property name="JAVA_DEBUGGER_PORT" value="29009"></system-property>
 *
 * Use the same prefix as the config's system property HTTP_LISTENER_PORT if it exists.
 *
 * @author Jennifer Chou
 */
@Service
public class SystemPropertyUpgrade implements ConfigurationUpgrade, PostConstruct {

    @Inject
    Configs configs;

    @Inject
    Domain domain;

    @Inject
    Servers servers;

    /*
     * Required to make gms changes before any changes to a cluster
     * or config can be saved. This is because GMS changed attribute
     * names from v2 to 3.1. (Issue 15195.)
     */
    @Inject
    @Named("gmsupgrade")
    @Optional
    ConfigurationUpgrade precondition = null;

    private String PREFIX = "2";
    private int DEFAULT_ADMIN_PORT = 4848;
    private static final String DAS_CONFIG = "server-config";
    private static final String DEFAULT_CONFIG = "default-config";
    private static final String DAS = "server";

    public void postConstruct() {
        upgradeConfigElements();
        upgradeServerElements();
    }

    private void upgradeConfigElements() {
        int incr = 0;
        for (Config c : configs.getConfig()) {
            try {
                if (!c.getName().equals(DAS_CONFIG)) {
                    SystemPropertyBag bag = c;
                    String httpVal = bag.getSystemProperty(HTTP).getValue();
                    if (httpVal != null) {
                        PREFIX = httpVal.substring(0, httpVal.length() - 4);
                        final int adminPort;
                        final int osgiPort;
                        final int debugPort;
                        if (!c.getName().equals(DEFAULT_CONFIG)) {
                            adminPort = DEFAULT_ADMIN_PORT + incr;
                            osgiPort = DEFAULT_OSGI_SHELL_TELNET_PORT + incr;
                            debugPort = DEFAULT_JAVA_DEBUGGER_PORT + incr;
                            incr++;
                        } else {
                            adminPort = DEFAULT_ADMIN_PORT;
                            osgiPort = DEFAULT_OSGI_SHELL_TELNET_PORT;
                            debugPort = DEFAULT_JAVA_DEBUGGER_PORT;
                        }

                        ConfigSupport.apply(new SingleConfigCode<SystemPropertyBag>() {

                            public Object run(SystemPropertyBag config) throws PropertyVetoException, TransactionFailure {

                                createSystemProperty(config, ADMIN, adminPort);
                                createSystemProperty(config, OSGI, osgiPort);
                                createSystemProperty(config, DEBUG, debugPort);

                                return null;
                            }
                        }, c);
                    }

                }
            } catch (Exception e) {
                Logger.getAnonymousLogger().log(Level.SEVERE, Strings.get("SystemPropertyUpgrade.Failure", c), e);
                throw new RuntimeException(e);
            }
        }
    }

    private void upgradeServerElements() {
        //TODO Do this per node host - we can reuse port #s if they are on different hosts
        int incr = 0;
        for (Cluster c : domain.getClusters().getCluster()) {
            for (Server s : c.getInstances()) {
                incr = createServerSystemProperty(s, incr);
            }
        }
    }

    private int createServerSystemProperty(Server s, int incr) {
        try {
            if (!s.getName().equals(DAS)) {
                SystemPropertyBag bag = s;
                if (bag.getSystemProperty(HTTP) != null) {
                    final String httpVal = bag.getSystemProperty(HTTP).getValue();
                    if (httpVal != null) {
                        PREFIX = httpVal.substring(0, httpVal.length() - 5);

                        String configAdminSP = null;
                        String configOsgiSP = null;
                        String configDebugSP = null;
                        for (SystemProperty sp : s.getConfig().getSystemProperty()) {
                            if (sp.getName().equals(ADMIN)) {
                                configAdminSP = sp.getValue();
                            }
                            if (sp.getName().equals(OSGI)) {
                                configOsgiSP = sp.getValue();
                            }
                            if (sp.getName().equals(DEBUG)) {
                                configDebugSP = sp.getValue();
                            }
                        }
                        int baseAdmin;
                        if (configAdminSP == null) {
                            baseAdmin = DEFAULT_ADMIN_PORT;
                        } else {
                            baseAdmin = Integer.parseInt(configAdminSP);
                        }
                        int baseOsgi;
                        if (configOsgiSP == null) {
                            baseOsgi = DEFAULT_OSGI_SHELL_TELNET_PORT;
                        } else {
                            baseOsgi = Integer.parseInt(configOsgiSP);
                        }
                        int baseDebug;
                        if (configDebugSP == null) {
                            baseDebug = DEFAULT_JAVA_DEBUGGER_PORT;
                        } else {
                            baseDebug = Integer.parseInt(configDebugSP);
                        }
                        incr++;
                        final int adminPort = baseAdmin + incr;
                        final int osgiPort = baseOsgi + incr;
                        final int debugPort = baseDebug + incr;

                        ConfigSupport.apply(new SingleConfigCode<SystemPropertyBag>() {

                            public Object run(SystemPropertyBag config) throws PropertyVetoException, TransactionFailure {

                                createSystemProperty(config, ADMIN, adminPort);
                                createSystemProperty(config, OSGI, osgiPort);
                                createSystemProperty(config, DEBUG, debugPort);

                                return null;
                            }
                        }, s);
                    }
                }
            }
            return incr;
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, Strings.get("SystemPropertyUpgrade.Failure", s), e);
            throw new RuntimeException(e);
        }
    }

    private void createSystemProperty(SystemPropertyBag spb, String portName, int portVal)
            throws TransactionFailure, PropertyVetoException {
        if (spb.getSystemProperty(portName) == null) {
            SystemProperty newSysProp = spb.createChild(SystemProperty.class);
            newSysProp.setName(portName);
            newSysProp.setValue(PREFIX + portVal);
            spb.getSystemProperty().add(newSysProp);
        }
    }
}
