/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.security.ee.web.integration;

import com.sun.enterprise.security.PrincipalGroupFactory;

import java.lang.ref.WeakReference;

import org.glassfish.internal.api.Globals;
import org.glassfish.security.common.Group;
import org.glassfish.security.common.UserNameAndPassword;
import org.glassfish.security.common.UserPrincipal;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Harpreet Singh
 */
@Service
public class PrincipalGroupFactoryImpl implements PrincipalGroupFactory {

    /** Creates a new instance of PrincipalGroupFactory */

    private static WeakReference<WebSecurityManagerFactory> webSecurityManagerFactory = new WeakReference<>(null);

    private static synchronized WebSecurityManagerFactory _getWebSecurityManagerFactory() {
        if (webSecurityManagerFactory.get() == null) {
            webSecurityManagerFactory = new WeakReference<>(Globals.get(WebSecurityManagerFactory.class));
        }

        return webSecurityManagerFactory.get();
    }


    private static WebSecurityManagerFactory getWebSecurityManagerFactory() {
        if (webSecurityManagerFactory.get() != null) {
            return webSecurityManagerFactory.get();
        }

        return _getWebSecurityManagerFactory();
    }


    @Override
    public UserPrincipal getPrincipalInstance(String name, String realm) {
        UserPrincipal userPrincipal = getWebSecurityManagerFactory().getAdminPrincipal(name, realm);
        if (userPrincipal == null) {
            userPrincipal = new UserNameAndPassword(name);
        }

        return userPrincipal;
    }


    @Override
    public Group getGroupInstance(String name, String realm) {
        Group group = getWebSecurityManagerFactory().getAdminGroup(name, realm);
        if (group == null) {
            group = new Group(name);
        }

        return group;
    }
}
