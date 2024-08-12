/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.extras.addons;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.config.ConfigAwareElement;
import org.glassfish.grizzly.config.dom.Http;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.http.ajp.AjpAddOn;
import org.glassfish.grizzly.http.server.AddOn;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

/**
 * Ajp service.
 *
 * @author Alexey Stashok
 */
@Service(name = "ajp")
@ContractsProvided({AjpAddOnProvider.class, AddOn.class})
public class AjpAddOnProvider extends AjpAddOn implements ConfigAwareElement<Http> {

    protected static final Logger _logger = Logger.getLogger("jakarta.enterprise.web");

    @Override
    public void configure(final ServiceLocator habitat,
            final NetworkListener networkListener, final Http http) {

        final boolean jkSupportEnabled = http.getJkEnabled() != null
                ? Boolean.parseBoolean(http.getJkEnabled())
                : Boolean.parseBoolean(networkListener.getJkEnabled());
        if (jkSupportEnabled) {
            final String jkPropertiesFilename =
                    Boolean.parseBoolean(http.getJkEnabled())
                    ? http.getJkConfigurationFile()
                    : networkListener.getJkConfigurationFile();

            File propertiesFile = null;

            if (jkPropertiesFilename != null) {
                propertiesFile = new File(jkPropertiesFilename);
            }


            final String systemPropertyFilename =
                    System.getProperty("com.sun.enterprise.web.connector.enableJK.propertyFile");

            if ((propertiesFile == null || !propertiesFile.exists())
                    && systemPropertyFilename != null) {
                propertiesFile = new File(systemPropertyFilename);
            }

            if (propertiesFile == null) {
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("jk properties configuration file not defined");
                }
                return;
            }

            if (!propertiesFile.exists()) {
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST,
                            "jk properties configuration file ''{0}'' doesn't exist",
                            propertiesFile.getAbsoluteFile());
                }
                return;
            }

            if (_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "Loading glassfish-jk.properties from {0}",
                        propertiesFile.getAbsolutePath());
            }

            Properties properties = null;

            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(propertiesFile));
                properties = new Properties();
                properties.load(is);

            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ignored) {
                    }
                }
            }

            configure(properties);
        }
    }
}
