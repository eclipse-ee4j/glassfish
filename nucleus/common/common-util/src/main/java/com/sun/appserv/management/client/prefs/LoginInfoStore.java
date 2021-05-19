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

package com.sun.appserv.management.client.prefs;
import java.util.Collection;

/**
 * An interface that represents the database of LoginInfo objects. Provides methods
 * to access and mutate the store. The general contract of store update comprises of the following:
 * <ul>
 *   <li> The store can contain at the most one LoginInfo for a host and port combination </li>
 *   <li> There is <i> no guarantee </i> that concurrent modification of this store by two
 *        different programs will always be consistent. </li>
 * </ul>
 * @since Appserver 9.0
 */
public interface LoginInfoStore {

    /**
     * Returns a {@link LoginInfo} corresponding to the given host and port, from this store.
     * The host may not be null. For a given host and port, there can be at most
     * one LoginInfo in this store.
     * @param host a non null String representing host name
     * @param port an integer specifying the port number
     * @return the corresponding LoginInfo, null if there is none
     * @throws StoreException if there is something wrong with reading the store
     * @throws IllegalArgumentException if the parameter host is null
     */
    public LoginInfo read(final String host, final int port) throws StoreException;

    /**
     * Removes the {@link LoginInfo} corresponding to the given host and port, from this store.
     * The host may not be null. If no such LoginInfo exists, StoreException results.
     * The caller thus must ensure if such a LoginInfo exists before calling this method.
     * Upon successful return, size of this store decreases by one.
     * @param host a non null String representing host name
     * @param port an integer specifying the port number
     * @throws StoreException if there is something wrong with reading the store or if there is
     *         no such LoginInfo
     * @throws IllegalArgumentException if the parameter host is null
     */
    public void remove(final String host, final int port) throws StoreException;

    /**
     * Stores the given LoginInfo in this store. Given LoginInfo may not be null.
     * Upon successful return, the size of this store increases by one. An exception is thrown
     * if there is already a LoginInfo with given host and port.
     * @param login a LoginInfo that needs to be stored
     * @throws StoreException if there's any problem or if there is already a LoginInfo
     * with given host and port
     * @throws IllegalArgumentException if the given LoginInfo is null
     */
    public void store(final LoginInfo login) throws StoreException;

    /**
     * Stores the given LoginInfo in this store. Given LoginInfo may not be null.
     * Upon successful return, the size of this store increases by one. An exception is thrown
     * if there is already a LoginInfo with given host and port and overwrite is false.
     * If overwrite is true, the given LoginInfo is stored regardless of whether it already
     * exists in this store. Depending upon the value of overwrite, the store is either unchanged
     * or not.
     * @param login a LoginInfo that needs to be stored
     * @throws StoreException if there's any problem in storing or if overwrite is false and
     * the LoginInfo with given host and port already exists
     * @throws IllegalArgumentException if the given LoginInfo is null
     */
    public void store(final LoginInfo login, final boolean overwrite) throws StoreException;

   /**
     * Checks whether a LoginInfo for given host and port exists in this store.
     * @param host a non null String representing host name
     * @param port an integer specifying the port number
     * @throws StoreException if there's any problem reading the store
     */
    public boolean exists(final String host, final int port) throws StoreException;

    /**
     * A convenience method that returns the Collection of LoginInfo instances stored in this store.
     * An empty Collection is returned when there are no LoginInfo items stored.
     * @return the Collection of LoginInfo instances
     * @throws StoreException if there's any problem reading the store
     */
    public Collection<LoginInfo> list() throws StoreException;

    /**
     * A convenience method that returns the number of LoginInfo instances stored in this store.
     * Zero is returned when no login information is stored.
     * @return an integer representing number of stored login information elements, 0 if none
     * @throws StoreException if there's any problem reading the store
     */
    public int size() throws StoreException;

    /** Returns the name of the store.
     * This is any name that the store implementation wants to use for identification, for instance.
     */
    public String getName();
}
