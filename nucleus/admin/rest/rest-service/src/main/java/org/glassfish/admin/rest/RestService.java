/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest;

import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import org.glassfish.internal.api.LocalPassword;
import org.glassfish.internal.api.RestInterfaceUID;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Ludovic Champenois ludo@dev.java.net
 * @author Rajeshwar Patil
 */
@Service
public class RestService implements RestInterfaceUID {

    public final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(RestService.class);
    private static String uid;

    @Inject
    private LocalPassword localPassword;

    @Override
    public String getUID() {
        if (uid == null) {
            uid = localPassword.getLocalPassword();
        }
        return uid;
    }

    public static String getRestUID() {
        return uid;
    }
}
