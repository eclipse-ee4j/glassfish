/*
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

package org.glassfish.api.admin;

import java.util.Iterator;
import java.util.Map;

import org.jvnet.hk2.annotations.Contract;

/**
 * Represents a fully-functional password alias store.
 * <p>
 * If the implementation holds the aliases and passwords in memory, it handles loading and saving the in-memory contents
 * from and to persistent storage, at the discretion of the implementer. For example, loading would typically happen
 * when the password alias store implementation is first instantiated, although an implementation could choose to load
 * lazily on first read. Saving is at the discretion of the implementer as well, although to maximize reliability the
 * implementation should persist changes as they occur. The {@link #putAll ) methods can help optimize that.
 *
 * @author tjquinn
 */
@Contract
public interface PasswordAliasStore {

    /**
     * Reports whether the store contains the specified alias.
     *
     * @param alias the alias to check for
     * @return true if the alias appears in the store; false otherwise
     */
    boolean containsKey(String alias);

    /**
     * Returns the password associated with the specified alias.
     *
     * @param alias the alias of interest
     * @return the password for that alias, if the store contains the alias; null otherwise
     */
    char[] get(String alias);

    /**
     * Reports whether the alias store is empty.
     *
     * @return
     */
    boolean isEmpty();

    /**
     * Returns an Iterator over aliases present in the alias store.
     *
     * @return
     */
    Iterator<String> keys();

    /**
     * Reports the number of aliases present in the store.
     *
     * @return
     */
    int size();

    /**
     * Deletes all password aliases from the store.
     */
    void clear();

    /**
     * Insert a new alias with the specified password, or assigns a new password to an existing alias.
     *
     * @param alias the alias to create or reassign
     * @param password the password to be associated with the alias
     */
    void put(String alias, char[] password);

    /**
     * Adds all alias/password pairs from the specified store to this store.
     *
     * @param otherStore the alias store from which to get entries
     */
    void putAll(PasswordAliasStore otherStore);

    /**
     * Adds a group of alias/password pairs in a single operation.
     * <p>
     * Callers might prefer to invoke this method once rather than invoking {@link #put ) repeatedly, for example if an
     * implementation persists each change as it is made.
     *
     * @param settings the alias/password pairs to add
     */
    void putAll(Map<String, char[]> settings);

    /**
     * Removes the specified alias (and the associated password) from the password alias store.
     *
     * @param alias the alias to be removed
     */
    void remove(String alias);
}
