/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SecureAdmin;
import com.sun.enterprise.security.SecurityLoggerInfo;

import jakarta.inject.Inject;

import java.beans.PropertyChangeEvent;

import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.PostStartupRunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.Changed;
import org.jvnet.hk2.config.Changed.TYPE;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.NotProcessed;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
 * Tracks changes to secure admin configuration, basically so it can report restart-required.
 *
 * @author Tim Quinn
 */
@Service
@RunLevel(PostStartupRunLevel.VAL)
public class SecureAdminConfigMonitor implements ConfigListener {

    private static final String restartRequiredMsg = Strings.get("secure.admin.change.requires.restart");

    /**
     * Must inject Domain to get notifications of SecureAdmin changes.
     * We cannot inject SecureAdmin itself because it might be null and that
     * bothers some components.
     */
    @Inject
    private Domain domain;


    @Override
    public UnprocessedChangeEvents changed(final PropertyChangeEvent[] events) {
        return ConfigSupport.sortAndDispatch(events, new Changed() {
            @Override
            public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type, Class<T> tClass, T t) {
                if (t instanceof Domain) {
                    return processDomain(type, (Domain) t, events);
                } else if (t instanceof SecureAdmin) {
                    return processSecureAdmin(type, (SecureAdmin) t, events);
                }
                return null;
            }
        }, SecurityLoggerInfo.getLogger());
    }

    private NotProcessed processDomain(final TYPE type, final Domain d, final PropertyChangeEvent[] events) {
        for (PropertyChangeEvent event : events) {
            if ((event.getOldValue() instanceof SecureAdmin && type == Changed.TYPE.REMOVE)
                || (event.getNewValue() instanceof SecureAdmin && type == Changed.TYPE.ADD)) {
                return new NotProcessed(restartRequiredMsg);
            }
        }
        return null;
    }

    private NotProcessed processSecureAdmin(final TYPE type, final SecureAdmin sa, final PropertyChangeEvent[] events) {
        // Any change to the secure admin config requires a restart.
        return new NotProcessed(restartRequiredMsg);
    }
}
