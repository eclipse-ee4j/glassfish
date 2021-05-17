/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.cditest.user.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ejb.EJBException;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;

import org.glassfish.cditest.security.api.Secure;
import org.glassfish.cditest.user.api.UserService;
import org.glassfish.cditest.user.api.model.Gender;
import org.glassfish.cditest.user.api.model.User;
import org.glassfish.cditest.user.model.UserImpl;
/**
 * <p>
 * Implementation of the UserService.
 * </p>
 *
 * @author chaoslayer
 */
@Stateless
@Local
@jakarta.interceptor.Interceptors(org.glassfish.cditest.security.interceptor.SecurityInterceptor.class)
public class UserServiceImpl implements UserService {
    private static final Logger LOG = Logger.getLogger(UserService.class.getName());

    /**
     * <p>
     * Add a new user
     * </p>
     *
     * <p>
     * The implementation must ensure that the provided user object is persisted
     * before returning the assigned persistent user ID.
     * </p>
     *
     * @param user
     *            The user object to persist
     * @return The newly created persistent ID of the user object
     *
     * @see UserImpl
     */
    @Override
    public Long addUser(final User user) throws EJBException
    {
        LOG.log(Level.INFO, "Storing user {0}", user);

        return new Long(123);
    }

    /**
     * <p>
     * Get a {@link User} by the user id
     * </p>
     *
     * @param userid
     *            The userid to search for
     * @return A {@link User} object or <code>null</code> if no user was found
     */
    @Override
    public User findById(long userId) throws EJBException
    {
        UserImpl u = new UserImpl();

        u.setId(userId);
        u.setEmailAddress("test@test.org");
        u.setFirstName("John");
        u.setLastName("Doe");
        u.setGender(Gender.UNISEX);
        u.setUsername("john-123");

        LOG.log(Level.INFO, "Returning user {0}", u);

        return u;
    }

}
