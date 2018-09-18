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

package com.sun.jaspic.config.jaas;

import com.sun.jaspic.config.helper.AuthContextHelper;
import com.sun.jaspic.config.helper.AuthConfigProviderHelper;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Map;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigFactory.RegistrationContext;

/**
 *
 * @author Ron Monzillo
 */
public abstract class JAASAuthConfigProvider extends AuthConfigProviderHelper {

    private static final String CONFIG_FILE_NAME_KEY = "config.file.name";
    private static final String DEFAULT_JAAS_APP_NAME = "other";
    private static final String ALL_APPS = "*";

    private String configFileName;
    private ExtendedConfigFile jaasConfig;

    private Map<String, ?> properties;
    private AuthConfigFactory factory;

    public JAASAuthConfigProvider(Map properties, AuthConfigFactory factory) {
        this.properties = properties;
        this.factory = factory;

        configFileName = getProperty(CONFIG_FILE_NAME_KEY,null);

        if (configFileName == null) {
            jaasConfig = new ExtendedConfigFile();
        } else {
            try {
                URI uri = new URI(configFileName);
                jaasConfig = new ExtendedConfigFile(uri);
            } catch (URISyntaxException use) {
                IllegalArgumentException iae = new IllegalArgumentException(use);
                throw iae;
            }
        }
       selfRegister();
    }

    public Map<String, ?> getProperties() {
        return properties;
    }


    public AuthConfigFactory getFactory() {
        return factory;
    }

    private RegistrationContext getRegistrationContext(String id) {

        final String layer = getLayer();
        final String appContext;
        if (id.toLowerCase(Locale.getDefault()).equals(DEFAULT_JAAS_APP_NAME)) {
            appContext = ALL_APPS;
        } else {
            appContext = id;
        }

        return new RegistrationContext() {

            final String description = "JAAS AuthConfig: " + appContext;

            public String getMessageLayer() {
                return layer;
            }

            public String getAppContext() {
                return appContext;
            }

            public String getDescription() {
                return description;
            }

            public boolean isPersistent() {
                return false;
            }
        };
    }

    public AuthConfigFactory.RegistrationContext[] getSelfRegistrationContexts() {
        final String[] appContexts = jaasConfig.getAppNames(getModuleTypes());
        RegistrationContext[] rvalue = new RegistrationContext[appContexts.length];
        for (int i = 0; i < appContexts.length; i++) {
            rvalue[i] = getRegistrationContext(appContexts[i]);
        }
        return rvalue;
    }

    public AuthContextHelper getAuthContextHelper(String appContext, boolean returnNullContexts)
            throws AuthException {
        return new JAASAuthContextHelper(getLoggerName(), returnNullContexts,
                jaasConfig, properties, appContext);
    }

    @Override
    public void refresh() {
        jaasConfig.refresh();
        super.refresh();
    }

}
