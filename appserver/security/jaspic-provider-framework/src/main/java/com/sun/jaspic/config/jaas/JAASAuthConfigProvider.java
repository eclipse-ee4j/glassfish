/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.config.AuthConfigFactory;
import jakarta.security.auth.message.config.AuthConfigFactory.RegistrationContext;

/**
 *
 * @author Ron Monzillo
 */
public abstract class JAASAuthConfigProvider extends AuthConfigProviderHelper {

    private static final String CONFIG_FILE_NAME_KEY = "config.file.name";
    private static final String DEFAULT_JAAS_APP_NAME = "other";
    private static final String ALL_APPS = "*";

    private final String configFileName;
    private ExtendedConfigFile jaasConfig;

    private final Map<String, String> properties;
    private final AuthConfigFactory factory;

    public JAASAuthConfigProvider(Map<String, String> properties, AuthConfigFactory factory) {
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

    @Override
    public Map<String, ?> getProperties() {
        return properties;
    }


    @Override
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

            @Override
            public String getMessageLayer() {
                return layer;
            }

            @Override
            public String getAppContext() {
                return appContext;
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public boolean isPersistent() {
                return false;
            }
        };
    }

    @Override
    public AuthConfigFactory.RegistrationContext[] getSelfRegistrationContexts() {
        final String[] appContexts = jaasConfig.getAppNames(getModuleTypes());
        RegistrationContext[] rvalue = new RegistrationContext[appContexts.length];
        for (int i = 0; i < appContexts.length; i++) {
            rvalue[i] = getRegistrationContext(appContexts[i]);
        }
        return rvalue;
    }

    @Override
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
