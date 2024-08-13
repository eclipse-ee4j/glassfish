/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

package com.sun.web.server;

import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.web.WebComponentInvocation;
import com.sun.enterprise.web.WebModule;

import jakarta.validation.ValidatorFactory;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
//END OF IASRI 4660742

import javax.naming.NamingException;

import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.web.LogFacade;

import static java.text.MessageFormat.format;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;
import static org.apache.catalina.ContainerEvent.AFTER_CONTEXT_DESTROYED;
import static org.apache.catalina.ContainerEvent.AFTER_FILTER_DESTROYED;
import static org.apache.catalina.ContainerEvent.BEFORE_CONTEXT_DESTROYED;
import static org.apache.catalina.ContainerEvent.PRE_DESTROY;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT;
import static org.glassfish.web.LogFacade.EXCEPTION_DURING_DESTROY_MANAGED_OBJECT;
import static org.glassfish.web.LogFacade.EXCEPTION_DURING_HANDLE_EVENT;
import static org.glassfish.web.LogFacade.EXCEPTION_GETTING_VALIDATOR_FACTORY;

/**
 * This class implements the Tomcat ContainerListener interface and handles Context and Session related events.
 *
 * @author Tony Ng
 */
public final class WebContainerListener implements ContainerListener {

    private static final Logger _logger = LogFacade.getLogger();
    private static final ResourceBundle rb = _logger.getResourceBundle();

    private static Set<String> beforeEvents = new HashSet<>();
    private static Set<String> afterEvents = new HashSet<>();

    static {

        // preInvoke events

        beforeEvents.add(ContainerEvent.BEFORE_CONTEXT_INITIALIZER_ON_STARTUP);
        beforeEvents.add(ContainerEvent.BEFORE_CONTEXT_INITIALIZED);
        beforeEvents.add(ContainerEvent.BEFORE_CONTEXT_DESTROYED);
        beforeEvents.add(ContainerEvent.BEFORE_CONTEXT_ATTRIBUTE_ADDED);
        beforeEvents.add(ContainerEvent.BEFORE_CONTEXT_ATTRIBUTE_REMOVED);
        beforeEvents.add(ContainerEvent.BEFORE_CONTEXT_ATTRIBUTE_REPLACED);
        beforeEvents.add(ContainerEvent.BEFORE_REQUEST_INITIALIZED);
        beforeEvents.add(ContainerEvent.BEFORE_REQUEST_DESTROYED);
        beforeEvents.add(ContainerEvent.BEFORE_SESSION_CREATED);
        beforeEvents.add(ContainerEvent.BEFORE_SESSION_DESTROYED);
        beforeEvents.add(ContainerEvent.BEFORE_SESSION_ID_CHANGED);
        beforeEvents.add(ContainerEvent.BEFORE_SESSION_ATTRIBUTE_ADDED);
        beforeEvents.add(ContainerEvent.BEFORE_SESSION_ATTRIBUTE_REMOVED);
        beforeEvents.add(ContainerEvent.BEFORE_SESSION_ATTRIBUTE_REPLACED);
        beforeEvents.add(ContainerEvent.BEFORE_SESSION_VALUE_UNBOUND);
        beforeEvents.add(ContainerEvent.BEFORE_FILTER_INITIALIZED);
        beforeEvents.add(ContainerEvent.BEFORE_FILTER_DESTROYED);
        beforeEvents.add(ContainerEvent.BEFORE_UPGRADE_HANDLER_INITIALIZED);
        beforeEvents.add(ContainerEvent.BEFORE_UPGRADE_HANDLER_DESTROYED);
        beforeEvents.add(ContainerEvent.BEFORE_READ_LISTENER_ON_DATA_AVAILABLE);
        beforeEvents.add(ContainerEvent.BEFORE_READ_LISTENER_ON_ALL_DATA_READ);
        beforeEvents.add(ContainerEvent.BEFORE_READ_LISTENER_ON_ERROR);
        beforeEvents.add(ContainerEvent.BEFORE_WRITE_LISTENER_ON_WRITE_POSSIBLE);
        beforeEvents.add(ContainerEvent.BEFORE_WRITE_LISTENER_ON_ERROR);
        beforeEvents.add(ContainerEvent.BEFORE_AUTHENTICATION);
        beforeEvents.add(ContainerEvent.BEFORE_POST_AUTHENTICATION);
        beforeEvents.add(ContainerEvent.BEFORE_LOGOUT);


        // postInvoke events

        afterEvents.add(ContainerEvent.AFTER_CONTEXT_INITIALIZER_ON_STARTUP);
        afterEvents.add(ContainerEvent.AFTER_CONTEXT_INITIALIZED);
        afterEvents.add(ContainerEvent.AFTER_CONTEXT_DESTROYED);
        afterEvents.add(ContainerEvent.AFTER_CONTEXT_ATTRIBUTE_ADDED);
        afterEvents.add(ContainerEvent.AFTER_CONTEXT_ATTRIBUTE_REMOVED);
        afterEvents.add(ContainerEvent.AFTER_CONTEXT_ATTRIBUTE_REPLACED);
        afterEvents.add(ContainerEvent.AFTER_REQUEST_INITIALIZED);
        afterEvents.add(ContainerEvent.AFTER_REQUEST_DESTROYED);
        afterEvents.add(ContainerEvent.AFTER_SESSION_CREATED);
        afterEvents.add(ContainerEvent.AFTER_SESSION_DESTROYED);
        afterEvents.add(ContainerEvent.AFTER_SESSION_ID_CHANGED);
        afterEvents.add(ContainerEvent.AFTER_SESSION_ATTRIBUTE_ADDED);
        afterEvents.add(ContainerEvent.AFTER_SESSION_ATTRIBUTE_REMOVED);
        afterEvents.add(ContainerEvent.AFTER_SESSION_ATTRIBUTE_REPLACED);
        afterEvents.add(ContainerEvent.AFTER_SESSION_VALUE_UNBOUND);
        afterEvents.add(ContainerEvent.AFTER_FILTER_INITIALIZED);
        afterEvents.add(ContainerEvent.AFTER_FILTER_DESTROYED);
        afterEvents.add(ContainerEvent.AFTER_UPGRADE_HANDLER_INITIALIZED);
        afterEvents.add(ContainerEvent.AFTER_UPGRADE_HANDLER_DESTROYED);
        afterEvents.add(ContainerEvent.AFTER_READ_LISTENER_ON_DATA_AVAILABLE);
        afterEvents.add(ContainerEvent.AFTER_READ_LISTENER_ON_ALL_DATA_READ);
        afterEvents.add(ContainerEvent.AFTER_READ_LISTENER_ON_ERROR);
        afterEvents.add(ContainerEvent.AFTER_WRITE_LISTENER_ON_WRITE_POSSIBLE);
        afterEvents.add(ContainerEvent.AFTER_WRITE_LISTENER_ON_ERROR);
        afterEvents.add(ContainerEvent.AFTER_AUTHENTICATION);
        afterEvents.add(ContainerEvent.AFTER_POST_AUTHENTICATION);
        afterEvents.add(ContainerEvent.AFTER_LOGOUT);
    }

    private final InvocationManager invocationManager;
    private final InjectionManager injectionManager;
    private final NamedNamingObjectProxy validationNamingProxy;

    public WebContainerListener(InvocationManager invocationMgr, InjectionManager injectionMgr, NamedNamingObjectProxy validationNamingProxy) {
        this.invocationManager = invocationMgr;
        this.injectionManager = injectionMgr;
        this.validationNamingProxy = validationNamingProxy;
    }

    @Override
    public void containerEvent(ContainerEvent event) {
        if (_logger.isLoggable(FINEST)) {
            _logger.log(FINEST, LogFacade.CONTAINER_EVENT, event.getType() + "," + event.getContainer() + "," + event.getData());
        }

        String type = event.getType();

        try {
            WebModule webModule = (WebModule) event.getContainer();
            if (beforeEvents.contains(type)) {
                preInvoke(webModule);

                if (type.equals(BEFORE_CONTEXT_DESTROYED)) {
                    try {
                        // Must close the validator factory
                        if (validationNamingProxy != null) {
                            Object validatorFactory = validationNamingProxy
                                .handle(JNDI_CTX_JAVA_COMPONENT + "ValidatorFactory");
                            if (validatorFactory != null) {
                                ((ValidatorFactory) validatorFactory).close();
                            }
                        }
                    } catch (NamingException exc) {
                       _logger.log(FINEST, EXCEPTION_GETTING_VALIDATOR_FACTORY, exc);
                    }
                }
            } else if (afterEvents.contains(type)) {
                if (type.equals(AFTER_FILTER_DESTROYED) || type.equals(AFTER_CONTEXT_DESTROYED)) {
                    preDestroy(event);
                }
                postInvoke(webModule);
            } else if (PRE_DESTROY.equals(type)) {
                preInvoke(webModule);
                preDestroy(event);
                postInvoke(webModule);
            }
        } catch (Throwable t) {
            _logger.log(Level.SEVERE, format(rb.getString(EXCEPTION_DURING_HANDLE_EVENT), new Object[] { type, event.getContainer() }), t);
        }
    }

    private void preInvoke(WebModule webModule) {
        invocationManager.preInvoke(new WebComponentInvocation(webModule));
    }

    private void postInvoke(WebModule webModule) {
        invocationManager.postInvoke(new WebComponentInvocation(webModule));
    }

    /**
     * Invokes preDestroy on the instance embedded in the given ContainerEvent.
     *
     * @param event The ContainerEvent to process
     */
    private void preDestroy(ContainerEvent event) {
        try {
            injectionManager.destroyManagedObject(event.getData(), false);
        } catch (Throwable t) {
            _logger.log(SEVERE, format(rb.getString(EXCEPTION_DURING_DESTROY_MANAGED_OBJECT), new Object[] { event.getData(), event.getContainer() }), t);
        }
    }
}
