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

package org.glassfish.admin.amxtest;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.client.ConnectionSource;

import javax.management.ObjectName;
import java.util.Set;


/**
 Observes various things as tests are run.
 */
public final class Observer {
    private static Observer INSTANCE = null;

    private final RegistrationListener mListener;

    private final DomainRoot mDomainRoot;

    private Observer(final DomainRoot domainRoot) {
        mDomainRoot = domainRoot;

        final ConnectionSource connSource =
                Util.getExtra(domainRoot).getConnectionSource();

        try {
            mListener = RegistrationListener.createInstance("Observer",
                                                            connSource.getExistingMBeanServerConnection());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized Observer
    create(final DomainRoot domainRoot) {
        if (INSTANCE == null) {
            INSTANCE = new Observer(domainRoot);
        } else {
            throw new IllegalArgumentException();
        }
        return INSTANCE;
    }

    public static Observer
    getInstance() {
        return INSTANCE;
    }

    public RegistrationListener
    getRegistrationListener() {
        return mListener;
    }

    public Set<ObjectName>
    getCurrentlyRegisteredAMX() {
        return mListener.getCurrentlyRegistered();
    }

    public void
    notifsLost() {
        mListener.notifsLost();
    }

}














