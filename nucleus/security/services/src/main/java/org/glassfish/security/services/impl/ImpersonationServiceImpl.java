/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.impl;

import jakarta.inject.Singleton;

import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.glassfish.security.common.Group;
import org.glassfish.security.common.UserNameAndPassword;
import org.glassfish.security.services.api.authentication.ImpersonationService;
import org.jvnet.hk2.annotations.Service;

/**
 * The Impersonation Service Implementation.
 *
 * @author jazheng
 */
@Service(name="impersonationService")
@Singleton
public class ImpersonationServiceImpl implements ImpersonationService {

    @Override
    public Subject impersonate(String user, String[] groups, Subject subject, boolean virtual) throws LoginException {
        // Use the supplied Subject or create a new Subject
        final Subject _subject = subject == null ? new Subject() : subject;

        if (user == null || user.isEmpty()) {
            return _subject;
        }

        // TODO - Add support for virtual = false after IdentityManager
        // is available in open source
        if (!virtual) {
            throw new UnsupportedOperationException("Use of non-virtual parameter is not supported");
        }
        // Build the Subject
        Set<Principal> principals = _subject.getPrincipals();
        principals.add(new UserNameAndPassword(user));
        if (groups != null) {
            for (String group : groups) {
                principals.add(new Group(group));
            }
        }

        // Return the impersonated Subject
        return _subject;
    }
}
