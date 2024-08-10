/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.event;

import com.sun.enterprise.admin.util.AdminLoggerInfo;
import com.sun.enterprise.util.StringUtils;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.glassfish.api.admin.AdminCommandEventBroker;

/**
 *
 * @author mmares
 */
public class AdminCommandEventBrokerImpl<T> implements AdminCommandEventBroker<T> {

    private static class ListenerGroup {

        private final Pattern pattern;
        private final List<AdminCommandListener> listeners = new ArrayList<AdminCommandListener>(1);

        private ListenerGroup(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        public String getOriginalPattern() {
            return pattern.pattern();
        }

        public boolean matches(CharSequence name) {
            return pattern.matcher(name).matches();
        }

        public Iterator<AdminCommandListener> listeners() {
            return listeners.iterator();
        }

        public boolean add(AdminCommandListener listener) {
            return listeners.add(listener);
        }

        public boolean remove(AdminCommandListener listener) {
            return listeners.remove(listener);
        }
    }

    private final List<ListenerGroup> listenerGroups = new ArrayList<ListenerGroup>();
    private static final Logger logger = AdminLoggerInfo.getLogger();

    public AdminCommandEventBrokerImpl() {
    }

    @Override
    public void fireEvent(String name, Object event) {
        if (event == null) {
            return;
        }
        if (name == null) {
            throw new IllegalArgumentException("Argument name must be defined");
        }
        IdentityHashMap<AdminCommandListener, AdminCommandListener> deduplicated = new IdentityHashMap<AdminCommandListener, AdminCommandListener>();
        synchronized (this) {
            for (ListenerGroup listenerGroup : listenerGroups) {
                if (listenerGroup.matches(name)) {
                    for (AdminCommandListener listener : listenerGroup.listeners) {
                        deduplicated.put(listener, listener);
                    }
                }
            }
        } //Call all listeners
        for (AdminCommandListener listener : deduplicated.keySet()) {
            try {
                listener.onAdminCommandEvent(name, event);
            } catch (Exception ex) {
                logger.log(Level.WARNING, AdminLoggerInfo.mExceptionFromEventListener, ex);
            }
        }
    }

    @Override
    public synchronized void fireEvent(Object event) {
        if (event == null) {
            return;
        }
        fireEvent(event.getClass().getName(), event);
    }

    @Override
    public synchronized void registerListener(String regexpForName, AdminCommandListener<T> listener) {
        if (regexpForName == null) {
            throw new IllegalArgumentException("Argument regexpForName must be defined");
        }
        if (listener == null) {
            return;
        }
        ListenerGroup lgrp = null;
        for (ListenerGroup listenerGroup : listenerGroups) {
            if (regexpForName.equals(listenerGroup.getOriginalPattern())) {
                lgrp = listenerGroup;
                break;
            }
        }
        if (lgrp == null) {
            lgrp = new ListenerGroup(regexpForName);
            listenerGroups.add(lgrp);
        }
        lgrp.add(listener);
        fireEvent(BrokerListenerRegEvent.EVENT_NAME_LISTENER_REG, new BrokerListenerRegEvent(this, listener));
    }

    @Override
    public synchronized boolean listening(String eventName) {
        if (eventName == null) {
            for (ListenerGroup listenerGroup : listenerGroups) {
                if (!listenerGroup.listeners.isEmpty()) {
                    return true;
                }
            }
        } else {
            for (ListenerGroup listenerGroup : listenerGroups) {
                if (listenerGroup.matches(eventName) && !listenerGroup.listeners.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public synchronized void unregisterListener(AdminCommandListener listener) {
        boolean removed = false;
        for (ListenerGroup listenerGroup : listenerGroups) {
            if (listenerGroup.remove(listener)) {
                removed = true;
            }
            //No break. Can be registered for more names
        }
        if (removed) {
            fireEvent(BrokerListenerRegEvent.EVENT_NAME_LISTENER_UNREG, new BrokerListenerRegEvent(this, listener));
        }
    }

    @Override
    public EventBrokerUtils getUtils() {
        return new EventBrokerUtils() {

            @Override
            public void sendMessage(String message) {
                if (StringUtils.ok(message)) {
                    fireEvent(USER_MESSAGE_NAME, message);
                }
            }

        };
    }

}
