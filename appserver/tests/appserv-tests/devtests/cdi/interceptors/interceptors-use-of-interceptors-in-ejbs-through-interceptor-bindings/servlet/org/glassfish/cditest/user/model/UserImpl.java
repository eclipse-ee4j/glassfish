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

package org.glassfish.cditest.user.model;

import org.glassfish.cditest.user.api.model.Gender;
import org.glassfish.cditest.user.api.model.User;

/**
 * <p>
 * Client-side implementation of {@link User}.
 * </p>
 *
 * @author chaoslayer
 */
public class UserImpl implements User
{

    private static final long serialVersionUID = 1L;

    private Long id;
    private String lastName;
    private String firstName;
    private Gender gender;
    private String username;
    private String emailAddress;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * <p>
     * Get the username used for login
     * </p>
     *
     * @return the value of username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * <p>
     * Set the username
     * </p>
     *
     * @param username
     *            new value of username
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

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
    public Gender getGender()
    {
        return gender;
    }

    /**
     * <p>
     * Set the gender for the user
     * </p>
     *
     * <p>
     * A gender is much better suited to our needs here as the pure biological
     * sex is.
     * </p>
     *
     * @param gender
     *            new value of gender
     */
    public void setGender(Gender gender)
    {
        this.gender = gender;
    }

    /**
     * Get the value of firstName
     *
     * @return the value of firstName
     */
    public String getFirstName()
    {
        return firstName;
    }

    /**
     * Set the value of firstName
     *
     * @param firstName
     *            new value of firstName
     */
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    /**
     * Get the value of lastName
     *
     * @return the value of lastName
     */
    public String getLastName()
    {
        return lastName;
    }

    /**
     * Set the value of lastName
     *
     * @param lastName
     *            new value of lastName
     */
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    /**
     * Get the value of emailAddress
     *
     * @return the value of emailAddress
     */
    public String getEmailAddress()
    {
        return emailAddress;
    }

    /**
     * Set the value of emailAddress
     *
     * @param emailAddress
     *            new value of emailAddress
     */
    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }

    /**
     * String representation of the object data
     *
     * @return The object data as String
     */
    @Override
    public String toString()
    {
        return "UserImpl [id=" + id +
                ", emailAddress=" + emailAddress +
                ", firstName=" + firstName +
                ", gender=" + gender +
                ", lastName=" + lastName +
                ", username=" + username + "]";
    }

}
