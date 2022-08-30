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

package com.sun.enterprise.connectors.work.context;

import com.sun.enterprise.connectors.work.LogFacade;
import com.sun.enterprise.security.SecurityContext;

import jakarta.security.auth.message.callback.CallerPrincipalCallback;
import jakarta.security.auth.message.callback.GroupPrincipalCallback;
import jakarta.security.auth.message.callback.PasswordValidationCallback;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.security.common.Group;
import org.glassfish.security.common.UserNameAndPassword;

/**
 * Connector callback handler to intercept the callbacks provided by the work instance
 * in order to map the security credentials between container and EIS domain
 *
 * @author Jagadish Ramu
 * @since GlassFish v3
 */
//TODO V3 need contract based handlers for individual callbacks ?
public class ConnectorCallbackHandler implements CallbackHandler {

    private static final Logger logger = LogFacade.getLogger();

    private static final List<String> supportedCallbacks = new ArrayList<>();
    static {
        supportedCallbacks.add(GroupPrincipalCallback.class.getName());
        supportedCallbacks.add(CallerPrincipalCallback.class.getName());
    }

    @LogMessageInfo(
            message = "Unsupported callback {0} during credential mapping.",
            comment = "Unsupported callback class.",
            level = "WARNING",
            cause = "Resource adapter has used a callback that is not supported by application server.",
            action = "Check whether the callback in question is supported by application server.",
            publish = true)
    private static final String RAR_UNSUPPORT_CALLBACK = "AS-RAR-05012";

    private final CallbackHandler handler;
    private boolean needMapping;
    // Warning: Mixes groups and users
    private final Map<Principal, Principal> securityMap;
    private final Subject executionSubject;

    public ConnectorCallbackHandler(Subject executionSubject, CallbackHandler handler, Map<Principal, Principal> securityMap) {
        this.handler = handler;
        if (securityMap != null && !securityMap.isEmpty()) {
            needMapping = true;
            logger.finest("translation required for security info ");
        } else {
            logger.finest("no translation required for security info ");
        }
        this.executionSubject = executionSubject;
        this.securityMap = securityMap;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        Callback[] mappedCallbacks = callbacks;
        if (callbacks != null) {
            List<Callback> asCallbacks = new ArrayList<>();

            boolean hasCallerPrincipalCallback = hasCallerPrincipalCallback(callbacks);

            if (needMapping) {
                for (Callback callback : callbacks) {
                    boolean callbackSupported = false;
                    for (String supportedCallback : supportedCallbacks) {
                        try {
                            //TODO V3 what if there is a callback impl that implements multiple callbacks ?
                            if (Class.forName(supportedCallback).isAssignableFrom(callback.getClass())) {
                                callbackSupported = true;
                                asCallbacks.add(handleSupportedCallback(callback));
                            }
                        } catch (ClassNotFoundException cnfe) {
                            if(logger.isLoggable(Level.FINEST)){
                                logger.log(Level.FINEST, "class not found", cnfe);
                            }
                        }
                    }
                    if (!callbackSupported) {
                        UnsupportedCallbackException uce = new UnsupportedCallbackException(callback);
                        logger.log(Level.WARNING, RAR_UNSUPPORT_CALLBACK, new Object[]{callback.getClass().getName(), uce});
                        throw uce;
                    }
                }

                mappedCallbacks = new Callback[asCallbacks.size()];
                for (int i = 0; i < asCallbacks.size(); i++) {
                    mappedCallbacks[i] = asCallbacks.get(i);
                }
            }
            //TODO V3 what happens to multiple callbacks ?
            handler.handle(mappedCallbacks);

            processResults(mappedCallbacks, hasCallerPrincipalCallback);
        }
    }

    private boolean hasCallerPrincipalCallback(Callback[] callbacks) {
        if (callbacks != null) {
            for (Callback c : callbacks) {
                if (c instanceof CallerPrincipalCallback) {
                    return true;
                }
            }
        }
        return false;
    }

    private void processResults(Callback[] mappedCallbacks, boolean hasCallerPrincipalCallback) {
        if (mappedCallbacks != null) {
            Subject s = new Subject();

            // Handle Single Principal as the caller identity
            if (!hasCallerPrincipalCallback) {
                Set<Principal> principals = executionSubject.getPrincipals();
                if (principals != null && principals.size() == 1) {
                    //process if there is only one principal
                    for (Principal p : principals) {
                        Principal mappedPrincipal = null;
                        if (needMapping) {
                            mappedPrincipal = getMappedPrincipal(p, null);
                        } else {
                            mappedPrincipal = p;
                        }

                        if (mappedPrincipal != null) {
                            s.getPrincipals().add(mappedPrincipal);
                        }
                    }
                    s.getPublicCredentials().addAll(executionSubject.getPublicCredentials());
                    s.getPrivateCredentials().addAll(executionSubject.getPrivateCredentials());
                }
            }

            //TODO V3 what happens for Public/Private Credentials of Mapped case (Case II)
            for (Callback callback : mappedCallbacks) {
                if (callback instanceof CallerPrincipalCallback) {
                    CallerPrincipalCallback cpc = (CallerPrincipalCallback) callback;
                    s.getPrincipals().addAll(cpc.getSubject().getPrincipals());
                    s.getPublicCredentials().addAll(cpc.getSubject().getPublicCredentials());
                    s.getPrivateCredentials().addAll(cpc.getSubject().getPrivateCredentials());
                } else if (callback instanceof GroupPrincipalCallback) {
                    GroupPrincipalCallback gpc = (GroupPrincipalCallback) callback;
                    s.getPrincipals().addAll(gpc.getSubject().getPrincipals());
                    s.getPublicCredentials().addAll(gpc.getSubject().getPublicCredentials());
                    s.getPrivateCredentials().addAll(gpc.getSubject().getPrivateCredentials());
                } else if (callback instanceof PasswordValidationCallback) {
                    PasswordValidationCallback pvc = (PasswordValidationCallback) callback;
                    s.getPrincipals().addAll(pvc.getSubject().getPrincipals());
                    s.getPublicCredentials().addAll(pvc.getSubject().getPublicCredentials());
                    s.getPrivateCredentials().addAll(pvc.getSubject().getPrivateCredentials());
                }
            }
            SecurityContext.setCurrent(new SecurityContext(s));
        }
    }

    private Callback handleSupportedCallback(Callback callback) throws UnsupportedCallbackException {
        /* TODO V3 need to merge the principals/maps after calling all the callbacks and then
           TODO V3 set the security context ? */
        if (callback instanceof CallerPrincipalCallback) {
            return handleCallerPrincipalCallbackWithMapping((CallerPrincipalCallback) callback);
        } else if (callback instanceof GroupPrincipalCallback) {
            return handleGroupPrincipalCallbackWithMapping((GroupPrincipalCallback) callback);
        } else {
            throw new UnsupportedCallbackException(callback);
        }
    }

    private Callback handleGroupPrincipalCallbackWithMapping(GroupPrincipalCallback gpc) {
        String[] groups = gpc.getGroups();
        List<String> asGroupNames = new ArrayList<>();
        for (String groupName : groups) {
            Group mappedGroup = (Group) securityMap.get(new Group(groupName));
            if (mappedGroup != null) {
                if(logger.isLoggable(Level.FINEST)){
                    logger.finest("got mapped group as [" + groupName + "] for eis-group [" + mappedGroup.getName() + "]");
                }
                asGroupNames.add(mappedGroup.getName());
            }
        }
        String[] asGroupsString = new String[asGroupNames.size()];
        for (int i = 0; i < asGroupNames.size(); i++) {
            asGroupsString[i] = asGroupNames.get(i);
        }
        return new GroupPrincipalCallback(gpc.getSubject(), asGroupsString);
    }

    public Callback handleCallerPrincipalCallbackWithMapping(CallerPrincipalCallback cpc) {
        Principal eisPrincipal = cpc.getPrincipal();
        String eisName = cpc.getName();
        Principal asPrincipal = getMappedPrincipal(eisPrincipal, eisName);
        return new CallerPrincipalCallback(cpc.getSubject(), asPrincipal);
    }

    private Principal getMappedPrincipal(Principal eisPrincipal, String eisName) {
        final Principal asPrincipal;
        if (eisPrincipal != null) {
            asPrincipal = securityMap.get(eisPrincipal);
            if(logger.isLoggable(Level.FINEST)){
                logger.finest("got mapped principal as [" + asPrincipal + "] for eis-group [" + eisPrincipal.getName() + "]");
            }
        } else if (eisName != null) {
            asPrincipal = securityMap.get(new UserNameAndPassword(eisName));
            if(logger.isLoggable(Level.FINEST)){
                logger.finest("got mapped principal as [" + asPrincipal + "] for eis-group [" + eisName + "]");
            }
        } else {
            asPrincipal = null;
        }
        return asPrincipal;
    }

}
