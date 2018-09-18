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

/*
 * PersistenceStore.java
 *
 * Created on March 3, 2000
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore;


import java.util.Collection;

/**
 * <P>This interface represents a Persistence store
 * that knows how to create, find, modify and delete persistence
 * capable objects from a backing store such as a database.
 */
public interface PersistenceStore {

    /**
     */
    public void execute(PersistenceManager pm, Collection actions);

    /**
     */
    public void executeBatch(PersistenceManager pm, UpdateObjectDesc request, boolean forceFlush);

    /**
     */
    public Object retrieve(PersistenceManager pm,
                               RetrieveDesc action,
                               ValueFetcher parameters);

    /**
     */
    public Class getClassByOidClass(Class oidType);

    /**
     */
    public StateManager getStateManager(Class classType);

    /**
     * Returns a new retrieve descriptor for an external (user) query.
     *
     * @param classType Type of the persistence capable class to be queried.
     * @return A new retrieve descriptor for an external (user) query.
     */
    public RetrieveDesc getRetrieveDesc(Class classType);

    /**
     * Returns a new retrieve descriptor for an external (user) query.
     * This retrieve descriptor can be used to query for the foreign
     * field <code>name</code>.
     *
     * @param fieldName Name of the foreign field to be queried.
     * @param classType Persistence capable class including <code>fieldName</code>.
     * @return A new retrieve descriptor for an external (user) query.
     */
    public RetrieveDesc getRetrieveDesc(String fieldName, Class classType);

    /**
     */
    public UpdateObjectDesc getUpdateObjectDesc(Class classType);


    /**
     */
    public PersistenceConfig getPersistenceConfig(
            Class classType);

    /**
     * Returns ConfigCache associated with this store.
     *
     * @return ConfigCache associated with this store.
     */
    public ConfigCache getConfigCache();
}
