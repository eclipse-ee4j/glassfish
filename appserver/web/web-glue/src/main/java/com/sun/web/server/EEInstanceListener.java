/*
 * Copyright (c) 2021, 2023 Contributors to Eclipse Foundation.
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

package com.sun.web.server;

import com.sun.enterprise.container.common.spi.util.InjectionException;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.security.integration.AppServSecurityContext;
import com.sun.enterprise.security.integration.RealmInitializer;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.web.WebComponentInvocation;
import com.sun.enterprise.web.WebModule;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.security.AccessControlException;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.AuthPermission;

import org.apache.catalina.Context;
import org.apache.catalina.InstanceEvent;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.Realm;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.servlets.DefaultServlet;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.wasp.servlet.JspServlet;
import org.glassfish.web.LogFacade;

import static com.sun.enterprise.security.integration.SecurityConstants.WEB_PRINCIPAL_CLASS;
import static com.sun.enterprise.util.Utility.isOneOf;
import static java.security.Policy.getPolicy;
import static java.text.MessageFormat.format;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;
import static org.apache.catalina.InstanceEvent.EventType.AFTER_DESTROY_EVENT;
import static org.apache.catalina.InstanceEvent.EventType.AFTER_FILTER_EVENT;
import static org.apache.catalina.InstanceEvent.EventType.AFTER_INIT_EVENT;
import static org.apache.catalina.InstanceEvent.EventType.AFTER_SERVICE_EVENT;
import static org.apache.catalina.InstanceEvent.EventType.BEFORE_FILTER_EVENT;
import static org.apache.catalina.InstanceEvent.EventType.BEFORE_SERVICE_EVENT;
import static org.glassfish.web.LogFacade.EXCEPTION_DURING_HANDLE_EVENT;
import static org.glassfish.web.LogFacade.NO_SERVER_CONTEXT;
import static org.glassfish.web.LogFacade.SECURITY_CONTEXT_FAILED;
import static org.glassfish.web.LogFacade.SECURITY_CONTEXT_OBTAINED;

/**
 * This class implements the Tomcat InstanceListener interface and handles the INIT,DESTROY and SERVICE, FILTER events.
 *
 * @author Vivek Nagar
 * @author Tony Ng
 */
public final class EEInstanceListener implements InstanceListener {

    private static final Logger _logger = LogFacade.getLogger();
    private static final ResourceBundle _rb = _logger.getResourceBundle();

    private static AuthPermission doAsPrivilegedPerm = new AuthPermission("doAsPrivileged");

    private InvocationManager invocationManager;
    private JavaEETransactionManager eeTransactionManager;
    private InjectionManager injectionManager;
    private AppServSecurityContext securityContext;

    private boolean initialized;

    public EEInstanceListener() {
    }

    @Override
    public void instanceEvent(InstanceEvent event) {
        Context context = (Context) event.getWrapper().getParent();
        if (!(context instanceof WebModule)) {
            return;
        }

        WebModule webModule = (WebModule) context;
        init(webModule);

        InstanceEvent.EventType eventType = event.getType();
        _logger.log(Level.FINEST, LogFacade.INSTANCE_EVENT, eventType);

        if (eventType.isBefore) {
            handleBeforeEvent(event, eventType);
        } else {
            handleAfterEvent(event, eventType);
        }
    }

    private synchronized void init(WebModule webModule) {
        if (initialized) {
            return;
        }

        ServerContext serverContext = webModule.getServerContext();
        if (serverContext == null) {
            throw new IllegalStateException(format(_rb.getString(NO_SERVER_CONTEXT), webModule.getName()));
        }

        ServiceLocator services = serverContext.getDefaultServices();

        invocationManager = services.getService(InvocationManager.class);
        eeTransactionManager = getJavaEETransactionManager(services);
        injectionManager = services.getService(InjectionManager.class);
        securityContext = services.getService(AppServSecurityContext.class);

        if (securityContext != null) {
            _logger.log(FINE, SECURITY_CONTEXT_OBTAINED, securityContext);
        } else {
            _logger.log(FINE, SECURITY_CONTEXT_FAILED);
        }

        initialized = true;
    }

    private void handleBeforeEvent(InstanceEvent event, InstanceEvent.EventType eventType) {
        Context context = (Context) event.getWrapper().getParent();
        if (!(context instanceof WebModule)) {
            return;
        }

        WebModule webModule = (WebModule) context;

        Object instance;
        if (eventType == BEFORE_FILTER_EVENT) {
            instance = event.getFilter();
        } else {
            instance = event.getServlet();
        }

        Realm realm = context.getRealm();
        if (realm != null) {

            ServletRequest request = event.getRequest();
            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpServletRequest = (HttpServletRequest) request;

                Principal principal = httpServletRequest.getUserPrincipal();
                Principal basePrincipal = getBasePrincipal(httpServletRequest, principal);

                if (principal != null && principal == basePrincipal && principal.getClass().getName().equals(WEB_PRINCIPAL_CLASS)) {
                    securityContext.setSecurityContextWithPrincipal(principal);
                } else if (principal != basePrincipal && principal != getCurrentCallerPrincipal()) {

                    // The wrapper has overridden getUserPrincipal
                    // reject the request if the wrapper does not have
                    // the necessary permission.

                    checkObjectForDoAsPermission(httpServletRequest);
                    securityContext.setSecurityContextWithPrincipal(principal);
                }
            }
        }

        ComponentInvocation componentInvocation;
        if (eventType == InstanceEvent.EventType.BEFORE_INIT_EVENT) {
            // The servletName is not available from servlet instance before servlet init.
            // We have to pass the servletName to ComponentInvocation so it can be retrieved
            // in RealmAdapter.getServletName().
            componentInvocation = new WebComponentInvocation(webModule, instance, event.getWrapper().getName());
        } else {
            componentInvocation = new WebComponentInvocation(webModule, instance);
        }

        try {
            invocationManager.preInvoke(componentInvocation);
            if (eventType == BEFORE_SERVICE_EVENT) {
                // Emit monitoring probe event
                webModule.beforeServiceEvent(event.getWrapper().getName());

                // Enlist resources with the transaction manager for service method
                if (eeTransactionManager != null) {
                    eeTransactionManager.enlistComponentResources();
                }
            }
        } catch (Exception ex) {
            invocationManager.postInvoke(componentInvocation);

            throw new RuntimeException(
                format(_rb.getString(EXCEPTION_DURING_HANDLE_EVENT), new Object[] { eventType, webModule }),
                ex);
        }
    }

    private Principal getBasePrincipal(HttpServletRequest baseHttpServletRequest, Principal principal) {
        Principal basePrincipal = principal;

        boolean wrapped = false;

        while (principal != null) {
            if (baseHttpServletRequest instanceof ServletRequestWrapper) {
                // Unwrap any wrappers to find the base object
                ServletRequest servletRequest = ((ServletRequestWrapper) baseHttpServletRequest).getRequest();

                if (servletRequest instanceof HttpServletRequest) {
                    baseHttpServletRequest = (HttpServletRequest) servletRequest;
                    wrapped = true;
                    continue;
                }
            }

            if (wrapped) {
                basePrincipal = baseHttpServletRequest.getUserPrincipal();
            }

            else if (baseHttpServletRequest instanceof RequestFacade) {
                // Try to avoid the getUnWrappedCoyoteRequest call
                // when we can identify see we have the exact class.
                if (baseHttpServletRequest.getClass() != RequestFacade.class) {
                    basePrincipal = ((RequestFacade) baseHttpServletRequest).getUnwrappedCoyoteRequest().getUserPrincipal();
                }
            } else {
                basePrincipal = baseHttpServletRequest.getUserPrincipal();
            }

            break;
        }

        return basePrincipal;
    }

    private Principal getCurrentCallerPrincipal() {
        AppServSecurityContext currentSecurityContext = securityContext.getCurrentSecurityContext();
        if (currentSecurityContext == null) {
            return null;
        }

        return currentSecurityContext.getCallerPrincipal();
    }

    private static void checkObjectForDoAsPermission(final Object o) throws AccessControlException {
        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    if (!getPolicy().implies(o.getClass().getProtectionDomain(), doAsPrivilegedPerm)) {
                        throw new AccessControlException("permission required to override getUserPrincipal", doAsPrivilegedPerm);
                    }

                    return null;
                }
            });
        }
    }

    private void handleAfterEvent(InstanceEvent event, InstanceEvent.EventType eventType) {
        Wrapper wrapper = event.getWrapper();
        Context context = (Context) wrapper.getParent();
        if (!(context instanceof WebModule)) {
            return;
        }

        WebModule webModule = (WebModule) context;

        Object instance;
        if (eventType == AFTER_FILTER_EVENT) {
            instance = event.getFilter();
        } else {
            instance = event.getServlet();
        }

        if (instance == null) {
            return;
        }

        // Emit monitoring probe event
        if (instance instanceof Servlet) {
            if (eventType == AFTER_INIT_EVENT) {
                webModule.servletInitializedEvent(wrapper.getName());
            } else if (eventType == AFTER_DESTROY_EVENT) {
                webModule.servletDestroyedEvent(wrapper.getName());
            }
        }

        // Must call InjectionManager#destroyManagedObject WITHIN
        // EE invocation context
        try {
            if (eventType == AFTER_DESTROY_EVENT && !DefaultServlet.class.equals(instance.getClass()) && !JspServlet.class.equals(instance.getClass())) {
                injectionManager.destroyManagedObject(instance, false);
            }
        } catch (InjectionException ie) {
            _logger.log(Level.SEVERE, format(_rb.getString(EXCEPTION_DURING_HANDLE_EVENT), new Object[] { eventType, webModule }), ie);
        }

        ComponentInvocation componentInvocation = new WebComponentInvocation(webModule, instance);

        try {
            invocationManager.postInvoke(componentInvocation);
        } catch (Exception ex) {
            throw new RuntimeException(format(_rb.getString(EXCEPTION_DURING_HANDLE_EVENT), new Object[] { eventType, webModule }), ex);
        } finally {
            if (eventType == AFTER_DESTROY_EVENT) {
                if (eeTransactionManager != null) {
                    eeTransactionManager.componentDestroyed(instance, componentInvocation);
                }
            } else if (isOneOf(eventType, AFTER_FILTER_EVENT, AFTER_SERVICE_EVENT)) {
                // Emit monitoring probe event
                if (eventType == AFTER_SERVICE_EVENT) {
                    ServletResponse response = event.getResponse();
                    int status = -1;
                    if (response instanceof HttpServletResponse) {
                        status = ((HttpServletResponse) response).getStatus();
                    }
                    webModule.afterServiceEvent(wrapper.getName(), status);
                }

                // Check it's top level invocation
                if (invocationManager.getCurrentInvocation() == null) {
                    try {
                        // Clear security context
                        Realm realm = context.getRealm();
                        if (realm instanceof RealmInitializer) {
                            // Cleanup not only securitycontext but also PolicyContext
                            ((RealmInitializer) realm).logout();
                        }
                    } catch (Exception ex) {
                        _logger.log(SEVERE, format(_rb.getString(EXCEPTION_DURING_HANDLE_EVENT), new Object[] { eventType, webModule }), ex);
                    }

                    if (eeTransactionManager != null) {
                        try {
                            if (eeTransactionManager.getTransaction() != null) {
                                eeTransactionManager.rollback();
                            }
                            eeTransactionManager.cleanTxnTimeout();
                        } catch (Exception ex) {
                        }
                    }
                }

                if (eeTransactionManager != null) {
                    eeTransactionManager.componentDestroyed(instance, componentInvocation);
                }
            }
        }
    }

    private JavaEETransactionManager getJavaEETransactionManager(ServiceLocator services) {
        ServiceHandle<JavaEETransactionManager> serviceHandle = services.getServiceHandle(JavaEETransactionManager.class);
        if (serviceHandle != null && serviceHandle.isActive()) {
            return serviceHandle.getService();
        }

        return null;
    }
}
