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

package org.glassfish.embeddable.web;

import jakarta.servlet.ServletContext;

import java.util.EventListener;

import org.glassfish.embeddable.web.config.SecurityConfig;

/**
 * Representation of a web application.
 *
 * <p/>See {@link WebContainer} for usage example.
 *
 * @author Rajiv Mordani
 * @author Jan Luehe
 */
// TODO: Add support for configuring environment entries
public interface Context extends ServletContext {

    /**
     * Adds the given <tt>Valve</tt> to this <tt>Context</tt>.
     *
     * @param valve the <tt>Valve</tt> to be added
     */
    //public void addValve(Valve valve);

    /**
     * Registers the given listener with this <tt>Context</tt>.
     *
     * <p>The given listener must be an instance of one or more of the
     * following interfaces:
     * <ul>
     * <li><tt>jakarta.servlet.ServletContextAttributeListener</tt>
     * <li><tt>jakarta.servlet.ServletRequestAttributeListener</tt>
     * <li><tt>jakarta.servlet.ServletRequestListener</tt>
     * <li><tt>jakarta.servlet.ServletContextListener</tt>
     * <li><tt>jakarta.servlet.http.HttpSessionAttributeListener</tt>
     * <li><tt>jakarta.servlet.http.HttpSessionIdListener</tt>
     * <li><tt>jakarta.servlet.http.HttpSessionListener</tt>
     * </ul>
     *
     * @param t the listener to be registered with this <tt>Context</tt>
     *
     * @throws IllegalArgumentException if the given listener is not
     * an instance of any of the above interfaces
     * @throws IllegalStateException if this context has already been
     * initialized and started
     */
    public <T extends EventListener> void addListener(T t);

    /**
     * Instantiates a listener from the given class and registers it with
     * this <tt>Context</tt>.
     *
     * <p>The given listener must be an instance of one or more of the
     * following interfaces:
     * <ul>
     * <li><tt>jakarta.servlet.ServletContextAttributeListener</tt>
     * <li><tt>jakarta.servlet.ServletRequestAttributeListener</tt>
     * <li><tt>jakarta.servlet.ServletRequestListener</tt>
     * <li><tt>jakarta.servlet.ServletContextListener</tt>
     * <li><tt>jakarta.servlet.http.HttpSessionAttributeListener</tt>
     * <li><tt>jakarta.servlet.http.HttpSessionListener</tt>
     * </ul>
     *
     * @param c the class from which to instantiate of the listener
     *
     * @throws IllegalArgumentException if the given class does not
     * implement any of the above interfaces
     * @throws IllegalStateException if this context has already been
     * initialized and started
     */
    public void addListener(Class <? extends EventListener> c);

    /**
     * Enables or disables directory listings on this <tt>Context</tt>.
     *
     * @param directoryListing true if directory listings are to be
     * enabled on this <tt>Context</tt>, false otherwise
     */
    public void setDirectoryListing(boolean directoryListing);

    /**
     * Checks whether directory listings are enabled or disabled on this
     * <tt>Context</tt>.
     *
     * @return true if directory listings are enabled on this
     * <tt>Context</tt>, false otherwise
     */
    public boolean isDirectoryListing();

    /**
     * Set the security related configuration for this context
     *
     * @see org.glassfish.embeddable.web.config.SecurityConfig
     *
     * @param config the security configuration for this context
     */
    public void setSecurityConfig(SecurityConfig config);

    /**
     * Gets the security related configuration for this context
     *
     * @see org.glassfish.embeddable.web.config.SecurityConfig
     *
     * @return the security configuration for this context
     */
    public SecurityConfig getSecurityConfig();

    /**
     * Set the location of the default web xml that will be used.
     *
     * @param defaultWebXml the defaultWebXml path to be used
     */
    public void setDefaultWebXml(String defaultWebXml);

    /**
     * Return the context path for this Context.
     */
    public String getPath();

    /**
     * Set the context path for this Context.
     */
    public void setPath(String path);

}
