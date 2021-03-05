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

package com.sun.enterprise.security.jmac;

import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
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
import com.sun.enterprise.security.jmac.config.GFServerConfigProvider;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Listener class to handle admin message-security-config element events.
 *
 * @author Nithya Subramanian
 */

@Service
@RunLevel(StartupRunLevel.VAL)
public class MessageSecurityConfigEventListenerImpl implements ConfigListener {

    private static Logger logger = LogDomains.getLogger(MessageSecurityConfigEventListenerImpl.class, LogDomains.SECURITY_LOGGER);

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private SecurityService service;

    /**
     * @param event - Event to be processed.
     * @throws AdminEventListenerException when the listener is unable to process the event.
     */
    public <T extends ConfigBeanProxy> NotProcessed handleUpdate(T instance) {
        NotProcessed np = null;
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "MessageSecurityConfigEventListenerImpl - " + "handleUpdate called");
        }
        // Handle only the MessageSecurityConfig.
        if (instance instanceof MessageSecurityConfig) {
            GFServerConfigProvider.loadConfigContext(service);
        } else {
            np = new NotProcessed("unimplemented: unknown instance: " + instance.getClass().getName());
        }
        return np;
    }

    /**
     * @param event Event to be processed.
     * @throws AdminEventListenerException when the listener is unable to process the event.
     */
    public <T extends ConfigBeanProxy> NotProcessed handleDelete(T instance) {
        NotProcessed np = null;
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "MessageSecurityConfigEventListenerImpl - " + "handleDelete called");
        }
        if (instance instanceof MessageSecurityConfig) {
            GFServerConfigProvider.loadConfigContext(service);
        } else {
            np = new NotProcessed("unimplemented: unknown instance: " + instance.getClass().getName());
        }
        return np;
    }

    /**
     * @param event Event to be processed.
     * @throws AdminEventListenerException when the listener is unable to process the event.
     */
    public <T extends ConfigBeanProxy> NotProcessed handleCreate(T instance) {
        NotProcessed np = null;
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "MessageSecurityConfigEventListenerImpl - " + "handleCreate called");
        }
        if (instance instanceof MessageSecurityConfig) {
            GFServerConfigProvider.loadConfigContext(service);
        } else {
            np = new NotProcessed("unimplemented: unknown instance: " + instance.getClass().getName());
        }
        return np;
    }

    @Override
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        ConfigSupport.sortAndDispatch(events, new Changed() {

            /**
             * Notification of a change on a configuration object
             *
             * @param type type of change : ADD mean the changedInstance was added to the parent REMOVE means the changedInstance
             * was removed from the parent, CHANGE means the changedInstance has mutated.
             * @param changedType type of the configuration object
             * @param changedInstance changed instance.
             */
            @Override
            public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type, Class<T> changedType, T changedInstance) {
                NotProcessed np = null;
                switch (type) {
                case ADD:
                    logger.fine("A new " + changedType.getName() + " was added : " + " " + changedInstance);
                    np = handleCreate(changedInstance);
                    break;
                case CHANGE:
                    logger.fine("A " + changedType.getName() + " was changed : " + changedInstance);
                    np = handleUpdate(changedInstance);
                    break;
                case REMOVE:
                    logger.fine("A " + changedType.getName() + " was removed : " + changedInstance);
                    np = handleDelete(changedInstance);
                    break;
                }
                return np;
            }
        }, logger);

        return null;
    }
}
