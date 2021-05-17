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

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */

package org.glassfish.cditest.user.api;

import jakarta.ejb.EJBException;
import org.glassfish.cditest.user.api.model.User;

/**
 * <p>
 * Manage user-related data
 * </p>
 *
 * <p>
 * A service that implements this interface is responsible for managing
 * user-related master-data
 * </p>
 *
 * @author kane
 *
 * @see User
 */
public interface UserService
{

    /**
     * <p>
     * Get a {@link User} by the user id
     * </p>
     *
     * @param userid
     *            The userid to search for
     * @return A {@link User} object or <code>null</code> if no user was found
     */
    public User findById(long userId) throws EJBException;

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
     * @see User
     */
    public Long addUser(User user) throws EJBException;
}
