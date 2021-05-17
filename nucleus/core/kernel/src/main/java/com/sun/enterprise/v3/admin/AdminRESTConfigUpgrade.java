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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.module.bootstrap.EarlyLogHandler;
import org.glassfish.grizzly.config.dom.Http;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.Protocols;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Adds the needed http.setEncodedSlashEnabled  to domain.xml
 * during an upgrade from a v2.X server. For more information see:
 * https://glassfish.dev.java.net/issues/show_bug.cgi?id=13627
 */
@Service
public class AdminRESTConfigUpgrade
        implements ConfigurationUpgrade, PostConstruct {

    @Inject
    Configs configs;

    // http://java.net/jira/browse/GLASSFISH-15576
    // This will force the Grizzly upgrade code to run before
    // AdminRESTConfigUpgrade runs.
    @Inject @Named("grizzlyconfigupgrade") @Optional
    ConfigurationUpgrade precondition = null;

    @Override
    public void postConstruct() {
        for (Config config : configs.getConfig()) {
            // we only want to handle configs that have an admin listener
            try {
                if (config.getAdminListener() == null) {
                    LogRecord lr = new LogRecord(Level.FINE, String.format(
                            "Skipping config %s. No admin listener.",
                            config.getName()));
                    lr.setLoggerName(getClass().getName());
                    EarlyLogHandler.earlyMessages.add(lr);
                    continue;
                }
            } catch (IllegalStateException ise) {
                /*
                 * I've only seen the exception rather than
                 * getAdminListener returning null. This should
                 * typically happen for any config besides
                 * <server-config>, but we'll proceed if any
                 * config has an admin listener.
                 */
                LogRecord lr = new LogRecord(Level.FINE, String.format(
                        "Skipping config %s. getAdminListener threw: %s",
                        config.getName(), ise.getLocalizedMessage()));
                lr.setLoggerName(getClass().getName());
                EarlyLogHandler.earlyMessages.add(lr);
                continue;
            }
            Protocols ps = config.getNetworkConfig().getProtocols();
            if (ps != null) {
                for (Protocol p : ps.getProtocol()) {
                    Http h = p.getHttp();
                    if (h != null
                            && "__asadmin".equals(h.getDefaultVirtualServer())) {
                        try {
                            ConfigSupport.apply(new HttpConfigCode(), h);
                        } catch (TransactionFailure tf) {
                            LogRecord lr = new LogRecord(Level.SEVERE,
                                    "Could not upgrade http element for admin console: "+ tf);
                            lr.setLoggerName(getClass().getName());
                            EarlyLogHandler.earlyMessages.add(lr);
                        }
                    }
                }


            }
        }
    }


    static private class HttpConfigCode implements SingleConfigCode<Http> {

        @Override
        public Object run(Http http) throws PropertyVetoException,
                TransactionFailure {

            http.setEncodedSlashEnabled("true");
            return null;
        }
    }
}
