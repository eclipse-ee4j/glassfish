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

package org.glassfish.cditest.user.api.model;

/**
 * A user login account within our system.
 *
 * @author kane
 *
 * @since 0.1
 */
public interface User
{

    public Long getId();

    /**
     * Get the value of firstName
     *
     * @return the value of firstName
     */
    public String getFirstName();

    /**
     * <p>
     * Get the gender for the user
     * </p>
     *
     * <p>
     * A gender is much better suited to our needs here as the pure biological
     * sex is.
     * </p>
     *
     * @return the value of gender
     */
    public Gender getGender();

    /**
     * Get the value of lastName
     *
     * @return the value of lastName
     */
    public String getLastName();

    /**
     * <p>
     * Get the username used for login
     * </p>
     *
     * @return the value of username
     */
    public String getUsername();

    /**
     * <p>
     * Get the email address of the user
     * </p>
     *
     * @return the value of the email
     */
    public String getEmailAddress();

}
