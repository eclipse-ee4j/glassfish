/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import static java.util.logging.Level.WARNING;

import java.beans.PropertyVetoException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.MessageSecurityConfig;
import com.sun.enterprise.config.serverbeans.ProviderConfig;
import com.sun.enterprise.config.serverbeans.RequestPolicy;
import com.sun.enterprise.config.serverbeans.ResponsePolicy;
import com.sun.enterprise.config.serverbeans.SecurityService;

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Adds the needed message-security-config information to domain.xml
 * during an upgrade from a v2.X server. For more information see:
 * https://glassfish.dev.java.net/issues/show_bug.cgi?id=13443
 */
@Service
public class AdminConsoleConfigUpgrade implements ConfigurationUpgrade, PostConstruct {

    private static final Logger LOG = Logger.getLogger(AdminConsoleConfigUpgrade.class.getName());

    private static final String AUTH_LAYER = "HttpServlet";
    private static final String PROVIDER_TYPE = "server";
    private static final String PROVIDER_ID = "GFConsoleAuthModule";
    private static final String CLASS_NAME = "org.glassfish.admingui.common.security.AdminConsoleAuthModule";
    private static final String AUTH_SOURCE = "sender";
    public static final String DEFAULT_ADMIN_PORT = "4848";
    private static final String LOGIN_PAGE_PROP = "loginPage";
    private static final String LOGIN_PAGE_VAL = "/login.jsf";
    private static final String LOGIN_ERR_PAGE_PROP = "loginErrorPage";
    private static final String LOGIN_ERR_PAGE_VAL = "/loginError.jsf";

    @Inject
    Configs configs;

    // This will force the Grizzly upgrade code to run before
    // AdminConsoleConfigUpgrade runs. Issue GLASSFISH-15599
    @Inject
    @Named("grizzlyconfigupgrade")
    @Optional
    ConfigurationUpgrade precondition;

    @Override
    public void postConstruct() {
        Config config = configs.getConfigByName("server-config");
        if (config != null) {
            SecurityService securityService = config.getSecurityService();
            if (securityService != null) {
                try {
                    ConfigSupport.apply(new AdminConsoleConfigCode(), securityService);
                } catch (TransactionFailure tf) {
                    LOG.log(Level.SEVERE, "Could not upgrade security service for admin console.", tf);
                }
            }
        }
    }

    static private class AdminConsoleConfigCode implements SingleConfigCode<SecurityService> {

        @Override
        public Object run(SecurityService service)
            throws PropertyVetoException, TransactionFailure {

            /*
             * TODO: if the element is already present, should we check it
             * instead of just returning? If so, don't forget to enroll
             * it in the transaction.
             */
            for (MessageSecurityConfig msc :
                service.getMessageSecurityConfig()) {
                if (AUTH_LAYER.equals(msc.getAuthLayer())) {
                    return null;
                }
            }

            // create/add message-security-config
            MessageSecurityConfig msConfig = service.createChild(MessageSecurityConfig.class);
            msConfig.setAuthLayer(AUTH_LAYER);
            service.getMessageSecurityConfig().add(msConfig);

            // create/add provider-config
            ProviderConfig pConfig = msConfig.createChild(ProviderConfig.class);
            pConfig.setProviderType(PROVIDER_TYPE);
            pConfig.setProviderId(PROVIDER_ID);
            pConfig.setClassName(CLASS_NAME);
            msConfig.getProviderConfig().add(pConfig);

            // create/add request-policy
            RequestPolicy reqPol = pConfig.createChild(RequestPolicy.class);
            reqPol.setAuthSource(AUTH_SOURCE);
            pConfig.setRequestPolicy(reqPol);

            // create/add response-policy
            ResponsePolicy resPol = pConfig.createChild(ResponsePolicy.class);
            pConfig.setResponsePolicy(resPol);

            // get admin port property from config
            Config parent = service.getParent(Config.class);
            if (parent.getAdminListener() == null) {
                LOG.log(WARNING, "Couldn't get admin port from config {0}. Using default {1}",
                    new Object[] {parent.getName(), DEFAULT_ADMIN_PORT});
            }

            // add properties
            Property logPageProp = pConfig.createChild(Property.class);
            logPageProp.setName(LOGIN_PAGE_PROP);
            logPageProp.setValue(LOGIN_PAGE_VAL);

            Property logErrPage = pConfig.createChild(Property.class);
            logErrPage.setName(LOGIN_ERR_PAGE_PROP);
            logErrPage.setValue(LOGIN_ERR_PAGE_VAL);

            List<Property> props = pConfig.getProperty();
            props.add(logPageProp);
            props.add(logErrPage);

            return null;
        }
    }

}
