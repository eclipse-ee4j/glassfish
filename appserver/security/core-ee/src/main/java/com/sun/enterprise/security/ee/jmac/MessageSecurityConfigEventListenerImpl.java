/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.security.ee.jmac;

import static java.util.logging.Level.FINE;

import java.beans.PropertyChangeEvent;
import java.util.logging.Logger;

import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Changed;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.NotProcessed;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import com.sun.enterprise.config.serverbeans.MessageSecurityConfig;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.auth.message.config.AuthConfigFactory;

/**
 * Listener class to handle admin message-security-config element events.
 *
 * @author Nithya Subramanian
 */
@Service
@RunLevel(StartupRunLevel.VAL)
public class MessageSecurityConfigEventListenerImpl implements ConfigListener {

    private static final Logger LOG = LogDomains.getLogger(MessageSecurityConfigEventListenerImpl.class,
        LogDomains.SECURITY_LOGGER, false);

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private SecurityService service; // required to be injected to register MessageSecurityConfigEventListenerImpl as an listener

    @Override
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        ConfigSupport.sortAndDispatch(events, new Changed() {
            @Override
            public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type, Class<T> changedType, T changedInstance) {
                switch (type) {
                    case ADD:
                        LOG.log(FINE, () -> "A new " + changedType.getName() + " was added: " + changedInstance);
                        return handle(changedInstance);

                    case CHANGE:
                        LOG.log(FINE, () -> "A " + changedType.getName() + " was changed: " + changedInstance);
                        return handle(changedInstance);

                    case REMOVE:
                        LOG.log(FINE, () -> "A " + changedType.getName() + " was removed: " + changedInstance);
                        return handle(changedInstance);

                    default:
                        return null;
                }
            }
        }, LOG);

        return null;
    }

    private <T extends ConfigBeanProxy> NotProcessed handle(T instance) {
        if (instance instanceof MessageSecurityConfig) {
            AuthConfigFactory factory = AuthConfigFactory.getFactory();
            if (factory != null) {
                factory.refresh();
            }
            return null;
        }

        return new NotProcessed("unimplemented: unknown instance: " + instance.getClass().getName());
    }

}
